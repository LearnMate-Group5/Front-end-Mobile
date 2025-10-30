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

    /** (Tuỳ chọn) Helper tách text thành chương bằng regex "Chapter …" */
    public static List<Chapter> splitByRegex(String fullText) {
        List<Chapter> out = new ArrayList<>();
        if (fullText == null || fullText.trim().isEmpty())
            return out;

        // Regex đơn giản: chia sau mỗi từ "chapter" (không phân biệt hoa thường)
        // Pattern: chỉ cần có từ "chapter" hoặc "chương" là chia chapter mới
        String[] parts = fullText.split("(?i)(?=\\bchapter\\b|\\bchương\\b)");

        if (parts.length <= 1) {
            out.add(new Chapter("Chapter 1", fullText));
            return out;
        }

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.isEmpty())
                continue;

            // Lấy dòng đầu làm title
            String[] lines = part.split("\n", 2);
            String title = lines.length > 0 ? lines[0].trim() : "Chapter " + (i + 1);
            String content = lines.length > 1 ? lines[1].trim() : part;

            out.add(new Chapter(title, content));
        }
        return out;
    }
}
