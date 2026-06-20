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

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import thymeleafexamples.gtvg.business.entities.GoogleCollaborator;


public class GoogleSsoService {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;


    public GoogleSsoService() {
        this(HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build(), new ObjectMapper());
    }


    public GoogleSsoService(final HttpClient httpClient, final ObjectMapper objectMapper) {
        super();
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }


    public GoogleCollaborator exchangeCodeForCollaborator(
            final GoogleSsoConfig config, final String code, final String redirectUri)
            throws IOException, InterruptedException {

        final String accessToken = requestAccessToken(config, code, redirectUri);
        return requestCollaboratorProfile(config, accessToken);

    }


    private String requestAccessToken(final GoogleSsoConfig config, final String code,
                                      final String redirectUri)
            throws IOException, InterruptedException {
        final String requestBody = formBody(
                "code", code,
                "client_id", config.getClientId(),
                "client_secret", config.getClientSecret(),
                "redirect_uri", redirectUri,
                "grant_type", "authorization_code");
        final HttpRequest request = HttpRequest.newBuilder(URI.create(config.getTokenEndpoint()))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        final HttpResponse<String> response =
                this.httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        final JsonNode tokenResponse = readSuccessfulJson(response, "Google token endpoint");
        final String accessToken = textValue(tokenResponse, "access_token");
        if (accessToken == null) {
            throw new IOException("Google token endpoint did not return an access token");
        }
        return accessToken;
    }


    private GoogleCollaborator requestCollaboratorProfile(final GoogleSsoConfig config,
                                                          final String accessToken)
            throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder(URI.create(config.getUserinfoEndpoint()))
                .timeout(REQUEST_TIMEOUT)
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        final HttpResponse<String> response =
                this.httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        final JsonNode profileResponse = readSuccessfulJson(response, "Google userinfo endpoint");
        final String email = textValue(profileResponse, "email");
        if (email == null) {
            throw new IOException("Google userinfo endpoint did not return an email");
        }
        final Boolean emailVerified = booleanValue(profileResponse, "email_verified", "verified_email");
        if (Boolean.FALSE.equals(emailVerified)) {
            throw new IOException("Google account email is not verified");
        }
        return new GoogleCollaborator(
                email,
                firstTextValue(profileResponse, "name", "email"),
                textValue(profileResponse, "given_name"),
                textValue(profileResponse, "family_name"),
                textValue(profileResponse, "picture"));
    }


    private JsonNode readSuccessfulJson(final HttpResponse<String> response, final String endpointName)
            throws IOException {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(endpointName + " returned HTTP " + response.statusCode());
        }
        return this.objectMapper.readTree(response.body());
    }


    private static String formBody(final String... pairs) {
        final List<String> encodedPairs = new ArrayList<String>();
        for (int i = 0; i < pairs.length; i += 2) {
            encodedPairs.add(encode(pairs[i]) + "=" + encode(pairs[i + 1]));
        }
        return String.join("&", encodedPairs);
    }


    private static String encode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }


    private static String firstTextValue(final JsonNode node, final String... fieldNames) {
        for (final String fieldName : fieldNames) {
            final String value = textValue(node, fieldName);
            if (value != null) {
                return value;
            }
        }
        return null;
    }


    private static String textValue(final JsonNode node, final String fieldName) {
        final JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        final String text = value.asText();
        return text == null || text.trim().isEmpty() ? null : text;
    }


    private static Boolean booleanValue(final JsonNode node, final String... fieldNames) {
        for (final String fieldName : fieldNames) {
            final JsonNode value = node.get(fieldName);
            if (value != null && !value.isNull()) {
                return Boolean.valueOf(value.asBoolean());
            }
        }
        return null;
    }

}
