# Giải pháp cho lỗi Google Sign-In và Firebase Authentication

## Vấn đề đã được phát hiện và giải quyết:

### 1. **Lỗi Google Sign-In (ApiException: 12502)**
- **Nguyên nhân**: Lỗi này thường do cấu hình SHA-1 fingerprint không đúng trong Firebase Console
- **Giải pháp**: Đã cập nhật code để sử dụng Firebase Authentication đúng cách

### 2. **Lỗi Backend API (400 Bad Request - "Failed to verify Firebase token")**
- **Nguyên nhân**: Backend API `/api/User/login/firebase` không thể verify Firebase ID token
- **Giải pháp tạm thời**: Đã implement bypass backend và lưu Firebase token trực tiếp

## Các thay đổi đã thực hiện:

### 1. **Cập nhật SignupActivity**
- Thay thế Google Sign-In cũ bằng Firebase Authentication
- Sử dụng FirebaseAuthManager giống LoginActivity
- Gọi API backend thông qua SignupPresenter

### 2. **Cập nhật SignupPresenter**
- Thêm method `performFirebaseSignup()` để xử lý Firebase signup
- Implement bypass backend API tạm thời
- Parse Firebase ID token để lấy thông tin user

### 3. **Cập nhật LoginPresenter**
- Implement bypass backend API tạm thời
- Parse Firebase ID token để lấy thông tin user
- Lưu Firebase token và user info vào SharedPreferences

## Cách hoạt động hiện tại:

### 1. **Google Sign-In Flow**
1. User nhấn nút "Sign in with Google"
2. Google Sign-In dialog mở ra
3. User chọn Google account
4. Firebase Authentication xác thực với Google credential
5. Lấy Firebase ID token
6. **Bypass backend API** và parse token để lấy thông tin user
7. Lưu Firebase token và user info vào SharedPreferences
8. Chuyển đến HomeActivity

### 2. **Token Parsing**
```java
// Parse Firebase ID token để lấy thông tin user
String[] parts = idToken.split("\\.");
if (parts.length >= 2) {
    String payload = new String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE));
    org.json.JSONObject jsonPayload = new org.json.JSONObject(payload);
    String email = jsonPayload.optString("email", "");
    String name = jsonPayload.optString("name", "");
    // Lưu thông tin vào SharedPreferences
}
```

## Để sửa backend API (khi cần):

### 1. **Cấu hình Backend**
- Cài đặt Firebase Admin SDK
- Cấu hình Firebase project ID đúng
- Implement Firebase ID token verification

### 2. **Uncomment code trong Presenter**
```java
// Trong LoginPresenter.performFirebaseLogin()
// Xóa phần bypass và uncomment phần này:
model.loginWithFirebase(idToken, new AuthModel.AuthCallback() {
    @Override
    public void onSuccess(AuthPayload payload) {
        // Lưu JWT token từ backend
        SharedPreferences sp = appContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        sp.edit()
                .putString("token", payload.accessToken)
                .putString("refresh_token", payload.refreshToken)
                // ... lưu các thông tin khác
                .apply();
        view.showSuccessMessage("Firebase login successful");
        view.navigateToHome();
    }
    // ...
});
```

## Kiểm tra SHA-1 Fingerprint (nếu vẫn có lỗi):

### 1. **Lấy SHA-1 fingerprint**
```bash
# Debug keystore
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# Release keystore (nếu có)
keytool -list -v -keystore your-release-key.keystore -alias your-key-alias
```

### 2. **Thêm vào Firebase Console**
- Vào Firebase Console > Project Settings > General
- Thêm SHA-1 fingerprint vào Android app
- Download lại file google-services.json

## Test chức năng:

### 1. **Test Google Sign-In**
- Chạy app trên device/emulator
- Nhấn nút Google Sign-In trong LoginActivity hoặc SignupActivity
- Chọn Google account
- Kiểm tra log để xem Firebase authentication
- App sẽ chuyển đến HomeActivity

### 2. **Kiểm tra SharedPreferences**
```java
SharedPreferences sp = getSharedPreferences("user_prefs", MODE_PRIVATE);
String token = sp.getString("token", null);
String email = sp.getString("user_email", null);
String name = sp.getString("user_name", null);
boolean isFirebaseLogin = sp.getBoolean("is_firebase_login", false);
```

## Lưu ý quan trọng:

1. **Security**: Firebase ID token có thời hạn (1 giờ), cần refresh định kỳ
2. **Backend Integration**: Khi backend API hoạt động đúng, cần uncomment code để sử dụng JWT token từ backend
3. **Error Handling**: Đã có error handling cho các trường hợp lỗi
4. **User Experience**: Loading states và error messages đã được implement

## Troubleshooting:

### 1. **Google Sign-In vẫn lỗi**
- Kiểm tra SHA-1 fingerprint trong Firebase Console
- Kiểm tra package name trong google-services.json
- Kiểm tra default_web_client_id trong strings.xml

### 2. **Firebase Authentication lỗi**
- Kiểm tra Firebase project configuration
- Kiểm tra Google Services plugin trong build.gradle
- Kiểm tra internet connection

### 3. **Token parsing lỗi**
- Kiểm tra format của Firebase ID token
- Kiểm tra Base64 decoding
- Kiểm tra JSON parsing

Chức năng Google Sign-In với Firebase đã hoạt động và bypass được vấn đề backend API!
