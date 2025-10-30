# 📚 API Integration - Home & File History Guide

## 🎯 Tổng quan

Đã tích hợp **API để lưu và hiển thị lịch sử files đã import** ở trang Home, bao gồm:

✅ **Lưu files đã import** lên server  
✅ **Hiển thị "Your Imported Books"** ở Home screen  
✅ **Click để mở file** đã import  
✅ **API cho chat history** (chuẩn bị sẵn)  
✅ **Sync data** khi mở app

## 🚀 API Endpoints đã tích hợp

### 1. GET /api/Ai/file - Lấy danh sách files

**Request:**

```http
GET http://localhost:2406/api/Ai/file
Authorization: Bearer {token}
```

**Response:**

```json
{
  "success": true,
  "message": "Files retrieved successfully",
  "files": [
    {
      "fileId": "uuid-string",
      "fileName": "Effective Java.pdf",
      "uploadedAt": "2025-10-30T10:30:00Z",
      "fileSize": 2048576,
      "status": "completed",
      "category": "Programming",
      "language": "en",
      "totalPages": 358,
      "sessionId": "session-uuid"
    }
  ],
  "totalCount": 5
}
```

### 2. GET /api/Ai/file/{fileId} - Lấy chi tiết 1 file

**Request:**

```http
GET http://localhost:2406/api/Ai/file/{fileId}
Authorization: Bearer {token}
```

**Response:**

```json
{
  "fileId": "uuid-string",
  "fileName": "Effective Java.pdf",
  "uploadedAt": "2025-10-30T10:30:00Z",
  "fileSize": 2048576,
  "ocrContent": "...",
  "translatedContent": "...",
  "currentContent": "...",
  "status": "completed",
  "sessionId": "session-uuid",
  "category": "Programming",
  "language": "en",
  "totalPages": 358
}
```

### 3. GET /api/Ai/session - Lấy danh sách chat sessions

**Request:**

```http
GET http://localhost:2406/api/Ai/session
Authorization: Bearer {token}
```

**Response:**

```json
{
  "success": true,
  "message": "Sessions retrieved",
  "sessions": [
    {
      "sessionId": "session-uuid",
      "createdAt": "2025-10-30T10:00:00Z",
      "lastMessageAt": "2025-10-30T11:30:00Z",
      "messageCount": 15
    }
  ],
  "totalCount": 3
}
```

### 4. GET /api/Ai/session/{sessionId} - Chi tiết session với messages

**Request:**

```http
GET http://localhost:2406/api/Ai/session/{sessionId}
Authorization: Bearer {token}
```

**Response:**

```json
{
  "sessionId": "session-uuid",
  "createdAt": "2025-10-30T10:00:00Z",
  "lastMessageAt": "2025-10-30T11:30:00Z",
  "messageCount": 15,
  "messages": [
    {
      "messageId": "msg-uuid",
      "role": "user",
      "content": "Explain chapter 1",
      "timestamp": "2025-10-30T10:05:00Z"
    },
    {
      "messageId": "msg-uuid-2",
      "role": "assistant",
      "content": "Chapter 1 discusses...",
      "timestamp": "2025-10-30T10:05:30Z"
    }
  ]
}
```

## 🎨 UI Implementation

### Home Screen Layout

```
┌─────────────────────────────────┐
│ Good Morning, User              │
│ Make today a little smarter.    │
├─────────────────────────────────┤
│ Featured Books                  │
│ [Book1] [Book2] [Book3] [Book4] │
├─────────────────────────────────┤
│ [Import Your Book Now]          │
├─────────────────────────────────┤
│ Your Imported Books             │ ⭐ NEW
│ [PDF1] [PDF2] [PDF3]           │ ⭐ NEW
├─────────────────────────────────┤
│ Recommended for you             │
│ [Rec1] [Rec2] [Rec3] [Rec4]    │
└─────────────────────────────────┘
```

### Imported Books Card

```
┌──────────────────┐
│                  │
│  [PDF Icon]      │
│                  │
├──────────────────┤
│ Effective Java   │ ← File name
│ Programming      │ ← Category
└──────────────────┘
```

## 💻 Code Implementation

### Models Created

**File:** `AiFileResponse.java`

```java
public class AiFileResponse {
    public String fileId;
    public String fileName;
    public String uploadedAt;
    public long fileSize;
    public String category;
    public String language;
    public int totalPages;
    public String status;
}
```

**File:** `AiFileListResponse.java`

```java
public class AiFileListResponse {
    public boolean success;
    public String message;
    public List<AiFileResponse> files;
    public int totalCount;
}
```

**File:** `ChatSessionResponse.java`

```java
public class ChatSessionResponse {
    public String sessionId;
    public String createdAt;
    public List<ChatMessage> messages;

    public static class ChatMessage {
        public String messageId;
        public String role; // "user" or "assistant"
        public String content;
        public String timestamp;
    }
}
```

### AiService Updates

**File:** `AiService.java`

```java
public interface AiService {
    // Existing
    @POST("api/Ai/upload")
    Call<UploadResponse> uploadPdf(@Part MultipartBody.Part File,
                                    @Part("UserId") RequestBody UserId);

    @GET("api/Ai/chapters/{jobId}")
    Call<ChaptersResponse> getChapters(@Path("jobId") String jobId);

    // NEW - File Management
    @GET("api/Ai/file")
    Call<AiFileListResponse> getFiles();

    @GET("api/Ai/file/{fileId}")
    Call<AiFileResponse> getFile(@Path("fileId") String fileId);

    // NEW - Chat Sessions
    @GET("api/Ai/session")
    Call<ChatSessionListResponse> getSessions();

    @GET("api/Ai/session/{sessionId}")
    Call<ChatSessionResponse> getSession(@Path("sessionId") String sessionId);
}
```

### HomeActivity Implementation

**File:** `HomeActivity.java`

```java
public class HomeActivity extends AppCompatActivity {
    private RecyclerView rvImportedFiles;
    private ImportedFilesAdapter importedFilesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup imported files recycler
        rvImportedFiles = findViewById(R.id.rvImportedFiles);
        rvImportedFiles.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        importedFilesAdapter = new ImportedFilesAdapter(new ArrayList<>());
        rvImportedFiles.setAdapter(importedFilesAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadImportedFiles(); // Load mỗi khi vào Home
    }

    private void loadImportedFiles() {
        AiService service = RetrofitClient.get().create(AiService.class);
        service.getFiles().enqueue(new Callback<AiFileListResponse>() {
            @Override
            public void onResponse(Call<AiFileListResponse> call,
                                 Response<AiFileListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    importedFilesAdapter.updateData(response.body().files);
                }
            }

            @Override
            public void onFailure(Call<AiFileListResponse> call, Throwable t) {
                Log.e("HomeActivity", "Error loading files: " + t.getMessage());
            }
        });
    }
}
```

## 🔄 Data Flow

### Luồng Import & Hiển thị

```
1. User import PDF từ ImportActivity
   ↓
2. Upload lên /api/Ai/upload
   ↓
3. Server lưu file với fileId
   ↓
4. Server phân tích & lưu metadata (category, language, etc)
   ↓
5. User quay về HomeActivity
   ↓
6. HomeActivity.onStart() → loadImportedFiles()
   ↓
7. Call GET /api/Ai/file
   ↓
8. Server trả về list files
   ↓
9. ImportedFilesAdapter hiển thị trong RecyclerView
   ↓
10. User click file → Mở ChapterListActivity
```

## 📱 User Experience

### Scenario 1: User mở app lần đầu

```
Home Screen:
- "Your Imported Books" section: Empty hoặc ẩn
- "Import Your Book Now" card hiển thị
```

### Scenario 2: User đã import 3 PDFs

```
Home Screen:
- "Your Imported Books" section:
  [Effective Java] [Clean Code] [Design Patterns]
- Click vào file → Mở ChapterListActivity với chapters
```

### Scenario 3: User import thêm PDF mới

```
ImportActivity:
1. Chọn PDF
2. Upload + Phân tích
3. Hiển thị thông tin (category, language)
4. Back về Home
5. Home tự động refresh → Hiển thị PDF mới
```

## 🎯 Backend Requirements

### Backend cần implement:

1. **Database Schema**

```sql
CREATE TABLE ai_files (
    file_id UUID PRIMARY KEY,
    user_id VARCHAR(255),
    file_name VARCHAR(255),
    uploaded_at TIMESTAMP,
    file_size BIGINT,
    category VARCHAR(100),
    language VARCHAR(10),
    total_pages INT,
    status VARCHAR(50),
    ocr_content TEXT,
    translated_content TEXT,
    current_content TEXT,
    session_id UUID
);

CREATE TABLE chat_sessions (
    session_id UUID PRIMARY KEY,
    user_id VARCHAR(255),
    created_at TIMESTAMP,
    last_message_at TIMESTAMP,
    message_count INT
);

CREATE TABLE chat_messages (
    message_id UUID PRIMARY KEY,
    session_id UUID,
    role VARCHAR(20), -- 'user' or 'assistant'
    content TEXT,
    timestamp TIMESTAMP
);
```

2. **API Endpoints**

**GET /api/Ai/file**

- Query database: `SELECT * FROM ai_files WHERE user_id = ?`
- Return list với category, language từ phân tích

**POST /api/Ai/upload**

- Lưu file vào storage
- Insert vào database
- Trigger background job để phân tích
- Return fileId

**GET /api/Ai/file/{fileId}**

- Query chi tiết file
- Include OCR content, translated content

**GET /api/Ai/session**

- Query chat sessions của user
- Sort by last_message_at DESC

## 🚀 Future Enhancements

### Phase 1: Hiện tại

- ✅ Hiển thị files đã import
- ✅ Click để mở file
- ✅ API models sẵn sàng

### Phase 2: Cải tiến (sau này)

- 🔲 Cache thumbnails từ server
- 🔲 Pull-to-refresh
- 🔲 Swipe to delete file
- 🔲 Filter by category
- 🔲 Search files
- 🔲 Sort options (date, name, category)

### Phase 3: Chat Integration

- 🔲 Hiển thị recent chat sessions
- 🔲 Continue conversation
- 🔲 Chat history với AI bot
- 🔲 Save favorite Q&A

## 📊 Testing

### Test Cases

**TC1: Load files when Home opens**

```
Given: User đã import 3 PDFs
When: User mở HomeActivity
Then: "Your Imported Books" hiển thị 3 items
```

**TC2: Empty state**

```
Given: User chưa import PDF nào
When: User mở HomeActivity
Then: "Your Imported Books" section empty/hidden
```

**TC3: Click file**

```
Given: File "Effective Java" trong list
When: User click vào file
Then: Open ChapterListActivity với file_id
```

**TC4: API failure**

```
Given: Server down
When: LoadImportedFiles() được gọi
Then: Log error, hiển thị list rỗng (không crash)
```

## 📝 Files Modified/Added

### Added:

- `AiFileResponse.java` ⭐ **NEW**
- `AiFileListResponse.java` ⭐ **NEW**
- `ChatSessionResponse.java` ⭐ **NEW**
- `ChatSessionListResponse.java` ⭐ **NEW**
- `API_INTEGRATION_HOME_GUIDE.md` ⭐ **NEW**

### Modified:

- `AiService.java` - Added 4 new endpoints
- `HomeActivity.java` - Added ImportedFilesAdapter + loadImportedFiles()
- `activity_home.xml` - Added rvImportedFiles RecyclerView

## ✅ Summary

Giờ đây app của bạn có thể:

✅ **LƯU FILES** lên server khi import  
✅ **HIỂN THỊ LỊCH SỬ** files đã import ở Home  
✅ **MỞ FILE** từ danh sách đã import  
✅ **SYNC DATA** mỗi khi mở app  
✅ **CHUẨN BỊ SẴN** cho chat history

**Build SUCCESS!** 🎉

Backend cần implement các endpoints theo OpenAPI spec để hoàn chỉnh tính năng này.
