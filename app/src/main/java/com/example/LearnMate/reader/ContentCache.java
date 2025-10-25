package com.example.LearnMate.reader;

import android.net.Uri;
import com.example.LearnMate.network.dto.ChapterResponse;
import com.example.LearnMate.network.dto.ChaptersResponse;
import com.example.LearnMate.network.dto.UploadResponse;

import java.util.ArrayList;
import java.util.List;

/** Bộ nhớ tạm cho nội dung đã parse/translate */
public final class ContentCache {

    /** Danh sách chương bản gốc (raw) và đã dịch (trans) */
    public static List<ChapterUtils.Chapter> RAW;
    public static List<ChapterUtils.Chapter> TRANS;

    /** Lưu lại Uri file PDF cuối cùng để ReaderActivity mở đúng */
    private static Uri lastPdfUri = Uri.EMPTY;

    /** Lưu lại jobId để track processing status */
    private static String currentJobId = null;

    private ContentCache() {
    }

    public static void setLastPdfUri(Uri uri) {
        lastPdfUri = (uri == null) ? Uri.EMPTY : uri;
    }

    public static Uri getLastPdfUri() {
        return lastPdfUri == null ? Uri.EMPTY : lastPdfUri;
    }

    public static void setCurrentJobId(String jobId) {
        currentJobId = jobId;
    }

    public static String getCurrentJobId() {
        return currentJobId;
    }

    /** Khởi tạo cache rỗng - chỉ sử dụng dữ liệu từ API */
    public static void initializeEmpty() {
        RAW = new ArrayList<>();
        TRANS = new ArrayList<>();
    }

    /** Thêm dữ liệu mẫu tạm thời để test - sẽ được thay thế bằng dữ liệu từ API */

    /** Convert từ API response thành chapters */
    public static void setChaptersFromApi(ChaptersResponse response) {
        if (response == null || response.chapters == null)
            return;

        RAW = new ArrayList<>();
        TRANS = new ArrayList<>();

        for (ChapterResponse chapter : response.chapters) {
            // Raw content (tiếng Anh)
            String rawTitle = chapter.title != null ? chapter.title : "Chapter " + chapter.chapterNumber;
            String rawContent = chapter.rawContent != null ? chapter.rawContent : "";
            RAW.add(new ChapterUtils.Chapter(rawTitle, rawContent));

            // Translated content (tiếng Việt)
            String transTitle = chapter.title != null ? chapter.title : "Chương " + chapter.chapterNumber;
            String transContent = chapter.translatedContent != null ? chapter.translatedContent : "";
            TRANS.add(new ChapterUtils.Chapter(transTitle, transContent));
        }
    }

    /** Convert từ UploadResponse mới thành chapters */
    public static void setChaptersFromUploadResponse(UploadResponse response) {
        if (response == null || response.content == null || response.content.isEmpty())
            return;

        RAW = new ArrayList<>();
        TRANS = new ArrayList<>();

        // Parse content string để tách raw và translated content
        String contentString = response.content;

        // Tìm và tách raw content (markdown) và translated content
        String rawContent = extractRawContent(contentString);
        String transContent = extractTranslatedContent(contentString);

        // Tạo chapters từ nội dung đã parse
        if (!rawContent.isEmpty()) {
            RAW.add(new ChapterUtils.Chapter("Document Content", rawContent));
        }

        if (!transContent.isEmpty()) {
            TRANS.add(new ChapterUtils.Chapter("Nội dung tài liệu", transContent));
        }

        // Nếu không có dữ liệu, tạo chapter rỗng
        if (RAW.isEmpty()) {
            RAW.add(new ChapterUtils.Chapter("Document Content", "No content available"));
        }
        if (TRANS.isEmpty()) {
            TRANS.add(new ChapterUtils.Chapter("Nội dung tài liệu", "Không có nội dung"));
        }
    }

    /** Trích xuất raw content (markdown) từ content string */
    private static String extractRawContent(String contentString) {
        try {
            // Tìm phần markdown trong content
            int markdownStart = contentString.indexOf("\"markdown\":\"");
            if (markdownStart == -1)
                return "";

            markdownStart += 12; // Bỏ qua "\"markdown\":\""
            int markdownEnd = contentString.indexOf("\",\"", markdownStart);
            if (markdownEnd == -1)
                markdownEnd = contentString.length() - 1;

            String markdown = contentString.substring(markdownStart, markdownEnd);
            // Unescape JSON string
            return markdown.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
        } catch (Exception e) {
            android.util.Log.e("ContentCache", "Error extracting raw content: " + e.getMessage());
            return "";
        }
    }

    /** Trích xuất translated content từ content string */
    private static String extractTranslatedContent(String contentString) {
        try {
            // Tìm phần trans trong content
            int transStart = contentString.indexOf("\"trans\":\"");
            if (transStart == -1)
                return "";

            transStart += 10; // Bỏ qua "\"trans\":\""
            int transEnd = contentString.indexOf("\",\"", transStart);
            if (transEnd == -1)
                transEnd = contentString.length() - 1;

            String trans = contentString.substring(transStart, transEnd);
            // Unescape JSON string
            return trans.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
        } catch (Exception e) {
            android.util.Log.e("ContentCache", "Error extracting translated content: " + e.getMessage());
            return "";
        }
    }

    /** Tạo nhiều chapters từ content items */
    private static void createMultipleChaptersFromContent(java.util.List<UploadResponse.ContentItem> contentItems) {
        RAW.clear();
        TRANS.clear();

        int chapterIndex = 1;
        for (UploadResponse.ContentItem item : contentItems) {
            if (item.markdown != null && !item.markdown.isEmpty()) {
                String rawTitle = "Chapter " + chapterIndex;
                String rawContent = item.markdown;
                RAW.add(new ChapterUtils.Chapter(rawTitle, rawContent));
            }

            if (item.trans != null && !item.trans.isEmpty()) {
                String transTitle = "Chương " + chapterIndex;
                String transContent = item.trans;
                TRANS.add(new ChapterUtils.Chapter(transTitle, transContent));
            }

            chapterIndex++;
        }
    }

    /** Kiểm tra xem có dữ liệu thật từ API chưa */
    public static boolean hasRealData() {
        return RAW != null && !RAW.isEmpty() && TRANS != null && !TRANS.isEmpty();
    }

    /** Kiểm tra xem có đang chờ dữ liệu từ API không */
    public static boolean isWaitingForData() {
        return currentJobId != null && !hasRealData();
    }

    /** Xóa toàn bộ dữ liệu cache */
    public static void clearAll() {
        RAW = null;
        TRANS = null;
        currentJobId = null;
        lastPdfUri = Uri.EMPTY;
    }

    /** Lấy số lượng chapters hiện có */
    public static int getChapterCount() {
        return RAW != null ? RAW.size() : 0;
    }
}
