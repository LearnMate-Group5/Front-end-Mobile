package com.example.LearnMate.payment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.LearnMate.payment.dto.PayOSOrderRequest;
import com.example.LearnMate.payment.dto.PayOSOrderResponse;

import java.util.UUID;

/**
 * Helper class để xử lý thanh toán PayOS
 * 
 * PayOS là payment gateway của Việt Nam
 * Documentation: https://pay.payos.vn/web4c/docs/
 */
public class PayOSPaymentHelper {
    
    private static final String TAG = "PayOSPaymentHelper";
    
    // PayOS Config - Thay bằng thông tin từ PayOS Portal
    private static final String PAYOS_CLIENT_ID = "YOUR_CLIENT_ID"; // Thay bằng Client ID của bạn
    private static final String PAYOS_API_KEY = "YOUR_API_KEY"; // Thay bằng API Key của bạn
    private static final String PAYOS_CHECKSUM_KEY = "YOUR_CHECKSUM_KEY"; // Thay bằng Checksum Key của bạn
    private static final String PAYOS_ENVIRONMENT = "sandbox"; // hoặc "production"
    
    // PayOS API endpoints
    private static final String PAYOS_SANDBOX_BASE_URL = "https://api-merchant.payos.vn";
    private static final String PAYOS_PRODUCTION_BASE_URL = "https://api.payos.vn";
    private static final String CREATE_PAYMENT_LINK = "/v2/payment-requests";
    
    private Context context;
    
    public PayOSPaymentHelper(Context context) {
        this.context = context;
    }
    
    /**
     * Tạo payment link và khởi động thanh toán PayOS
     * 
     * @param activity Activity để nhận kết quả
     * @param amount Số tiền thanh toán (VND)
     * @param description Mô tả đơn hàng
     * @param callback Callback để xử lý kết quả
     */
    public void pay(Activity activity, long amount, String description, PaymentCallback callback) {
        try {
            Log.d(TAG, "Starting PayOS payment: " + amount + " VND");
            
            // Tạo payment request
            PayOSOrderRequest paymentRequest = createPaymentRequest(amount, description);
            
            // Gọi API tạo payment link
            createPaymentLink(paymentRequest, callback);
            
        } catch (Exception e) {
            Log.e(TAG, "Error initiating payment", e);
            if (callback != null) {
                callback.onError("Lỗi khởi tạo thanh toán: " + e.getMessage());
            }
        }
    }
    
    /**
     * Tạo payment request
     */
    private PayOSOrderRequest createPaymentRequest(long amount, String description) {
        PayOSOrderRequest request = new PayOSOrderRequest();
        
        // Tạo orderCode unique (số nguyên dương)
        int orderCode = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        
        request.setOrderCode(orderCode);
        request.setAmount(amount);
        request.setDescription(description);
        request.setCancelUrl("learnmate://payment/cancel");
        request.setReturnUrl("learnmate://payment/return");
        request.setItems(java.util.Collections.singletonList(
            new PayOSOrderRequest.Item("Premium Subscription", 1, amount)
        ));
        
        return request;
    }
    
    /**
     * Gọi API tạo payment link
     * Note: Trong production, nên gọi qua backend server để bảo mật API Key và Checksum Key
     */
    private void createPaymentLink(PayOSOrderRequest request, PaymentCallback callback) {
        // TODO: Implement API call để tạo payment link
        // Có 2 cách:
        // 1. Gọi trực tiếp từ app (không an toàn vì expose API Key, Checksum Key)
        // 2. Gọi qua backend server (recommended) ✅ RECOMMENDED
        
        // Option 1: Gọi qua Backend API (Recommended)
        // PayOSService service = RetrofitClient.getPayOSService(context);
        // service.createPaymentLink(request).enqueue(new Callback<PayOSOrderResponse>() {
        //     @Override
        //     public void onResponse(Call<PayOSOrderResponse> call, Response<PayOSOrderResponse> response) {
        //         if (response.isSuccessful() && response.body() != null) {
        //             callback.onPaymentLinkCreated(response.body());
        //         }
        //     }
        //     @Override
        //     public void onFailure(Call<PayOSOrderResponse> call, Throwable t) {
        //         callback.onError("Network error: " + t.getMessage());
        //     }
        // });
        
        // Option 1: Gọi qua Backend API (Recommended) ✅
        // Uncomment khi backend API đã sẵn sàng:
        /*
        try {
            PayOSService service = com.example.LearnMate.network.RetrofitClient.getPayOSService(context);
            service.createPaymentLink(request).enqueue(new retrofit2.Callback<PayOSOrderResponse>() {
                @Override
                public void onResponse(retrofit2.Call<PayOSOrderResponse> call, retrofit2.Response<PayOSOrderResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "Payment link created from backend: " + response.body().getData().getCheckoutUrl());
                        callback.onPaymentLinkCreated(response.body());
                    } else {
                        callback.onError("Backend error: " + response.message());
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<PayOSOrderResponse> call, Throwable t) {
                    Log.e(TAG, "Backend API error", t);
                    callback.onError("Network error: " + t.getMessage());
                }
            });
            return; // Exit early nếu dùng backend API
        } catch (Exception e) {
            Log.e(TAG, "Error calling backend API", e);
            // Fallback to mock
        }
        */
        
        Log.w(TAG, "createPaymentLink: Using mock response - implement backend API");
        Log.w(TAG, "Note: Should call backend API to create payment link securely");
        
        // Tạm thời: Tạo mock payment link để test UI
        if (callback != null) {
            PayOSOrderResponse mockResponse = new PayOSOrderResponse();
            mockResponse.setCode("00"); // Success
            mockResponse.setDesc("Success");
            PayOSOrderResponse.PaymentData data = new PayOSOrderResponse.PaymentData();
            data.setCheckoutUrl("https://pay.payos.vn/web/?orderCode=" + request.getOrderCode()); // Mock URL
            mockResponse.setData(data);
            
            Log.d(TAG, "Mock payment link created: " + mockResponse.getData().getCheckoutUrl());
            callback.onPaymentLinkCreated(mockResponse);
        }
    }
    
    /**
     * Mở PayOS payment page trong WebView Activity
     * PayOS sẽ redirect về returnUrl sau khi thanh toán
     */
    public void openPaymentPage(Activity activity, String checkoutUrl) {
        try {
            Intent intent = new Intent(activity, com.example.LearnMate.payment.PaymentWebViewActivity.class);
            intent.putExtra(com.example.LearnMate.payment.PaymentWebViewActivity.EXTRA_CHECKOUT_URL, checkoutUrl);
            activity.startActivityForResult(intent, PayOSConstants.REQUEST_CODE_PAYOS);
            Log.d(TAG, "Opening PayOS payment page in WebView: " + checkoutUrl);
        } catch (Exception e) {
            Log.e(TAG, "Error opening PayOS payment page", e);
            // Fallback: Mở trong browser
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse(checkoutUrl));
                activity.startActivityForResult(browserIntent, PayOSConstants.REQUEST_CODE_PAYOS);
            } catch (Exception ex) {
                Log.e(TAG, "Error opening in browser", ex);
                Toast.makeText(context, "Không thể mở trang thanh toán", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Xử lý kết quả thanh toán từ PayOS
     * Call method này trong onActivityResult hoặc khi nhận webhook
     */
    public void handlePaymentResult(int requestCode, int resultCode, Intent data, PaymentCallback callback) {
        if (requestCode == PayOSConstants.REQUEST_CODE_PAYOS) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri returnUri = data.getData();
                if (returnUri != null) {
                    String status = returnUri.getQueryParameter("status");
                    String orderCode = returnUri.getQueryParameter("orderCode");
                    
                    Log.d(TAG, "Payment result - Status: " + status + ", OrderCode: " + orderCode);
                    
                    if ("success".equals(status) || "PAID".equals(status)) {
                        // Thanh toán thành công
                        if (callback != null) {
                            callback.onPaymentSuccess(orderCode);
                        }
                    } else {
                        // Thanh toán thất bại hoặc bị hủy
                        String errorMsg = "Thanh toán không thành công";
                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
                        if (callback != null) {
                            callback.onPaymentFailed(errorMsg);
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Thanh toán bị hủy", Toast.LENGTH_SHORT).show();
                if (callback != null) {
                    callback.onPaymentFailed("User cancelled");
                }
            }
        }
    }
    
    /**
     * Verify payment status với PayOS (qua backend)
     */
    public void verifyPayment(String orderCode, PaymentCallback callback) {
        // TODO: Gọi backend API để verify payment
        // Backend sẽ query PayOS API: GET /v2/payment-requests/{orderCode}
        Log.d(TAG, "Verify payment: " + orderCode);
    }
    
    /**
     * Callback interface cho payment flow
     */
    public interface PaymentCallback {
        void onPaymentLinkCreated(PayOSOrderResponse response);
        void onPaymentSuccess(String orderCode);
        void onPaymentFailed(String errorMessage);
        void onError(String error);
    }
}

