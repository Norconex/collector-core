/* Copyright 2014-2015 Norconex Inc.
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

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Hold Mongo connection details.
 * @author Pascal Essiembre
 */
public class MongoConnectionDetails implements Serializable {

    private static final long serialVersionUID = 1336825758405391097L;

    private int port;
    private String host;
    private String databaseName;
    private String username;
    private String password;

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
                .toString();
    }
}
