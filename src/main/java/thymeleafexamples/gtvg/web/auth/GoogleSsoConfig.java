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
package thymeleafexamples.gtvg.web.auth;

import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;


public class GoogleSsoConfig {

    private static final String DEFAULT_AUTHORIZATION_ENDPOINT =
            "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String DEFAULT_TOKEN_ENDPOINT =
            "https://oauth2.googleapis.com/token";
    private static final String DEFAULT_USERINFO_ENDPOINT =
            "https://openidconnect.googleapis.com/v1/userinfo";
    private static final String CALLBACK_PATH = "/auth/google/callback";

    private final String clientId;
    private final String clientSecret;
    private final String configuredRedirectUri;
    private final String authorizationEndpoint;
    private final String tokenEndpoint;
    private final String userinfoEndpoint;


    public static GoogleSsoConfig fromEnvironment() {
        return new GoogleSsoConfig(
                readEnv("GOOGLE_CLIENT_ID"),
                readEnv("GOOGLE_CLIENT_SECRET"),
                readEnv("GOOGLE_REDIRECT_URI"),
                readEnvOrDefault("GOOGLE_OAUTH_AUTH_URL", DEFAULT_AUTHORIZATION_ENDPOINT),
                readEnvOrDefault("GOOGLE_OAUTH_TOKEN_URL", DEFAULT_TOKEN_ENDPOINT),
                readEnvOrDefault("GOOGLE_OAUTH_USERINFO_URL", DEFAULT_USERINFO_ENDPOINT));
    }


    public GoogleSsoConfig(final String clientId, final String clientSecret,
                           final String configuredRedirectUri, final String authorizationEndpoint,
                           final String tokenEndpoint, final String userinfoEndpoint) {
        super();
        this.clientId = trimToNull(clientId);
        this.clientSecret = trimToNull(clientSecret);
        this.configuredRedirectUri = trimToNull(configuredRedirectUri);
        this.authorizationEndpoint =
                Objects.requireNonNull(trimToNull(authorizationEndpoint), "authorizationEndpoint");
        this.tokenEndpoint = Objects.requireNonNull(trimToNull(tokenEndpoint), "tokenEndpoint");
        this.userinfoEndpoint = Objects.requireNonNull(trimToNull(userinfoEndpoint), "userinfoEndpoint");
    }


    public boolean isConfigured() {
        return this.clientId != null && this.clientSecret != null;
    }


    public String getClientId() {
        return this.clientId;
    }


    public String getClientSecret() {
        return this.clientSecret;
    }


    public String getAuthorizationEndpoint() {
        return this.authorizationEndpoint;
    }


    public String getTokenEndpoint() {
        return this.tokenEndpoint;
    }


    public String getUserinfoEndpoint() {
        return this.userinfoEndpoint;
    }


    public String resolveRedirectUri(final HttpServletRequest request) {
        if (this.configuredRedirectUri != null) {
            return this.configuredRedirectUri;
        }
        return buildRequestBaseUrl(request) + request.getContextPath() + CALLBACK_PATH;
    }


    private static String buildRequestBaseUrl(final HttpServletRequest request) {
        final String forwardedProto = firstHeaderValue(request, "X-Forwarded-Proto");
        final String forwardedHost = firstHeaderValue(request, "X-Forwarded-Host");
        final String scheme = forwardedProto != null ? forwardedProto : request.getScheme();
        if (forwardedHost != null) {
            return scheme + "://" + forwardedHost;
        }
        final int port = request.getServerPort();
        final boolean defaultPort =
                ("http".equalsIgnoreCase(scheme) && port == 80) ||
                ("https".equalsIgnoreCase(scheme) && port == 443);
        return scheme + "://" + request.getServerName() + (defaultPort ? "" : ":" + port);
    }


    private static String firstHeaderValue(final HttpServletRequest request, final String name) {
        final String value = trimToNull(request.getHeader(name));
        if (value == null) {
            return null;
        }
        final int commaIndex = value.indexOf(',');
        return commaIndex >= 0 ? trimToNull(value.substring(0, commaIndex)) : value;
    }


    private static String readEnvOrDefault(final String name, final String defaultValue) {
        final String value = readEnv(name);
        return value != null ? value : defaultValue;
    }


    private static String readEnv(final String name) {
        return trimToNull(System.getenv(name));
    }


    private static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}
