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
package thymeleafexamples.gtvg.business.services;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import thymeleafexamples.gtvg.business.entities.DatabaseDemoStatus;

public class DatabaseDemoService {

    private static final String DATABASE_URL_ENV = "DATABASE_URL";

    public DatabaseDemoStatus getStatus() {
        final String databaseUrl = System.getenv(DATABASE_URL_ENV);
        if (databaseUrl == null || databaseUrl.trim().isEmpty()) {
            final DatabaseDemoStatus status = new DatabaseDemoStatus();
            status.setConfigured(false);
            status.setConnected(false);
            status.setMessage("DATABASE_URL is not configured yet.");
            return status;
        }

        try {
            final ConnectionConfig connectionConfig = ConnectionConfig.from(databaseUrl);
            try (Connection connection = DriverManager.getConnection(
                    connectionConfig.getJdbcUrl(), connectionConfig.getProperties())) {
                initializeDemoTable(connection);
                insertHeartbeat(connection);
                return readStatus(connection, connectionConfig.getSafeDataSource());
            }
        } catch (final SQLException e) {
            return failedStatus("Could not connect to PostgreSQL: " + e.getMessage());
        } catch (final RuntimeException e) {
            return failedStatus("DATABASE_URL is invalid: " + e.getMessage());
        }
    }

    private static DatabaseDemoStatus failedStatus(final String message) {
        final DatabaseDemoStatus status = new DatabaseDemoStatus();
        status.setConfigured(true);
        status.setConnected(false);
        status.setMessage(message);
        return status;
    }

    private static void initializeDemoTable(final Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS render_demo_heartbeat (" +
                    "id BIGSERIAL PRIMARY KEY, " +
                    "message TEXT NOT NULL, " +
                    "created_at TIMESTAMPTZ NOT NULL DEFAULT now()" +
                    ")");
        }
    }

    private static void insertHeartbeat(final Connection connection) throws SQLException {
        try (PreparedStatement statement =
                     connection.prepareStatement("INSERT INTO render_demo_heartbeat (message) VALUES (?)")) {
            statement.setString(1, "SMEConnect Render Postgres demo");
            statement.executeUpdate();
        }
    }

    private static DatabaseDemoStatus readStatus(final Connection connection, final String safeDataSource)
            throws SQLException {
        final DatabaseDemoStatus status = new DatabaseDemoStatus();
        status.setConfigured(true);
        status.setConnected(true);
        status.setMessage("Connected to Render PostgreSQL.");
        status.setDataSource(safeDataSource);

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT current_database() AS database_name, " +
                     "current_user AS database_user, " +
                     "version() AS database_version, " +
                     "CURRENT_TIMESTAMP::text AS database_time, " +
                     "(SELECT COUNT(*) FROM render_demo_heartbeat) AS heartbeat_count")) {
            if (resultSet.next()) {
                status.setDatabaseName(resultSet.getString("database_name"));
                status.setDatabaseUser(resultSet.getString("database_user"));
                status.setDatabaseVersion(resultSet.getString("database_version"));
                status.setDatabaseTime(resultSet.getString("database_time"));
                status.setHeartbeatCount(Integer.valueOf(resultSet.getInt("heartbeat_count")));
            }
        }

        return status;
    }

    private static final class ConnectionConfig {

        private final String jdbcUrl;
        private final Properties properties;
        private final String safeDataSource;

        private ConnectionConfig(final String jdbcUrl, final Properties properties, final String safeDataSource) {
            this.jdbcUrl = jdbcUrl;
            this.properties = properties;
            this.safeDataSource = safeDataSource;
        }

        private static ConnectionConfig from(final String databaseUrl) {
            if (databaseUrl.startsWith("jdbc:postgresql:")) {
                return new ConnectionConfig(databaseUrl, new Properties(), sanitizeJdbcUrl(databaseUrl));
            }

            final URI uri = URI.create(databaseUrl);
            final String scheme = uri.getScheme();
            if (!"postgres".equals(scheme) && !"postgresql".equals(scheme)) {
                throw new IllegalArgumentException("expected postgres:// or postgresql:// URL");
            }

            final Properties properties = new Properties();
            final String userInfo = uri.getUserInfo();
            if (userInfo != null) {
                final String[] credentials = userInfo.split(":", 2);
                properties.setProperty("user", decode(credentials[0]));
                if (credentials.length > 1) {
                    properties.setProperty("password", decode(credentials[1]));
                }
            }

            final StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
            jdbcUrl.append(uri.getHost());
            if (uri.getPort() > 0) {
                jdbcUrl.append(':').append(uri.getPort());
            }
            jdbcUrl.append(uri.getPath());
            if (uri.getQuery() != null && !uri.getQuery().isEmpty()) {
                jdbcUrl.append('?').append(uri.getQuery());
            }

            return new ConnectionConfig(jdbcUrl.toString(), properties, sanitizeUri(uri));
        }

        private static String decode(final String value) {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        }

        private static String sanitizeUri(final URI uri) {
            final StringBuilder safe = new StringBuilder();
            safe.append(uri.getScheme()).append("://").append(uri.getHost());
            if (uri.getPort() > 0) {
                safe.append(':').append(uri.getPort());
            }
            safe.append(uri.getPath());
            return safe.toString();
        }

        private static String sanitizeJdbcUrl(final String jdbcUrl) {
            return jdbcUrl.replaceFirst("//[^/@]+@", "//");
        }

        private String getJdbcUrl() {
            return this.jdbcUrl;
        }

        private Properties getProperties() {
            return this.properties;
        }

        private String getSafeDataSource() {
            return this.safeDataSource;
        }
    }

}
