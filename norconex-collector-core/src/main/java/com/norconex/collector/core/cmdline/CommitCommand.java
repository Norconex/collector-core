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
package com.norconex.collector.core.cmdline;

import com.norconex.collector.core.crawler.Crawler;

import picocli.CommandLine.Command;

/**
 * Force Committers to commit their queue.
 * @author Pascal Essiembre
 * @since 2.0.0
 */
@Command(
    name = "commit",
    description = "Committer to commit remains from their queue "
                + "from a previous run."
)
public class CommitCommand extends AbstractSubCommand {
    @Override
    public void runCommand() {
        for (Crawler crawler : getCollector().getCrawlers()) {
            crawler.getCrawlerConfig().getCommitter().commit();
        }
    }
}
