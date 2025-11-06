package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Response model cho Subscription Plan
 * Sử dụng cho GET /api/Subscription/plans và GET /api/Subscription/plans/{subscriptionId}
 */
public class SubscriptionPlanResponse {
    @SerializedName("subscriptionId")
    public String subscriptionId;

    @SerializedName("name")
    public String name;

    @SerializedName("type")
    public String type;

    @SerializedName("status")
    public String status;

    @SerializedName("originalPrice")
    public long originalPrice;

    @SerializedName("discount")
    public int discount;

    // Helper method để tính giá sau discount
    public long getFinalPrice() {
        if (discount > 0 && discount <= 100) {
            return originalPrice * (100 - discount) / 100;
        }
        return originalPrice;
    }
}

