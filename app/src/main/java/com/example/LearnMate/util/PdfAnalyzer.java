package com.example.LearnMate.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility class để phân tích và phân loại PDF
 */
public class PdfAnalyzer {
    private static final String TAG = "PdfAnalyzer";

    /**
     * Kết quả phân tích PDF
     */
    public static class AnalysisResult {
        public String title; // Title từ metadata hoặc auto-detect
        public String author; // Author từ metadata
        public String subject; // Subject từ metadata
        public int totalPages; // Tổng số trang
        public long fileSize; // Kích thước file (bytes)
        public String detectedLanguage; // Ngôn ngữ detect được (en, vi, etc)
        public String suggestedCategory; // Category gợi ý
        public List<String> keywords; // Keywords extract được
        public String summary; // Tóm tắt ngắn (first paragraph)

        public AnalysisResult() {
            this.keywords = new ArrayList<>();
        }

        @Override
        public String toString() {
            return "AnalysisResult{" +
                    "title='" + title + '\'' +
                    ", author='" + author + '\'' +
                    ", totalPages=" + totalPages +
                    ", language='" + detectedLanguage + '\'' +
                    ", category='" + suggestedCategory + '\'' +
                    ", keywords=" + keywords +
                    '}';
        }
    }

    /**
     * Phân tích PDF và trả về kết quả
     */
    public static AnalysisResult analyze(Context context, Uri pdfUri) {
        AnalysisResult result = new AnalysisResult();

        try {
            // Initialize PDFBox
            PDFBoxResourceLoader.init(context);

            Log.d(TAG, "Analyzing PDF: " + pdfUri.toString());

            // Open PDF document
            try (InputStream inputStream = context.getContentResolver().openInputStream(pdfUri)) {
                if (inputStream == null) {
                    Log.e(TAG, "Cannot open input stream");
                    return result;
                }

                // Get file size
                result.fileSize = inputStream.available();

                PDDocument document = PDDocument.load(inputStream);

                // 1. Extract metadata
                extractMetadata(document, result);

                // 2. Get total pages
                result.totalPages = document.getNumberOfPages();

                // 3. Extract text from first few pages for analysis
                String sampleText = extractSampleText(document, 3); // First 3 pages

                // 4. Detect language
                result.detectedLanguage = detectLanguage(sampleText);

                // 5. Extract keywords
                result.keywords = extractKeywords(sampleText);

                // 6. Suggest category based on content
                result.suggestedCategory = suggestCategory(sampleText, result.keywords, result.subject);

                // 7. Generate summary
                result.summary = generateSummary(sampleText);

                // 8. If title is empty, try to extract from content
                if (result.title == null || result.title.trim().isEmpty()) {
                    result.title = extractTitleFromContent(sampleText);
                }

                document.close();

                Log.d(TAG, "Analysis completed: " + result.toString());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error analyzing PDF: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * Extract metadata từ PDF
     */
    private static void extractMetadata(PDDocument document, AnalysisResult result) {
        try {
            PDDocumentInformation info = document.getDocumentInformation();
            if (info != null) {
                result.title = info.getTitle();
                result.author = info.getAuthor();
                result.subject = info.getSubject();

                // Extract keywords from metadata if available
                String metaKeywords = info.getKeywords();
                if (metaKeywords != null && !metaKeywords.isEmpty()) {
                    String[] keys = metaKeywords.split("[,;]");
                    for (String key : keys) {
                        result.keywords.add(key.trim());
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not extract metadata: " + e.getMessage());
        }
    }

    /**
     * Extract text từ n trang đầu tiên để phân tích
     */
    private static String extractSampleText(PDDocument document, int maxPages) {
        try {
            PDFTextStripper stripper = new PDFTextStripper();
            int endPage = Math.min(maxPages, document.getNumberOfPages());
            stripper.setStartPage(1);
            stripper.setEndPage(endPage);
            return stripper.getText(document);
        } catch (Exception e) {
            Log.e(TAG, "Error extracting sample text: " + e.getMessage());
            return "";
        }
    }

    /**
     * Detect ngôn ngữ dựa trên sample text
     */
    private static String detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "unknown";
        }

        text = text.toLowerCase();

        // Simple language detection based on common words
        // Vietnamese indicators
        int viCount = countMatches(text, "\\b(và|của|có|không|được|này|cho|trong|một|đã|là)\\b");

        // English indicators
        int enCount = countMatches(text, "\\b(the|and|is|in|to|of|that|for|with|on|are|was)\\b");

        // Japanese indicators (Hiragana/Katakana)
        int jpCount = countMatches(text, "[\\u3040-\\u309F\\u30A0-\\u30FF]");

        // Chinese indicators
        int cnCount = countMatches(text, "[\\u4E00-\\u9FFF]");

        // Determine language
        if (viCount > enCount && viCount > 5)
            return "vi";
        if (enCount > viCount && enCount > 5)
            return "en";
        if (jpCount > 10)
            return "ja";
        if (cnCount > 10)
            return "zh";

        return "en"; // Default to English
    }

    /**
     * Count regex matches in text
     */
    private static int countMatches(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * Extract keywords từ content
     */
    private static List<String> extractKeywords(String text) {
        List<String> keywords = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return keywords;
        }

        String lowerText = text.toLowerCase();

        // Define keyword categories with their indicators
        Map<String, String[]> categoryKeywords = new HashMap<>();
        categoryKeywords.put("Programming", new String[] { "java", "python", "code", "programming", "software",
                "algorithm", "function", "class", "method" });
        categoryKeywords.put("Science", new String[] { "research", "experiment", "hypothesis", "theory", "science",
                "study", "analysis", "data" });
        categoryKeywords.put("Business", new String[] { "business", "management", "marketing", "strategy", "company",
                "profit", "customer", "sales" });
        categoryKeywords.put("Mathematics",
                new String[] { "equation", "theorem", "proof", "mathematics", "calculus", "algebra", "geometry" });
        categoryKeywords.put("History",
                new String[] { "history", "century", "war", "ancient", "civilization", "empire", "historical" });
        categoryKeywords.put("Literature",
                new String[] { "novel", "story", "character", "plot", "narrative", "fiction", "author", "chapter" });
        categoryKeywords.put("Education",
                new String[] { "learning", "teaching", "student", "education", "course", "lesson", "training" });
        categoryKeywords.put("Technology",
                new String[] { "technology", "digital", "computer", "internet", "ai", "machine learning", "cloud" });

        // Count occurrences of keywords
        for (Map.Entry<String, String[]> entry : categoryKeywords.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lowerText.contains(keyword)) {
                    if (!keywords.contains(keyword)) {
                        keywords.add(keyword);
                    }
                }
            }
        }

        return keywords;
    }

    /**
     * Suggest category dựa trên content analysis
     */
    private static String suggestCategory(String text, List<String> keywords, String subject) {
        // Priority 1: Use subject from metadata
        if (subject != null && !subject.trim().isEmpty()) {
            return classifySubject(subject);
        }

        // Priority 2: Analyze keywords
        if (keywords != null && !keywords.isEmpty()) {
            return classifyByKeywords(keywords);
        }

        // Priority 3: Analyze text content
        if (text != null && !text.isEmpty()) {
            return classifyByContent(text);
        }

        return "General"; // Default category
    }

    /**
     * Classify based on subject metadata
     */
    private static String classifySubject(String subject) {
        String lower = subject.toLowerCase();

        if (lower.contains("program") || lower.contains("code") || lower.contains("software"))
            return "Programming";
        if (lower.contains("business") || lower.contains("management") || lower.contains("marketing"))
            return "Business";
        if (lower.contains("science") || lower.contains("research"))
            return "Science";
        if (lower.contains("math") || lower.contains("calculus") || lower.contains("algebra"))
            return "Mathematics";
        if (lower.contains("history") || lower.contains("historical"))
            return "History";
        if (lower.contains("novel") || lower.contains("fiction") || lower.contains("literature"))
            return "Literature";
        if (lower.contains("education") || lower.contains("learning"))
            return "Education";
        if (lower.contains("technology") || lower.contains("tech"))
            return "Technology";

        return "General";
    }

    /**
     * Classify based on extracted keywords
     */
    private static String classifyByKeywords(List<String> keywords) {
        Map<String, Integer> categoryScores = new HashMap<>();

        for (String keyword : keywords) {
            String category = getKeywordCategory(keyword);
            categoryScores.put(category, categoryScores.getOrDefault(category, 0) + 1);
        }

        // Find category with highest score
        String bestCategory = "General";
        int maxScore = 0;

        for (Map.Entry<String, Integer> entry : categoryScores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                bestCategory = entry.getKey();
            }
        }

        return bestCategory;
    }

    /**
     * Get category for a keyword
     */
    private static String getKeywordCategory(String keyword) {
        keyword = keyword.toLowerCase();

        if (keyword.matches(".*(java|python|code|programming|software|algorithm).*"))
            return "Programming";
        if (keyword.matches(".*(business|management|marketing|strategy).*"))
            return "Business";
        if (keyword.matches(".*(science|research|experiment).*"))
            return "Science";
        if (keyword.matches(".*(math|equation|theorem|calculus|algebra).*"))
            return "Mathematics";
        if (keyword.matches(".*(history|historical|ancient|century).*"))
            return "History";
        if (keyword.matches(".*(novel|fiction|story|literature).*"))
            return "Literature";
        if (keyword.matches(".*(education|learning|teaching).*"))
            return "Education";
        if (keyword.matches(".*(technology|tech|digital|computer).*"))
            return "Technology";

        return "General";
    }

    /**
     * Classify based on content analysis
     */
    private static String classifyByContent(String text) {
        String lower = text.toLowerCase();

        // Simple scoring based on keyword frequency
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Programming", countTechTerms(lower));
        scores.put("Business", countBusinessTerms(lower));
        scores.put("Science", countScienceTerms(lower));
        scores.put("Literature", countLiteratureTerms(lower));

        String bestCategory = "General";
        int maxScore = 3; // Minimum threshold

        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                bestCategory = entry.getKey();
            }
        }

        return bestCategory;
    }

    private static int countTechTerms(String text) {
        return countMatches(text,
                "\\b(java|python|code|programming|software|algorithm|function|class|method|api|database)\\b");
    }

    private static int countBusinessTerms(String text) {
        return countMatches(text,
                "\\b(business|management|marketing|strategy|company|profit|customer|sales|revenue|market)\\b");
    }

    private static int countScienceTerms(String text) {
        return countMatches(text,
                "\\b(research|experiment|hypothesis|theory|science|study|analysis|data|scientific|method)\\b");
    }

    private static int countLiteratureTerms(String text) {
        return countMatches(text,
                "\\b(novel|story|character|plot|narrative|fiction|author|chapter|protagonist|theme)\\b");
    }

    /**
     * Generate summary từ first paragraph
     */
    private static String generateSummary(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Get first 200 characters or first paragraph
        String[] paragraphs = text.split("\\n\\n");
        String firstParagraph = paragraphs.length > 0 ? paragraphs[0] : text;

        if (firstParagraph.length() > 200) {
            // Find sentence boundary
            int end = firstParagraph.indexOf('.', 200);
            if (end > 0) {
                return firstParagraph.substring(0, end + 1).trim();
            }
            return firstParagraph.substring(0, 200).trim() + "...";
        }

        return firstParagraph.trim();
    }

    /**
     * Extract title từ content nếu metadata không có
     */
    private static String extractTitleFromContent(String text) {
        if (text == null || text.isEmpty()) {
            return "Untitled Document";
        }

        // Try to find first non-empty line (likely to be title)
        String[] lines = text.split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && line.length() > 3 && line.length() < 100) {
                return line;
            }
        }

        return "Untitled Document";
    }

    /**
     * Async analysis với callback
     */
    public interface AnalysisCallback {
        void onAnalysisComplete(AnalysisResult result);

        void onError(Exception e);
    }

    public static void analyzeAsync(Context context, Uri pdfUri, AnalysisCallback callback) {
        new Thread(() -> {
            try {
                AnalysisResult result = analyze(context, pdfUri);
                callback.onAnalysisComplete(result);
            } catch (Exception e) {
                callback.onError(e);
            }
        }).start();
    }
}
