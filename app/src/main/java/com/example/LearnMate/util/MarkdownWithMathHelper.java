package com.example.LearnMate.util;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

/**
 * Helper class for rendering markdown with LaTeX/MathJax support
 * Uses WebView with MathJax to render mathematical formulas
 */
public class MarkdownWithMathHelper {
    
    private static final String TAG = "MarkdownWithMathHelper";
    
    /**
     * Check if text contains LaTeX formulas
     * Detects patterns like $...$, $$...$$, \(...\), \[...\]
     */
    public static boolean containsLaTeX(String text) {
        if (text == null || text.isEmpty()) return false;
        
        // More robust detection: look for $ pairs (inline math) or $$ pairs (block math)
        // Count $ signs - if there are at least 2, likely has LaTeX
        int dollarCount = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '$') {
                dollarCount++;
            }
        }
        
        // If we have at least 2 $ signs, check for valid patterns
        if (dollarCount >= 2) {
            // Check for block math: $$...$$
            if (text.contains("$$")) {
                return true;
            }
            
            // Check for inline math: $...$ (non-greedy, at least one character between)
            // Pattern: $ (not preceded by $) ... any chars ... $ (not followed by $)
            java.util.regex.Pattern inlinePattern = java.util.regex.Pattern.compile("(?<!\\$)\\$[^$\\n]{1,500}\\$(?!\\$)");
            if (inlinePattern.matcher(text).find()) {
                return true;
            }
        }
        
        // Check for LaTeX delimiters: \(...\) or \[...\]
        if (text.contains("\\(") || text.contains("\\[")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if text contains markdown formatting (headers, bold, italic, etc.)
     * Use WebView for better rendering even without LaTeX
     */
    public static boolean containsMarkdown(String text) {
        if (text == null || text.isEmpty()) return false;
        
        // Check for markdown patterns
        if (text.contains("#") || text.contains("**") || text.contains("__") || 
            text.contains("*") || text.contains("_") || text.contains("`") ||
            text.contains("[") && text.contains("](")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Convert markdown to HTML with MathJax support
     */
    public static String convertMarkdownToHtmlWithMath(String markdown) {
        if (markdown == null) return "";
        
        Log.d(TAG, "Converting markdown to HTML. Original length: " + markdown.length());
        
        // First, protect LaTeX from markdown processing
        // Extract and protect LaTeX expressions before markdown conversion
        java.util.List<String> protectedLatex = new java.util.ArrayList<>();
        String protectedMarkdown = protectLatexExpressions(markdown, protectedLatex);
        
        Log.d(TAG, "Protected " + protectedLatex.size() + " LaTeX expressions");
        for (int i = 0; i < protectedLatex.size(); i++) {
            Log.d(TAG, "LaTeX " + i + ": " + protectedLatex.get(i));
        }
        
        // Convert markdown to HTML (this will process markdown but skip LaTeX placeholders)
        String html = MarkdownHelper.convertMarkdownToHtmlStatic(protectedMarkdown);
        
        // Restore LaTeX expressions (keep them as $...$ for MathJax)
        // Important: Restore in reverse order to avoid conflicts with placeholders
        for (int i = protectedLatex.size() - 1; i >= 0; i--) {
            String latex = protectedLatex.get(i);
            
            // Escape HTML special characters but preserve $ delimiters and LaTeX syntax
            // Only escape &, <, > but preserve all LaTeX syntax including:
            // - $ delimiters
            // - \ commands (like \alpha, \frac, etc.)
            // - { } brackets
            // - ^ _ for superscript/subscript
            // - Unicode characters (α, β, etc.) - these should be preserved
            // - All mathematical symbols
            String escapedLatex = latex.replace("&", "&amp;")
                                       .replace("<", "&lt;")
                                       .replace(">", "&gt;");
            // Note: We don't escape $, \, {, }, ^, _, or Unicode characters
            // These are essential for LaTeX rendering
            
            // Find and replace the placeholder
            // Use simple replace (not replaceAll) to avoid regex issues with special chars
            String placeholder = "__PROTECTED_LATEX_" + i + "__";
            if (html.contains(placeholder)) {
                html = html.replace(placeholder, escapedLatex);
            }
        }
        
        // Clean up: remove any remaining placeholders (shouldn't happen, but safety check)
        html = html.replaceAll("__PROTECTED_LATEX_\\d+__", "");
        
        Log.d(TAG, "Final HTML length: " + html.length());
        return html;
    }
    
    /**
     * Protect LaTeX expressions from markdown processing
     */
    private static String protectLatexExpressions(String text, java.util.List<String> protectedLatex) {
        if (text == null) return "";
        
        // First, protect block math $$...$$ (do this first to avoid conflicts)
        java.util.regex.Pattern blockPattern = java.util.regex.Pattern.compile("\\$\\$([\\s\\S]*?)\\$\\$");
        java.util.regex.Matcher blockMatcher = blockPattern.matcher(text);
        
        StringBuffer sb = new StringBuffer();
        int index = 0;
        while (blockMatcher.find()) {
            String latex = blockMatcher.group(0); // Full match including $$
            protectedLatex.add(latex);
            blockMatcher.appendReplacement(sb, "__PROTECTED_LATEX_" + index + "__");
            index++;
        }
        blockMatcher.appendTail(sb);
        text = sb.toString();
        
        // Then protect inline math $...$ (but not if it's part of $$)
        // Improved pattern to handle complex formulas and special characters
        // Use non-greedy matching with reasonable max length (2000 chars for very complex formulas)
        // This handles:
        // - Simple: $x^2$, $a_1$
        // - Complex: $\cos ^{2} 3 x=\frac{1+\cos 6 x}{2}$
        // - With Greek letters: $\alpha$, $\beta$, $\sin \alpha$
        // - With fractions: $\frac{a}{b}$
        // - With special characters: $a^{2+3}$, $x_{i+1}$
        java.util.regex.Pattern inlinePattern = java.util.regex.Pattern.compile(
            "(?<!\\$)\\$([^$\\n]{1,2000}?)\\$(?!\\$)", 
            java.util.regex.Pattern.DOTALL
        );
        java.util.regex.Matcher inlineMatcher = inlinePattern.matcher(text);
        
        sb = new StringBuffer();
        while (inlineMatcher.find()) {
            String latex = inlineMatcher.group(0); // Full match including $
            // Only add if it's a valid LaTeX expression (has some content between $)
            String latexContent = inlineMatcher.group(1).trim();
            if (!latexContent.isEmpty() && latexContent.length() > 0) {
                // Additional validation: should contain LaTeX-like characters or structures
                // This helps avoid false positives with regular $ signs in text
                // Check for LaTeX operators, Greek letters, or mathematical symbols
                if (latexContent.matches(".*[\\^_\\\\{}()\\[\\]\\+\\-\\*\\/=\\<>\\|\\&\\~\\`'\"\\s].*") ||
                    latexContent.matches(".*[a-zA-Zαβγδεζηθικλμνξοπρστυφχψω].*") ||
                    latexContent.length() > 1) {
                    protectedLatex.add(latex);
                    inlineMatcher.appendReplacement(sb, "__PROTECTED_LATEX_" + index + "__");
                    index++;
                }
            }
        }
        inlineMatcher.appendTail(sb);
        text = sb.toString();
        
        // Also protect LaTeX delimiters \(...\) and \[...\] if used (less common but supported)
        java.util.regex.Pattern latexDelimPattern = java.util.regex.Pattern.compile(
            "(\\\\\\[[\\s\\S]*?\\\\\\]|\\\\\\([\\s\\S]*?\\\\\\))", 
            java.util.regex.Pattern.DOTALL
        );
        java.util.regex.Matcher delimMatcher = latexDelimPattern.matcher(text);
        
        sb = new StringBuffer();
        while (delimMatcher.find()) {
            String latex = delimMatcher.group(0);
            protectedLatex.add(latex);
            delimMatcher.appendReplacement(sb, "__PROTECTED_LATEX_" + index + "__");
            index++;
        }
        delimMatcher.appendTail(sb);
        text = sb.toString();
        
        return text;
    }
    
    /**
     * Create HTML template with MathJax
     */
    public static String createHtmlWithMathJax(String content) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">\n" +
                "    <script type=\"text/javascript\" async\n" +
                "        src=\"https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js\">\n" +
                "    </script>\n" +
                "    <script>\n" +
                "        window.MathJax = {\n" +
                "            tex: {\n" +
                "                inlineMath: [['$', '$'], ['\\\\(', '\\\\)']],\n" +
                "                displayMath: [['$$', '$$'], ['\\\\[', '\\\\]']],\n" +
                "                processEscapes: true,\n" +
                "                processEnvironments: true,\n" +
                "                processRefs: true,\n" +
                "                digits: /^(?:[0-9]+(?:{,[0-9]{3}})*(?:\\.[0-9]*)?|\\.[0-9]+)/,\n" +
                "                tags: 'ams',\n" +
                "                tagSide: 'right',\n" +
                "                tagIndent: '0.8em',\n" +
                "                useLabelIds: true,\n" +
                "                multlineWidth: '85%',\n" +
                "                autoload: {\n" +
                "                    color: [],\n" +
                "                    colorv2: ['color'],\n" +
                "                    bbox: ['bbox'],\n" +
                "                    cancel: ['cancel', 'bcancel', 'xcancel', 'cancelto'],\n" +
                "                    enclose: ['enclose'],\n" +
                "                    extpfeil: ['extpfeil', 'xtwoheadrightarrow', 'xtwoheadleftarrow', 'xmapsto', 'xlongequal', 'xtofrom'],\n" +
                "                    mhchem: ['ce', 'pu']\n" +
                "                },\n" +
                "                packages: {'[+]': ['base', 'ams', 'newcommand', 'configMacros', 'autoload', 'require', 'bbox', 'cancel', 'enclose', 'extpfeil', 'mhchem']},\n" +
                "                macros: {\n" +
                "                    RR: '{\\\\mathbb{R}}',\n" +
                "                    NN: '{\\\\mathbb{N}}',\n" +
                "                    ZZ: '{\\\\mathbb{Z}}',\n" +
                "                    QQ: '{\\\\mathbb{Q}}',\n" +
                "                    CC: '{\\\\mathbb{C}}'\n" +
                "                }\n" +
                "            },\n" +
                "            options: {\n" +
                "                skipHtmlTags: ['script', 'noscript', 'style', 'textarea', 'pre', 'code', 'annotation', 'annotation-xml'],\n" +
                "                ignoreHtmlClass: 'tex2jax_ignore',\n" +
                "                processHtmlClass: 'tex2jax_process',\n" +
                "                renderActions: {\n" +
                "                    addMenu: [0, '', '']\n" +
                "                }\n" +
                "            },\n" +
                "            startup: {\n" +
                "                typeset: false,\n" +
                "                ready: function() {\n" +
                "                    MathJax.startup.defaultReady();\n" +
                "                    MathJax.startup.promise.then(function() {\n" +
                "                        console.log('MathJax is ready');\n" +
                "                    });\n" +
                "                }\n" +
                "            },\n" +
                "            loader: {\n" +
                "                load: ['[tex]/ams', '[tex]/newcommand', '[tex]/configMacros', '[tex]/autoload', '[tex]/require', '[tex]/bbox', '[tex]/cancel', '[tex]/enclose', '[tex]/extpfeil', '[tex]/mhchem']\n" +
                "            }\n" +
                "        };\n" +
                "    </script>\n" +
                "    <style>\n" +
                "        * { box-sizing: border-box; }\n" +
                "        body {\n" +
                "            margin: 0;\n" +
                "            padding: 12px 16px;\n" +
                "            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Noto Sans', sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol';\n" +
                "            font-size: 16px;\n" +
                "            line-height: 1.75;\n" +
                "            color: #374151;\n" +
                "            background-color: transparent;\n" +
                "            word-wrap: break-word;\n" +
                "            overflow-wrap: break-word;\n" +
                "        }\n" +
                "        p { \n" +
                "            margin: 0.75em 0; \n" +
                "            line-height: 1.75;\n" +
                "        }\n" +
                "        p:first-child { margin-top: 0; }\n" +
                "        p:last-child { margin-bottom: 0; }\n" +
                "        h1, h2, h3, h4, h5, h6 { \n" +
                "            margin: 1.25em 0 0.75em 0; \n" +
                "            font-weight: 600; \n" +
                "            line-height: 1.4;\n" +
                "            color: #111827;\n" +
                "        }\n" +
                "        h1:first-child, h2:first-child, h3:first-child, h4:first-child, h5:first-child, h6:first-child { margin-top: 0; }\n" +
                "        h1 { font-size: 1.875em; font-weight: 700; }\n" +
                "        h2 { font-size: 1.5em; font-weight: 600; }\n" +
                "        h3 { font-size: 1.25em; font-weight: 600; }\n" +
                "        h4 { font-size: 1.125em; font-weight: 600; }\n" +
                "        h5 { font-size: 1em; font-weight: 600; }\n" +
                "        h6 { font-size: 0.875em; font-weight: 600; }\n" +
                "        code, tt { \n" +
                "            background-color: rgba(175, 184, 193, 0.2); \n" +
                "            padding: 2px 6px; \n" +
                "            border-radius: 4px; \n" +
                "            font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace; \n" +
                "            font-size: 0.9em;\n" +
                "            color: #e83e8c;\n" +
                "        }\n" +
                "        pre { \n" +
                "            background-color: #f6f8fa; \n" +
                "            padding: 16px; \n" +
                "            border-radius: 6px; \n" +
                "            overflow-x: auto; \n" +
                "            margin: 1em 0;\n" +
                "            line-height: 1.45;\n" +
                "        }\n" +
                "        pre code { \n" +
                "            background-color: transparent; \n" +
                "            padding: 0; \n" +
                "            color: inherit;\n" +
                "            font-size: 0.85em;\n" +
                "        }\n" +
                "        a { \n" +
                "            color: #2563eb; \n" +
                "            text-decoration: none; \n" +
                "        }\n" +
                "        a:hover { \n" +
                "            text-decoration: underline; \n" +
                "        }\n" +
                "        ul, ol { \n" +
                "            margin: 0.75em 0; \n" +
                "            padding-left: 1.5em; \n" +
                "        }\n" +
                "        li { \n" +
                "            margin: 0.25em 0; \n" +
                "            line-height: 1.75;\n" +
                "        }\n" +
                "        blockquote { \n" +
                "            margin: 1em 0; \n" +
                "            padding-left: 1em; \n" +
                "            border-left: 3px solid #d1d5db; \n" +
                "            color: #6b7280; \n" +
                "        }\n" +
                "        table { \n" +
                "            border-collapse: collapse; \n" +
                "            margin: 1em 0; \n" +
                "            width: 100%; \n" +
                "        }\n" +
                "        th, td { \n" +
                "            border: 1px solid #e5e7eb; \n" +
                "            padding: 8px 12px; \n" +
                "            text-align: left; \n" +
                "        }\n" +
                "        th { \n" +
                "            background-color: #f9fafb; \n" +
                "            font-weight: 600; \n" +
                "        }\n" +
                "        hr { \n" +
                "            border: none; \n" +
                "            border-top: 1px solid #e5e7eb; \n" +
                "            margin: 1.5em 0; \n" +
                "        }\n" +
                "        /* MathJax Styling - ChatGPT-like */\n" +
                "        .MathJax { \n" +
                "            font-size: 1.1em !important; \n" +
                "            color: inherit !important; \n" +
                "            display: inline-block !important;\n" +
                "            line-height: 1.2 !important;\n" +
                "        }\n" +
                "        .MathJax_SVG, .MathJax_SVG_Display { \n" +
                "            fill: currentColor !important; \n" +
                "            stroke: currentColor !important;\n" +
                "        }\n" +
                "        .MathJax_Display { \n" +
                "            text-align: left !important; \n" +
                "            margin: 1em 0 !important; \n" +
                "            padding: 0.5em 0 !important;\n" +
                "            display: block !important;\n" +
                "            overflow-x: auto;\n" +
                "            overflow-y: hidden;\n" +
                "        }\n" +
                "        .MathJax_SVG_Display { \n" +
                "            margin: 1em 0 !important; \n" +
                "            padding: 0.5em 0 !important;\n" +
                "        }\n" +
                "        /* Inline math should be properly aligned */\n" +
                "        .MathJax span { \n" +
                "            display: inline !important; \n" +
                "            margin: 0 !important;\n" +
                "            padding: 0 !important;\n" +
                "        }\n" +
                "        /* Ensure proper spacing around math */\n" +
                "        p .MathJax, li .MathJax, td .MathJax { \n" +
                "            margin: 0 2px !important;\n" +
                "        }\n" +
                "        /* Better rendering for fractions and complex expressions */\n" +
                "        .MathJax .mjx-char { \n" +
                "            display: inline-block !important;\n" +
                "        }\n" +
                "        /* Support for Greek letters and special characters */\n" +
                "        body { \n" +
                "            font-feature-settings: 'kern' 1, 'liga' 1;\n" +
                "            text-rendering: optimizeLegibility;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                content +
                "\n<script>\n" +
                "    (function() {\n" +
                "        var renderAttempts = 0;\n" +
                "        var maxAttempts = 50; // 5 seconds max wait\n" +
                "        \n" +
                "        function adjustWebViewHeight() {\n" +
                "            try {\n" +
                "                var body = document.body;\n" +
                "                var html = document.documentElement;\n" +
                "                var height = Math.max(\n" +
                "                    body.scrollHeight, body.offsetHeight,\n" +
                "                    html.clientHeight, html.scrollHeight, html.offsetHeight\n" +
                "                );\n" +
                "                // Add some padding for better display\n" +
                "                height = height + 20;\n" +
                "                // Notify Android to adjust WebView height\n" +
                "                if (window.Android && window.Android.setWebViewHeight) {\n" +
                "                    window.Android.setWebViewHeight(height);\n" +
                "                }\n" +
                "                console.log('Content height: ' + height + 'px');\n" +
                "            } catch (e) {\n" +
                "                console.error('Error adjusting height:', e);\n" +
                "            }\n" +
                "        }\n" +
                "        \n" +
                "        function renderMath() {\n" +
                "            renderAttempts++;\n" +
                "            \n" +
                "            if (window.MathJax && window.MathJax.startup && window.MathJax.startup.promise) {\n" +
                "                window.MathJax.startup.promise.then(function() {\n" +
                "                    if (window.MathJax.typesetPromise) {\n" +
                "                        return window.MathJax.typesetPromise();\n" +
                "                    } else if (window.MathJax.typeset) {\n" +
                "                        window.MathJax.typeset();\n" +
                "                        return Promise.resolve();\n" +
                "                    }\n" +
                "                }).then(function() {\n" +
                "                    console.log('MathJax rendering complete');\n" +
                "                    // Adjust height after MathJax renders\n" +
                "                    setTimeout(adjustWebViewHeight, 300);\n" +
                "                }).catch(function(err) {\n" +
                "                    console.error('MathJax rendering error:', err);\n" +
                "                    // Still adjust height even if MathJax fails\n" +
                "                    setTimeout(adjustWebViewHeight, 300);\n" +
                "                });\n" +
                "            } else if (window.MathJax && window.MathJax.typesetPromise) {\n" +
                "                window.MathJax.typesetPromise().then(function() {\n" +
                "                    console.log('MathJax rendering complete (direct)');\n" +
                "                    setTimeout(adjustWebViewHeight, 300);\n" +
                "                }).catch(function(err) {\n" +
                "                    console.error('MathJax error:', err);\n" +
                "                    setTimeout(adjustWebViewHeight, 300);\n" +
                "                });\n" +
                "            } else if (window.MathJax && window.MathJax.typeset) {\n" +
                "                window.MathJax.typeset();\n" +
                "                console.log('MathJax typeset called');\n" +
                "                setTimeout(adjustWebViewHeight, 500);\n" +
                "            } else if (renderAttempts < maxAttempts) {\n" +
                "                console.log('Waiting for MathJax to load... (' + renderAttempts + '/' + maxAttempts + ')');\n" +
                "                setTimeout(renderMath, 100);\n" +
                "            } else {\n" +
                "                console.error('MathJax failed to load after ' + maxAttempts + ' attempts');\n" +
                "                // Adjust height even if MathJax fails to load\n" +
                "                adjustWebViewHeight();\n" +
                "            }\n" +
                "        }\n" +
                "        \n" +
                "        // Initial height adjustment\n" +
                "        adjustWebViewHeight();\n" +
                "        \n" +
                "        // Start rendering immediately\n" +
                "        renderMath();\n" +
                "        \n" +
                "        // Also try on DOMContentLoaded and load events\n" +
                "        if (document.readyState === 'loading') {\n" +
                "            document.addEventListener('DOMContentLoaded', function() {\n" +
                "                setTimeout(function() {\n" +
                "                    renderMath();\n" +
                "                    adjustWebViewHeight();\n" +
                "                }, 200);\n" +
                "            });\n" +
                "        }\n" +
                "        \n" +
                "        window.addEventListener('load', function() {\n" +
                "            setTimeout(function() {\n" +
                "                renderMath();\n" +
                "                adjustWebViewHeight();\n" +
                "            }, 500);\n" +
                "        });\n" +
                "        \n" +
                "        // Periodic height adjustment for dynamic content\n" +
                "        setInterval(adjustWebViewHeight, 1000);\n" +
                "    })();\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>";
    }
    
    /**
     * Setup WebView for rendering markdown with MathJax
     */
    @android.annotation.SuppressLint("SetJavaScriptEnabled")
    public static void setupWebViewForMath(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        
        // Disable scrolling in WebView (we'll use the parent ScrollView)
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setScrollContainer(false);
        
        // Transparent background
        webView.setBackgroundColor(0x00000000);
        webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null); // Use hardware acceleration for better performance
        
        // Set WebViewClient to handle page load - create new instance each time
        // Don't reuse WebViewClient to avoid conflicts
    }
    
    /**
     * Render markdown with MathJax in WebView
     */
    public static void renderMarkdownWithMath(WebView webView, String markdown) {
        if (webView == null || markdown == null || markdown.isEmpty()) {
            if (webView != null) {
                webView.loadData("", "text/html", "UTF-8");
            }
            return;
        }
        
        try {
            Log.d(TAG, "Rendering markdown with MathJax. Markdown length: " + markdown.length());
            
            // Convert markdown to HTML
            String htmlContent = convertMarkdownToHtmlWithMath(markdown);
            
            Log.d(TAG, "HTML content preview (first 500 chars): " + 
                (htmlContent.length() > 500 ? htmlContent.substring(0, 500) + "..." : htmlContent));
            
            // Create full HTML document with MathJax
            String fullHtml = createHtmlWithMathJax(htmlContent);
            
            // Setup WebViewClient with proper handlers
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    Log.d(TAG, "WebView page finished loading");
                    
                    // Measure content height multiple times at different intervals
                    // This ensures we catch the content even if it takes time to render
                    measureAndAdjustHeight(view, 0);
                    
                    // Measure after a short delay (for immediate content)
                    view.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            measureAndAdjustHeight(view, 1);
                        }
                    }, 300);
                    
                    // Measure after content has time to layout
                    view.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            measureAndAdjustHeight(view, 2);
                        }
                    }, 800);
                    
                    // Wait for MathJax to load and then trigger rendering
                    view.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            view.evaluateJavascript(
                                "(function() {" +
                                "  console.log('Checking MathJax...');" +
                                "  if (window.MathJax) {" +
                                "    console.log('MathJax found');" +
                                "    if (window.MathJax.startup && window.MathJax.startup.promise) {" +
                                "      window.MathJax.startup.promise.then(function() {" +
                                "        console.log('MathJax startup complete');" +
                                "        if (window.MathJax.typesetPromise) {" +
                                "          return window.MathJax.typesetPromise();" +
                                "        }" +
                                "      }).then(function() {" +
                                "        console.log('MathJax rendering complete');" +
                                "      }).catch(function(err) {" +
                                "        console.error('MathJax error:', err);" +
                                "      });" +
                                "    } else if (window.MathJax.typesetPromise) {" +
                                "      window.MathJax.typesetPromise().then(function() {" +
                                "        console.log('MathJax typeset complete');" +
                                "      });" +
                                "    }" +
                                "  } else {" +
                                "    console.log('MathJax not loaded yet');" +
                                "  }" +
                                "})();",
                                new android.webkit.ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String value) {
                                        // After MathJax rendering, measure height multiple times
                                        // MathJax can change content height significantly
                                        view.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                measureAndAdjustHeight(view, 3);
                                            }
                                        }, 500);
                                        
                                        view.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                measureAndAdjustHeight(view, 4);
                                            }
                                        }, 1500);
                                        
                                        view.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                measureAndAdjustHeight(view, 5);
                                            }
                                        }, 3000);
                                    }
                                }
                            );
                        }
                    }, 1000);
                    
                    // Final measurement after everything should be loaded
                    // This is especially important for very long content
                    view.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            measureAndAdjustHeight(view, 6);
                        }
                    }, 4000);
                }
                
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return false;
                }
            });
            
            // Load HTML in WebView
            webView.loadDataWithBaseURL("https://cdn.jsdelivr.net/", fullHtml, "text/html", "UTF-8", null);
            
            Log.d(TAG, "HTML loaded into WebView");
            
        } catch (Exception e) {
            Log.e(TAG, "Error rendering markdown with MathJax: " + e.getMessage(), e);
            e.printStackTrace();
            // Fallback: show plain text
            webView.loadData("<html><body><p>" + 
                markdown.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + 
                "</p></body></html>", "text/html", "UTF-8");
        }
    }
    
    /**
     * Measure content height and adjust WebView height
     */
    private static void measureAndAdjustHeight(final WebView webView, final int attempt) {
        if (webView == null || attempt > 15) {
            return; // Max 15 attempts (for very long content that takes time to render)
        }
        
        webView.evaluateJavascript(
            "(function() {" +
            "  try {" +
            "    // Wait for content to be ready" +
            "    if (document.readyState !== 'complete') {" +
            "      return -1; // Not ready yet" +
            "    }" +
            "    var body = document.body;" +
            "    var html = document.documentElement;" +
            "    " +
            "    // Force layout calculation" +
            "    var height = Math.max(" +
            "      body.scrollHeight || 0," +
            "      body.offsetHeight || 0," +
            "      html.clientHeight || 0," +
            "      html.scrollHeight || 0," +
            "      html.offsetHeight || 0" +
            "    );" +
            "    " +
            "    // If height is 0, try getting it from the first child or content" +
            "    if (height === 0 && body.firstElementChild) {" +
            "      var child = body.firstElementChild;" +
            "      height = Math.max(" +
            "        child.scrollHeight || 0," +
            "        child.offsetHeight || 0" +
            "      );" +
            "    }" +
            "    " +
            "    // Add padding for better display" +
            "    return height > 0 ? height + 30 : 0;" +
            "  } catch(e) {" +
            "    console.error('Error measuring height:', e);" +
            "    return 0;" +
            "  }" +
            "})();",
            new android.webkit.ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    try {
                        // Remove quotes and parse height
                        if (value != null && !value.equals("null") && !value.isEmpty()) {
                            String heightStr = value.replace("\"", "").trim();
                            if (!heightStr.isEmpty() && !heightStr.equals("null")) {
                                int height = (int) Float.parseFloat(heightStr);
                                
                                if (height == -1) {
                                    // Content not ready yet, retry
                                    if (attempt < 10) {
                                        webView.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                measureAndAdjustHeight(webView, attempt + 1);
                                            }
                                        }, 400);
                                    }
                                    return;
                                }
                                
                                if (height > 0) {
                                    // Get current layout params
                                    android.view.ViewGroup.LayoutParams params = webView.getLayoutParams();
                                    if (params != null) {
                                        // Only update if height has changed significantly (more than 10px difference)
                                        // This avoids unnecessary layout passes
                                        if (Math.abs(params.height - height) > 10 || params.height == android.view.ViewGroup.LayoutParams.WRAP_CONTENT) {
                                            params.height = height;
                                            webView.setLayoutParams(params);
                                            
                                            float density = webView.getContext().getResources().getDisplayMetrics().density;
                                            int heightDp = (int) (height / density);
                                            Log.d(TAG, "WebView height adjusted to: " + height + "px (" + heightDp + "dp), attempt: " + attempt);
                                            
                                            // Request layout update on the main thread
                                            webView.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    webView.requestLayout();
                                                    // Also notify parent to update
                                                    android.view.View parent = (android.view.View) webView.getParent();
                                                    if (parent != null) {
                                                        parent.requestLayout();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                } else if (attempt < 10) {
                                    // Retry if height is 0 (content might not be loaded yet)
                                    // For very long content, this might take several attempts
                                    webView.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            measureAndAdjustHeight(webView, attempt + 1);
                                        }
                                    }, 500);
                                } else {
                                    Log.w(TAG, "Failed to measure WebView height after " + attempt + " attempts");
                                    // Set a minimum height to ensure something is displayed
                                    android.view.ViewGroup.LayoutParams params = webView.getLayoutParams();
                                    if (params != null && params.height == android.view.ViewGroup.LayoutParams.WRAP_CONTENT) {
                                        params.height = 200; // Minimum height
                                        webView.setLayoutParams(params);
                                        webView.requestLayout();
                                    }
                                }
                            }
                        } else if (attempt < 10) {
                            // Retry if no height value
                            webView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    measureAndAdjustHeight(webView, attempt + 1);
                                }
                            }, 500);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing height: " + e.getMessage(), e);
                        if (attempt < 10) {
                            webView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    measureAndAdjustHeight(webView, attempt + 1);
                                }
                            }, 500);
                        }
                    }
                }
            }
        );
    }
}
