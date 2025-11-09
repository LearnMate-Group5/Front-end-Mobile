package com.example.LearnMate.payment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.MoMoService;
import com.example.LearnMate.network.api.SubscriptionService;
import com.example.LearnMate.network.dto.ChoosePlanResponse;
import com.example.LearnMate.payment.dto.MoMoOrderRequest;
import com.example.LearnMate.payment.dto.MoMoOrderResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Helper class để xử lý MoMo payment flow
 * 
 * Flow:
 * 1. Choose subscription plan -> Nhận userSubscriptionId
 * 2. Create MoMo order với userSubscriptionId
 * 3. Open MoMo app với deeplink
 * 4. Handle return từ MoMo app
 */
public class MoMoPaymentHelper {
    
    private static final String TAG = "MoMoPaymentHelper";
    public static final String MOMO_DEEPLINK_SCHEME = "learnmate";
    // Deep link redirects to PaymentSuccessActivity after payment completion
    public static final String MOMO_RETURN_URL = "learnmate://payment/success";
    
    private Activity activity;
    private MoMoPaymentListener listener;
    
    public interface MoMoPaymentListener {
        void onChoosePlanSuccess(ChoosePlanResponse response);
        void onChoosePlanFailed(String error);
        void onCreateOrderSuccess(MoMoOrderResponse response);
        void onCreateOrderFailed(String error);
    }
    
    public MoMoPaymentHelper(Activity activity) {
        this.activity = activity;
    }
    
    public void setListener(MoMoPaymentListener listener) {
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
     * Bước 2: Tạo MoMo order
     * POST /api/Payment/momo/create
     * 
     * @param userSubscriptionId UUID từ response của choosePlan
     * @param planName Tên gói subscription để hiển thị
     */
    public void createMoMoOrder(String userSubscriptionId, String planName) {
        Log.d(TAG, "Creating MoMo order for userSubscriptionId: " + userSubscriptionId);
        
        // Build request
        MoMoOrderRequest request = new MoMoOrderRequest(
            userSubscriptionId,  // orderId = userSubscriptionId
            "Thanh toán gói dịch vụ " + planName,  // orderInfo
            MOMO_RETURN_URL,  // redirectUrl (deep link)
            "Thanh toán gói dịch vụ",  // extraData
            "vi"  // lang
        );
        
        MoMoService service = RetrofitClient.getMoMoService(activity);
        Call<MoMoOrderResponse> call = service.createOrder(request);
        
        call.enqueue(new Callback<MoMoOrderResponse>() {
            @Override
            public void onResponse(Call<MoMoOrderResponse> call, Response<MoMoOrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MoMoOrderResponse result = response.body();
                    Log.d(TAG, "MoMo order created! Deeplink: " + result.getDeeplink());
                    
                    if (listener != null) {
                        listener.onCreateOrderSuccess(result);
                    }
                    
                    // Tự động mở MoMo app
                    openMoMoApp(result.getDeeplink());
                } else {
                    String error = "Failed to create MoMo order: " + response.code();
                    Log.e(TAG, error);
                    if (listener != null) {
                        listener.onCreateOrderFailed(error);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<MoMoOrderResponse> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                if (listener != null) {
                    listener.onCreateOrderFailed(error);
                }
            }
        });
    }
    
    /**
     * Bước 3: Mở MoMo app với deeplink
     * 
     * @param deeplink Deeplink từ MoMo response
     */
    public void openMoMoApp(String deeplink) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deeplink));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
            Log.d(TAG, "Opened MoMo app with deeplink");
        } catch (Exception e) {
            Log.e(TAG, "Failed to open MoMo app", e);
            Toast.makeText(activity, "Không thể mở ứng dụng MoMo. Vui lòng cài đặt MoMo app.", Toast.LENGTH_LONG).show();
        }
    }
    
}
