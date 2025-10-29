# 🚀 Hướng dẫn Test API Integration với Máy Ảo Android

## 📋 Tổng quan

Hướng dẫn này giúp bạn test tích hợp API upload và dịch PDF trong máy ảo Android.

## 🔧 Cấu hình hiện tại

### API Endpoints

- **Upload**: `POST /api/Ai/upload` (multipart/form-data)
  - File: PDF file
  - UserId: String
- **Get Chapters**: `GET /api/Ai/chapters/{jobId}`
- **Get Status**: `GET /api/Ai/status/{jobId}`

### Network Configuration

- **Base URL**: `http://10.0.2.2:2406/` (localhost trong máy ảo Android)
- **Polling Interval**: 5 giây
- **Max Attempts**: 60 lần (5 phút timeout)

## 🧪 Các bước test

### 1. Chuẩn bị Server

```bash
# Đảm bảo server đang chạy trên port 2406
# Kiểm tra bằng cách truy cập: http://localhost:2406
```

### 2. Test trong Máy Ảo Android

#### Bước 1: Mở app trong máy ảo

1. Build và chạy app trên máy ảo Android
2. Điều hướng đến Import Activity

#### Bước 2: Import PDF

1. Ấn nút "Import from File"
2. Chọn file PDF từ storage
3. App sẽ:
   - Upload file lên API
   - Hiển thị dữ liệu mẫu tạm thời
   - Bắt đầu polling để lấy dữ liệu thật

#### Bước 3: Test Raw/Translate

1. Ấn nút "Raw" hoặc "Translate" trên PDF card
2. App sẽ hiển thị:
   - Dữ liệu mẫu tạm thời (nếu API chưa xử lý xong)
   - Dữ liệu thật từ API (khi processing hoàn tất)

## 🔍 Debug và Monitoring

### Logs để theo dõi

```bash
# Xem logs trong Android Studio Logcat
# Filter by tag: "ChapterPolling"
```

### Các log quan trọng

- `Polling attempt X/Y for jobId: xxx`
- `Response received: 200`
- `Status: completed, Chapters: X`
- `✅ Đã lấy được X chapters từ API!`

### Network Debug

- Kiểm tra network requests trong Logcat
- Xem HTTP status codes và response bodies
- Kiểm tra timeout và retry logic

## 🐛 Troubleshooting

### Lỗi thường gặp

#### 1. Connection Refused

```
Network Error: Connection refused
```

**Giải pháp**: Đảm bảo server đang chạy trên port 2406

#### 2. 404 Not Found

```
API Error: 404 - Not Found
```

**Giải pháp**: Kiểm tra endpoint URL và server routes

#### 3. Timeout

```
⏰ Timeout: Không thể lấy chapters sau 60 lần thử
```

**Giải pháp**: Tăng MAX_ATTEMPTS hoặc kiểm tra server performance

#### 4. Upload Failed

```
Upload lỗi: 500
```

**Giải pháp**: Kiểm tra file format và server processing

## 📱 Test Cases

### Test Case 1: Upload thành công

1. ✅ Upload PDF file
2. ✅ Nhận jobId từ response
3. ✅ Hiển thị dữ liệu mẫu tạm thời
4. ✅ Bắt đầu polling

### Test Case 2: Lấy dữ liệu từ API

1. ✅ Polling hoạt động
2. ✅ Nhận dữ liệu thật từ API
3. ✅ Thay thế dữ liệu mẫu
4. ✅ Hiển thị thông báo thành công

### Test Case 3: Error Handling

1. ✅ Xử lý lỗi network
2. ✅ Xử lý timeout
3. ✅ Xử lý API errors
4. ✅ Hiển thị thông báo lỗi phù hợp

## 🎯 Expected Results

### Khi thành công

- Upload PDF → Nhận jobId
- Hiển thị dữ liệu mẫu tạm thời
- Polling → Lấy dữ liệu thật
- Thay thế dữ liệu mẫu bằng dữ liệu thật
- Hiển thị: "✅ Đã lấy được X chapters từ API!"

### Khi có lỗi

- Hiển thị thông báo lỗi rõ ràng
- Retry logic hoạt động
- Timeout handling
- User experience tốt

## 📊 Performance Metrics

### Thời gian xử lý

- Upload: ~2-5 giây
- Processing: ~30-60 giây (tùy file size)
- Polling: 5 giây/interval
- Total: ~1-2 phút

### Memory Usage

- Cache dữ liệu mẫu: ~1MB
- Cache dữ liệu thật: ~5-10MB (tùy file size)

## 🔄 Next Steps

1. **Test với file PDF thật** từ Google Drive
2. **Optimize polling interval** dựa trên server performance
3. **Add progress indicator** cho user experience
4. **Implement retry mechanism** cho failed uploads
5. **Add file size validation**

---

## 📞 Support

Nếu gặp vấn đề, kiểm tra:

1. Server logs
2. Android logs (Logcat)
3. Network connectivity
4. File format compatibility

