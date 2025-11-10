package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response model cho GET /api/Payment/history
 */
public class PaymentHistoryResponse {
    @SerializedName("items")
    public List<PaymentHistoryItem> items;
    
    @SerializedName("pageNumber")
    public Integer pageNumber;
    
    @SerializedName("pageSize")
    public Integer pageSize;
    
    @SerializedName("totalCount")
    public Integer totalCount;
    
    @SerializedName("totalPages")
    public Integer totalPages;
    
    @SerializedName("hasPreviousPage")
    public Boolean hasPreviousPage;
    
    @SerializedName("hasNextPage")
    public Boolean hasNextPage;
    
    /**
     * Item trong danh sách lịch sử thanh toán
     */
    public static class PaymentHistoryItem {
        @SerializedName("id")
        public String id;
        
        @SerializedName("orderId")
        public String orderId;
        
        @SerializedName("amount")
        public Long amount;
        
        @SerializedName("paymentGateway")
        public String paymentGateway;
        
        @SerializedName("status")
        public String status;
        
        @SerializedName("orderInfo")
        public String orderInfo;
        
        @SerializedName("message")
        public String message;
        
        @SerializedName("createdAt")
        public String createdAt;
        
        @SerializedName("updatedAt")
        public String updatedAt;
        
        @SerializedName("expiresAt")
        public String expiresAt;
        
        @SerializedName("transactionId")
        public String transactionId;
        
        @SerializedName("paymentUrl")
        public String paymentUrl;
    }
}

