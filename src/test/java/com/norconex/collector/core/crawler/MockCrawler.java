/* Copyright 2019-2020 Norconex Inc.
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

import com.norconex.collector.core.Collector;
import com.norconex.collector.core.doc.CrawlDoc;
import com.norconex.collector.core.doc.CrawlDocInfo;
import com.norconex.collector.core.pipeline.importer.ImporterPipelineContext;
import com.norconex.importer.response.ImporterResponse;
import com.norconex.jef5.status.JobStatusUpdater;
import com.norconex.jef5.suite.JobSuite;

public class MockCrawler extends Crawler {

    public MockCrawler(CrawlerConfig config, Collector collector) {
        super(config, collector);
    }
    public MockCrawler(String id, Collector collector) {
        super(new MockCrawlerConfig(), collector);
        getCrawlerConfig().setId(id);
    }

    public void initMockCrawler() {
        super.initCrawler();
    }

    @Override
    protected void prepareExecution(JobStatusUpdater statusUpdater,
            JobSuite suite, boolean resume) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void cleanupExecution(JobStatusUpdater statusUpdater,
            JobSuite suite) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void executeQueuePipeline(CrawlDocInfo ref) {
        throw new UnsupportedOperationException();
    }

//    @Override
//    protected Doc wrapDocument(CrawlDocInfo ref,
//            Doc document) {
//        throw new UnsupportedOperationException();
//    }

    @Override
    protected void markReferenceVariationsAsProcessed(CrawlDocInfo ref) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected CrawlDocInfo createChildDocInfo(
            String embeddedReference, CrawlDocInfo parentCrawlRef) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ImporterResponse executeImporterPipeline(
            ImporterPipelineContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void executeCommitterPipeline(Crawler crawler,
            CrawlDoc doc) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void resumeExecution(JobStatusUpdater statusUpdater,
            JobSuite suite) {
        throw new UnsupportedOperationException();
    }
}
