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
package thymeleafexamples.gtvg.business.persistence;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DatabaseConnectionProperties {

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String safeDataSource;

    public static DatabaseConnectionProperties from(final String databaseUrl) {
        if (databaseUrl.startsWith("jdbc:")) {
            return new DatabaseConnectionProperties(
                    databaseUrl,
                    null,
                    null,
                    sanitizeJdbcUrl(databaseUrl));
        }

        final URI uri = URI.create(databaseUrl);
        final String scheme = uri.getScheme();
        if (!"postgres".equals(scheme) && !"postgresql".equals(scheme)) {
            throw new IllegalArgumentException("expected postgres://, postgresql://, or jdbc: URL");
        }

        final String[] credentials = Optional.ofNullable(uri.getUserInfo())
                .map(userInfo -> userInfo.split(":", 2))
                .orElse(new String[0]);
        final String username = credentials.length > 0 ? decode(credentials[0]) : null;
        final String password = credentials.length > 1 ? decode(credentials[1]) : null;

        final StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
        jdbcUrl.append(uri.getHost());
        if (uri.getPort() > 0) {
            jdbcUrl.append(':').append(uri.getPort());
        }
        jdbcUrl.append(uri.getPath());
        if (uri.getQuery() != null && !uri.getQuery().isEmpty()) {
            jdbcUrl.append('?').append(uri.getQuery());
        }

        return new DatabaseConnectionProperties(jdbcUrl.toString(), username, password, sanitizeUri(uri));
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
        return jdbcUrl
                .replaceFirst("//[^/@]+@", "//")
                .replaceAll("(?i)(password=)[^&]+", "$1****");
    }

}
