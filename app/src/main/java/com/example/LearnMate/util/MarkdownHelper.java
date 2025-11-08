package com.example.LearnMate.util;

import android.graphics.Typeface;
import android.text.Html;
import android.text.Spanned;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.LeadingMarginSpan;
import android.widget.TextView;
import android.util.Log;

/**
 * Helper class for rendering markdown content
 * Uses Markwon if available (via reflection), otherwise falls back to HTML rendering
 */
public class MarkdownHelper {
    
    private static Boolean markwonAvailable = null;
    
    /**
     * Check if Markwon is available at runtime
     */
    private static boolean isMarkwonAvailable() {
        if (markwonAvailable == null) {
            try {
                Class.forName("io.noties.markwon.Markwon");
                markwonAvailable = true;
            } catch (ClassNotFoundException e) {
                markwonAvailable = false;
            }
        }
        return markwonAvailable;
    }
    
    /**
     * Render markdown to TextView
     * Uses Markwon if available, otherwise falls back to basic HTML rendering
     */
    public static void renderMarkdown(TextView textView, String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            textView.setText("");
            return;
        }
        
        try {
            // Try to use Markwon if available (using reflection to avoid compile-time dependency)
            if (isMarkwonAvailable()) {
                try {
                    Object markwon = Class.forName("io.noties.markwon.Markwon")
                        .getMethod("create", android.content.Context.class)
                        .invoke(null, textView.getContext());
                    Class.forName("io.noties.markwon.Markwon")
                        .getMethod("setMarkdown", TextView.class, String.class)
                        .invoke(markwon, textView, markdown);
                    return;
                } catch (Exception e) {
                    // Markwon failed, fall back to HTML rendering
                    android.util.Log.w("MarkdownHelper", "Failed to use Markwon: " + e.getMessage());
                }
            }
            
            // Fallback: Convert basic markdown to HTML and render
            String html = convertMarkdownToHtml(markdown);
            try {
                // Use FROM_HTML_MODE_COMPACT for better rendering
                Spanned spanned = Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT, null, null);
                textView.setText(spanned);
                // Enable movement method for better text selection
                textView.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
            } catch (Exception e) {
                Log.e("MarkdownHelper", "Error rendering HTML: " + e.getMessage());
                // Fallback to plain text if HTML parsing fails
                textView.setText(markdown);
            }
        } catch (Exception e) {
            // Ultimate fallback: just show plain text
            android.util.Log.e("MarkdownHelper", "Error rendering markdown: " + e.getMessage());
            textView.setText(markdown);
        }
    }
    
    /**
     * Basic markdown to HTML converter (handles common cases)
     * Improved version that properly handles headers, bold, italic, code blocks, etc.
     * Made public for use by MarkdownWithMathHelper
     */
    public static String convertMarkdownToHtmlStatic(String markdown) {
        return convertMarkdownToHtml(markdown);
    }
    
    /**
     * Basic markdown to HTML converter (handles common cases)
     * Improved version that properly handles headers, bold, italic, code blocks, etc.
     */
    private static String convertMarkdownToHtml(String markdown) {
        if (markdown == null) return "";
        
        // First, extract and protect code blocks (```...```) to avoid processing markdown inside them
        java.util.List<String> codeBlocks = new java.util.ArrayList<>();
        String protectedMarkdown = protectCodeBlocks(markdown, codeBlocks);
        
        // Split into lines to process headers properly
        String[] lines = protectedMarkdown.split("\n");
        StringBuilder htmlBuilder = new StringBuilder();
        
        for (String line : lines) {
            String processedLine = line.trim();
            
            // Check for code block placeholders first
            java.util.regex.Pattern codeBlockPattern = java.util.regex.Pattern.compile("__CODE_BLOCK_START_(\\d+)__");
            java.util.regex.Matcher codeBlockMatcher = codeBlockPattern.matcher(line);
            
            if (codeBlockMatcher.find()) {
                // Found code block start placeholder
                int blockIndex = Integer.parseInt(codeBlockMatcher.group(1));
                if (blockIndex < codeBlocks.size()) {
                    String code = codeBlocks.get(blockIndex);
                    // Remove leading/trailing newlines and whitespace
                    code = code.replaceAll("^\\s+", "").replaceAll("\\s+$", "");
                    // Use <pre><tt> which is better supported by Android Html.fromHtml
                    htmlBuilder.append("<pre><tt>").append(escapeHtmlForCode(code)).append("</tt></pre>");
                }
                continue; // Skip to next line (end placeholder will be skipped in next iteration)
            } else if (processedLine.matches("__CODE_BLOCK_END_\\d+__")) {
                // Skip end placeholder line
                continue;
            }
            
            // Process headers first (before other formatting)
            // Check from longest to shortest to avoid conflicts
            if (processedLine.startsWith("###### ") && processedLine.length() > 7) {
                // H6
                processedLine = processedLine.substring(7);
                processedLine = "<h6>" + processInlineMarkdown(processedLine) + "</h6>";
            } else if (processedLine.startsWith("##### ") && processedLine.length() > 6) {
                // H5
                processedLine = processedLine.substring(6);
                processedLine = "<h5>" + processInlineMarkdown(processedLine) + "</h5>";
            } else if (processedLine.startsWith("#### ") && processedLine.length() > 5) {
                // H4
                processedLine = processedLine.substring(5);
                processedLine = "<h4>" + processInlineMarkdown(processedLine) + "</h4>";
            } else if (processedLine.startsWith("### ") && processedLine.length() > 4) {
                // H3
                processedLine = processedLine.substring(4);
                processedLine = "<h3>" + processInlineMarkdown(processedLine) + "</h3>";
            } else if (processedLine.startsWith("## ") && processedLine.length() > 3) {
                // H2
                processedLine = processedLine.substring(3);
                processedLine = "<h2>" + processInlineMarkdown(processedLine) + "</h2>";
            } else if (processedLine.startsWith("# ") && processedLine.length() > 2) {
                // H1
                processedLine = processedLine.substring(2);
                processedLine = "<h1>" + processInlineMarkdown(processedLine) + "</h1>";
            } else if (!processedLine.isEmpty()) {
                // Regular line - process inline markdown
                processedLine = processInlineMarkdown(line); // Use original line to preserve spacing
                processedLine = "<p>" + processedLine + "</p>";
            }
            
            htmlBuilder.append(processedLine);
        }
        
        String html = htmlBuilder.toString();
        
        // Remove empty paragraphs
        html = html.replaceAll("<p>\\s*</p>", "");
        
        // Add line breaks between different elements for better readability
        html = html.replace("</h1>", "</h1><br/>");
        html = html.replace("</h2>", "</h2><br/>");
        html = html.replace("</h3>", "</h3><br/>");
        html = html.replace("</p>", "</p><br/>");
        html = html.replace("</pre>", "</pre><br/>");
        
        return html;
    }
    
    /**
     * Protect code blocks by replacing them with placeholders
     * Returns modified markdown and stores code blocks in the list
     */
    private static String protectCodeBlocks(String markdown, java.util.List<String> codeBlocks) {
        // Pattern to match code blocks: ```language\ncode\n``` or ```code```
        // Handle both cases: with language and without
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("```([\\w]*)?\\n?([\\s\\S]*?)```");
        java.util.regex.Matcher matcher = pattern.matcher(markdown);
        
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        int blockIndex = 0;
        
        while (matcher.find()) {
            // Add text before code block
            result.append(markdown, lastEnd, matcher.start());
            // Store code block content (group 2 is the actual code)
            String code = matcher.group(2);
            codeBlocks.add(code);
            // Add placeholder
            result.append("\n__CODE_BLOCK_START_").append(blockIndex).append("__\n");
            result.append("__CODE_BLOCK_END_").append(blockIndex).append("__\n");
            lastEnd = matcher.end();
            blockIndex++;
        }
        
        // Add remaining text
        result.append(markdown, lastEnd, markdown.length());
        
        return result.toString();
    }
    
    /**
     * Escape HTML for code blocks (preserve formatting)
     */
    private static String escapeHtmlForCode(String code) {
        if (code == null) return "";
        return code.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    /**
     * Process inline markdown (bold, italic, code, links) within a line
     * This method should NOT process LaTeX expressions (they are already protected)
     */
    private static String processInlineMarkdown(String line) {
        if (line == null || line.isEmpty()) return "";
        
        String result = line;
        
        // Skip processing if line contains LaTeX placeholder (protected by MarkdownWithMathHelper)
        if (result.contains("__PROTECTED_LATEX_")) {
            // LaTeX is already protected, just escape HTML and return
            return escapeHtmlExceptPlaceholders(result);
        }
        
        // Process in order: code first (to protect formulas), then bold, then italic, then links
        
        // Inline code: `code` - process first to protect formulas and equations
        // Use a placeholder to protect code content from other processing
        java.util.List<String> inlineCodes = new java.util.ArrayList<>();
        java.util.regex.Pattern codePattern = java.util.regex.Pattern.compile("`([^`\n]+)`");
        java.util.regex.Matcher codeMatcher = codePattern.matcher(result);
        
        StringBuffer sb = new StringBuffer();
        int codeIndex = 0;
        while (codeMatcher.find()) {
            String codeContent = codeMatcher.group(1);
            inlineCodes.add(codeContent);
            codeMatcher.appendReplacement(sb, "___INLINE_CODE_" + codeIndex + "___");
            codeIndex++;
        }
        codeMatcher.appendTail(sb);
        result = sb.toString();
        
        // Now escape HTML in non-code parts (but preserve placeholders)
        result = escapeHtmlExceptPlaceholders(result);
        
        // Restore inline codes with proper formatting
        // Use <tt> tag which is better supported by Android Html.fromHtml
        for (int i = 0; i < inlineCodes.size(); i++) {
            String code = escapeHtmlForCode(inlineCodes.get(i));
            result = result.replace("___INLINE_CODE_" + i + "___", "<tt>" + code + "</tt>");
        }
        
        // Bold: **text** (double asterisks) - must come before single asterisk italic
        // But skip if it's part of a LaTeX placeholder
        if (!result.contains("__PROTECTED_LATEX_")) {
            result = result.replaceAll("\\*\\*([^*]+?)\\*\\*", "<b>$1</b>");
            result = result.replaceAll("__(?!_)([^_]+?)__(?!_)", "<b>$1</b>");
            
            // Italic: *text* (single asterisk, not part of bold)
            result = result.replaceAll("(?<!\\*)\\*([^*]+?)\\*(?!\\*)", "<i>$1</i>");
            
            // Italic: _text_ (single underscore, not part of bold)
            result = result.replaceAll("(?<!_)_([^_]+?)_(?!_)", "<i>$1</i>");
            
            // Links: [text](url)
            result = result.replaceAll("\\[([^\\]]+)\\]\\(([^\\)]+)\\)", "<a href=\"$2\">$1</a>");
        }
        
        return result;
    }
    
    /**
     * Escape HTML except placeholders (like ___INLINE_CODE_X___)
     */
    private static String escapeHtmlExceptPlaceholders(String text) {
        if (text == null) return "";
        
        // Escape HTML but preserve placeholders
        StringBuilder sb = new StringBuilder();
        boolean inPlaceholder = false;
        int i = 0;
        
        while (i < text.length()) {
            if (i < text.length() - 3 && text.substring(i, i + 3).equals("___")) {
                // Start of placeholder - find end
                int end = text.indexOf("___", i + 3);
                if (end > i) {
                    // Preserve placeholder
                    sb.append(text, i, end + 3);
                    i = end + 3;
                    continue;
                }
            }
            
            char c = text.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#39;");
                    break;
                default:
                    sb.append(c);
                    break;
            }
            i++;
        }
        
        return sb.toString();
    }
}

