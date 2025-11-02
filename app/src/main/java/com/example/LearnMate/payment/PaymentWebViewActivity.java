package com.example.LearnMate.payment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.LearnMate.R;

/**
 * Activity hiển thị PayOS payment page trong WebView
 */
public class PaymentWebViewActivity extends AppCompatActivity {
    
    private static final String TAG = "PaymentWebViewActivity";
    public static final String EXTRA_CHECKOUT_URL = "checkout_url";
    
    private WebView webView;
    private ProgressBar progressBar;
    private String checkoutUrl;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_webview);
        
        checkoutUrl = getIntent().getStringExtra(EXTRA_CHECKOUT_URL);
        
        if (checkoutUrl == null || checkoutUrl.isEmpty()) {
            Toast.makeText(this, "Không có URL thanh toán", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        Log.d(TAG, "Loading PayOS payment page: " + checkoutUrl);
        
        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        setupWebView();
        loadPaymentPage();
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        webView = findViewById(R.id.webViewPayment);
        progressBar = findViewById(R.id.progressBarPayment);
        
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        
        // Enable mixed content (http/https)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        
        webView.setWebViewClient(new PaymentWebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    
    private void loadPaymentPage() {
        webView.loadUrl(checkoutUrl);
    }
    
    /**
     * WebViewClient để handle PayOS redirect về deep link
     */
    private class PaymentWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            Log.d(TAG, "Loading URL: " + url);
            
            // Kiểm tra nếu là deep link return từ PayOS
            if (url.startsWith("learnmate://payment/")) {
                handlePayOSReturn(url);
                return true;
            }
            
            // Cho phép WebView load URL thông thường
            return false;
        }
        
        @Override
        @SuppressWarnings("deprecation")
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "Loading URL (deprecated): " + url);
            
            if (url.startsWith("learnmate://payment/")) {
                handlePayOSReturn(url);
                return true;
            }
            
            return false;
        }
        
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d(TAG, "Page finished: " + url);
        }
    }
    
    /**
     * Xử lý khi PayOS redirect về deep link
     */
    private void handlePayOSReturn(String returnUrl) {
        Log.d(TAG, "PayOS return URL: " + returnUrl);
        
        try {
            Uri uri = Uri.parse(returnUrl);
            String status = uri.getQueryParameter("status");
            String orderCode = uri.getQueryParameter("orderCode");
            
            Log.d(TAG, "Payment result - Status: " + status + ", OrderCode: " + orderCode);
            
            // Tạo result intent
            Intent resultIntent = new Intent();
            resultIntent.setData(uri);
            
            if ("PAID".equals(status) || "success".equals(status)) {
                setResult(RESULT_OK, resultIntent);
                Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
            } else {
                setResult(RESULT_CANCELED, resultIntent);
                Toast.makeText(this, "Thanh toán không thành công", Toast.LENGTH_SHORT).show();
            }
            
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error handling PayOS return", e);
            finish();
        }
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}

