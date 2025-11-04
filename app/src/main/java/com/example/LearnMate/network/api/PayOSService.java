package com.example.LearnMate.network.api;

import com.example.LearnMate.payment.dto.PayOSOrderRequest;
import com.example.LearnMate.payment.dto.PayOSOrderResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Service để gọi Backend API cho PayOS
 * Backend sẽ xử lý việc tạo payment link với PayOS và trả về checkout_url
 */
public interface PayOSService {
    
    /**
     * Tạo PayOS payment link qua backend
     * Backend sẽ:
     * 1. Nhận request từ app
     * 2. Tạo PayOS payment request
     * 3. Sign với Checksum Key (HMAC SHA256)
     * 4. Gọi PayOS API: POST https://api-merchant.payos.vn/v2/payment-requests
     * 5. Trả về checkout_url cho app
     */
    @POST("api/payment/payos/create-payment-link")
    Call<PayOSOrderResponse> createPaymentLink(@Body PayOSOrderRequest request);
    
    /**
     * Query payment status từ PayOS
     * Backend sẽ gọi: GET https://api-merchant.payos.vn/v2/payment-requests/{orderCode}
     */
    @GET("api/payment/payos/verify/{orderCode}")
    Call<PayOSOrderResponse> verifyPayment(@Path("orderCode") int orderCode);
}




