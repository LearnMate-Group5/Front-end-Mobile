package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChaptersResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("jobId")
    public String jobId;

    @SerializedName("status")
    public String status; // "processing", "completed", "failed"

    @SerializedName("chapters")
    public List<ChapterResponse> chapters;

    @SerializedName("totalChapters")
    public int totalChapters;

    @SerializedName("processingProgress")
    public int processingProgress;
}

