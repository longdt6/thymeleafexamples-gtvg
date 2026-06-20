# Google SSO cho luồng Cộng tác viên

Tài liệu này mô tả cơ chế hoạt động của tính năng Google SSO khi người dùng bấm
`Tham gia với tư cách Cộng tác viên`, và cách cấu hình để chạy với Google OAuth
thật trên môi trường deploy.

## Luồng hoạt động

1. Người dùng bấm nút `Tham gia với tư cách Cộng tác viên` trên trang chủ.
2. Nút này gọi endpoint nội bộ:
   - `GET /auth/google/collaborator`
3. `GoogleSsoStartController` kiểm tra cấu hình `GOOGLE_CLIENT_ID` và
   `GOOGLE_CLIENT_SECRET`.
   - Nếu thiếu cấu hình, người dùng được redirect về trang chủ và thấy banner lỗi.
   - Nếu đủ cấu hình, server sinh `state` ngẫu nhiên để chống CSRF, lưu `state` và
     `redirect_uri` vào session.
4. Server redirect người dùng sang Google OAuth:
   - `https://accounts.google.com/o/oauth2/v2/auth`
   - Scope đang dùng: `openid email profile`
   - Response type: `code`
   - Prompt: `select_account`
5. Sau khi người dùng đăng nhập và đồng ý quyền truy cập, Google redirect về:
   - `GET /auth/google/callback?code=...&state=...`
6. `GoogleSsoCallbackController` kiểm tra `state` trong query có khớp với `state`
   đã lưu trong session không.
   - Nếu không khớp hoặc thiếu `code`, server redirect về trang chủ và hiển thị lỗi.
7. Nếu hợp lệ, server đổi authorization code lấy access token qua:
   - `POST https://oauth2.googleapis.com/token`
8. Server dùng access token để lấy hồ sơ Google qua:
   - `GET https://openidconnect.googleapis.com/v1/userinfo`
9. Email, tên, họ, tên đệm và avatar được lưu vào session dưới dạng
   `GoogleCollaborator`.
10. Người dùng được redirect về trang chủ và thấy banner đã kết nối Google thành công.

## Biến môi trường bắt buộc

Các biến sau phải có trên môi trường deploy thật:

- `GOOGLE_CLIENT_ID`: OAuth Client ID lấy từ Google Cloud Console.
- `GOOGLE_CLIENT_SECRET`: OAuth Client Secret lấy từ Google Cloud Console.

Nếu thiếu một trong hai biến này, endpoint SSO không gọi Google và sẽ hiển thị lỗi
cấu hình trên trang chủ.

## Biến môi trường tùy chọn

- `GOOGLE_REDIRECT_URI`: Redirect URI cố định để gửi cho Google. Nên cấu hình biến
  này nếu app chạy sau reverse proxy/load balancer hoặc domain public khác với host
  nội bộ của servlet container.
- `GOOGLE_OAUTH_AUTH_URL`: Override authorization endpoint. Mặc định là
  `https://accounts.google.com/o/oauth2/v2/auth`.
- `GOOGLE_OAUTH_TOKEN_URL`: Override token endpoint. Mặc định là
  `https://oauth2.googleapis.com/token`.
- `GOOGLE_OAUTH_USERINFO_URL`: Override userinfo endpoint. Mặc định là
  `https://openidconnect.googleapis.com/v1/userinfo`.

Nếu không đặt `GOOGLE_REDIRECT_URI`, app tự dựng redirect URI theo request hiện tại:

```text
<scheme>://<host><context-path>/auth/google/callback
```

App có hỗ trợ `X-Forwarded-Proto` và `X-Forwarded-Host`, nhưng với production nên
đặt `GOOGLE_REDIRECT_URI` rõ ràng để tránh sai domain khi đi qua proxy.

## Cấu hình Google Cloud OAuth

1. Vào Google Cloud Console.
2. Chọn hoặc tạo project cho app.
3. Vào `APIs & Services` -> `OAuth consent screen`.
4. Cấu hình consent screen:
   - App name: tên sản phẩm hiển thị với người dùng.
   - User support email: email hỗ trợ.
   - Authorized domains: domain public của app, ví dụ `example.com`.
   - Scopes: dùng các scope cơ bản `openid`, `email`, `profile`.
5. Vào `APIs & Services` -> `Credentials`.
6. Tạo credential mới:
   - Type: `OAuth client ID`.
   - Application type: `Web application`.
7. Thêm Authorized redirect URI đúng với domain deploy:
   - `https://<host>/auth/google/callback`
   - Ví dụ: `https://smeconnect.example.com/auth/google/callback`
8. Copy `Client ID` vào `GOOGLE_CLIENT_ID`.
9. Copy `Client secret` vào `GOOGLE_CLIENT_SECRET`.

Giá trị Authorized redirect URI trên Google Cloud phải khớp chính xác với redirect
URI mà app gửi sang Google. Khác scheme, host, path hoặc context path đều có thể làm
Google trả lỗi `redirect_uri_mismatch`.

## Cấu hình deploy thật

Ví dụ biến môi trường cho app chạy ở `https://smeconnect.example.com`:

```text
GOOGLE_CLIENT_ID=<client-id-tu-google-cloud>
GOOGLE_CLIENT_SECRET=<client-secret-tu-google-cloud>
GOOGLE_REDIRECT_URI=https://smeconnect.example.com/auth/google/callback
```

Nếu WAR được deploy dưới context path khác root, ví dụ `/gtvg`, redirect URI phải
bao gồm context path:

```text
GOOGLE_REDIRECT_URI=https://smeconnect.example.com/gtvg/auth/google/callback
```

Và Authorized redirect URI trên Google Cloud cũng phải dùng đúng URL đó.

## Kiểm tra sau deploy

1. Mở trang chủ của app.
2. Bấm `Tham gia với tư cách Cộng tác viên`.
3. Trình duyệt phải chuyển sang màn hình đăng nhập/consent của Google.
4. Đăng nhập bằng tài khoản Google hợp lệ.
5. Sau callback, app phải quay lại trang chủ và hiển thị banner đã kết nối Google
   cho Cộng tác viên kèm tên/email.

Nếu Google hiển thị `invalid_client`, hãy kiểm tra lại `GOOGLE_CLIENT_ID` và
`GOOGLE_CLIENT_SECRET`.

Nếu Google hiển thị `redirect_uri_mismatch`, hãy kiểm tra:

- `GOOGLE_REDIRECT_URI` trên deploy.
- Authorized redirect URI trong Google Cloud.
- Context path của WAR nếu app không chạy ở root `/`.
- Scheme `https` thay vì `http` trên production.

## File liên quan

- `GoogleSsoStartController`: tạo request OAuth và redirect sang Google.
- `GoogleSsoCallbackController`: xử lý callback, validate `state`, đổi `code` lấy
  profile.
- `GoogleSsoConfig`: đọc biến môi trường và dựng redirect URI.
- `GoogleSsoService`: gọi Google token endpoint và userinfo endpoint.
- `GoogleCollaborator`: model session lưu thông tin cộng tác viên đăng nhập Google.
- `home.html`: chứa CTA cộng tác viên và banner trạng thái SSO.
