package com.example.LearnMate.payment.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Request model cho ZaloPay Payment API
 * POST /api/Payment/zalopay/create
 */
public class ZaloPayOrderRequest {
    
    @SerializedName("orderId")
    private String orderId;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("redirectUrl")
    private String redirectUrl;

    public ZaloPayOrderRequest(String orderId, String description, String redirectUrl) {
        this.orderId = orderId;
        this.description = description;
        this.redirectUrl = redirectUrl;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
