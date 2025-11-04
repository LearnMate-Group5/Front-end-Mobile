package com.example.LearnMate.payment;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

/**
 * Helper class để xử lý thanh toán ZaloPay
 */
public class ZaloPayPaymentHelper {
    
    private static final String TAG = "ZaloPayPaymentHelper";
    
    // App URI Scheme cho deep link callback
    private static final String APP_URI_SCHEME = "learnmate";
    
    private Activity activity;
    
    public ZaloPayPaymentHelper(Activity activity) {
        this.activity = activity;
    }
    
    /**
     * Gọi hàm thanh toán ZaloPay
     * 
     * @param amount Số tiền thanh toán (VND)
     * @param description Mô tả đơn hàng
     * @param callback Callback để nhận kết quả thanh toán
     */
    public void pay(long amount, String description, ZaloPayPaymentCallback callback) {
        // Chạy trên background thread để gọi API
        new Thread(() -> {
            try {
                Log.d(TAG, "=== Starting ZaloPay payment ===");
                Log.d(TAG, "Amount: " + amount + " VND");
                Log.d(TAG, "Description: " + description);
                
                // Kiểm tra ZaloPay SDK đã được init chưa
                if (ZaloPaySDK.getInstance() == null) {
                    Log.e(TAG, "ZaloPay SDK chưa được khởi tạo!");
                    activity.runOnUiThread(() -> {
                        if (callback != null) {
                            callback.onPaymentError("ZaloPay SDK chưa được khởi tạo. Vui lòng khởi động lại app.", null, null);
                        }
                    });
                    return;
                }
                
                // Bước 1: Gọi API ZaloPay để tạo order và lấy zptranstoken
                Log.d(TAG, "Step 1: Creating order on ZaloPay server...");
                String appUser = "LearnMateUser"; // Có thể lấy từ user info
                ZaloPayApiClient.ZaloPayOrderResponse orderResponse = 
                    ZaloPayApiClient.createOrder(amount, description, appUser);
                
                if (!orderResponse.isSuccess() || orderResponse.getZpTransToken() == null || orderResponse.getZpTransToken().isEmpty()) {
                    String returnMessage = orderResponse.getReturnMessage();
                    final String errorMsg = (returnMessage == null || returnMessage.isEmpty()) 
                        ? "Không thể tạo đơn hàng. Vui lòng thử lại sau." 
                        : returnMessage;
                    final String appTransID = orderResponse.getAppTransID();
                    
                    Log.e(TAG, "Failed to create order: " + errorMsg);
                    Log.e(TAG, "Return code: " + orderResponse.getReturnCode());
                    
                    activity.runOnUiThread(() -> {
                        if (callback != null) {
                            callback.onPaymentError("Không thể tạo đơn hàng: " + errorMsg, 
                                null, appTransID);
                        }
                    });
                    return;
                }
                
                String zpTransToken = orderResponse.getZpTransToken();
                String appTransID = orderResponse.getAppTransID();
                
                Log.d(TAG, "✓ Order created successfully:");
                Log.d(TAG, "  - appTransID: " + appTransID);
                Log.d(TAG, "  - zpTransToken: " + zpTransToken);
                
                // Bước 2: Gọi SDK để thanh toán
                activity.runOnUiThread(() -> {
                    try {
                        Log.d(TAG, "Step 2: Calling ZaloPaySDK.payOrder()...");
                        Log.d(TAG, "  - Activity: " + activity.getClass().getSimpleName());
                        Log.d(TAG, "  - zpTransToken: " + zpTransToken);
                        Log.d(TAG, "  - URI Scheme: " + APP_URI_SCHEME);
                        
                        // Gọi hàm thanh toán từ ZPDK
                        // Note: Loading dialog sẽ được dismiss trong callback
                        ZaloPaySDK.getInstance().payOrder(
                            activity,
                            zpTransToken,
                            APP_URI_SCHEME,
                            new ZaloPayPaymentListener(callback, appTransID)
                        );
                        
                        Log.d(TAG, "✓ payOrder() called successfully - ZaloPay app should open now");
                    } catch (Exception e) {
                        Log.e(TAG, "Error calling payOrder", e);
                        e.printStackTrace();
                        if (callback != null) {
                            callback.onPaymentError("Lỗi gọi thanh toán: " + e.getMessage(), zpTransToken, appTransID);
                        }
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error initiating ZaloPay payment", e);
                e.printStackTrace();
                activity.runOnUiThread(() -> {
                    if (callback != null) {
                        callback.onPaymentError("Lỗi khởi tạo thanh toán: " + e.getMessage() + 
                            "\nChi tiết: " + e.getClass().getSimpleName(), null, null);
                    }
                });
            }
        }).start();
    }
    
    /**
     * Xử lý kết quả từ deep link callback
     * Cần gọi trong Activity.onNewIntent()
     */
    public void handleResult(Intent intent) {
        try {
            ZaloPaySDK.getInstance().onResult(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error handling ZaloPay result", e);
        }
    }
    
    /**
     * PayOrderListener implementation
     */
    private static class ZaloPayPaymentListener implements PayOrderListener {
        
        private ZaloPayPaymentCallback callback;
        private String appTransID;
        
        public ZaloPayPaymentListener(ZaloPayPaymentCallback callback, String appTransID) {
            this.callback = callback;
            this.appTransID = appTransID;
        }
        
        @Override
        public void onPaymentSucceeded(final String transactionId, final String transToken, final String appTransID) {
            Log.d(TAG, "Payment succeeded - transactionId: " + transactionId + ", appTransID: " + appTransID);
            if (callback != null) {
                callback.onPaymentSucceeded(transactionId, transToken, appTransID);
            }
        }
        
        @Override
        public void onPaymentCanceled(String zpTransToken, String appTransID) {
            Log.d(TAG, "Payment canceled - appTransID: " + appTransID);
            if (callback != null) {
                callback.onPaymentCanceled(zpTransToken, appTransID);
            }
        }
        
        @Override
        public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
            Log.e(TAG, "=== Payment error ===");
            Log.e(TAG, "Error code: " + zaloPayError);
            Log.e(TAG, "Error name: " + (zaloPayError != null ? zaloPayError.name() : "null"));
            Log.e(TAG, "zpTransToken: " + zpTransToken);
            Log.e(TAG, "appTransID: " + appTransID);
            
            if (callback != null) {
                String errorMessage = "Lỗi thanh toán";
                
                if (zaloPayError != null) {
                    errorMessage = "Lỗi thanh toán: " + zaloPayError.name();
                    
                    // Xử lý các lỗi cụ thể
                    if (zaloPayError == ZaloPayError.PAYMENT_APP_NOT_FOUND) {
                        errorMessage = "Không tìm thấy ứng dụng ZaloPay. Vui lòng cài đặt ZaloPay từ CH Play hoặc App Store.";
                    } else {
                        errorMessage = "Lỗi thanh toán: " + zaloPayError.name() + " (Code: " + zaloPayError + ")";
                    }
                }
                
                callback.onPaymentError(errorMessage, zpTransToken, appTransID);
            }
        }
    }
    
    /**
     * Callback interface cho kết quả thanh toán
     */
    public interface ZaloPayPaymentCallback {
        /**
         * Thanh toán thành công
         */
        void onPaymentSucceeded(String transactionId, String transToken, String appTransID);
        
        /**
         * User hủy thanh toán
         */
        void onPaymentCanceled(String zpTransToken, String appTransID);
        
        /**
         * Lỗi thanh toán
         */
        void onPaymentError(String errorMessage, String zpTransToken, String appTransID);
    }
}
