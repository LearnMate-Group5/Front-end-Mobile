package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response cho danh sách files
 */
public class AiFileListResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("message")
    public String message;

    @SerializedName("files")
    public List<AiFileResponse> files;

    @SerializedName("totalCount")
    public int totalCount;
}
