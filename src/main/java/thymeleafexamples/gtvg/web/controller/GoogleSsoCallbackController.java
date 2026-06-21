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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.web.IWebExchange;
import thymeleafexamples.gtvg.business.entities.GoogleCollaborator;
import thymeleafexamples.gtvg.web.auth.GoogleSsoConfig;
import thymeleafexamples.gtvg.web.auth.GoogleSsoService;


public class GoogleSsoCallbackController implements IServletGTVGController {

    private final GoogleSsoService googleSsoService;


    public GoogleSsoCallbackController() {
        this(new GoogleSsoService());
    }


    public GoogleSsoCallbackController(final GoogleSsoService googleSsoService) {
        super();
        this.googleSsoService = googleSsoService;
    }


    public void process(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final HttpSession session = request.getSession(true);
        final String error = trimToNull(request.getParameter("error"));
        if (error != null) {
            session.setAttribute("googleSsoError", "Google SSO bi huy hoac khong duoc chap thuan: " + error);
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        final String code = trimToNull(request.getParameter("code"));
        final String state = trimToNull(request.getParameter("state"));
        final String expectedState = (String) session.getAttribute("googleOAuthState");
        final String redirectUri = (String) session.getAttribute("googleOAuthRedirectUri");
        session.removeAttribute("googleOAuthState");
        session.removeAttribute("googleOAuthRedirectUri");

        if (code == null || state == null || expectedState == null || !expectedState.equals(state)) {
            session.setAttribute("googleSsoError", "Google SSO khong hop le. Vui long thu lai.");
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        try {
            final GoogleSsoConfig config = GoogleSsoConfig.fromEnvironment();
            if (!config.isConfigured() || redirectUri == null) {
                session.setAttribute(
                        "googleSsoError",
                        "Google SSO chua duoc cau hinh. Vui long thiet lap GOOGLE_CLIENT_ID va GOOGLE_CLIENT_SECRET.");
                response.sendRedirect(request.getContextPath() + "/");
                return;
            }
            final GoogleCollaborator collaborator = this.googleSsoService.exchangeCodeForCollaborator(
                    config, code, redirectUri);
            session.setAttribute("googleCollaborator", collaborator);
            session.setAttribute("googleSsoSuccess", "Da ket noi Google thanh cong.");
        } catch (final Exception e) {
            session.setAttribute("googleSsoError", "Google SSO khong thanh cong: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/");

    }


    public void process(final IWebExchange webExchange, final ITemplateEngine templateEngine,
                        final Writer writer) throws Exception {
        throw new UnsupportedOperationException("Google SSO callback requires servlet response access");
    }


    private static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}
