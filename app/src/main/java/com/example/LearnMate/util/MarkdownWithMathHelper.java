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
            // Only escape &, <, > but not $, \, {, }, ^, _, etc.
            // But be careful: we need to preserve LaTeX syntax exactly
            String escapedLatex = latex.replace("&", "&amp;")
                                       .replace("<", "&lt;")
                                       .replace(">", "&gt;");
            
            // Find and replace the placeholder
            // Use simple replace (not replaceAll) to avoid regex issues with special chars
            html = html.replace("__PROTECTED_LATEX_" + i + "__", escapedLatex);
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
        // Use non-greedy matching with a reasonable max length (1000 chars for complex formulas)
        // This handles complex formulas like $\cos ^{2} 3 x=\frac{1+\cos 6 x}{2}$
        // Pattern: $ followed by any chars (including spaces, newlines) up to $ (not followed by $)
        java.util.regex.Pattern inlinePattern = java.util.regex.Pattern.compile("(?<!\\$)\\$([^$]{1,1000}?)\\$(?!\\$)", java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher inlineMatcher = inlinePattern.matcher(text);
        
        sb = new StringBuffer();
        while (inlineMatcher.find()) {
            String latex = inlineMatcher.group(0); // Full match including $
            // Only add if it's a valid LaTeX expression (has some content between $)
            String latexContent = inlineMatcher.group(1).trim();
            if (!latexContent.isEmpty()) {
                protectedLatex.add(latex);
                inlineMatcher.appendReplacement(sb, "__PROTECTED_LATEX_" + index + "__");
                index++;
            }
        }
        inlineMatcher.appendTail(sb);
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
                "                autoload: {\n" +
                "                    color: [],\n" +
                "                    colorv2: ['color']\n" +
                "                },\n" +
                "                packages: {'[+]': ['ams', 'newcommand', 'configMacros']}\n" +
                "            },\n" +
                "            options: {\n" +
                "                skipHtmlTags: ['script', 'noscript', 'style', 'textarea', 'pre', 'code'],\n" +
                "                ignoreHtmlClass: 'tex2jax_ignore',\n" +
                "                processHtmlClass: 'tex2jax_process'\n" +
                "            },\n" +
                "            startup: {\n" +
                "                ready: function() {\n" +
                "                    MathJax.startup.defaultReady();\n" +
                "                    MathJax.startup.promise.then(function() {\n" +
                "                        console.log('MathJax is ready');\n" +
                "                    });\n" +
                "                }\n" +
                "            }\n" +
                "        };\n" +
                "    </script>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            margin: 0;\n" +
                "            padding: 12px;\n" +
                "            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;\n" +
                "            font-size: 16px;\n" +
                "            line-height: 1.6;\n" +
                "            color: #333;\n" +
                "            background-color: transparent;\n" +
                "        }\n" +
                "        p { margin: 8px 0; }\n" +
                "        h1, h2, h3, h4, h5, h6 { margin: 12px 0 8px 0; font-weight: bold; }\n" +
                "        h1 { font-size: 24px; }\n" +
                "        h2 { font-size: 20px; }\n" +
                "        h3 { font-size: 18px; }\n" +
                "        code, tt { background-color: #f0f0f0; padding: 2px 4px; border-radius: 3px; font-family: monospace; }\n" +
                "        pre { background-color: #f5f5f5; padding: 10px; border-radius: 5px; overflow-x: auto; }\n" +
                "        pre code { background-color: transparent; padding: 0; }\n" +
                "        a { color: #6200ea; text-decoration: none; }\n" +
                "        a:hover { text-decoration: underline; }\n" +
                "        .MathJax { font-size: 1.05em !important; color: inherit !important; }\n" +
                "        .MathJax_SVG { fill: currentColor !important; }\n" +
                "        .MathJax_SVG_Display { margin: 0.5em 0 !important; }\n" +
                "        /* Style like ChatGPT - clean and readable */\n" +
                "        .MathJax_Display { text-align: left !important; margin: 0.5em 0 !important; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                content +
                "\n<script>\n" +
                "    (function() {\n" +
                "        var renderAttempts = 0;\n" +
                "        var maxAttempts = 50; // 5 seconds max wait\n" +
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
                "                }).catch(function(err) {\n" +
                "                    console.error('MathJax rendering error:', err);\n" +
                "                });\n" +
                "            } else if (window.MathJax && window.MathJax.typesetPromise) {\n" +
                "                window.MathJax.typesetPromise().then(function() {\n" +
                "                    console.log('MathJax rendering complete (direct)');\n" +
                "                }).catch(function(err) {\n" +
                "                    console.error('MathJax error:', err);\n" +
                "                });\n" +
                "            } else if (window.MathJax && window.MathJax.typeset) {\n" +
                "                window.MathJax.typeset();\n" +
                "                console.log('MathJax typeset called');\n" +
                "            } else if (renderAttempts < maxAttempts) {\n" +
                "                console.log('Waiting for MathJax to load... (' + renderAttempts + '/' + maxAttempts + ')');\n" +
                "                setTimeout(renderMath, 100);\n" +
                "            } else {\n" +
                "                console.error('MathJax failed to load after ' + maxAttempts + ' attempts');\n" +
                "            }\n" +
                "        }\n" +
                "        \n" +
                "        // Start rendering immediately\n" +
                "        renderMath();\n" +
                "        \n" +
                "        // Also try on DOMContentLoaded and load events\n" +
                "        if (document.readyState === 'loading') {\n" +
                "            document.addEventListener('DOMContentLoaded', function() {\n" +
                "                setTimeout(renderMath, 200);\n" +
                "            });\n" +
                "        }\n" +
                "        \n" +
                "        window.addEventListener('load', function() {\n" +
                "            setTimeout(renderMath, 500);\n" +
                "        });\n" +
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
        
        // Disable scrolling in WebView (we'll use the parent ScrollView)
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setScrollContainer(false);
        
        // Transparent background
        webView.setBackgroundColor(0x00000000);
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null); // For better compatibility
        
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
                                null
                            );
                        }
                    }, 1000);
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
}
