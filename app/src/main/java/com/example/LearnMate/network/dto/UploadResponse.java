package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UploadResponse {
    @SerializedName("statusCode")
    public int statusCode;

    @SerializedName("success")
    public boolean success;

    @SerializedName("content")
    public String content; // Content là string, không phải array

    @SerializedName("message")
    public String message;

    @SerializedName("jobId")
    public String jobId;

    @SerializedName("fileName")
    public String fileName;

    @SerializedName("totalChapters")
    public int totalChapters;

    @SerializedName("status")
    public String status; // "processing", "completed", "failed"

    // Inner class cho content items
    public static class ContentItem {
        @SerializedName("trans")
        public String trans; // Nội dung đã dịch

        @SerializedName("markdown")
        public String markdown; // Nội dung gốc

        @SerializedName("metadata")
        public Metadata metadata;

        @SerializedName("pageContent")
        public String pageContent;
    }

    public static class Metadata {
        @SerializedName("source")
        public String source;

        @SerializedName("blobType")
        public String blobType;

        @SerializedName("loc")
        public Location loc;
    }

    public static class Location {
        @SerializedName("lines")
        public Lines lines;
    }

    public static class Lines {
        @SerializedName("from")
        public int from;

        @SerializedName("to")
        public int to;
    }
}
