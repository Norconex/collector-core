/* Copyright 2014 Norconex Inc.
 * 
 * This file is part of Norconex Collector Core.
 * 
 * Norconex Collector Core is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Norconex Collector Core is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Norconex Collector Core. If not, 
 * see <http://www.gnu.org/licenses/>.
 */
package com.norconex.collector.core.data.store.impl.mongo;

import java.io.Serializable;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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
        if (!(other instanceof MongoConnectionDetails))
            return false;
        MongoConnectionDetails castOther = (MongoConnectionDetails) other;
        return new EqualsBuilder().append(port, castOther.port)
                .append(host, castOther.host)
                .append(databaseName, castOther.databaseName)
                .append(username, castOther.username)
                .append(password, castOther.password).isEquals();
    }
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(port).append(host)
                .append(databaseName).append(username).append(password)
                .toHashCode();
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString())
                .append("port", port).append("host", host)
                .append("databaseName", databaseName)
                .append("username", username).append("password", password)
                .toString();
    }
    
    
}
