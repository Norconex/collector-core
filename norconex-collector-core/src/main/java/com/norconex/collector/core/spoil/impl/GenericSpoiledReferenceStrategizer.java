/* Copyright 2015-2018 Norconex Inc.
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
package com.norconex.collector.core.spoil.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.norconex.collector.core.data.CrawlState;
import com.norconex.collector.core.spoil.ISpoiledReferenceStrategizer;
import com.norconex.collector.core.spoil.SpoiledReferenceStrategy;
import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.commons.lang.xml.XML;

/**
 * <p>
 * Generic implementation of {@link ISpoiledReferenceStrategizer} that
 * offers a simple mapping between the crawl state of references that have
 * turned "bad" and the strategy to adopt for each.
 * Whenever a crawl state does not have a strategy associated, the fall-back
 * strategy is used (default being <code>DELETE</code>).
 * </p>
 * <p>
 * The mappings defined by default are as follow:
 * </p>
 *
 * <table border="1" style="width:300px;" summary="Default mappings">
 *   <tr><td><b>Crawl state</b></td><td><b>Strategy</b></td></tr>
 *   <tr><td>NOT_FOUND</td><td>DELETE</td></tr>
 *   <tr><td>BAD_STATUS</td><td>GRACE_ONCE</td></tr>
 *   <tr><td>ERROR</td><td>GRACE_ONCE</td></tr>
 * </table>
 *
 * <h3>XML configuration usage:</h3>
 * <pre>
 *  &lt;spoiledReferenceStrategizer
 *      class="com.norconex.collector.core.spoil.impl.GenericSpoiledReferenceStrategizer"
 *      fallbackStrategy="[DELETE|GRACE_ONCE|IGNORE]"&gt;
 *    &lt;mapping state="(any crawl state)" strategy="[DELETE|GRACE_ONCE|IGNORE]" /&gt;
 *    (repeat mapping tag as needed)
 *  &lt;/spoiledReferenceStrategizer&gt;
 * </pre>
 *
 * <h4>Usage example:</h4>
 * <p>
 * The following indicates we should ignore (do nothing) errors processing
 * documents, and send a deletion request if they are not found or have
 * resulted in a bad status.
 * </p>
 * <pre>
 *  &lt;spoiledReferenceStrategizer
 *      class="com.norconex.collector.core.spoil.impl.GenericSpoiledReferenceStrategizer"&gt;
 *    &lt;mapping state="NOT_FOUND" strategy="DELETE" /&gt;
 *    &lt;mapping state="BAD_STATUS" strategy="DELETE" /&gt;
 *    &lt;mapping state="ERROR" strategy="IGNORE" /&gt;
 *  &lt;/spoiledReferenceStrategizer&gt;
 * </pre>
 *
 * @author Pascal Essiembre
 * @since 1.2.0
 */
public class GenericSpoiledReferenceStrategizer implements
        ISpoiledReferenceStrategizer, IXMLConfigurable {

    public static final SpoiledReferenceStrategy DEFAULT_FALLBACK_STRATEGY =
            SpoiledReferenceStrategy.DELETE;

    private final Map<CrawlState, SpoiledReferenceStrategy> mappings =
            new HashMap<>();
    private SpoiledReferenceStrategy fallbackStrategy =
            DEFAULT_FALLBACK_STRATEGY;

    public GenericSpoiledReferenceStrategizer() {
        super();
        // store default mappings
        mappings.put(CrawlState.NOT_FOUND, SpoiledReferenceStrategy.DELETE);
        mappings.put(CrawlState.BAD_STATUS,
                SpoiledReferenceStrategy.GRACE_ONCE);
        mappings.put(CrawlState.ERROR, SpoiledReferenceStrategy.GRACE_ONCE);
    }

    @Override
    public SpoiledReferenceStrategy resolveSpoiledReferenceStrategy(
            String reference, CrawlState state) {

        SpoiledReferenceStrategy strategy = mappings.get(state);
        if (strategy == null) {
            strategy = getFallbackStrategy();
        }
        if (strategy == null) {
            strategy = DEFAULT_FALLBACK_STRATEGY;
        }
        return strategy;
    }

    public SpoiledReferenceStrategy getFallbackStrategy() {
        return fallbackStrategy;
    }
    public void setFallbackStrategy(SpoiledReferenceStrategy fallbackStrategy) {
        this.fallbackStrategy = fallbackStrategy;
    }

    public void addMapping(
            CrawlState state, SpoiledReferenceStrategy strategy) {
        mappings.put(state, strategy);
    }

    @Override
    public void loadFromXML(XML xml) {
        SpoiledReferenceStrategy fallback = xml.getEnum(
                SpoiledReferenceStrategy.class,
                "@fallbackStrategy", fallbackStrategy);
        if (fallback != null) {
            setFallbackStrategy(fallback);
        }

        for (XML node : xml.getXMLList("mapping")) {
            String attribState = node.getString("@state", null);
            SpoiledReferenceStrategy strategy = node.getEnum(
                    SpoiledReferenceStrategy.class, "@strategy");
            if (StringUtils.isBlank(attribState) || strategy == null) {
                continue;
            }
            CrawlState state = CrawlState.valueOf(attribState);
            addMapping(state, strategy);
        }
    }

    @Override
    public void saveToXML(XML xml) {
        xml.setAttribute("fallbackStrategy", getFallbackStrategy());
        for (Entry<CrawlState, SpoiledReferenceStrategy> entry :
                mappings.entrySet()) {
            xml.addElement("mapping")
                    .setAttribute("state", entry.getKey())
                    .setAttribute("strategy", entry.getValue());
        }
    }


    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof GenericSpoiledReferenceStrategizer)) {
            return false;
        }
        GenericSpoiledReferenceStrategizer castOther =
                (GenericSpoiledReferenceStrategizer) other;
        return new EqualsBuilder()
                .append(fallbackStrategy, castOther.fallbackStrategy)
                .append(mappings, castOther.mappings)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(fallbackStrategy)
                .append(mappings)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("fallbackStrategy", fallbackStrategy)
                .append("mappings", mappings)
                .toString();
    }
}
