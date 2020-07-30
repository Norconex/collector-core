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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.norconex.collector.core.CollectorException;
import com.norconex.committer.core3.CommitterContext;
import com.norconex.committer.core3.CommitterException;
import com.norconex.committer.core3.DeleteRequest;
import com.norconex.committer.core3.ICommitter;
import com.norconex.committer.core3.UpsertRequest;
import com.norconex.commons.lang.io.CachedInputStream;
import com.norconex.commons.lang.io.CachedStreamFactory;
import com.norconex.commons.lang.map.Properties;

/**
 * Wrapper around multiple Committers so they can all be handled as one.
 * @author Pascal Essiembre
 * @since 3.0.0
 */
public class CrawlerCommitters {//implements ICommitter {

    private static final Logger LOG =
            LoggerFactory.getLogger(CrawlerCommitters.class);

    private final List<ICommitter> committers = new ArrayList<>();
    private final CachedStreamFactory cacheFactory;

    public CrawlerCommitters(
            CachedStreamFactory cacheFactory, List<ICommitter> committers) {
        super();
        if (committers != null) {
            this.committers.addAll(committers);
        }
        this.cacheFactory = cacheFactory;
    }

    public boolean isEmpty() {
        return committers.isEmpty();
    }


//    public List<ICommitter> getCommitters() {
//        return committers;
//    }

    public void init(CommitterContext baseContext) {
        MutableInt idx = new MutableInt();
        executeAll("init", c -> {
            CommitterContext ctx = baseContext.withWorkdir(
                    baseContext.getWorkDir().resolve(idx.toString()));
            idx.increment();
            c.init(ctx);
        });
    }

    public void upsert(UpsertRequest upsertRequest) {
        CachedInputStream content = CachedInputStream.cache(
                upsertRequest.getContent(), cacheFactory);

        // if more than one committer, create copies first. Else,
        // sends straight
        executeAll("upsert", c -> {
            if (c.accept(upsertRequest)) {
                Properties meta = new Properties();
                meta.loadFromMap(upsertRequest.getMetadata());
                UpsertRequest req = new UpsertRequest(
                        upsertRequest.getReference(), meta, content);
                c.upsert(req);
                content.rewind();
            }
        });
    }

    public void delete(DeleteRequest deleteRequest) {
        // if more than one committer, create copies first. Else,
        // sends straight
        executeAll("delete", c -> {
            DeleteRequest req = deleteRequest;
            if (committers.size() > 1) {
                Properties meta = new Properties();
                meta.loadFromMap(deleteRequest.getMetadata());
                req = new DeleteRequest(
                        deleteRequest.getReference(), meta);
            }
            if (c.accept(req)) {
                c.delete(req);
            }
        });
    }

    public void close() {
        executeAll("close", ICommitter::close);
    }

    public void clean() {
        executeAll("clean", ICommitter::clean);
    }

    private void executeAll(String operation, CommitterConsumer consumer) {
        List<String> failures = new ArrayList<>();
        for (ICommitter committer : committers) {
            try {
                consumer.accept(committer);
            } catch (CommitterException e) {
                LOG.error("Could not execute \"{}\" on committer: {}",
                        operation, committer, e);
                failures.add(committer.getClass().getSimpleName());
            }
        }
        if (!failures.isEmpty()) {
            throw new CollectorException(
                    "Could not execute \"" + operation + "\" on "
                    + failures.size() + " committer(s): \""
                    + StringUtils.join(failures, ", ")
                    + "\". Check the logs for more details.");
        }
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

    @FunctionalInterface
    private interface CommitterConsumer {
        public void accept(ICommitter c) throws CommitterException;
    }
}
