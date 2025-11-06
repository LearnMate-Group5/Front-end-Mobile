package com.example.LearnMate.network.api;

import com.example.LearnMate.network.dto.ChoosePlanResponse;
import com.example.LearnMate.network.dto.CurrentSubscriptionResponse;
import com.example.LearnMate.network.dto.SubscriptionPlanResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Service interface cho Subscription API
 */
public interface SubscriptionService {
    
    /**
     * Lấy thông tin subscription hiện tại của user
     * GET /api/Subscription/plans/my/current
     * 
     * @return CurrentSubscriptionResponse hoặc null nếu chưa có subscription
     */
    @GET("api/Subscription/plans/my/current")
    Call<List<CurrentSubscriptionResponse>> getCurrentSubscription();

    /**
     * Lấy danh sách tất cả subscription plans có sẵn
     * GET /api/Subscription/plans
     * 
     * @return Danh sách SubscriptionPlanResponse
     */
    @GET("api/Subscription/plans")
    Call<List<SubscriptionPlanResponse>> getPlans();

    /**
     * Lấy thông tin chi tiết của một subscription plan
     * GET /api/Subscription/plans/{subscriptionId}
     * 
     * @param subscriptionId UUID của subscription plan
     * @return SubscriptionPlanResponse
     */
    @GET("api/Subscription/plans/{subscriptionId}")
    Call<SubscriptionPlanResponse> getPlan(@Path("subscriptionId") String subscriptionId);

    /**
     * Chọn và đăng ký một subscription plan
     * POST /api/Subscription/plans/{subscriptionId}/choose
     * 
     * @param subscriptionId UUID của subscription plan muốn đăng ký
     * @return ChoosePlanResponse với thông tin subscription đã đăng ký
     */
    @POST("api/Subscription/plans/{subscriptionId}/choose")
    Call<ChoosePlanResponse> choosePlan(@Path("subscriptionId") String subscriptionId);

    /**
     * Hủy subscription hiện tại
     * POST /api/Subscription/plans/cancel
     * 
     * @return Void response (204 No Content hoặc 200 OK)
     */
    @POST("api/Subscription/plans/cancel")
    Call<Void> cancelSubscription();
}

