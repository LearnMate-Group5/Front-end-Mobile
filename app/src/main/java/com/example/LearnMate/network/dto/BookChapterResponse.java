package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Response model cho Book Chapter từ API GET /api/Book/{bookId}/chapters
 */
public class BookChapterResponse {
    @SerializedName("chapterId")
    public String chapterId;

    @SerializedName("bookId")
    public String bookId;

    @SerializedName("pageIndex")
    public int pageIndex;

    @SerializedName("title")
    public String title;

    @SerializedName("content")
    public String content;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("updatedAt")
    public String updatedAt;

    @SerializedName("createdBy")
    public String createdBy;

    @SerializedName("updatedBy")
    public String updatedBy;
}

