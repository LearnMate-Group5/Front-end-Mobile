# API Integration Guide - PDF Chapter Extraction

## Tổng quan

Hệ thống đã được cập nhật để tích hợp với API thực tế để lấy chapters từ PDF và hiển thị cả raw và translated content.

## API Endpoints

### 1. Upload PDF

```
POST /api/Ai/upload
Content-Type: multipart/form-data

Parameters:
- File: PDF file (binary)
- UserId: User identifier (string)

Response:
{
  "success": true,
  "message": "Upload successful",
  "jobId": "unique-job-id",
  "fileName": "document.pdf",
  "totalChapters": 5,
  "status": "processing"
}
```

### 2. Get Chapters

```
GET /api/Ai/chapters/{jobId}

Response:
{
  "success": true,
  "jobId": "unique-job-id",
  "status": "completed",
  "chapters": [
    {
      "chapterNumber": 1,
      "title": "Chapter 1: Introduction",
      "rawContent": "This is the first chapter...",
      "translatedContent": "Đây là chương đầu tiên...",
      "pageRange": "1-5"
    }
  ],
  "totalChapters": 5,
  "processingProgress": 100
}
```

## Luồng hoạt động

### 1. Upload PDF

- User chọn file PDF từ Google Drive
- App upload file lên API với UserId
- API trả về jobId và status "processing"
- App bắt đầu polling để lấy chapters

### 2. Polling Chapters

- App gọi API mỗi 3 giây để kiểm tra status
- Khi status = "completed", app lưu chapters vào ContentCache
- Tối đa 20 lần thử (1 phút) trước khi timeout

### 3. Hiển thị Chapters

- Raw mode: Hiển thị chapters tiếng Anh
- Translate mode: Hiển thị chapters tiếng Việt
- User có thể click vào chapter để đọc chi tiết

## Cấu trúc Code

### Model Classes

- `UploadResponse.java`: Parse response từ upload API
- `ChapterResponse.java`: Model cho từng chapter
- `ChaptersResponse.java`: Parse response từ chapters API

### Service Classes

- `AiService.java`: Retrofit interface cho API calls
- `ChapterPollingService.java`: Service polling chapters

### Cache Management

- `ContentCache.java`: Lưu trữ chapters và jobId
- `ChapterUtils.java`: Utility cho chapter data

## Tính năng

### ✅ Đã hoàn thành

- Upload PDF với multipart form data
- Polling chapters từ API
- Hiển thị chapters theo mode (raw/translate)
- Navigation giữa các chapters
- Font size control
- Error handling và timeout

### 🔄 Fallback System

- Nếu API chưa sẵn sàng, app sử dụng dữ liệu mẫu
- Khi API trả về dữ liệu thật, tự động thay thế dữ liệu mẫu
- User experience không bị gián đoạn

## Cấu hình

### API Base URL

```java
// Trong ApiConfig.java
public static final String BASE_URL = "http://localhost:2406";
```

### Polling Settings

```java
// Trong ChapterPollingService.java
private static final int POLLING_INTERVAL = 3000; // 3 giây
private static final int MAX_ATTEMPTS = 20; // Tối đa 20 lần thử
```

## Testing

### 1. Test với dữ liệu mẫu

- Upload PDF → App hiển thị dữ liệu mẫu ngay lập tức
- Có thể test tất cả tính năng UI

### 2. Test với API thật

- Deploy API với endpoints mới
- Upload PDF thật → Polling → Hiển thị chapters thật

## Lưu ý

1. **API Response Format**: Đảm bảo API trả về đúng format JSON như spec
2. **Error Handling**: App có xử lý lỗi cho network, timeout, và API errors
3. **Performance**: Polling chỉ chạy khi cần thiết, tự động dừng khi hoàn tất
4. **User Experience**: Dữ liệu mẫu đảm bảo app hoạt động ngay cả khi API chưa sẵn sàng














