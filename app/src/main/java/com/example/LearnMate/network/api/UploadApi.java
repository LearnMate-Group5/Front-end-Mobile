package com.example.LearnMate.network.api;

import com.example.LearnMate.network.OcrTranslateResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadApi {
    @Multipart
    @POST("upload-and-translate") // endpoint webhook của n8n
    Call<OcrTranslateResponse> uploadAndTranslate(
            @Part MultipartBody.Part File,
            @Part("userId") RequestBody userId
    );
}
