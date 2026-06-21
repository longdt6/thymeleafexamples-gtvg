# Google SSO for Collaborator Signup

This document explains how Google SSO works when a user clicks the collaborator
signup CTA, and how to configure the application for a real Google OAuth deploy.

## Runtime flow

1. The user clicks the collaborator signup CTA on the home page.
2. The CTA calls the internal endpoint:
   - `GET /auth/google/collaborator`
3. `GoogleSsoStartController` checks `GOOGLE_CLIENT_ID` and
   `GOOGLE_CLIENT_SECRET`.
   - If either value is missing, the user is redirected back to the home page and
     sees a configuration error banner.
   - If both values are present, the server creates a random `state` value for
     CSRF protection and stores `state` plus `redirect_uri` in the HTTP session.
4. The server redirects the user to Google OAuth:
   - `https://accounts.google.com/o/oauth2/v2/auth`
   - Scope: `openid email profile`
   - Response type: `code`
   - Prompt: `select_account`
5. After the user signs in and grants access, Google redirects back to:
   - `GET /auth/google/callback?code=...&state=...`
6. `GoogleSsoCallbackController` verifies that the callback `state` matches the
   `state` stored in the session.
   - If `state` does not match, or `code` is missing, the server redirects back
     to the home page and shows an error banner.
7. If the callback is valid, the server exchanges the authorization code for an
   access token through:
   - `POST https://oauth2.googleapis.com/token`
8. The server uses the access token to fetch the Google profile through:
   - `GET https://openidconnect.googleapis.com/v1/userinfo`
9. Email, full name, given name, family name, and avatar URL are stored in the
   session as `GoogleCollaborator`.
10. The user is redirected back to the home page and sees a successful Google
    connection banner.

## Required environment variables

These variables must exist in the real deploy environment:

- `GOOGLE_CLIENT_ID`: OAuth Client ID from Google Cloud Console.
- `GOOGLE_CLIENT_SECRET`: OAuth Client Secret from Google Cloud Console.

If either value is missing, the SSO endpoint does not call Google and shows a
configuration error on the home page.

## Optional environment variables

- `GOOGLE_REDIRECT_URI`: Fixed redirect URI sent to Google. Set this in
  production when the app runs behind a reverse proxy/load balancer or when the
  public domain differs from the servlet container host.
- `GOOGLE_OAUTH_AUTH_URL`: Authorization endpoint override. Default:
  `https://accounts.google.com/o/oauth2/v2/auth`.
- `GOOGLE_OAUTH_TOKEN_URL`: Token endpoint override. Default:
  `https://oauth2.googleapis.com/token`.
- `GOOGLE_OAUTH_USERINFO_URL`: Userinfo endpoint override. Default:
  `https://openidconnect.googleapis.com/v1/userinfo`.

If `GOOGLE_REDIRECT_URI` is not set, the app builds the redirect URI from the
current request:

```text
<scheme>://<host><context-path>/auth/google/callback
```

The app supports `X-Forwarded-Proto` and `X-Forwarded-Host`, but production
deploys should set `GOOGLE_REDIRECT_URI` explicitly to avoid proxy host issues.

## Google Cloud OAuth setup

1. Open Google Cloud Console.
2. Select or create the project for this app.
3. Go to `APIs & Services` -> `OAuth consent screen`.
4. Configure the consent screen:
   - App name: the product name shown to users.
   - User support email: support contact email.
   - Authorized domains: the public app domain, for example `example.com`.
   - Scopes: use the basic scopes `openid`, `email`, and `profile`.
5. Go to `APIs & Services` -> `Credentials`.
6. Create a new credential:
   - Type: `OAuth client ID`.
   - Application type: `Web application`.
7. Add the authorized redirect URI for the deployed domain:
   - `https://<host>/auth/google/callback`
   - Example: `https://smeconnect.example.com/auth/google/callback`
8. Copy `Client ID` to `GOOGLE_CLIENT_ID`.
9. Copy `Client secret` to `GOOGLE_CLIENT_SECRET`.

The authorized redirect URI in Google Cloud must exactly match the redirect URI
sent by the app. Different scheme, host, path, or context path values can cause
Google to return `redirect_uri_mismatch`.

## Real deploy configuration

Example environment variables for an app running at
`https://smeconnect.example.com`:

```text
GOOGLE_CLIENT_ID=<google-cloud-client-id>
GOOGLE_CLIENT_SECRET=<google-cloud-client-secret>
GOOGLE_REDIRECT_URI=https://smeconnect.example.com/auth/google/callback
```

If the WAR is deployed under a non-root context path, for example `/gtvg`, the
redirect URI must include that context path:

```text
GOOGLE_REDIRECT_URI=https://smeconnect.example.com/gtvg/auth/google/callback
```

The Google Cloud authorized redirect URI must use the same URL.

## Post-deploy verification

1. Open the application home page.
2. Click the collaborator signup CTA.
3. The browser should navigate to the Google sign-in/consent screen.
4. Sign in with a valid Google account.
5. After the callback, the app should return to the home page and show a Google
   connection banner with the collaborator name/email.

If Google shows `invalid_client`, check `GOOGLE_CLIENT_ID` and
`GOOGLE_CLIENT_SECRET`.

If Google shows `redirect_uri_mismatch`, check:

- `GOOGLE_REDIRECT_URI` in the deploy environment.
- Authorized redirect URI in Google Cloud.
- WAR context path if the app does not run at root `/`.
- Production scheme, which should normally be `https`.

## Related files

- `GoogleSsoStartController`: creates the OAuth request and redirects to Google.
- `GoogleSsoCallbackController`: handles the callback, validates `state`, and
  exchanges `code` for the profile.
- `GoogleSsoConfig`: reads environment variables and builds the redirect URI.
- `GoogleSsoService`: calls the Google token endpoint and userinfo endpoint.
- `GoogleCollaborator`: session model for the Google collaborator profile.
- `home.html`: contains the collaborator CTA and SSO status banners.
