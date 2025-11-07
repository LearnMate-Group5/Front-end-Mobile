package com.example.LearnMate.network.api;

import com.example.LearnMate.payment.dto.MoMoOrderRequest;
import com.example.LearnMate.payment.dto.MoMoOrderResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Service interface cho MoMo Payment API
 */
public interface MoMoService {
    
    /**
     * Tạo MoMo order để thanh toán subscription
     * POST /api/Payment/momo/create
     * 
     * @param request MoMoOrderRequest chứa thông tin order
     * @return MoMoOrderResponse với payUrl, deeplink, qrCodeUrl
     */
    @POST("api/Payment/momo/create")
    Call<MoMoOrderResponse> createOrder(@Body MoMoOrderRequest request);
}
