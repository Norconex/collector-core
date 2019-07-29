/* Copyright 2014-2018 Norconex Inc.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.norconex.commons.lang.encrypt.EncryptionKey;
import com.norconex.commons.lang.encrypt.EncryptionUtil;

/**
 * <p>
 * Hold Mongo connection details.
 * </p>
 * @author Pascal Essiembre
 */
public class MongoConnectionDetails implements Serializable {

    private static final long serialVersionUID = 1336825758405391097L;

    private int port;
    private String host;
    private String databaseName;
    private String username;
    private String password;
    private String mechanism;
    private EncryptionKey passwordKey;
    private boolean sslEnabled;
    private boolean sslInvalidHostNameAllowed;

    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public String getDatabaseName() {
        return databaseName;
    }
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets whether to use SSL.
     * @return <code>true</code> if SSL should be used
     * @since 1.10.0
     */
    public boolean isSslEnabled() {
        return sslEnabled;
    }
    /**
     * Sets whether to use SSL.
     * @param sslEnabled <code>true</code> if SSL should be used
     * @since 1.10.0
     */
    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    /**
     * Gets whether invalid host names should be allowed if SSL is enabled.
     * @return <code>true</code> if invalid host names are allowed
     * @since 1.10.0
     */
    public boolean isSslInvalidHostNameAllowed() {
        return sslInvalidHostNameAllowed;
    }
    /**
     * Sets whether invalid host names should be allowed if SSL is enabled.
     * Use caution before allowing invalid hosts.
     * @param sslInvalidHostNameAllowed <code>true</code> if invalid host
     *         names are allowed
     * @since 1.10.0
     */
    public void setSslInvalidHostNameAllowed(
            boolean sslInvalidHostNameAllowed) {
        this.sslInvalidHostNameAllowed = sslInvalidHostNameAllowed;
    }

    /**
     * Gets the authentication mechanism to use (<code>MONGODB-CR</code>,
     * <code>SCRAM-SHA-1</code> or <code>null</code> to use default).
     * @return authentication mechanism
     * @since 1.8.1
     */
    public String getMechanism() {
        return mechanism;
    }
    /**
     * Sets the authentication mechanism to use (<code>MONGODB-CR</code>,
     * <code>SCRAM-SHA-1</code> or <code>null</code> to use default).
     * @param mechanism authentication mechanism
     * @since 1.8.1
     */
    public void setMechanism(String mechanism) {
        this.mechanism = mechanism;
    }
    /**
     * Gets the password encryption key.
     * @return the password key or <code>null</code> if the password is not
     * encrypted.
     * @see EncryptionUtil
     * @since 1.8.0
     */
    public EncryptionKey getPasswordKey() {
        return passwordKey;
    }
    /**
     * Sets the password encryption key. Only required when
     * the password is encrypted.
     * @param passwordKey password key
     * @see EncryptionUtil
     * @since 1.8.0
     */
    public void setPasswordKey(EncryptionKey passwordKey) {
        this.passwordKey = passwordKey;
    }
    /**
     * Gets a safe database name using MongoUtil, and treating a crawlerId as
     * the default.
     *
     * @param crawlerId crawler id from collector configuration
     * @see MongoCrawlDataStore
     * @since 1.9.1
     * @return database name safe to use in Mongo
     */
    public String getSafeDatabaseName(String crawlerId) {
        return MongoUtil.getSafeDBName(getDatabaseName(), crawlerId);
    }
    /**
     * Builds a MongoClient object based on these connection details.
     * Takes the crawler id as a default Mongo database name.
     *
     * @param crawlerId crawler id from collector configuration
     * @see MongoCrawlDataStore
     * @since 1.9.1
     * @return instance of MongoClient
     */
    public MongoClient buildMongoClient(String crawlerId) {
        String dbName = getSafeDatabaseName(crawlerId);

        int realPort = getPort();
        if (realPort <= 0) {
            realPort = ServerAddress.defaultPort();
        }

        ServerAddress server = new ServerAddress(getHost(), realPort);
        List<MongoCredential> credentialsList = new ArrayList<>();
        if (StringUtils.isNoneBlank(getUsername())) {
            // password may be encrypted, decrypt properly
            String password =
                    EncryptionUtil.decrypt(getPassword(), getPasswordKey());

            // build credential and add to list
            MongoCredential credential = buildMongoCredential(getUsername(),
                    dbName, password.toCharArray(), getMechanism());
            credentialsList.add(credential);
        }

        return new MongoClient(server, credentialsList,
                MongoClientOptions.builder()
                    .sslEnabled(sslEnabled)
                    .sslInvalidHostNameAllowed(sslInvalidHostNameAllowed)
                    .build());
    }
    /**
     * Builds a MongoCredential object based on these connection details.
     *
     * @param username Mongo username
     * @param dbName Mongo database name
     * @param password Mongo password
     * @param mechanism Mongo authentication mechanism
     * @return instance of MongoCredential
     * @see #buildMongoClient(String)
     * @since 1.9.1
     */
    public static MongoCredential buildMongoCredential(
            String username,
            String dbName,
            char[] password,
            String mechanism) {
        if (MongoCredential.MONGODB_CR_MECHANISM.equals(mechanism)) {
            return MongoCredential.createMongoCRCredential(
                    username, dbName, password);
        }
        return MongoCredential.createCredential(username, dbName, password);
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof MongoConnectionDetails)) {
            return false;
        }
        MongoConnectionDetails castOther = (MongoConnectionDetails) other;
        return new EqualsBuilder()
                .append(port, castOther.port)
                .append(host, castOther.host)
                .append(databaseName, castOther.databaseName)
                .append(username, castOther.username)
                .append(password, castOther.password)
                .append(mechanism, castOther.mechanism)
                .append(passwordKey, castOther.passwordKey)
                .append(sslEnabled, castOther.sslEnabled)
                .append(sslInvalidHostNameAllowed,
                        castOther.sslInvalidHostNameAllowed)
                .isEquals();
    }
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(port)
                .append(host)
                .append(databaseName)
                .append(username)
                .append(password)
                .append(mechanism)
                .append(passwordKey)
                .append(sslEnabled)
                .append(sslInvalidHostNameAllowed)
                .toHashCode();
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("port", port)
                .append("host", host)
                .append("databaseName", databaseName)
                .append("username", username)
                .append("password", password)
                .append("mechanism", mechanism)
                .append("passwordKey", passwordKey)
                .append("sslEnabled", sslEnabled)
                .append("sslInvalidHostNameAllowed", sslInvalidHostNameAllowed)
                .toString();
    }
}
