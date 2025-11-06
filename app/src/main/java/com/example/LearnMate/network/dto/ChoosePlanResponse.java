package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Response model cho Choose Plan
 * Sử dụng cho POST /api/Subscription/plans/{subscriptionId}/choose
 */
public class ChoosePlanResponse {
    @SerializedName("userSubscriptionId")
    public String userSubscriptionId;

    @SerializedName("subscriptionId")
    public String subscriptionId;

    @SerializedName("userId")
    public String userId;

    @SerializedName("status")
    public String status;

    @SerializedName("subscribedAt")
    public String subscribedAt;

    @SerializedName("expiredAt")
    public String expiredAt;

    @SerializedName("name")
    public String name;

    @SerializedName("type")
    public String type;

    @SerializedName("subscriptionStatus")
    public String subscriptionStatus;

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

