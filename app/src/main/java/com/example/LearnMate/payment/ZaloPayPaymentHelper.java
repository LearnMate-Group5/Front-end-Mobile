package com.example.LearnMate.payment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.SubscriptionService;
import com.example.LearnMate.network.api.ZaloPayService;
import com.example.LearnMate.network.dto.ChoosePlanResponse;
import com.example.LearnMate.payment.dto.ZaloPayOrderRequest;
import com.example.LearnMate.payment.dto.ZaloPayOrderResponse;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

/**
 * Helper class để xử lý ZaloPay payment flow sử dụng ZaloPay SDK
 * 
 * Flow:
 * 1. Choose subscription plan -> Nhận userSubscriptionId
 * 2. Create ZaloPay order với userSubscriptionId
 * 3. Gọi ZaloPay SDK để mở app/xử lý thanh toán
 * 4. Handle callback từ ZaloPay
 */
public class ZaloPayPaymentHelper {
    
    private static final String TAG = "ZaloPayPaymentHelper";
    public static final String ZALOPAY_DEEPLINK_SCHEME = "learnmate";
    // Deep link redirects to PaymentSuccessActivity after payment completion
    public static final String ZALOPAY_RETURN_URL = "learnmate://payment/success";
    
    private Activity activity;
    private ZaloPayPaymentListener listener;
    
    public interface ZaloPayPaymentListener {
        void onChoosePlanSuccess(ChoosePlanResponse response);
        void onChoosePlanFailed(String error);
        void onCreateOrderSuccess(ZaloPayOrderResponse response);
        void onCreateOrderFailed(String error);
        void onPaymentSuccess(String transactionId);
        void onPaymentFailed(String error);
        void onPaymentCancelled();
    }
    
    public ZaloPayPaymentHelper(Activity activity) {
        this.activity = activity;
        // Initialize ZaloPay SDK - Use SANDBOX for testing, PRODUCTION for live
        ZaloPaySDK.init(2554, Environment.SANDBOX);
    }
    
    public void setListener(ZaloPayPaymentListener listener) {
        this.listener = listener;
    }
    
    /**
     * Bước 1: Chọn subscription plan
     * POST /api/Subscription/plans/{subscriptionId}/choose
     * 
     * @param subscriptionPlanId UUID của subscription plan
     */
    public void choosePlan(String subscriptionPlanId) {
        Log.d(TAG, "Choosing plan: " + subscriptionPlanId);
        
        SubscriptionService service = RetrofitClient.getSubscriptionService(activity);
        Call<ChoosePlanResponse> call = service.choosePlan(subscriptionPlanId);
        
        call.enqueue(new Callback<ChoosePlanResponse>() {
            @Override
            public void onResponse(Call<ChoosePlanResponse> call, Response<ChoosePlanResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChoosePlanResponse result = response.body();
                    Log.d(TAG, "Choose plan success! UserSubscriptionId: " + result.userSubscriptionId);
                    
                    if (listener != null) {
                        listener.onChoosePlanSuccess(result);
                    }
                } else {
                    String error = "Failed to choose plan: " + response.code();
                    Log.e(TAG, error);
                    if (listener != null) {
                        listener.onChoosePlanFailed(error);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ChoosePlanResponse> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                if (listener != null) {
                    listener.onChoosePlanFailed(error);
                }
            }
        });
    }
    
    /**
     * Bước 2: Tạo ZaloPay order
     * POST /api/Payment/zalopay/create
     * 
     * @param userSubscriptionId UUID từ response của choosePlan
     * @param planName Tên gói subscription để hiển thị
     */
    public void createZaloPayOrder(String userSubscriptionId, String planName) {
        Log.d(TAG, "Creating ZaloPay order for userSubscriptionId: " + userSubscriptionId);
        
        // Build request
        ZaloPayOrderRequest request = new ZaloPayOrderRequest(
            userSubscriptionId,  // orderId = userSubscriptionId
            "Thanh toán gói dịch vụ " + planName,  // description
            ZALOPAY_RETURN_URL  // redirectUrl (deep link)
        );
        
        ZaloPayService service = RetrofitClient.getZaloPayService(activity);
        Call<ZaloPayOrderResponse> call = service.createOrder(request);
        
        call.enqueue(new Callback<ZaloPayOrderResponse>() {
            @Override
            public void onResponse(Call<ZaloPayOrderResponse> call, Response<ZaloPayOrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ZaloPayOrderResponse result = response.body();
                    Log.d(TAG, "ZaloPay order created! zpTransToken: " + result.getZpTransToken());
                    
                    if (listener != null) {
                        listener.onCreateOrderSuccess(result);
                    }
                    
                    // Bước 3: Tự động gọi ZaloPay SDK để thanh toán
                    payWithZaloPaySDK(result.getZpTransToken());
                } else {
                    String error = "Failed to create ZaloPay order: " + response.code();
                    Log.e(TAG, error);
                    if (listener != null) {
                        listener.onCreateOrderFailed(error);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ZaloPayOrderResponse> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                if (listener != null) {
                    listener.onCreateOrderFailed(error);
                }
            }
        });
    }
    
    /**
     * Bước 3: Gọi ZaloPay SDK để thanh toán
     * Sử dụng zpTransToken từ response
     * 
     * @param zpTransToken Token từ ZaloPay response
     */
    private void payWithZaloPaySDK(String zpTransToken) {
        Log.d(TAG, "Calling ZaloPay SDK with zpTransToken");
        
        ZaloPaySDK.getInstance().payOrder(activity, zpTransToken, "demozpdk://app", new PayOrderListener() {
            @Override
            public void onPaymentSucceeded(String transactionId, String zpTransToken, String appTransId) {
                Log.d(TAG, "Payment succeeded! TransactionId: " + transactionId);
                Toast.makeText(activity, "Thanh toán thành công!", Toast.LENGTH_LONG).show();
                
                if (listener != null) {
                    listener.onPaymentSuccess(transactionId);
                }
            }
            
            @Override
            public void onPaymentCanceled(String zpTransToken, String appTransId) {
                Log.d(TAG, "Payment cancelled by user");
                Toast.makeText(activity, "Đã hủy thanh toán", Toast.LENGTH_SHORT).show();
                
                if (listener != null) {
                    listener.onPaymentCancelled();
                }
            }
            
            @Override
            public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransId) {
                Log.e(TAG, "Payment error: " + zaloPayError.toString());
                Toast.makeText(activity, "Lỗi thanh toán: " + zaloPayError.toString(), Toast.LENGTH_LONG).show();
                
                if (listener != null) {
                    listener.onPaymentFailed(zaloPayError.toString());
                }
            }
        });
    }
    
}
