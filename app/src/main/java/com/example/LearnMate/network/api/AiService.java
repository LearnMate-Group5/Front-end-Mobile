// com/example/LearnMate/network/api/AiService.java
package com.example.LearnMate.network.api;

import com.example.LearnMate.network.dto.ChaptersResponse;
import com.example.LearnMate.network.dto.UploadResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface AiService {
    /**
     * Upload PDF file để xử lý và dịch
     * 
     * @param File   - File PDF (multipart)
     * @param UserId - ID người dùng
     * @return UploadResponse với jobId để tracking
     */
    @Multipart
    @POST("api/Ai/upload")
    Call<UploadResponse> uploadPdf(
            @Part MultipartBody.Part File, // tên Part phải là "File"
            @Part("UserId") RequestBody UserId // và "UserId"
    );

    /**
     * Lấy kết quả xử lý và dịch theo jobId
     * 
     * @param jobId - ID công việc từ upload response
     * @return ChaptersResponse với chapters đã dịch
     */
    @GET("api/Ai/chapters/{jobId}")
    Call<ChaptersResponse> getChapters(@Path("jobId") String jobId);

    /**
     * Kiểm tra trạng thái xử lý
     * 
     * @param jobId - ID công việc
     * @return UploadResponse với status hiện tại
     */
    @GET("api/Ai/status/{jobId}")
    Call<UploadResponse> getStatus(@Path("jobId") String jobId);
}
