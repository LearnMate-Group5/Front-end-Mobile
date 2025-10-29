package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

public class ChapterResponse {
    @SerializedName("chapterNumber")
    public int chapterNumber;

    @SerializedName("title")
    public String title;

    @SerializedName("rawContent")
    public String rawContent;

    @SerializedName("translatedContent")
    public String translatedContent;

    @SerializedName("pageRange")
    public String pageRange;
}

