/* Copyright 2019 Norconex Inc.
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

import static com.norconex.collector.core.CollectorEvent.COLLECTOR_ENDED;
import static com.norconex.collector.core.CollectorEvent.COLLECTOR_STARTED;
import static com.norconex.collector.core.crawler.CrawlerEvent.CRAWLER_FINISHED;
import static com.norconex.collector.core.crawler.CrawlerEvent.CRAWLER_STARTED;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.junit.jupiter.api.function.Executable;

import com.norconex.collector.core.Collector;
import com.norconex.collector.core.CollectorEvent;
import com.norconex.collector.core.MockCollector;
import com.norconex.commons.lang.event.EventManager;
import com.norconex.commons.lang.file.FileUtil;

public final class MockCrawlerLifeCycle {

    private MockCrawlerLifeCycle() {
        super();
    }

    public static void mock(Executable executable, Crawler crawler) {
        try {
            Path workdir = Files.createTempDirectory("collector-workdir-");
            mockInDir(workdir, executable, crawler);
            FileUtil.delete(workdir.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void mockInDir(
            Path workdir, Executable executable, Crawler crawler) {
        Objects.requireNonNull("'workdir' must not be null.");
        MockCollector collector =
                new MockCollector("LifeCycle MockCollector", workdir);
        doMock(executable, collector, crawler);
        collector = null;
    }

    public static void mock(Executable executable, Object... objects) {
        try {
            Path workdir = Files.createTempDirectory("collector-workdir-");
            mockInDir(workdir, executable, objects);
            FileUtil.delete(workdir.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void mockInDir(
            Path workdir, Executable executable, Object... objects) {
        Objects.requireNonNull("'workdir' must not be null.");
        MockCollector collector =
                new MockCollector("LifeCycle MockCollector", workdir);
        MockCrawler crawler =
                new MockCrawler("LifeCycle MockCrawler", collector);
        doMock(executable, collector, crawler, objects);
        collector = null;
        crawler = null;
    }
    private static void doMock(Executable executable,
            Collector collector, Crawler crawler, Object... objects) {

        EventManager eventManager = collector.getEventManager();
        if (crawler != null) {
            eventManager.addListenersFromScan(crawler);
        }
        for (Object obj: objects) {
            eventManager.addListenersFromScan(obj);
        }
        eventManager.fire(CollectorEvent.create(COLLECTOR_STARTED, collector));
        eventManager.fire(CrawlerEvent.create(CRAWLER_STARTED, crawler));
        try {
            executable.execute();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        eventManager.fire(CrawlerEvent.create(CRAWLER_FINISHED, crawler));
        eventManager.fire(CollectorEvent.create(COLLECTOR_ENDED, collector));
    }
}
