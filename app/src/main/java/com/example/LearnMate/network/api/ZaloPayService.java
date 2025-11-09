package com.example.LearnMate.network.api;

import com.example.LearnMate.payment.dto.ZaloPayOrderRequest;
import com.example.LearnMate.payment.dto.ZaloPayOrderResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Service interface cho ZaloPay Payment API
 */
public interface ZaloPayService {
    
    /**
     * Tạo ZaloPay order để thanh toán subscription
     * POST /api/Payment/zalopay/create
     * 
     * @param request ZaloPayOrderRequest chứa thông tin order
     * @return ZaloPayOrderResponse với orderUrl, zpTransToken, appTransId, orderToken, qrCode
     */
    @POST("api/Payment/zalopay/create")
    Call<ZaloPayOrderResponse> createOrder(@Body ZaloPayOrderRequest request);
}
