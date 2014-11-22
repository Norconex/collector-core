/* Copyright 2014 Norconex Inc.
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
package com.norconex.collector.core.crawler.event;

import com.norconex.collector.core.crawler.ICrawler;

/**
 * <p>Allows implementers to react to any crawler-specific events.</p>
 * <p>Keep in mind that if defined as part of crawler defaults, 
 * a single instance of this listener will be shared amongst crawlers
 * (unless overwritten).</p>
 * @author Pascal Essiembre
 */
public interface ICrawlerEventListener {

    /**
     * Fired when a crawler event occurs.
     * @param crawler the crawler that fired the event
     * @param event the event
     */
    void crawlerEvent(ICrawler crawler, CrawlerEvent event);
}
