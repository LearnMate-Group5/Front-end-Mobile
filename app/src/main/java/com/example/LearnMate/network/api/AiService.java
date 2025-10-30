// com/example/LearnMate/network/api/AiService.java
package com.example.LearnMate.network.api;

import com.example.LearnMate.network.dto.AiFileListResponse;
import com.example.LearnMate.network.dto.AiFileResponse;
import com.example.LearnMate.network.dto.ChatSessionListResponse;
import com.example.LearnMate.network.dto.ChatSessionResponse;
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

    /**
     * Lấy danh sách tất cả files đã upload
     * 
     * @return Danh sách files
     */
    @GET("api/Ai/file")
    Call<AiFileListResponse> getFiles();

    /**
     * Lấy thông tin chi tiết của 1 file
     * 
     * @param fileId - UUID của file
     * @return Chi tiết file
     */
    @GET("api/Ai/file/{fileId}")
    Call<AiFileResponse> getFile(@Path("fileId") String fileId);

    /**
     * Lấy danh sách chat sessions
     * 
     * @return Danh sách sessions
     */
    @GET("api/Ai/session")
    Call<ChatSessionListResponse> getSessions();

    /**
     * Lấy chi tiết chat session
     * 
     * @param sessionId - ID của session
     * @return Chi tiết session với messages
     */
    @GET("api/Ai/session/{sessionId}")
    Call<ChatSessionResponse> getSession(@Path("sessionId") String sessionId);
}
