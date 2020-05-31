/* Copyright 2020 Norconex Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.norconex.collector.core.crawler;

import static java.math.RoundingMode.DOWN;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.commons.lang.map.FifoMap;
import com.norconex.commons.lang.time.DurationFormatter;
import com.norconex.commons.lang.time.DurationUnit;

/**
 * Tracks useful metrics from start/resume to end.  If the crawler was stopped
 * then restarted, the session starts when restarted.
 * @author Pascal Essiembre
 * @since 3.0.0
 */
class CrawlerMetrics {

    //TODO clean this class and make it more central and userul.

    private static final Logger LOG =
            LoggerFactory.getLogger(CrawlerMetrics.class);

    private static final long METRICS_LOGGING_INTERVAL =
            TimeUnit.SECONDS.toMillis(5);
    private static final long ONE_SECOND_MILLIS =
            TimeUnit.SECONDS.toMillis(1);

    // timestamp up to minute => qty processed
    private final Map<Long, AtomicInteger> seconds = new FifoMap<>(61);

    // This processedCount does not take into account alternate references such
    // as redirects. It is a cleaner representation for end-users and speed
    // things a bit bit not having to obtain that value from the database at
    // every progress change.,
    private final AtomicLong processedCount = new AtomicLong();

    private final List<IntervalMetrics> intervals = new ArrayList<>();

    private final DurationFormatter durationFormatter = new DurationFormatter()
            .withOuterLastSeparator(" and ")
            .withOuterSeparator(", ")
            .withUnitPrecision(2)
            .withLowestUnit(DurationUnit.SECOND);

    private long lastLoggingMillis = System.currentTimeMillis();
    private long startFromSeconds =
            (lastLoggingMillis / ONE_SECOND_MILLIS) + 1;
    private long totalDocuments;

    public CrawlerMetrics(long processedCount) {
        super();
        this.processedCount.set(processedCount);
        intervals.add(new IntervalMetrics(5));
        intervals.add(new IntervalMetrics(15));
        intervals.add(new IntervalMetrics(60));
    }

    public long getTotalDocuments() {
        return totalDocuments;
    }
    public void setTotalDocuments(long totalDocuments) {
        this.totalDocuments = totalDocuments;
    }

    public void startTracking() {
        lastLoggingMillis = System.currentTimeMillis();
        startFromSeconds =
                (lastLoggingMillis / ONE_SECOND_MILLIS) + 1;
    }

    public void incrementProcessedCount() {
        long nowMillis = System.currentTimeMillis();
        processedCount.incrementAndGet();
        long nowSeconds = nowMillis / ONE_SECOND_MILLIS;
        seconds.computeIfAbsent(
                nowSeconds, k -> new AtomicInteger()).incrementAndGet();

        if (LOG.isInfoEnabled()) {
            logMetrics(nowMillis, nowSeconds);
        }
    }

    private synchronized void logMetrics(long nowMillis, long nowSeconds) {
        long elapsed = nowMillis - lastLoggingMillis;
        if (elapsed < METRICS_LOGGING_INTERVAL
                || totalDocuments == 0) {
            return;
        }

        lastLoggingMillis = nowMillis;

        StringBuilder b = new StringBuilder();

        b.append("METRICS:");

        // Percentage
        label(b, "Progress");
        String percent = BigDecimal.valueOf(processedCount.longValue())
                .divide(BigDecimal.valueOf(totalDocuments), 2, DOWN)
                .stripTrailingZeros().toPlainString();
        b.append(percent).append("% (");
        b.append(processedCount.longValue()).append('/');
        b.append(totalDocuments);
        b.append(")");

        // Docs per seconds
        BigDecimal lastAverage = null;
        String sep = "";
        for (IntervalMetrics interval : intervals) {
            if (nowSeconds - startFromSeconds > interval.intervalSeconds) {
                if (StringUtils.isBlank(sep)) {
                    label(b, "Throughput");
                }
                lastAverage = interval.average(nowSeconds);
                b.append(sep)
                 .append(lastAverage.toPlainString())
                 .append(" docs/sec (last ")
                 .append(interval.intervalSeconds)
                 .append("s)");
                sep = " | ";
            }
        }

        // Elasped time (for current session, which could be resumed)
        label(b, "Elapsed time");
        b.append(durationFormatter.format(
                nowMillis - (startFromSeconds * ONE_SECOND_MILLIS)));

        // Remaining time
        if (lastAverage != null && lastAverage.doubleValue() > 0d) {
            label(b, "Remaining time");
            b.append(durationFormatter.format(BigDecimal.valueOf(
                    totalDocuments - processedCount.longValue())
                            .divide(lastAverage, 3, RoundingMode.DOWN)
                            .movePointRight(3).longValue()));
        }

        if (LOG.isInfoEnabled()) {
            LOG.info(b.toString());
        }
    }

    private void label(StringBuilder b, String label) {
        b.append("\n    " + StringUtils.rightPad(label, 15) + ": ");
    }

    public long getProcessedCount() {
        return processedCount.longValue();
    }

    @Override
    public boolean equals(final Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    @Override
    public String toString() {
        return new ReflectionToStringBuilder(
                this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
    }

    private class IntervalMetrics {
        private final long intervalSeconds;
        public IntervalMetrics(long intervalSeconds) {
            super();
            this.intervalSeconds = intervalSeconds;
        }
        public BigDecimal average(long nowSeconds) {
            // current minute is being populated so start with previous
            long lastSeconds = nowSeconds - 1;
            // we do +1 to include the last minute
            long firstSeconds = lastSeconds - intervalSeconds + 1;

            long total = 0L;
            for (Entry<Long, AtomicInteger> en : seconds.entrySet()) {
                if (en.getKey() >= firstSeconds && en.getKey() <= lastSeconds) {
                    total += en.getValue().intValue();
                }
            }
            BigDecimal docsPerSec = BigDecimal.ZERO;
            if (total > 0) {
                docsPerSec = BigDecimal.valueOf(total)
                        .divide(BigDecimal.valueOf(intervalSeconds), 2, DOWN)
                        .stripTrailingZeros();
            }
            return docsPerSec;
        }
    }
}
