package com.example.LearnMate.reader;

import java.util.ArrayList;
import java.util.List;

/** Tiện ích chia/dữ liệu chương cực gọn cho app */
public final class ChapterUtils {

    /** Model chương dùng trong UI */
    public static class Chapter {
        public final String title;
        public final String content;
        public final String translatedContent;

        public Chapter(String title, String content) {
            this.title = title == null ? "" : title;
            this.content = content == null ? "" : content;
            this.translatedContent = ""; // Mặc định không có nội dung dịch
        }

        public Chapter(String title, String content, String translatedContent) {
            this.title = title == null ? "" : title;
            this.content = content == null ? "" : content;
            this.translatedContent = translatedContent == null ? "" : translatedContent;
        }
    }

    private ChapterUtils() {
    }

    /** (Tuỳ chọn) Helper tách text thành chương bằng regex “Chapter …” */
    public static List<Chapter> splitByRegex(String fullText) {
        List<Chapter> out = new ArrayList<>();
        if (fullText == null || fullText.trim().isEmpty())
            return out;

        // Ví dụ regex đơn giản – bạn có thể nâng cấp tuỳ file PDF
        String[] parts = fullText.split("(?i)\\bchapter\\s+\\d+\\b");
        if (parts.length <= 1) {
            out.add(new Chapter("Chapter 1", fullText));
            return out;
        }
        for (int i = 1; i < parts.length; i++) {
            out.add(new Chapter("Chapter " + i, parts[i].trim()));
        }
        return out;
    }
}
