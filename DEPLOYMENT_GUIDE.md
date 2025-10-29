# 🚀 LearnMate App Deployment Guide

## 📱 Quick Start (5 phút)

### 1. Build APK Debug (Đơn giản nhất)

```bash
# Windows
deploy_script.bat

# Linux/Mac
chmod +x deploy_script.sh
./deploy_script.sh
```

### 2. Install APK lên thiết bị

- Copy file `app-debug.apk` vào điện thoại
- Bật "Unknown sources" trong Settings
- Install APK

## 🏪 Deploy lên Google Play Store

### Bước 1: Chuẩn bị

1. Tạo Google Play Console account ($25)
2. Tạo keystore cho signing:

```bash
keytool -genkey -v -keystore learnmate-release-key.keystore -alias learnmate -keyalg RSA -keysize 2048 -validity 10000
```

### Bước 2: Cấu hình signing

Tạo file `app/keystore.properties`:

```properties
storePassword=your_store_password
keyPassword=your_key_password
keyAlias=learnmate
storeFile=../learnmate-release-key.keystore
```

### Bước 3: Build và Upload

```bash
# Build App Bundle
./gradlew bundleRelease

# Upload file: app/build/outputs/bundle/release/app-release.aab
# Lên Google Play Console → Production → Create new release
```

## 🔥 Deploy lên Firebase App Distribution (Miễn phí)

### Bước 1: Setup Firebase

1. Truy cập [Firebase Console](https://console.firebase.google.com)
2. Tạo project mới
3. Thêm Android app với package: `com.example.LearnMate`
4. Download `google-services.json` → đặt vào `app/`

### Bước 2: Upload APK

1. Firebase Console → App Distribution
2. Upload file `app-release.apk`
3. Thêm testers (email)
4. Gửi link download cho testers

## 📊 Các loại build

| Loại            | Mục đích                 | File output       | Cách build                  |
| --------------- | ------------------------ | ----------------- | --------------------------- |
| **Debug APK**   | Testing, Development     | `app-debug.apk`   | `./gradlew assembleDebug`   |
| **Release APK** | Production, Distribution | `app-release.apk` | `./gradlew assembleRelease` |
| **App Bundle**  | Google Play Store        | `app-release.aab` | `./gradlew bundleRelease`   |

## 🛠️ Troubleshooting

### Lỗi: "Keystore not found"

- Kiểm tra file `app/keystore.properties` có tồn tại
- Kiểm tra đường dẫn keystore trong file properties

### Lỗi: "Build failed"

- Chạy `./gradlew clean` trước khi build
- Kiểm tra Android SDK đã cài đặt đúng

### Lỗi: "Signing failed"

- Kiểm tra password keystore
- Kiểm tra alias name

## 📱 Test trên thiết bị

### Cách 1: USB Debugging

1. Bật Developer Options
2. Bật USB Debugging
3. Kết nối USB → Run app từ Android Studio

### Cách 2: Install APK

1. Build APK → Copy vào thiết bị
2. Bật "Unknown sources"
3. Install APK

## 🎯 Best Practices

### Trước khi deploy:

- [ ] Test trên nhiều thiết bị
- [ ] Kiểm tra performance
- [ ] Test các tính năng chính
- [ ] Cập nhật version code/name

### Security:

- [ ] Không commit keystore vào git
- [ ] Backup keystore an toàn
- [ ] Sử dụng ProGuard cho release

### Performance:

- [ ] Enable R8 minification
- [ ] Optimize images
- [ ] Remove unused resources

## 📞 Support

Nếu gặp vấn đề:

1. Kiểm tra log trong Android Studio
2. Chạy `./gradlew clean` và build lại
3. Kiểm tra Android SDK version
4. Kiểm tra keystore configuration

---

**Happy Deploying! 🚀**

