package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response model cho Book từ API GET /api/Book
 */
public class BookResponse {
    @SerializedName("bookId")
    public String bookId;

    @SerializedName("title")
    public String title;

    @SerializedName("author")
    public String author;

    @SerializedName("description")
    public String description;

    @SerializedName("imageBase64")
    public String imageBase64;

    @SerializedName("categories")
    public List<String> categories;

    @SerializedName("likeCount")
    public int likeCount;

    @SerializedName("viewCount")
    public int viewCount;

    @SerializedName("isActive")
    public boolean isActive;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("updatedAt")
    public String updatedAt;

    @SerializedName("createdBy")
    public String createdBy;

    @SerializedName("updatedBy")
    public String updatedBy;
}

