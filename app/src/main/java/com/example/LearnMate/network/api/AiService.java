// com/example/LearnMate/network/api/AiService.java
package com.example.LearnMate.network.api;

import com.example.LearnMate.network.dto.AiFileListResponse;
import com.example.LearnMate.network.dto.AiFileResponse;
import com.example.LearnMate.network.dto.ChatSessionItemResponse;
import com.example.LearnMate.network.dto.ChatSessionDetailResponse;
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

import java.util.List;

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
     * API có thể trả về: wrapper object {success, files[]}, array trực tiếp [], hoặc single object {}
     * 
     * @return Danh sách files
     */
    @GET("api/Ai/file")
    Call<List<AiFileResponse>> getFiles();

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
     * API trả về array trực tiếp: [{sessionId, userId, title, createdDate, lastActivityDate, messageCount}, ...]
     * 
     * @return Danh sách sessions (array trực tiếp)
     */
    @GET("api/Ai/session")
    Call<List<ChatSessionItemResponse>> getSessions();

    /**
     * Lấy chi tiết chat session
     * API trả về: {sessionId, userId, messages: [{id, sessionId, message: "JSON string"}, ...]}
     * message field là JSON string: {"type": "human/ai", "content": "..."}
     * 
     * @param sessionId - ID của session
     * @return Chi tiết session với messages
     */
    @GET("api/Ai/session/{sessionId}")
    Call<ChatSessionDetailResponse> getSession(@Path("sessionId") String sessionId);
}
