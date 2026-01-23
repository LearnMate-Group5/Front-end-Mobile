<p align="center">
  <img src="app/src/main/res/drawable/logo_learnmate.png" alt="LearnMate" width="140">
</p>

<h1 align="center">LearnMate</h1>

<p align="center">
  Ứng dụng Android giúp đọc tài liệu, khám phá sách và học tập cùng trợ lý AI.
</p>

<p align="center">
  <a href="#tinh-nang-noi-bat">Tính năng</a> •
  <a href="#screenshots">Screenshots</a> •
  <a href="#cau-hinh-va-chay">Cấu hình</a> •
  <a href="#tai-lieu-lien-quan">Tài liệu</a>
</p>

<p align="center">
  <img alt="Platform" src="https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white">
  <img alt="Language" src="https://img.shields.io/badge/language-Java-007396?logo=java&logoColor=white">
  <img alt="Build" src="https://img.shields.io/badge/build-Gradle-02303A?logo=gradle&logoColor=white">
</p>

<p align="center">
  <img src="app/src/image/welcome.png" alt="Welcome Screen" width="860">
</p>

## Giới thiệu
LearnMate là ứng dụng đọc sách và tài liệu có hỗ trợ AI. Bạn có thể import tài liệu, đọc theo chương, dùng AI Highlight, chat và TTS, quản lý sách yêu thích, và nâng cấp gói subscription qua MoMo/ZaloPay.

## Tính năng nổi bật
- Đăng nhập/đăng ký, hỗ trợ Google Sign-In và Firebase Authentication.
- Thư viện sách: nổi bật, đề xuất, và quản lý danh sách yêu thích.
- Import file (PDF/DOC/DOCX/PNG/JPG) từ thiết bị hoặc Google Drive, tạo thumbnail và phân tích nội dung.
- Đọc tài liệu theo chương, chuyển đổi raw/translated, lưu bookmark và tùy chỉnh font.
- AI Highlight: tra cứu nhanh thông tin từ đoạn văn được bôi chọn.
- TTS (Text-to-Speech) cho chương đang đọc.
- AI Chatbot với lịch sử phiên chat.
- Subscription và thanh toán qua MoMo/ZaloPay (PayOS đang ở chế độ mock).

## Công nghệ
- Android (Java), AndroidX, Material Design
- Retrofit + OkHttp + Gson
- Firebase Auth, Google Play Services Auth
- PDFBox Android (xử lý PDF)
- ZaloPay SDK (AAR)

## Cấu hình và chạy
1. Cập nhật base URL backend tại `app/src/main/java/com/example/LearnMate/network/ApiConfig.java`.
   - Khi test Android emulator, có thể dùng `10.0.2.2` thay cho `localhost`.
2. Thêm cấu hình Firebase vào `app/google-services.json`.
3. (Tùy chọn) Tạo `app/keystore.properties` để build bản release:
   ```properties
   storeFile=path/to/keystore.jks
   storePassword=your_store_password
   keyAlias=your_key_alias
   keyPassword=your_key_password
   ```
4. Sync Gradle và chạy project.

## Chạy ứng dụng
- Android Studio: Run module `app`
- CLI:
  ```bash
  ./gradlew assembleDebug
  ```
  Trên Windows: `gradlew.bat assembleDebug`

## Tài liệu liên quan
- ZaloPay integration: `ZALOPAY_INTEGRATION.md`
- AI Upload API spec: `api_spec_updated.json`

## Screenshots
<table>
  <tr>
    <td align="center">
      <img src="app/src/image/welcome.png" width="220" alt="Welcome">
      <br><sub>Welcome</sub>
    </td>
    <td align="center">
      <img src="app/src/image/sign%20in.png" width="220" alt="Sign In">
      <br><sub>Sign In</sub>
    </td>
    <td align="center">
      <img src="app/src/image/home%20.png" width="220" alt="Home">
      <br><sub>Home</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="app/src/image/search.png" width="220" alt="Search">
      <br><sub>Search</sub>
    </td>
    <td align="center">
      <img src="app/src/image/book%20details.png" width="220" alt="Book Details">
      <br><sub>Book Details</sub>
    </td>
    <td align="center">
      <img src="app/src/image/read%20book.png" width="220" alt="Reader">
      <br><sub>Reader</sub>
    </td>
  </tr>
  <tr>
    <td align="center" colspan="3">
      <img src="app/src/image/edit.png" width="220" alt="Edit Profile">
      <br><sub>Edit Profile</sub>
    </td>
  </tr>
</table>

## Ghi chú triển khai
- ZaloPay đang chạy ở SANDBOX; đổi sang PRODUCTION khi release.
- PayOS hiện dùng mock payment link, cần backend thật để bảo mật API key.

## Đóng góp
PRs và góp ý luôn được hoan nghênh. Ảnh màn hình đang lấy từ `app/src/image`, nếu bạn muốn thêm hoặc thay đổi, chỉ cần cập nhật thư mục này.
