package com.example.LearnMate.payment.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO từ PayOS Create Payment Link API
 */
public class PayOSOrderResponse {
    
    @SerializedName("code")
    private String code; // "00" = Success
    
    @SerializedName("desc")
    private String desc; // Description
    
    @SerializedName("data")
    private PaymentData data;
    
    // Getters and Setters
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    public PaymentData getData() {
        return data;
    }
    
    public void setData(PaymentData data) {
        this.data = data;
    }
    
    public boolean isSuccess() {
        return "00".equals(code);
    }
    
    /**
     * Payment Data model
     */
    public static class PaymentData {
        @SerializedName("accountNumber")
        private String accountNumber;
        
        @SerializedName("accountName")
        private String accountName;
        
        @SerializedName("amount")
        private long amount;
        
        @SerializedName("description")
        private String description;
        
        @SerializedName("orderCode")
        private int orderCode;
        
        @SerializedName("paymentLinkId")
        private String paymentLinkId;
        
        @SerializedName("qrCode")
        private String qrCode;
        
        @SerializedName("checkoutUrl")
        private String checkoutUrl;
        
        // Getters and Setters
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        
        public String getAccountName() { return accountName; }
        public void setAccountName(String accountName) { this.accountName = accountName; }
        
        public long getAmount() { return amount; }
        public void setAmount(long amount) { this.amount = amount; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public int getOrderCode() { return orderCode; }
        public void setOrderCode(int orderCode) { this.orderCode = orderCode; }
        
        public String getPaymentLinkId() { return paymentLinkId; }
        public void setPaymentLinkId(String paymentLinkId) { this.paymentLinkId = paymentLinkId; }
        
        public String getQrCode() { return qrCode; }
        public void setQrCode(String qrCode) { this.qrCode = qrCode; }
        
        public String getCheckoutUrl() { return checkoutUrl; }
        public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }
    }
}




