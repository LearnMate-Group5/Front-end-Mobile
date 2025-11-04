# Hướng dẫn sử dụng ZaloPay Sandbox

## Câu hỏi: Cài APK trên máy thực và tải ZaloPay Sandbox có xài được không?

### ✅ **CÓ, hoàn toàn được!**

## Giải thích:

### 1. **ZaloPay Sandbox App**
- ZaloPay cung cấp ứng dụng **ZaloPay Sandbox** riêng cho môi trường test
- App này tương tự ZaloPay thật nhưng chỉ dùng để test thanh toán
- Bạn có thể tải từ:
  - **CH Play**: Tìm "ZaloPay Sandbox"
  - **App Store**: Tìm "ZaloPay Sandbox"
  - Hoặc từ ZaloPay Developer Portal

### 2. **Cách hoạt động:**

#### **Khi build APK với SANDBOX environment:**
```
Environment.SANDBOX → Kết nối đến sandbox.zalopay.com.vn
```

#### **Flow thanh toán:**
1. App của bạn gọi API `createorder` → `sandbox.zalopay.com.vn`
2. Nhận `zptranstoken` từ Sandbox server
3. Gọi `ZaloPaySDK.payOrder()` → SDK sẽ mở **ZaloPay Sandbox App**
4. User thanh toán trong Sandbox app
5. Callback về app của bạn

### 3. **Lưu ý quan trọng:**

#### ✅ **Có thể:**
- Cài APK release/debug trên máy thực
- Cài ZaloPay Sandbox app trên máy thực
- Test thanh toán với Sandbox (không cần tiền thật)
- Test tất cả các kênh thanh toán (Visa/Master, ATM, ZaloPay Wallet, etc.)

#### ❌ **Không thể:**
- Dùng Sandbox để test với **ZaloPay app thật** (app production)
- Sandbox chỉ hoạt động với **ZaloPay Sandbox app**

### 4. **Cấu hình hiện tại:**

Trong `LearnMateApplication.java`:
```java
private static final Environment ZALOPAY_ENVIRONMENT = Environment.SANDBOX;
```

Trong `ZaloPayApiClient.java`:
```java
private static final String CREATE_ORDER_URL = SANDBOX_CREATE_ORDER_URL;
// = "https://sandbox.zalopay.com.vn/v001/tpe/createorder"
```

### 5. **Khi nào chuyển sang Production:**

Khi app sẵn sàng release:
1. Đổi `Environment.SANDBOX` → `Environment.PRODUCTION`
2. Đổi URL sang `PRODUCTION_CREATE_ORDER_URL`
3. User sẽ cần cài **ZaloPay app thật** (không phải Sandbox)
4. Thanh toán sẽ dùng **tiền thật**

### 6. **Test trên máy thực:**

#### **Bước 1: Build APK**
```bash
./gradlew assembleRelease
# APK sẽ ở: app/build/outputs/apk/release/app-release.apk
```

#### **Bước 2: Cài APK trên máy thực**
```bash
adb install app-release.apk
```

#### **Bước 3: Cài ZaloPay Sandbox**
- Tải từ CH Play hoặc App Store
- Hoặc download APK từ ZaloPay Developer Portal

#### **Bước 4: Test thanh toán**
- Mở app của bạn
- Vào Subscription → Upgrade
- Chọn ZaloPay Sandbox để thanh toán
- Test với các kênh khác nhau

### 7. **Tài khoản test:**

ZaloPay Sandbox cung cấp:
- Tài khoản test để đăng nhập
- Thẻ test để test thanh toán
- Số tiền test (không phải tiền thật)

Chi tiết xem tại: https://developers.zalopay.vn/

---

## Tóm lại:

✅ **Có thể cài APK trên máy thực và dùng ZaloPay Sandbox**
✅ **Sandbox hoàn toàn miễn phí, không cần tiền thật**
✅ **Test được đầy đủ các tính năng thanh toán**

**Lưu ý:** Phải cài **ZaloPay Sandbox app** (không phải ZaloPay app thật) khi dùng SANDBOX environment.

