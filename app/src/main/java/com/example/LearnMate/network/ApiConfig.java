package com.example.LearnMate.network;

/**
 * Cấu hình tập trung cho tất cả API Base URLs
 * 
 * ⚠️ QUAN TRỌNG: Khi deploy lên AWS, chỉ cần thay đổi BASE_URL ở đây
 * Tất cả các Retrofit client sẽ tự động sử dụng URL mới
 * 
 * Lưu ý:
 * - URL phải kết thúc bằng dấu "/"
 * - Đảm bảo URL có protocol (http:// hoặc https://)
 * - Khi test trên Android emulator, có thể dùng 10.0.2.2 thay vì localhost
 */
public final class ApiConfig {
    private ApiConfig() {
        
    }


    public static final String BASE_URL = "http://chooy-alb-158419892.us-east-1.elb.amazonaws.com/";


    public static final String AI_CHAT_BASE_URL = BASE_URL; 


    public static final String AI_TRANSLATE_BASE_URL = BASE_URL; 


}

























