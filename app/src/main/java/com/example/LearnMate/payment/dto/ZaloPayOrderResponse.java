package com.example.LearnMate.payment.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Response model cho ZaloPay Payment API
 * POST /api/Payment/zalopay/create
 */
public class ZaloPayOrderResponse {
    
    @SerializedName("orderUrl")
    private String orderUrl;
    
    @SerializedName("appTransId")
    private String appTransId;
    
    @SerializedName("zpTransToken")
    private String zpTransToken;
    
    @SerializedName("orderToken")
    private String orderToken;
    
    @SerializedName("qrCode")
    private String qrCode;

    // Getters and Setters
    public String getOrderUrl() {
        return orderUrl;
    }

    public void setOrderUrl(String orderUrl) {
        this.orderUrl = orderUrl;
    }

    public String getAppTransId() {
        return appTransId;
    }

    public void setAppTransId(String appTransId) {
        this.appTransId = appTransId;
    }

    public String getZpTransToken() {
        return zpTransToken;
    }

    public void setZpTransToken(String zpTransToken) {
        this.zpTransToken = zpTransToken;
    }

    public String getOrderToken() {
        return orderToken;
    }

    public void setOrderToken(String orderToken) {
        this.orderToken = orderToken;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
}
