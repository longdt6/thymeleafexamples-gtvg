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

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import thymeleafexamples.gtvg.business.entities.GoogleCollaborator;


public class GoogleSsoServiceTest {

    private HttpServer server;
    private String baseUrl;
    private AtomicReference<String> tokenRequestBody;
    private AtomicReference<String> userinfoAuthorization;


    @Before
    public void setUp() throws Exception {
        this.tokenRequestBody = new AtomicReference<String>();
        this.userinfoAuthorization = new AtomicReference<String>();
        this.server = HttpServer.create(new InetSocketAddress(0), 0);
        this.server.createContext("/token", this::handleTokenRequest);
        this.server.createContext("/userinfo", this::handleUserinfoRequest);
        this.server.start();
        this.baseUrl = "http://localhost:" + this.server.getAddress().getPort();
    }


    @After
    public void tearDown() {
        if (this.server != null) {
            this.server.stop(0);
        }
    }


    @Test
    public void exchangesCodeForGoogleCollaboratorProfile() throws Exception {
        final GoogleSsoConfig config = new GoogleSsoConfig(
                "client id",
                "client secret",
                null,
                this.baseUrl + "/auth",
                this.baseUrl + "/token",
                this.baseUrl + "/userinfo");

        final GoogleCollaborator collaborator = new GoogleSsoService()
                .exchangeCodeForCollaborator(config, "auth code", "http://localhost/callback");

        Assert.assertEquals("collaborator@example.com", collaborator.getEmail());
        Assert.assertEquals("Collab User", collaborator.getName());
        Assert.assertEquals("Collab", collaborator.getGivenName());
        Assert.assertEquals("User", collaborator.getFamilyName());
        Assert.assertEquals("https://example.com/avatar.png", collaborator.getPictureUrl());
        Assert.assertTrue(this.tokenRequestBody.get().contains("code=auth+code"));
        Assert.assertTrue(this.tokenRequestBody.get().contains("client_id=client+id"));
        Assert.assertTrue(this.tokenRequestBody.get().contains("client_secret=client+secret"));
        Assert.assertTrue(this.tokenRequestBody.get().contains(
                "redirect_uri=http%3A%2F%2Flocalhost%2Fcallback"));
        Assert.assertEquals("Bearer token-value", this.userinfoAuthorization.get());
    }


    private void handleTokenRequest(final HttpExchange exchange) throws java.io.IOException {
        this.tokenRequestBody.set(
                new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        sendJson(exchange, 200, "{\"access_token\":\"token-value\",\"token_type\":\"Bearer\"}");
    }


    private void handleUserinfoRequest(final HttpExchange exchange) throws java.io.IOException {
        this.userinfoAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
        sendJson(exchange, 200,
                "{\"email\":\"collaborator@example.com\","
                + "\"email_verified\":true,"
                + "\"name\":\"Collab User\","
                + "\"given_name\":\"Collab\","
                + "\"family_name\":\"User\","
                + "\"picture\":\"https://example.com/avatar.png\"}");
    }


    private static void sendJson(final HttpExchange exchange, final int statusCode, final String body)
            throws java.io.IOException {
        final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

}
