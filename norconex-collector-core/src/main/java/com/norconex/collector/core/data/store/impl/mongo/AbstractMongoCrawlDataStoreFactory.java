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
 *      &lt;cachedCollectionName&gt;(Custom "cached" collection name)&lt;/cachedCollectionName&gt;
 *      &lt;referencesCollectionName&gt;(Custom "references" collection name)&lt;/referencesCollectionName&gt;
 *      &lt;mechanism&gt;(Optional authentication mechanism)&lt;/mechanism&gt;
 *      &lt;sslEnabled&gt;[false|true]&lt;/sslEnabled&gt;
 *      &lt;sslInvalidHostNameAllowed&gt;[false|true]&lt;/sslInvalidHostNameAllowed&gt;
 *
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
 * <h3>Authentication mechanism</h3>
 * <p>
 * As of 1.8.1, it is now possible to specify the MongoDB authentication
 * mechanism to use.  The following are supported:
 * </p>
 * <ul>
 *  <li>MONGODB-CR</li>
 *  <li>SCRAM-SHA-1</li>
 * </ul>
 * <p>
 * When no mechanism is specified, the default mechanism will be
 * the Challenge Response (MONGODB-CR) for MongoDB 2 and and
 * SCRAM SHA1 (SCRAM-SHA-1) for MongoDB 3+.
 * The following is an example forcing MONGODB-CR authentication:
 * <pre>
 *      &lt;username&gt;joe_user&lt;/username&gt;
 *      &lt;password&gt;joe_pwd&lt;/password&gt;
 *      &lt;mechanism&gt;MONGODB-CR&lt;/mechanism&gt;
 * </pre>
 *
 * <p>
 * As of 1.9.0, you can define your own collection names with
 * {@link #setReferencesCollectionName(String)} and
 * {@link #setCachedCollectionName(String)}.
 * </p>
 *
 * <p>
 * As of 1.10.0, you can enable SSL.
 * </p>
 *
 * @author Pascal Essiembre
 * @see BaseMongoSerializer
 */
public abstract class AbstractMongoCrawlDataStoreFactory
        implements ICrawlDataStoreFactory, IXMLConfigurable {

    private final MongoConnectionDetails connDetails =
            new MongoConnectionDetails();
    private String referencesCollectionName =
            MongoCrawlDataStore.DEFAULT_REFERENCES_COL_NAME;
    private String cachedCollectionName =
            MongoCrawlDataStore.DEFAULT_CACHED_COL_NAME;

    @Override
    public ICrawlDataStore createCrawlDataStore(
            ICrawlerConfig config, boolean resume) {
        return new MongoCrawlDataStore(
                config.getId(),
                resume,
                getConnectionDetails(),
                createMongoSerializer(),
                getReferencesCollectionName(),
                getCachedCollectionName());
    }

    public MongoConnectionDetails getConnectionDetails() {
        return connDetails;
    }

    /**
     * Gets the references collection name. Defaults to "references".
     * @return collection name
     * @since 1.9.0
     */
    public String getReferencesCollectionName() {
        return referencesCollectionName;
    }
    /**
     * Sets the references collection name.
     * @param referencesCollectionName collection name
     * @since 1.9.0
     */
    public void setReferencesCollectionName(String referencesCollectionName) {
        this.referencesCollectionName = referencesCollectionName;
    }
    /**
     * Gets the cached collection name. Defaults to "cached".
     * @return collection name
     * @since 1.9.0
     */
    public String getCachedCollectionName() {
        return cachedCollectionName;
    }
    /**
     * Sets the cached collection name.
     * @param cachedCollectionName collection name
     * @since 1.9.0
     */
    public void setCachedCollectionName(String cachedCollectionName) {
        this.cachedCollectionName = cachedCollectionName;
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
        setCachedCollectionName(xml.getString(
                "cachedCollectionName", getCachedCollectionName()));
        setReferencesCollectionName(xml.getString(
                "referencesCollectionName", getReferencesCollectionName()));

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

        // SSL
        connDetails.setSslEnabled(
                xml.getBoolean("sslEnabled", connDetails.isSslEnabled()));
        connDetails.setSslInvalidHostNameAllowed(
                xml.getBoolean("sslInvalidHostNameAllowed",
                        connDetails.isSslInvalidHostNameAllowed()));
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
            writer.writeElementString("mechanism", connDetails.getMechanism());
            writer.writeElementString(
                    "cachedCollectionName", getCachedCollectionName());
            writer.writeElementString("referencesCollectionName",
                    getReferencesCollectionName());

            // Encrypted password:
            EncryptionKey key = connDetails.getPasswordKey();
            if (key != null) {
                writer.writeElementString("passwordKey", key.getValue());
                if (key.getSource() != null) {
                    writer.writeElementString("passwordKeySource",
                            key.getSource().name().toLowerCase());
                }
            }

            // SSL
            writer.writeElementBoolean(
                    "sslEnabled", connDetails.isSslEnabled());
            writer.writeElementBoolean("sslInvalidHostNameAllowed",
                    connDetails.isSslInvalidHostNameAllowed());

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
                .append(referencesCollectionName,
                        castOther.referencesCollectionName)
                .append(cachedCollectionName, castOther.cachedCollectionName)
                .isEquals();
    }
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(connDetails)
                .append(referencesCollectionName)
                .append(cachedCollectionName)
                .toHashCode();
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("connDetails", connDetails)
                .append("referencesCollectionName", referencesCollectionName)
                .append("cachedCollectionName", cachedCollectionName)
                .toString();
    }
}
