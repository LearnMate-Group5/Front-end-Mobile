package com.example.LearnMate.network.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface AiUploadService {
    @Multipart
    @POST("api/Ai/upload")
    Call<ResponseBody> uploadFile(
            @Part MultipartBody.Part file,
            @Part("UserId") RequestBody userId
    );
}
