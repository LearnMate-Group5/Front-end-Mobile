package com.example.LearnMate.payment;

import android.util.Log;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * Helper class để generate ZaloPay transaction token
 * Sử dụng cho demo khi chưa có backend API
 * 
 * Documentation: https://developers.zalopay.vn/v2/docs/
 */
public class ZaloPayTokenGenerator {
    
    private static final String TAG = "ZaloPayTokenGenerator";
    
    // ZaloPay Configuration - Demo Keys
    private static final String APP_ID = "554";
    private static final String KEY1 = "8NdU5pG5R2spGHGhyO99HN1OhD8IQJBn";
    private static final String KEY2 = "uUfsWgfLkRLzq6W2uNXTCxrfxs51auny";
    
    /**
     * Generate transaction token cho ZaloPay
     * 
     * @param amount Số tiền thanh toán (VND)
     * @param description Mô tả đơn hàng
     * @return ZaloPayTransactionToken chứa appTransID và zpTransToken
     */
    public static ZaloPayTransactionToken generateToken(long amount, String description) {
        try {
            // Tạo appTransID (unique transaction ID)
            String appTransID = generateAppTransID();
            
            // Tạo zpTransToken bằng HMAC SHA256
            String zpTransToken = generateZpTransToken(appTransID, amount, description);
            
            Log.d(TAG, "Generated token - appTransID: " + appTransID);
            Log.d(TAG, "Generated token - zpTransToken: " + zpTransToken);
            
            return new ZaloPayTransactionToken(appTransID, zpTransToken);
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating ZaloPay token", e);
            throw new RuntimeException("Failed to generate ZaloPay token", e);
        }
    }
    
    /**
     * Tạo appTransID theo format: YYMMDD_appid_random
     * Format: YYMMDD + "_" + appid + "_" + random (6 digits)
     */
    private static String generateAppTransID() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd", Locale.US);
        String dateStr = dateFormat.format(new Date());
        String random = String.format(Locale.US, "%06d", new Random().nextInt(1000000));
        return dateStr + "_" + APP_ID + "_" + random;
    }
    
    /**
     * Generate zpTransToken bằng HMAC SHA256
     * 
     * Format: appid + "|" + apptransid + "|" + appuser + "|" + amount + "|" + apptime + "|" + embeddata + "|" + item
     * Sau đó sign bằng HMAC SHA256 với KEY1
     */
    private static String generateZpTransToken(String appTransID, long amount, String description) {
        try {
            // Tạo các tham số
            String appUser = "LearnMateUser"; // User ID hoặc username
            long appTime = System.currentTimeMillis(); // Timestamp
            String embedData = "{}"; // JSON string, có thể để trống
            String item = "[]"; // JSON array của items, có thể để trống
            
            // Tạo data string để sign
            String data = APP_ID + "|" + appTransID + "|" + appUser + "|" + amount + "|" + appTime + "|" + embedData + "|" + item;
            
            Log.d(TAG, "Data to sign: " + data);
            
            // Sign bằng HMAC SHA256 với KEY1
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(KEY1.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating zpTransToken", e);
            throw new RuntimeException("Failed to generate zpTransToken", e);
        }
    }
    
    /**
     * Class để lưu transaction token
     */
    public static class ZaloPayTransactionToken {
        private String appTransID;
        private String zpTransToken;
        
        public ZaloPayTransactionToken(String appTransID, String zpTransToken) {
            this.appTransID = appTransID;
            this.zpTransToken = zpTransToken;
        }
        
        public String getAppTransID() {
            return appTransID;
        }
        
        public String getZpTransToken() {
            return zpTransToken;
        }
    }
}
