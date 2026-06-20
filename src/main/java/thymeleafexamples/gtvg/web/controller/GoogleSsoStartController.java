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
package thymeleafexamples.gtvg.web.controller;

import java.io.Writer;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.web.IWebExchange;
import thymeleafexamples.gtvg.web.auth.GoogleSsoConfig;


public class GoogleSsoStartController implements IServletGTVGController {

    private static final String SCOPE = "openid email profile";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();


    public void process(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final GoogleSsoConfig config = GoogleSsoConfig.fromEnvironment();
        if (!config.isConfigured()) {
            request.getSession(true).setAttribute(
                    "googleSsoError",
                    "Google SSO chua duoc cau hinh. Vui long thiet lap GOOGLE_CLIENT_ID va GOOGLE_CLIENT_SECRET.");
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        final String state = randomState();
        final String redirectUri = config.resolveRedirectUri(request);
        request.getSession(true).setAttribute("googleOAuthState", state);
        request.getSession(true).setAttribute("googleOAuthRedirectUri", redirectUri);

        response.sendRedirect(config.getAuthorizationEndpoint() + "?" + queryString(
                "client_id", config.getClientId(),
                "redirect_uri", redirectUri,
                "response_type", "code",
                "scope", SCOPE,
                "state", state,
                "prompt", "select_account"));

    }


    public void process(final IWebExchange webExchange, final ITemplateEngine templateEngine,
                        final Writer writer) throws Exception {
        throw new UnsupportedOperationException("Google SSO start requires servlet response access");
    }


    private static String randomState() {
        final byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }


    private static String queryString(final String... pairs) {
        final StringBuilder queryStringBuilder = new StringBuilder();
        for (int i = 0; i < pairs.length; i += 2) {
            if (queryStringBuilder.length() > 0) {
                queryStringBuilder.append('&');
            }
            queryStringBuilder.append(encode(pairs[i])).append('=').append(encode(pairs[i + 1]));
        }
        return queryStringBuilder.toString();
    }


    private static String encode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

}
