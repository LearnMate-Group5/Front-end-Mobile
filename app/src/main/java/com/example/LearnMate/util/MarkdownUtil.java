package com.example.LearnMate.util;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Map;
import java.util.HashMap;

/**
 * Utility class để xử lý và render Markdown trong chat messages
 * Xử lý các ký tự đặc biệt từ PDF như ##, $, ^2, etc.
 * Sử dụng Html.fromHtml() để render mà không cần thêm dependencies
 */
public class MarkdownUtil {

    /**
     * Clean và normalize markdown content từ PDF
     * Xử lý các vấn đề phổ biến khi extract từ PDF:
     * - Multiple spaces
     * - Special characters
     * - Math notation
     */
    public static String cleanMarkdown(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }

        String cleaned = content;

        // 1. Normalize line breaks
        cleaned = cleaned.replace("\r\n", "\n").replace("\r", "\n");

        // 2. Remove excessive spaces but keep single spaces
        cleaned = cleaned.replaceAll("[ \\t]+", " ");

        // 3. Fix math expressions: Convert $...$ to HTML format
        cleaned = processMathExpressions(cleaned);

        // 4. Fix superscript: Convert A^2 to A<sup>2</sup>
        cleaned = processSuperscripts(cleaned);

        // 5. Fix subscripts (nếu có): Convert H_2O to H<sub>2</sub>O
        cleaned = processSubscripts(cleaned);

        // 6. Normalize headers: Ensure proper spacing around ##
        cleaned = cleaned.replaceAll("\\n\\s*##+\\s+", "\n\n## ");
        cleaned = cleaned.replaceAll("\\n\\s*#+\\s+", "\n\n# ");

        // 7. Fix code blocks: Ensure proper spacing
        cleaned = cleaned.replaceAll("\\n```", "\n\n```");
        cleaned = cleaned.replaceAll("```\\n", "```\n\n");

        // 8. Fix inline code: Ensure proper spacing
        cleaned = cleaned.replaceAll("([^`])`([^`]+)`([^`])", "$1 `$2` $3");

        // 9. Fix lists: Ensure proper spacing
        cleaned = cleaned.replaceAll("\\n([-*+])\\s+", "\n\n$1 ");

        // 10. Remove excessive blank lines (max 2 consecutive)
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n");

        // 11. Trim each line (nhưng giữ lại structure)
        String[] lines = cleaned.split("\\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty() || (i > 0 && i < lines.length - 1 && !lines[i-1].trim().isEmpty() && !lines[i+1].trim().isEmpty())) {
                sb.append(line);
                if (i < lines.length - 1) {
                    sb.append("\n");
                }
            } else if (line.isEmpty() && i < lines.length - 1) {
                sb.append("\n");
            }
        }
        cleaned = sb.toString().trim();

        return cleaned;
    }

    /**
     * Process math expressions: Convert $...$ to HTML format
     * Example: $(A+B)^2 = A^2 + 2AB + B^2$ -> formatted math
     */
    private static String processMathExpressions(String text) {
        // Pattern để tìm các biểu thức math trong $...$
        // Hỗ trợ cả $...$ và $$...$$
        Pattern pattern = Pattern.compile("\\$\\$?([^$]+)\\$\\$?");
        Matcher matcher = pattern.matcher(text);
        
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String mathExpr = matcher.group(1);
            // Xử lý superscripts và subscripts bên trong math expression trước khi escape HTML
            String processedMath = processMathContent(mathExpr);
            // Escape HTML special characters (nhưng giữ lại các tags <sup>, <sub> đã thêm)
            // Cần escape cẩn thận để không làm hỏng các tags HTML đã thêm
            processedMath = escapeHtmlForMath(processedMath);
            // Convert math expression to HTML với italic và formatting
            String replacement = "<i>" + processedMath + "</i>";
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Process content inside math expressions: convert ^ to superscript, _ to subscript
     */
    private static String processMathContent(String mathExpr) {
        // Xử lý superscripts trong math: A^2 -> A<sup>2</sup>
        mathExpr = mathExpr.replaceAll("([A-Za-z0-9\\)\\]\\}])\\^(\\d+|\\w+)", "$1<sup>$2</sup>");
        // Xử lý subscripts trong math: H_2 -> H<sub>2</sub>
        mathExpr = mathExpr.replaceAll("([A-Za-z0-9])\\_(\\d+|\\w+)", "$1<sub>$2</sub>");
        return mathExpr;
    }
    
    /**
     * Escape HTML special characters nhưng giữ lại các tags HTML hợp lệ như <sup>, <sub>
     */
    private static String escapeHtmlForMath(String text) {
        // Tạm thời thay thế các tags HTML hợp lệ bằng placeholders
        text = text.replace("<sup>", "___SUP_OPEN___")
                   .replace("</sup>", "___SUP_CLOSE___")
                   .replace("<sub>", "___SUB_OPEN___")
                   .replace("</sub>", "___SUB_CLOSE___");
        
        // Escape HTML
        text = escapeHtml(text);
        
        // Thay thế placeholders trở lại thành tags
        text = text.replace("___SUP_OPEN___", "<sup>")
                   .replace("___SUP_CLOSE___", "</sup>")
                   .replace("___SUB_OPEN___", "<sub>")
                   .replace("___SUB_CLOSE___", "</sub>");
        
        return text;
    }

    /**
     * Process superscripts: Convert A^2 to A<sup>2</sup>
     * Example: A^2 -> A<sup>2</sup>, (A+B)^2 -> (A+B)<sup>2</sup>
     * Tránh xử lý superscripts bên trong HTML tags (đã được xử lý bởi processMathExpressions)
     */
    private static String processSuperscripts(String text) {
        // Pattern để tìm superscript: word^number hoặc )^number
        // Sử dụng negative lookbehind và lookahead để tránh match bên trong HTML tags
        // Chỉ match các superscript không nằm trong các tag HTML như <i>, <sup>, etc.
        Pattern pattern = Pattern.compile("(?<!<[^>]*)([A-Za-z0-9\\)\\]\\}])\\^(\\d+)(?![^<]*>)");
        Matcher matcher = pattern.matcher(text);
        
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            // Kiểm tra xem có nằm trong HTML tag không bằng cách kiểm tra context
            int start = matcher.start();
            int end = matcher.end();
            String before = text.substring(Math.max(0, start - 50), start);
            String after = text.substring(end, Math.min(text.length(), end + 50));
            
            // Nếu có tag HTML mở chưa đóng trước đó, bỏ qua
            if (countOccurrences(before, '<') > countOccurrences(before, '>')) {
                matcher.appendReplacement(result, matcher.group(0));
                continue;
            }
            
            String base = matcher.group(1);
            String exponent = matcher.group(2);
            String replacement = base + "<sup>" + exponent + "</sup>";
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Count occurrences of a character in a string
     */
    private static int countOccurrences(String text, char ch) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if (c == ch) count++;
        }
        return count;
    }

    /**
     * Process subscripts: Convert H_2O to H<sub>2</sub>O
     * Tránh xử lý subscripts bên trong HTML tags (đã được xử lý bởi processMathExpressions)
     */
    private static String processSubscripts(String text) {
        // Pattern để tìm subscript: word_number
        // Tránh match bên trong HTML tags
        Pattern pattern = Pattern.compile("(?<!<[^>]*)([A-Za-z])\\_(\\d+)(?![^<]*>)");
        Matcher matcher = pattern.matcher(text);
        
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            // Kiểm tra xem có nằm trong HTML tag không
            int start = matcher.start();
            String before = text.substring(Math.max(0, start - 50), start);
            
            // Nếu có tag HTML mở chưa đóng trước đó, bỏ qua
            if (countOccurrences(before, '<') > countOccurrences(before, '>')) {
                matcher.appendReplacement(result, matcher.group(0));
                continue;
            }
            
            String base = matcher.group(1);
            String subscript = matcher.group(2);
            String replacement = base + "<sub>" + subscript + "</sub>";
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    /**
     * Escape HTML special characters
     */
    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    /**
     * Render markdown to TextView using Html.fromHtml()
     * Hỗ trợ HTML tags từ cleanMarkdown và các ký tự đặc biệt
     */
    public static void renderMarkdown(Context context, TextView textView, String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            textView.setText("");
            return;
        }

        try {
            // Clean markdown first
            String cleaned = cleanMarkdown(markdown);
            
            // Convert markdown syntax to HTML
            String html = convertMarkdownToHtml(cleaned);
            
            // Render HTML to TextView
            Spanned spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
            textView.setText(spanned);
        } catch (Exception e) {
            android.util.Log.e("MarkdownUtil", "Error rendering markdown: " + e.getMessage(), e);
            // Fallback: hiển thị text gốc
            textView.setText(markdown);
        }
    }
    
    /**
     * Convert markdown syntax to HTML
     */
    private static String convertMarkdownToHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        
        String html = markdown;
        
        // Code blocks trước (để tránh xử lý nội dung bên trong)
        Pattern codeBlockPattern = Pattern.compile("```([^`]+)```", Pattern.DOTALL);
        Matcher codeBlockMatcher = codeBlockPattern.matcher(html);
        StringBuffer codeBlockBuffer = new StringBuffer();
        int codeBlockIndex = 0;
        Map<String, String> codeBlockMap = new HashMap<>();
        while (codeBlockMatcher.find()) {
            String placeholder = "___CODE_BLOCK_" + codeBlockIndex + "___";
            codeBlockMap.put(placeholder, "<pre><code>" + codeBlockMatcher.group(1) + "</code></pre>");
            codeBlockMatcher.appendReplacement(codeBlockBuffer, placeholder);
            codeBlockIndex++;
        }
        codeBlockMatcher.appendTail(codeBlockBuffer);
        html = codeBlockBuffer.toString();
        
        // Inline code
        html = html.replaceAll("`([^`]+)`", "<code>$1</code>");
        
        // Headers: ###, ##, #
        html = html.replaceAll("(?m)^###\\s+(.+)$", "<h3>$1</h3>");
        html = html.replaceAll("(?m)^##\\s+(.+)$", "<h2>$1</h2>");
        html = html.replaceAll("(?m)^#\\s+(.+)$", "<h1>$1</h1>");
        
        // Bold: **text** -> <b>text</b>
        html = html.replaceAll("\\*\\*([^*]+)\\*\\*", "<b>$1</b>");
        
        // Italic: *text* -> <i>text</i> (nhưng tránh conflict với bold)
        html = html.replaceAll("(?<!\\*)\\*([^*]+)\\*(?!\\*)", "<i>$1</i>");
        
        // Lists: xử lý từng dòng
        String[] lines = html.split("\\n");
        StringBuilder result = new StringBuilder();
        boolean inList = false;
        
        for (String line : lines) {
            if (line.matches("^[-*+]\\s+.+$")) {
                // List item
                if (!inList) {
                    result.append("<ul>");
                    inList = true;
                }
                result.append("<li>").append(line.replaceFirst("^[-*+]\\s+", "")).append("</li>");
            } else {
                // Non-list item
                if (inList) {
                    result.append("</ul>");
                    inList = false;
                }
                if (!line.trim().isEmpty()) {
                    // Nếu không phải là header, code block, hoặc đã có tag HTML
                    if (!line.matches("^<[h1-6]|<pre|<ul|<li") && !line.matches(".*</[h1-6]|</pre|</ul|</li>.*")) {
                        result.append("<p>").append(line).append("</p>");
                    } else {
                        result.append(line);
                    }
                } else {
                    result.append("<br/>");
                }
            }
        }
        if (inList) {
            result.append("</ul>");
        }
        html = result.toString();
        
        // Restore code blocks
        for (Map.Entry<String, String> entry : codeBlockMap.entrySet()) {
            html = html.replace(entry.getKey(), entry.getValue());
        }
        
        // Clean up: remove empty paragraphs
        html = html.replaceAll("<p>\\s*</p>", "");
        html = html.replaceAll("<p>(<h[1-6]>)", "$1");
        html = html.replaceAll("(</h[1-6]>)</p>", "$1");
        html = html.replaceAll("<p>(<ul>)", "$1");
        html = html.replaceAll("(</ul>)</p>", "$1");
        html = html.replaceAll("<p>(<pre>)", "$1");
        html = html.replaceAll("(</pre>)</p>", "$1");
        
        // Clean up multiple <br/>
        html = html.replaceAll("(<br/>){3,}", "<br/><br/>");
        
        return html;
    }
}

