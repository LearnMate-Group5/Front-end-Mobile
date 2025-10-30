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
        java.util.List<ChapterUtils.Chapter> rawChapters = new java.util.ArrayList<>();
        java.util.List<ChapterUtils.Chapter> transChapters = new java.util.ArrayList<>();

        if (!rawContent.isEmpty()) {
            rawChapters.addAll(splitIntoChapters(rawContent, false));
        }

        if (!transContent.isEmpty()) {
            transChapters.addAll(splitIntoChapters(transContent, true));
        }

        // Nếu không có dữ liệu, tạo chapter rỗng
        if (rawChapters.isEmpty()) {
            rawChapters.add(new ChapterUtils.Chapter("Document Content", "No content available"));
        }
        if (transChapters.isEmpty()) {
            transChapters.add(new ChapterUtils.Chapter("Nội dung tài liệu", "Không có nội dung"));
        }

        // Tạo chapters với cả raw và translated content
        RAW = new java.util.ArrayList<>();
        TRANS = new java.util.ArrayList<>();

        // Đảm bảo số lượng chapters bằng nhau
        int maxChapters = Math.max(rawChapters.size(), transChapters.size());

        for (int i = 0; i < maxChapters; i++) {
            String rawTitle = i < rawChapters.size() ? rawChapters.get(i).title : "Chapter " + (i + 1);
            String rawChapterContent = i < rawChapters.size() ? rawChapters.get(i).content : "";
            String transTitle = i < transChapters.size() ? transChapters.get(i).title : "Chương " + (i + 1);
            String transChapterContent = i < transChapters.size() ? transChapters.get(i).content : "";

            // Tạo chapter với cả raw và translated content
            RAW.add(new ChapterUtils.Chapter(rawTitle, rawChapterContent, transChapterContent));
            TRANS.add(new ChapterUtils.Chapter(transTitle, transChapterContent, rawChapterContent));
        }
    }

    /** Chia content thành nhiều chapters dựa trên các dấu hiệu chapter */
    private static java.util.List<ChapterUtils.Chapter> splitIntoChapters(String content, boolean isTranslated) {
        java.util.List<ChapterUtils.Chapter> chapters = new java.util.ArrayList<>();

        // Tìm tất cả các vị trí có từ "chapter" hoặc "chương" (case insensitive)
        // Pattern đơn giản: chỉ cần có từ "chapter" hoặc "chương" là chia chapter
        String chapterPattern = "(?i)\\bchapter\\b|\\bchương\\b";

        java.util.List<Integer> chapterPositions = new java.util.ArrayList<>();
        java.util.List<String> chapterTitles = new java.util.ArrayList<>();

        java.util.regex.Pattern p = java.util.regex.Pattern.compile(chapterPattern);
        java.util.regex.Matcher m = p.matcher(content);

        while (m.find()) {
            int position = m.start();

            // Lấy cả dòng chứa từ "chapter" để làm title
            int lineStart = position;
            int lineEnd = content.indexOf('\n', position);
            if (lineEnd == -1) {
                lineEnd = content.length();
            }

            // Tìm đầu dòng
            while (lineStart > 0 && content.charAt(lineStart - 1) != '\n') {
                lineStart--;
            }

            String title = content.substring(lineStart, lineEnd).trim();
            android.util.Log.d("ContentCache", "Found chapter at position " + position + ": " + title);
            chapterPositions.add(lineStart); // Dùng đầu dòng làm vị trí bắt đầu
            chapterTitles.add(title);
        }

        // Sắp xếp theo vị trí và loại bỏ duplicate positions
        java.util.Set<Integer> uniquePositions = new java.util.TreeSet<>(chapterPositions);
        chapterPositions = new java.util.ArrayList<>(uniquePositions);

        android.util.Log.d("ContentCache", "Total chapters found: " + chapterPositions.size());

        if (chapterPositions.isEmpty()) {
            // Không tìm thấy chapter, chia dựa trên độ dài nội dung
            if (content.length() > 5000) {
                // Nội dung dài, chia thành nhiều phần
                int chunkSize = content.length() / 5; // Chia thành 5 phần
                for (int i = 0; i < content.length(); i += chunkSize) {
                    int endPos = Math.min(i + chunkSize, content.length());
                    String chapterContent = content.substring(i, endPos).trim();
                    if (!chapterContent.isEmpty()) {
                        String title = isTranslated ? "Phần " + (chapters.size() + 1) : "Part " + (chapters.size() + 1);
                        chapters.add(new ChapterUtils.Chapter(title, chapterContent));
                    }
                }
            } else {
                // Nội dung ngắn, tạo 1 chapter duy nhất
                String title = isTranslated ? "Nội dung tài liệu" : "Document Content";
                chapters.add(new ChapterUtils.Chapter(title, content));
            }
            return chapters;
        }

        // Tạo chapters từ các vị trí đã tìm
        for (int i = 0; i < chapterPositions.size(); i++) {
            int startPos = chapterPositions.get(i);
            int endPos = (i + 1 < chapterPositions.size()) ? chapterPositions.get(i + 1) : content.length();

            String chapterContent = content.substring(startPos, endPos).trim();
            String chapterTitle = extractChapterTitle(chapterContent, isTranslated);

            android.util.Log.d("ContentCache", "Creating chapter " + (i + 1) + ": " + chapterTitle);

            if (!chapterContent.isEmpty()) {
                chapters.add(new ChapterUtils.Chapter(chapterTitle, chapterContent));
            }
        }

        return chapters;
    }

    /** Trích xuất title của chapter từ nội dung */
    private static String extractChapterTitle(String chapterContent, boolean isTranslated) {
        // Lấy dòng đầu tiên làm title
        String[] lines = chapterContent.split("\n");
        if (lines.length > 0) {
            String firstLine = lines[0].trim();
            // Loại bỏ các ký tự đặc biệt và giới hạn độ dài
            firstLine = firstLine.replaceAll("[#*_]", "").trim();
            if (firstLine.length() > 50) {
                firstLine = firstLine.substring(0, 50) + "...";
            }
            return firstLine.isEmpty() ? (isTranslated ? "Chương" : "Chapter") : firstLine;
        }
        return isTranslated ? "Chương" : "Chapter";
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
