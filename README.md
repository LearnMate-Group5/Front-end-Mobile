<p align="center">
  <img src="app/src/main/res/drawable/logo_learnmate.png" alt="LearnMate" width="140">
</p>

<h1 align="center">LearnMate</h1>

<p align="center">
  Android app for reading, learning, and studying with AI assistance.
</p>

<p align="center">
  <a href="#features">Features</a> -
  <a href="#screenshots">Screenshots</a> -
  <a href="#setup-and-run">Setup</a> -
  <a href="#related-docs">Docs</a>
</p>

<p align="center">
  <img alt="Platform" src="https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white">
  <img alt="Language" src="https://img.shields.io/badge/language-Java-007396?logo=java&logoColor=white">
  <img alt="Build" src="https://img.shields.io/badge/build-Gradle-02303A?logo=gradle&logoColor=white">
</p>

## Overview
LearnMate lets users import documents, read by chapters, highlight text with AI, chat, and listen via TTS. It also supports subscriptions and payments.

## Features
- Auth with Google Sign-In and Firebase Authentication
- Book library with featured, recommended, and favorites
- Import PDF, DOC, DOCX, PNG, JPG from device or Google Drive
- Chapter reader with raw and translated modes, bookmarks, and font size control
- AI tools: highlight, chatbot, and text-to-speech
- Subscription and payments via MoMo and ZaloPay

## Tech Stack
- Android (Java), AndroidX, Material Design
- Retrofit, OkHttp, Gson
- Firebase Auth, Google Play Services Auth
- PDFBox Android
- ZaloPay SDK (AAR)

## Setup and Run
1. Update backend base URL at `app/src/main/java/com/example/LearnMate/network/ApiConfig.java`.
   - On Android emulator, you can use `10.0.2.2` instead of `localhost`.
2. Add Firebase config at `app/google-services.json`.
3. (Optional) Create `app/keystore.properties` for release builds:
   ```properties
   storeFile=path/to/keystore.jks
   storePassword=your_store_password
   keyAlias=your_key_alias
   keyPassword=your_key_password
   ```
4. Sync Gradle and run the project.

## Run App
- Android Studio: Run module `app`
- CLI:
  ```bash
  ./gradlew assembleDebug
  ```
  On Windows: `gradlew.bat assembleDebug`

## Related Docs
- ZaloPay integration: `ZALOPAY_INTEGRATION.md`
- AI Upload API spec: `api_spec_updated.json`

## Screenshots
<table>
  <tr>
    <td align="center">
      <img src="app/src/image/sign%20in.png" width="220" alt="Sign In">
      <br><sub>Sign In</sub>
    </td>
    <td align="center">
      <img src="app/src/image/home%20.png" width="220" alt="Home">
      <br><sub>Home</sub>
    </td>
    <td align="center">
      <img src="app/src/image/search.png" width="220" alt="Search">
      <br><sub>Search</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="app/src/image/book%20details.png" width="220" alt="Book Details">
      <br><sub>Book Details</sub>
    </td>
    <td align="center">
      <img src="app/src/image/read%20book.png" width="220" alt="Reader">
      <br><sub>Reader</sub>
    </td>
    <td align="center">
      <img src="app/src/image/edit.png" width="220" alt="Edit Profile">
      <br><sub>Edit Profile</sub>
    </td>
  </tr>
</table>
