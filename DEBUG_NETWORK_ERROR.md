# 🔍 Hướng dẫn Debug Network Error khi Import từ Google Drive

## 📋 Tổng quan

Hướng dẫn này giúp bạn debug lỗi network error khi import file PDF từ Google Drive.

## 🐛 Các lỗi thường gặp

### 1. **Connection Refused**

```
Network error: Connection refused
```

**Nguyên nhân**: Server không chạy hoặc không thể kết nối
**Giải pháp**:

- Kiểm tra server có đang chạy trên port 2406 không
- Kiểm tra URL: `http://10.0.2.2:2406/`
- Test bằng curl: `curl http://localhost:2406/api/Ai/upload`

### 2. **Timeout Error**

```
Network error: timeout
```

**Nguyên nhân**: File quá lớn hoặc network chậm
**Giải pháp**:

- Đã tăng timeout lên 300 giây
- Kiểm tra kích thước file (nên < 50MB)
- Kiểm tra kết nối internet

### 3. **File Access Error**

```
File error: Cannot open input stream
```

**Nguyên nhân**: Không thể đọc file từ Google Drive
**Giải pháp**:

- Kiểm tra quyền truy cập file
- Thử download file về local trước
- Kiểm tra URI format

## 🔍 Debug Steps

### Bước 1: Kiểm tra Logs

```bash
# Trong Android Studio Logcat
# Filter by tags: "ImportActivity", "FileUtils", "ChapterPolling"
```

### Bước 2: Kiểm tra URI

```java
// Log sẽ hiển thị:
// ImportActivity: Starting upload for URI: content://...
// FileUtils: Processing URI: content://...
// FileUtils: FileName: example.pdf
// FileUtils: MimeType: application/pdf
```

### Bước 3: Kiểm tra File Copy

```java
// Log sẽ hiển thị:
// FileUtils: File copied successfully: 1234567 bytes
```

### Bước 4: Kiểm tra Network Request

```java
// Log sẽ hiển thị:
// ImportActivity: File part created successfully, starting upload...
// Retrofit: --> POST /api/Ai/upload
// Retrofit: <-- 200 OK
```

## 🛠️ Troubleshooting

### Kiểm tra Server

```bash
# Test server bằng curl
curl -X POST http://localhost:2406/api/Ai/upload \
  -F "File=@test.pdf" \
  -F "UserId=test"
```

### Kiểm tra Network

```bash
# Test kết nối từ máy ảo
adb shell ping 10.0.2.2
```

### Kiểm tra File

```bash
# Kiểm tra file có thể đọc được không
adb shell ls -la /data/data/com.example.LearnMate/cache/
```

## 📱 Test Cases

### Test Case 1: File từ Local Storage

1. ✅ Copy file PDF vào local storage
2. ✅ Import từ local storage
3. ✅ Kiểm tra logs

### Test Case 2: File từ Google Drive

1. ✅ Download file từ Google Drive
2. ✅ Import file đã download
3. ✅ Kiểm tra logs

### Test Case 3: File nhỏ (< 1MB)

1. ✅ Tạo file PDF nhỏ
2. ✅ Import file nhỏ
3. ✅ Kiểm tra upload time

### Test Case 4: File lớn (> 10MB)

1. ✅ Tạo file PDF lớn
2. ✅ Import file lớn
3. ✅ Kiểm tra timeout

## 🔧 Các thay đổi đã thực hiện

### 1. **FileUtils.java**

- ✅ Thêm logging chi tiết
- ✅ Tạo file cache với tên unique
- ✅ Xử lý lỗi input stream
- ✅ Cleanup file cache khi có lỗi

### 2. **ImportActivity.java**

- ✅ Thêm logging cho upload process
- ✅ Thêm error handling chi tiết
- ✅ Log URI và file info

### 3. **RetrofitClient.java**

- ✅ Tăng timeout lên 300 giây
- ✅ Tăng connect timeout lên 60 giây
- ✅ Retry on connection failure

## 📊 Expected Logs

### Khi thành công:

```
ImportActivity: Starting upload for URI: content://...
FileUtils: Processing URI: content://...
FileUtils: FileName: example.pdf
FileUtils: MimeType: application/pdf
FileUtils: File copied successfully: 1234567 bytes
ImportActivity: File part created successfully, starting upload...
ImportActivity: ✅ Upload thành công và đã lấy được dữ liệu!
```

### Khi có lỗi:

```
ImportActivity: Starting upload for URI: content://...
FileUtils: Processing URI: content://...
FileUtils: Error copying file: Cannot open input stream
ImportActivity: File error: Cannot open input stream
```

## 🎯 Next Steps

1. **Kiểm tra logs** trong Android Studio Logcat
2. **Test với file nhỏ** trước
3. **Kiểm tra server** có đang chạy không
4. **Test network connectivity** từ máy ảo
5. **Thử download file** về local trước khi import

## 📞 Support

Nếu vẫn gặp lỗi, hãy:

1. Copy logs từ Logcat
2. Kiểm tra server status
3. Test với file khác
4. Kiểm tra network connectivity

