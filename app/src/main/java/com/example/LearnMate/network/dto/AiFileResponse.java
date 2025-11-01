package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response model cho AI File từ backend
 */
public class AiFileResponse {
    @SerializedName("fileId")
    public String fileId;

    @SerializedName("fileName")
    public String fileName;

    @SerializedName("uploadedAt")
    public String uploadedAt;

    @SerializedName("fileSize")
    public long fileSize;

    @SerializedName("ocrContent")
    public String ocrContent;

    @SerializedName("translatedContent")
    public String translatedContent;

    @SerializedName("currentContent")
    public String currentContent;

    @SerializedName("status")
    public String status; // "processing", "completed", "failed"

    @SerializedName("sessionId")
    public String sessionId;

    @SerializedName("category")
    public String category;

    @SerializedName("language")
    public String language;

    @SerializedName("totalPages")
    public int totalPages;
}

