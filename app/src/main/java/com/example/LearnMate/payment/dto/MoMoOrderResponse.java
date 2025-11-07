package com.example.LearnMate.payment.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Response model cho MoMo Payment API
 * POST /api/Payment/momo/create
 */
public class MoMoOrderResponse {
    
    @SerializedName("payUrl")
    private String payUrl;
    
    @SerializedName("deeplink")
    private String deeplink;
    
    @SerializedName("qrCodeUrl")
    private String qrCodeUrl;
    
    @SerializedName("orderId")
    private String orderId;
    
    @SerializedName("requestId")
    private String requestId;

    // Getters and Setters
    public String getPayUrl() {
        return payUrl;
    }

    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }

    public String getDeeplink() {
        return deeplink;
    }

    public void setDeeplink(String deeplink) {
        this.deeplink = deeplink;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
