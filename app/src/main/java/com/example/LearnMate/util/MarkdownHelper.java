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
        
        // Split into lines to process headers and lists properly
        String[] lines = protectedMarkdown.split("\n");
        StringBuilder htmlBuilder = new StringBuilder();
        boolean inOrderedList = false;
        boolean inUnorderedList = false;
        
        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex];
            String processedLine = line.trim();
            
            // Check for code block placeholders first
            java.util.regex.Pattern codeBlockPattern = java.util.regex.Pattern.compile("__CODE_BLOCK_START_(\\d+)__");
            java.util.regex.Matcher codeBlockMatcher = codeBlockPattern.matcher(line);
            
            if (codeBlockMatcher.find()) {
                // Close any open lists
                if (inOrderedList) {
                    htmlBuilder.append("</ol>");
                    inOrderedList = false;
                }
                if (inUnorderedList) {
                    htmlBuilder.append("</ul>");
                    inUnorderedList = false;
                }
                
                // Found code block start placeholder
                int blockIndex = Integer.parseInt(codeBlockMatcher.group(1));
                if (blockIndex < codeBlocks.size()) {
                    String code = codeBlocks.get(blockIndex);
                    // Remove leading/trailing newlines and whitespace
                    code = code.replaceAll("^\\s+", "").replaceAll("\\s+$", "");
                    // Use <pre><code> for better styling
                    htmlBuilder.append("<pre><code>").append(escapeHtmlForCode(code)).append("</code></pre>");
                }
                continue; // Skip to next line (end placeholder will be skipped in next iteration)
            } else if (processedLine.matches("__CODE_BLOCK_END_\\d+__")) {
                // Skip end placeholder line
                continue;
            }
            
            // Process headers first (before other formatting)
            // Check from longest to shortest to avoid conflicts
            if (processedLine.startsWith("###### ") && processedLine.length() > 7) {
                // Close any open lists
                if (inOrderedList) {
                    htmlBuilder.append("</ol>");
                    inOrderedList = false;
                }
                if (inUnorderedList) {
                    htmlBuilder.append("</ul>");
                    inUnorderedList = false;
                }
                // H6
                processedLine = processedLine.substring(7);
                htmlBuilder.append("<h6>").append(processInlineMarkdown(processedLine)).append("</h6>");
                continue;
            } else if (processedLine.startsWith("##### ") && processedLine.length() > 6) {
                if (inOrderedList) {
                    htmlBuilder.append("</ol>");
                    inOrderedList = false;
                }
                if (inUnorderedList) {
                    htmlBuilder.append("</ul>");
                    inUnorderedList = false;
                }
                // H5
                processedLine = processedLine.substring(6);
                htmlBuilder.append("<h5>").append(processInlineMarkdown(processedLine)).append("</h5>");
                continue;
            } else if (processedLine.startsWith("#### ") && processedLine.length() > 5) {
                if (inOrderedList) {
                    htmlBuilder.append("</ol>");
                    inOrderedList = false;
                }
                if (inUnorderedList) {
                    htmlBuilder.append("</ul>");
                    inUnorderedList = false;
                }
                // H4
                processedLine = processedLine.substring(5);
                htmlBuilder.append("<h4>").append(processInlineMarkdown(processedLine)).append("</h4>");
                continue;
            } else if (processedLine.startsWith("### ") && processedLine.length() > 4) {
                if (inOrderedList) {
                    htmlBuilder.append("</ol>");
                    inOrderedList = false;
                }
                if (inUnorderedList) {
                    htmlBuilder.append("</ul>");
                    inUnorderedList = false;
                }
                // H3
                processedLine = processedLine.substring(4);
                htmlBuilder.append("<h3>").append(processInlineMarkdown(processedLine)).append("</h3>");
                continue;
            } else if (processedLine.startsWith("## ") && processedLine.length() > 3) {
                if (inOrderedList) {
                    htmlBuilder.append("</ol>");
                    inOrderedList = false;
                }
                if (inUnorderedList) {
                    htmlBuilder.append("</ul>");
                    inUnorderedList = false;
                }
                // H2
                processedLine = processedLine.substring(3);
                htmlBuilder.append("<h2>").append(processInlineMarkdown(processedLine)).append("</h2>");
                continue;
            } else if (processedLine.startsWith("# ") && processedLine.length() > 2) {
                if (inOrderedList) {
                    htmlBuilder.append("</ol>");
                    inOrderedList = false;
                }
                if (inUnorderedList) {
                    htmlBuilder.append("</ul>");
                    inUnorderedList = false;
                }
                // H1
                processedLine = processedLine.substring(2);
                htmlBuilder.append("<h1>").append(processInlineMarkdown(processedLine)).append("</h1>");
                continue;
            }
            
            // Check for ordered list items (numbered list: 1. item, 2. item, etc.)
            java.util.regex.Pattern orderedListPattern = java.util.regex.Pattern.compile("^(\\d+)\\.\\s+(.+)$");
            java.util.regex.Matcher orderedMatcher = orderedListPattern.matcher(processedLine);
            
            if (orderedMatcher.find()) {
                if (!inOrderedList) {
                    // Close unordered list if open
                    if (inUnorderedList) {
                        htmlBuilder.append("</ul>");
                        inUnorderedList = false;
                    }
                    htmlBuilder.append("<ol>");
                    inOrderedList = true;
                }
                String listContent = orderedMatcher.group(2);
                htmlBuilder.append("<li>").append(processInlineMarkdown(listContent)).append("</li>");
                continue;
            }
            
            // Check for unordered list items (- item, * item)
            if (processedLine.matches("^[-*]\\s+.+$")) {
                if (!inUnorderedList) {
                    // Close ordered list if open
                    if (inOrderedList) {
                        htmlBuilder.append("</ol>");
                        inOrderedList = false;
                    }
                    htmlBuilder.append("<ul>");
                    inUnorderedList = true;
                }
                String listContent = processedLine.substring(2).trim();
                htmlBuilder.append("<li>").append(processInlineMarkdown(listContent)).append("</li>");
                continue;
            }
            
            // Close lists if we hit a non-list line
            if (inOrderedList || inUnorderedList) {
                if (!processedLine.isEmpty()) {
                    if (inOrderedList) {
                        htmlBuilder.append("</ol>");
                        inOrderedList = false;
                    }
                    if (inUnorderedList) {
                        htmlBuilder.append("</ul>");
                        inUnorderedList = false;
                    }
                }
            }
            
            // Regular line - process inline markdown
            if (!processedLine.isEmpty()) {
                processedLine = processInlineMarkdown(line); // Use original line to preserve spacing
                htmlBuilder.append("<p>").append(processedLine).append("</p>");
            }
        }
        
        // Close any remaining open lists
        if (inOrderedList) {
            htmlBuilder.append("</ol>");
        }
        if (inUnorderedList) {
            htmlBuilder.append("</ul>");
        }
        
        String html = htmlBuilder.toString();
        
        // Remove empty paragraphs
        html = html.replaceAll("<p>\\s*</p>", "");
        
        // Don't add extra breaks - let CSS handle spacing
        // Just ensure proper structure
        
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
     * Improved to better handle special characters and avoid conflicts with LaTeX
     */
    private static String processInlineMarkdown(String line) {
        if (line == null || line.isEmpty()) return "";
        
        String result = line;
        
        // Skip processing if line contains LaTeX placeholder (protected by MarkdownWithMathHelper)
        if (result.contains("__PROTECTED_LATEX_")) {
            // LaTeX is already protected, just escape HTML and return
            // But we still need to escape HTML entities for non-LaTeX parts
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
        // Use <code> tag for better styling support
        for (int i = 0; i < inlineCodes.size(); i++) {
            String code = escapeHtmlForCode(inlineCodes.get(i));
            result = result.replace("___INLINE_CODE_" + i + "___", "<code>" + code + "</code>");
        }
        
        // Process markdown formatting, but be careful not to break LaTeX placeholders
        // Use a more robust approach to split and process
        java.util.regex.Pattern latexPlaceholderPattern = java.util.regex.Pattern.compile("(__PROTECTED_LATEX_\\d+__)");
        java.util.regex.Matcher placeholderMatcher = latexPlaceholderPattern.matcher(result);
        
        StringBuilder processedResult = new StringBuilder();
        int lastEnd = 0;
        
        while (placeholderMatcher.find()) {
            // Process text before placeholder
            String beforePlaceholder = result.substring(lastEnd, placeholderMatcher.start());
            if (!beforePlaceholder.isEmpty()) {
                processedResult.append(processMarkdownInText(beforePlaceholder));
            }
            
            // Keep LaTeX placeholder as is
            processedResult.append(placeholderMatcher.group(0));
            
            lastEnd = placeholderMatcher.end();
        }
        
        // Process remaining text after last placeholder
        if (lastEnd < result.length()) {
            String remaining = result.substring(lastEnd);
            if (!remaining.isEmpty()) {
                processedResult.append(processMarkdownInText(remaining));
            }
        }
        
        // If no placeholders were found, process the whole string
        if (processedResult.length() == 0) {
            return processMarkdownInText(result);
        }
        
        return processedResult.toString();
    }
    
    /**
     * Process markdown formatting in text (bold, italic, links)
     * This is safe to call on text that doesn't contain LaTeX
     * Improved to be more careful with special characters to avoid false positives
     */
    private static String processMarkdownInText(String text) {
        if (text == null || text.isEmpty()) return text;
        
        String result = text;
        
        // Bold: **text** (double asterisks) - must come before single asterisk italic
        // Pattern: **text** but not ***text*** (which should be bold+italic)
        // Be careful: don't match if there are spaces around the asterisks in a way that suggests it's not markdown
        result = result.replaceAll("(?<!\\*)(?<!\\S)\\*\\*([^*\\n]+?)\\*\\*(?!\\*)(?!\\S)", "<b>$1</b>");
        
        // Bold: __text__ (double underscores) - be very careful to avoid false positives
        // Only match if it's clearly markdown formatting (word boundaries, not mathematical)
        result = result.replaceAll("(?<!_)(?<!\\w)__([^_\\n\\^\\\\{}()\\[\\]\\+\\-\\*\\/=\\<>]+?)__(?!_)(?!\\w)", "<b>$1</b>");
        
        // Italic: *text* (single asterisk, not part of bold)
        // Only match if surrounded by word boundaries or whitespace
        result = result.replaceAll("(?<!\\*)(?<!\\S)\\*([^*\\n\\*\\s]+?)\\*(?!\\*)(?!\\S)", "<i>$1</i>");
        
        // Italic: _text_ (single underscore) - be very careful
        // Don't match if it looks like mathematical notation (contains ^, \, {, }, etc.)
        // Only match simple text with underscores for emphasis
        result = result.replaceAll("(?<!_)(?<!\\w)(?<!\\^)(?<!\\\\)_([^_\\n\\^\\\\{}()\\[\\]\\+\\-\\*\\/=\\<>\\s]+?)_(?!_)(?!\\w)(?!\\^)", "<i>$1</i>");
        
        // Links: [text](url) - most reliable markdown pattern
        result = result.replaceAll("\\[([^\\]\\n]+)\\]\\(([^\\)\\n]+)\\)", "<a href=\"$2\">$1</a>");
        
        return result;
    }
    
    /**
     * Escape HTML except placeholders (like ___INLINE_CODE_X___ and __PROTECTED_LATEX_X__)
     * Improved to handle Unicode characters and special symbols properly
     */
    private static String escapeHtmlExceptPlaceholders(String text) {
        if (text == null) return "";
        
        // Escape HTML but preserve placeholders
        StringBuilder sb = new StringBuilder();
        int i = 0;
        
        while (i < text.length()) {
            // Check for inline code placeholder
            if (i < text.length() - 3 && text.substring(i, i + 3).equals("___")) {
                // Start of inline code placeholder - find end
                int end = text.indexOf("___", i + 3);
                if (end > i) {
                    // Preserve placeholder
                    sb.append(text, i, end + 3);
                    i = end + 3;
                    continue;
                }
            }
            
            // Check for LaTeX placeholder
            if (i < text.length() - 18 && text.substring(i, i + 18).startsWith("__PROTECTED_LATEX_")) {
                // Find the end of LaTeX placeholder (ends with __)
                int placeholderStart = i;
                int placeholderEnd = text.indexOf("__", i + 18);
                if (placeholderEnd > i && placeholderEnd < text.length() - 1) {
                    // Check if it's followed by another __ or end of string
                    if (text.substring(placeholderEnd + 2).matches("^[^_].*") || 
                        placeholderEnd + 2 >= text.length()) {
                        // Preserve LaTeX placeholder
                        sb.append(text, placeholderStart, placeholderEnd + 2);
                        i = placeholderEnd + 2;
                        continue;
                    }
                }
            }
            
            char c = text.charAt(i);
            // Escape HTML special characters
            // Note: Unicode characters like α, β, etc. are preserved as-is
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
                    // Preserve all other characters including Unicode (α, β, etc.)
                    sb.append(c);
                    break;
            }
            i++;
        }
        
        return sb.toString();
    }
}

