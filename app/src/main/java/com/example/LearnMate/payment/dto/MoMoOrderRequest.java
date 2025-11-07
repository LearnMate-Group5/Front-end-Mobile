package com.example.LearnMate.payment.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Request model cho MoMo Payment API
 * POST /api/Payment/momo/create
 */
public class MoMoOrderRequest {
    
    @SerializedName("orderId")
    private String orderId;
    
    @SerializedName("orderInfo")
    private String orderInfo;
    
    @SerializedName("redirectUrl")
    private String redirectUrl;
    
    @SerializedName("extraData")
    private String extraData;
    
    @SerializedName("lang")
    private String lang;

    public MoMoOrderRequest(String orderId, String orderInfo, String redirectUrl, String extraData, String lang) {
        this.orderId = orderId;
        this.orderInfo = orderInfo;
        this.redirectUrl = redirectUrl;
        this.extraData = extraData;
        this.lang = lang;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
