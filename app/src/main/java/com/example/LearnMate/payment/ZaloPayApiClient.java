package com.example.LearnMate.payment;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Client để gọi ZaloPay API tạo đơn hàng
 * Sử dụng để lấy zptranstoken từ ZaloPay server
 */
public class ZaloPayApiClient {
    
    private static final String TAG = "ZaloPayApiClient";
    
    // ZaloPay Configuration - Demo
    private static final String APP_ID = "554";
    private static final String KEY1 = "8NdU5pG5R2spGHGhyO99HN1OhD8IQJBn";
    
    // API Endpoints
    private static final String SANDBOX_CREATE_ORDER_URL = "https://sandbox.zalopay.com.vn/v001/tpe/createorder";
    private static final String PRODUCTION_CREATE_ORDER_URL = "https://zalopay.com.vn/v001/tpe/createorder";
    
    // Sử dụng SANDBOX cho demo
    private static final String CREATE_ORDER_URL = SANDBOX_CREATE_ORDER_URL;
    
    /**
     * Tạo đơn hàng trên ZaloPay server và lấy zptranstoken
     * 
     * @param amount Số tiền thanh toán (VND)
     * @param description Mô tả đơn hàng
     * @param appUser Thông tin người dùng
     * @return ZaloPayOrderResponse chứa zptranstoken và orderurl
     */
    public static ZaloPayOrderResponse createOrder(long amount, String description, String appUser) {
        try {
            Log.d(TAG, "=== Creating ZaloPay Order ===");
            Log.d(TAG, "Amount: " + amount);
            Log.d(TAG, "Description: " + description);
            
            // Tạo appTransID
            String appTransID = generateAppTransID();
            long appTime = System.currentTimeMillis();
            
            // Tạo embeddata và item
            String embedData = "{\"merchantinfo\":\"LearnMate Premium Subscription\"}";
            String item = "[{\"itemid\":\"premium\",\"itemname\":\"Premium Subscription\",\"itemprice\":" + amount + ",\"itemquantity\":1}]";
            
            // Tạo data string để sign
            String data = APP_ID + "|" + appTransID + "|" + appUser + "|" + amount + "|" + appTime + "|" + embedData + "|" + item;
            
            // Tạo MAC
            String mac = generateMAC(data);
            
            Log.d(TAG, "appTransID: " + appTransID);
            Log.d(TAG, "appTime: " + appTime);
            Log.d(TAG, "MAC: " + mac);
            
            // Tạo request parameters
            StringBuilder postData = new StringBuilder();
            postData.append("appid=").append(URLEncoder.encode(APP_ID, "UTF-8"));
            postData.append("&appuser=").append(URLEncoder.encode(appUser, "UTF-8"));
            postData.append("&apptime=").append(URLEncoder.encode(String.valueOf(appTime), "UTF-8"));
            postData.append("&amount=").append(URLEncoder.encode(String.valueOf(amount), "UTF-8"));
            postData.append("&apptransid=").append(URLEncoder.encode(appTransID, "UTF-8"));
            postData.append("&embeddata=").append(URLEncoder.encode(embedData, "UTF-8"));
            postData.append("&item=").append(URLEncoder.encode(item, "UTF-8"));
            postData.append("&description=").append(URLEncoder.encode(description, "UTF-8"));
            postData.append("&bankcode=").append(URLEncoder.encode("zalopayapp", "UTF-8"));
            postData.append("&mac=").append(URLEncoder.encode(mac, "UTF-8"));
            
            Log.d(TAG, "Sending request to: " + CREATE_ORDER_URL);
            
            // Gọi API
            URL url = new URL(CREATE_ORDER_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            
            // Set timeout để tránh bị đứng
            conn.setConnectTimeout(15000); // 15 seconds
            conn.setReadTimeout(15000); // 15 seconds
            
            // Gửi data
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Đọc response
            int responseCode = conn.getResponseCode();
            Log.d(TAG, "Response Code: " + responseCode);
            
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(responseCode == 200 ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            
            String responseBody = response.toString();
            Log.d(TAG, "Response Body: " + responseBody);
            
            if (responseBody == null || responseBody.isEmpty()) {
                Log.e(TAG, "✗ Empty response from server");
                return new ZaloPayOrderResponse(false, null, null, appTransID, -1, "Không nhận được phản hồi từ server ZaloPay");
            }
            
            // Parse JSON response
            JSONObject jsonResponse = new JSONObject(responseBody);
            int returnCode = jsonResponse.optInt("returncode", -1);
            String returnMessage = jsonResponse.optString("returnmessage", "");
            String zpTransToken = jsonResponse.optString("zptranstoken", "");
            String orderUrl = jsonResponse.optString("orderurl", "");
            
            Log.d(TAG, "returncode: " + returnCode);
            Log.d(TAG, "returnmessage: " + returnMessage);
            Log.d(TAG, "zptranstoken: " + (zpTransToken.isEmpty() ? "EMPTY" : zpTransToken.substring(0, Math.min(20, zpTransToken.length())) + "..."));
            
            if (returnCode == 1 && !zpTransToken.isEmpty()) {
                Log.d(TAG, "✓ Order created successfully");
                return new ZaloPayOrderResponse(true, zpTransToken, orderUrl, appTransID, returnCode, returnMessage);
            } else {
                String errorMsg = returnMessage.isEmpty() ? "Không thể tạo đơn hàng (returncode: " + returnCode + ")" : returnMessage;
                Log.e(TAG, "✗ Order creation failed: " + errorMsg);
                return new ZaloPayOrderResponse(false, null, null, appTransID, returnCode, errorMsg);
            }
            
        } catch (java.net.SocketTimeoutException e) {
            Log.e(TAG, "Timeout when creating order", e);
            return new ZaloPayOrderResponse(false, null, null, null, -1, 
                "Kết nối timeout. Vui lòng kiểm tra kết nối internet và thử lại.");
        } catch (java.net.UnknownHostException e) {
            Log.e(TAG, "Cannot connect to ZaloPay server", e);
            return new ZaloPayOrderResponse(false, null, null, null, -1, 
                "Không thể kết nối đến server ZaloPay. Vui lòng kiểm tra kết nối internet.");
        } catch (java.io.IOException e) {
            Log.e(TAG, "Network error when creating order", e);
            return new ZaloPayOrderResponse(false, null, null, null, -1, 
                "Lỗi kết nối mạng: " + e.getMessage());
        } catch (org.json.JSONException e) {
            Log.e(TAG, "Error parsing JSON response", e);
            return new ZaloPayOrderResponse(false, null, null, null, -1, 
                "Lỗi xử lý phản hồi từ server: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error creating ZaloPay order", e);
            e.printStackTrace();
            return new ZaloPayOrderResponse(false, null, null, null, -1, 
                "Lỗi không xác định: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
    
    /**
     * Tạo appTransID theo format: YYMMDD_appid_random
     */
    private static String generateAppTransID() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd", Locale.US);
        String dateStr = dateFormat.format(new Date());
        String random = String.format(Locale.US, "%06d", new Random().nextInt(1000000));
        return dateStr + "_" + APP_ID + "_" + random;
    }
    
    /**
     * Tạo MAC bằng HMAC SHA256
     */
    private static String generateMAC(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(KEY1.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
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
            Log.e(TAG, "Error generating MAC", e);
            throw new RuntimeException("Failed to generate MAC", e);
        }
    }
    
    /**
     * Response class cho create order API
     */
    public static class ZaloPayOrderResponse {
        private boolean success;
        private String zpTransToken;
        private String orderUrl;
        private String appTransID;
        private int returnCode;
        private String returnMessage;
        
        public ZaloPayOrderResponse(boolean success, String zpTransToken, String orderUrl, 
                                   String appTransID, int returnCode, String returnMessage) {
            this.success = success;
            this.zpTransToken = zpTransToken;
            this.orderUrl = orderUrl;
            this.appTransID = appTransID;
            this.returnCode = returnCode;
            this.returnMessage = returnMessage;
        }
        
        public boolean isSuccess() { return success; }
        public String getZpTransToken() { return zpTransToken; }
        public String getOrderUrl() { return orderUrl; }
        public String getAppTransID() { return appTransID; }
        public int getReturnCode() { return returnCode; }
        public String getReturnMessage() { return returnMessage; }
    }
}

