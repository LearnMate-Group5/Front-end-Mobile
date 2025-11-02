package com.example.LearnMate.payment;

/**
 * Constants cho PayOS integration
 */
public class PayOSConstants {
    
    // Request code cho PayOS payment
    public static final int REQUEST_CODE_PAYOS = 10002;
    
    // Payment status codes
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_CANCELLED = "cancelled";
    public static final String STATUS_FAILED = "failed";
    
    // Deep link schemes
    public static final String RETURN_URL_SCHEME = "learnmate://payment/return";
    public static final String CANCEL_URL_SCHEME = "learnmate://payment/cancel";
    
    private PayOSConstants() {
        // Utility class
    }
}

