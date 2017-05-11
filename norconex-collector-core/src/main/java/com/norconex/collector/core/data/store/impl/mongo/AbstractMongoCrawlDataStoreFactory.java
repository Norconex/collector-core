/* Copyright 2014-2017 Norconex Inc.
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
package com.norconex.collector.core.data.store.impl.mongo;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.norconex.collector.core.crawler.ICrawlerConfig;
import com.norconex.collector.core.data.store.ICrawlDataStore;
import com.norconex.collector.core.data.store.ICrawlDataStoreFactory;
import com.norconex.commons.lang.config.IXMLConfigurable;
import com.norconex.commons.lang.config.XMLConfigurationUtil;
import com.norconex.commons.lang.encrypt.EncryptionKey;
import com.norconex.commons.lang.encrypt.EncryptionUtil;
import com.norconex.commons.lang.xml.EnhancedXMLStreamWriter;

/**
 * <p>Mongo implementation of {@link ICrawlDataStore}.</p>
 *
 * <p>All the references are stored in a collection named 'references'.
 * They go from the "QUEUED", "ACTIVE" and "PROCESSED" stages.</p>
 *
 * <p>The cached references are stored in a separated collection named
 * "cached".
 * </p>
 *
 * <p>
 * As of 1.8.0, <code>password</code> can take a password that has been
 * encrypted using {@link EncryptionUtil} (or command-line encrypt.[bat|sh]).
 * In order for the password to be decrypted properly by the crawler, you need
 * to specify the encryption key used to encrypt it. The key can be stored
 * in a few supported locations and a combination of
 * <code>passwordKey</code>
 * and <code>passwordKeySource</code> must be specified to properly
 * locate the key. The supported sources are:
 * </p>
 * <table border="1" summary="">
 *   <tr>
 *     <th><code>passwordKeySource</code></th>
 *     <th><code>passwordKey</code></th>
 *   </tr>
 *   <tr>
 *     <td><code>key</code></td>
 *     <td>The actual encryption key.</td>
 *   </tr>
 *   <tr>
 *     <td><code>file</code></td>
 *     <td>Path to a file containing the encryption key.</td>
 *   </tr>
 *   <tr>
 *     <td><code>environment</code></td>
 *     <td>Name of an environment variable containing the key.</td>
 *   </tr>
 *   <tr>
 *     <td><code>property</code></td>
 *     <td>Name of a JVM system property containing the key.</td>
 *   </tr>
 * </table>
 *
 * <p>
 * Implementing classes should contain the following XML configuration usage:
 * </p>
 * <pre>
 *  &lt;crawlDataStoreFactory class="(class name)"&gt;
 *      &lt;host&gt;(Optional Mongo server hostname. Default to localhost)&lt;/host&gt;
 *      &lt;port&gt;(Optional Mongo port. Default to 27017)&lt;/port&gt;
 *      &lt;dbname&gt;(Optional Mongo database name. Default to crawl id)&lt;/dbname&gt;
 *      &lt;username&gt;(Optional user name)&lt;/username&gt;
 *      &lt;password&gt;(Optional user password)&lt;/password&gt;
 *      &lt;!-- Use the following if password is encrypted. --&gt;
 *      &lt;passwordKey&gt;(the encryption key or a reference to it)&lt;/passwordKey&gt;
 *      &lt;passwordKeySource&gt;[key|file|environment|property]&lt;/passwordKeySource&gt;
 *  &lt;/crawlDataStoreFactory&gt;
 * </pre>
 * <p>
 * If "username" is not provided, no authentication will be attempted.
 * The "username" must be a valid user that has the "readWrite" role over
 * the database (set with "dbname").
 * </p>
 *
 * @author Pascal Essiembre
 * @see BaseMongoSerializer
 */
public abstract class AbstractMongoCrawlDataStoreFactory
        implements ICrawlDataStoreFactory, IXMLConfigurable {

    private final MongoConnectionDetails connDetails =
            new MongoConnectionDetails();

    @Override
    public ICrawlDataStore createCrawlDataStore(
            ICrawlerConfig config, boolean resume) {

        return new MongoCrawlDataStore(
                config.getId(),
                resume,
                getConnectionDetails(),
                createMongoSerializer());
    }

    public MongoConnectionDetails getConnectionDetails() {
        return connDetails;
    }

    protected abstract IMongoSerializer createMongoSerializer();

    @Override
    public void loadFromXML(Reader in) throws IOException {
        XMLConfiguration xml = XMLConfigurationUtil.newXMLConfiguration(in);
        connDetails.setPort(xml.getInt("port", connDetails.getPort()));
        connDetails.setHost(xml.getString("host", connDetails.getHost()));
        connDetails.setDatabaseName(
                xml.getString("dbname", connDetails.getDatabaseName()));
        connDetails.setUsername(
                xml.getString("username", connDetails.getUsername()));
        connDetails.setPassword(
                xml.getString("password", connDetails.getPassword()));
        connDetails.setMechanism(
                xml.getString("mechanism", connDetails.getMechanism()));

        // encrypted password:
        String xmlKey = xml.getString("passwordKey", null);
        String xmlSource = xml.getString("passwordKeySource", null);
        if (StringUtils.isNotBlank(xmlKey)) {
            EncryptionKey.Source source = null;
            if (StringUtils.isNotBlank(xmlSource)) {
                source = EncryptionKey.Source.valueOf(xmlSource.toUpperCase());
            }
            connDetails.setPasswordKey(new EncryptionKey(xmlKey, source));
        }
    }

    @Override
    public void saveToXML(Writer out) throws IOException {
        try {
            EnhancedXMLStreamWriter writer = new EnhancedXMLStreamWriter(out);
            writer.writeStartElement("crawlDataStoreFactory");
            writer.writeAttribute("class", getClass().getCanonicalName());

            writer.writeElementInteger("port", connDetails.getPort());
            writer.writeElementString("host", connDetails.getHost());
            writer.writeElementString("dbname", connDetails.getDatabaseName());
            writer.writeElementString("username", connDetails.getUsername());
            writer.writeElementString("password", connDetails.getPassword());

            String mechanism = connDetails.getMechanism();
            if (StringUtils.isNotBlank(mechanism)) {
                writer.writeElementString("mechanism", connDetails.getMechanism());
            }

            // Encrypted password:
            EncryptionKey key = connDetails.getPasswordKey();
            if (key != null) {
                writer.writeElementString("passwordKey", key.getValue());
                if (key.getSource() != null) {
                    writer.writeElementString("passwordKeySource",
                            key.getSource().name().toLowerCase());
                }
            }

            writer.flush();
            writer.close();
        } catch (XMLStreamException e) {
            throw new IOException("Cannot save as XML.", e);
        }

    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof AbstractMongoCrawlDataStoreFactory)) {
            return false;
        }
        AbstractMongoCrawlDataStoreFactory castOther =
                (AbstractMongoCrawlDataStoreFactory) other;
        return new EqualsBuilder()
                .append(connDetails, castOther.connDetails)
                .isEquals();
    }
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(connDetails)
                .toHashCode();
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("connDetails", connDetails)
                .toString();
    }
}
