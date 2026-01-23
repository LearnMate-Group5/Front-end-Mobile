package com.example.LearnMate.network.api;

import com.example.LearnMate.network.dto.PaymentHistoryResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Service interface cho Payment API
 */
public interface PaymentService {
    
    /**
     * Lấy lịch sử thanh toán của user hiện tại
     * GET /api/Payment/history
     * 
     * @param pageNumber Số trang (mặc định: 1)
     * @param pageSize Kích thước trang (mặc định: 10, tối đa: 100)
     * @param status Lọc theo trạng thái thanh toán (optional)
     * @param paymentGateway Lọc theo cổng thanh toán (optional)
     * @return PaymentHistoryResponse với danh sách lịch sử thanh toán
     */
    @GET("/api/Payment/history")
    Call<PaymentHistoryResponse> getPaymentHistory(
            @Query("pageNumber") Integer pageNumber,
            @Query("pageSize") Integer pageSize,
            @Query("status") String status,
            @Query("paymentGateway") String paymentGateway
    );
}

