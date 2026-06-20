/*
 * =============================================================================
 *
 *   Copyright (c) 2011-2016, The THYMELEAF team (http://www.thymeleaf.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * =============================================================================
 */
package thymeleafexamples.gtvg.business.entities;

public class DatabaseDemoStatus {

    private boolean configured = false;
    private boolean connected = false;
    private String message = null;
    private String dataSource = null;
    private String databaseName = null;
    private String databaseUser = null;
    private String databaseVersion = null;
    private String databaseTime = null;
    private Integer heartbeatCount = null;

    public boolean isConfigured() {
        return this.configured;
    }

    public void setConfigured(final boolean configured) {
        this.configured = configured;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(final boolean connected) {
        this.connected = connected;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getDataSource() {
        return this.dataSource;
    }

    public void setDataSource(final String dataSource) {
        this.dataSource = dataSource;
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public void setDatabaseName(final String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabaseUser() {
        return this.databaseUser;
    }

    public void setDatabaseUser(final String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public String getDatabaseVersion() {
        return this.databaseVersion;
    }

    public void setDatabaseVersion(final String databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    public String getDatabaseTime() {
        return this.databaseTime;
    }

    public void setDatabaseTime(final String databaseTime) {
        this.databaseTime = databaseTime;
    }

    public Integer getHeartbeatCount() {
        return this.heartbeatCount;
    }

    public void setHeartbeatCount(final Integer heartbeatCount) {
        this.heartbeatCount = heartbeatCount;
    }

}
