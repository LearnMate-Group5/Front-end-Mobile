package com.example.LearnMate.payment.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Request DTO cho PayOS Create Payment Link API
 */
public class PayOSOrderRequest {
    
    @SerializedName("orderCode")
    private int orderCode; // Số nguyên dương, unique
    
    @SerializedName("amount")
    private long amount; // VND
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("buyerName")
    private String buyerName;
    
    @SerializedName("buyerEmail")
    private String buyerEmail;
    
    @SerializedName("buyerPhone")
    private String buyerPhone;
    
    @SerializedName("buyerAddress")
    private String buyerAddress;
    
    @SerializedName("items")
    private List<Item> items;
    
    @SerializedName("cancelUrl")
    private String cancelUrl;
    
    @SerializedName("returnUrl")
    private String returnUrl;
    
    @SerializedName("expiredAt")
    private Long expiredAt; // Unix timestamp (seconds)
    
    // Getters and Setters
    public int getOrderCode() {
        return orderCode;
    }
    
    public void setOrderCode(int orderCode) {
        this.orderCode = orderCode;
    }
    
    public long getAmount() {
        return amount;
    }
    
    public void setAmount(long amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getBuyerName() {
        return buyerName;
    }
    
    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }
    
    public String getBuyerEmail() {
        return buyerEmail;
    }
    
    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }
    
    public String getBuyerPhone() {
        return buyerPhone;
    }
    
    public void setBuyerPhone(String buyerPhone) {
        this.buyerPhone = buyerPhone;
    }
    
    public String getBuyerAddress() {
        return buyerAddress;
    }
    
    public void setBuyerAddress(String buyerAddress) {
        this.buyerAddress = buyerAddress;
    }
    
    public List<Item> getItems() {
        return items;
    }
    
    public void setItems(List<Item> items) {
        this.items = items;
    }
    
    public String getCancelUrl() {
        return cancelUrl;
    }
    
    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }
    
    public String getReturnUrl() {
        return returnUrl;
    }
    
    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }
    
    public Long getExpiredAt() {
        return expiredAt;
    }
    
    public void setExpiredAt(Long expiredAt) {
        this.expiredAt = expiredAt;
    }
    
    /**
     * Item model cho PayOS order
     */
    public static class Item {
        @SerializedName("name")
        private String name;
        
        @SerializedName("quantity")
        private int quantity;
        
        @SerializedName("price")
        private long price;
        
        public Item(String name, int quantity, long price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }
        
        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public long getPrice() { return price; }
    }
}

