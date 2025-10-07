package com.example.LearnMate.model;

public class Book {
    private final String id;
    private final String title;
    private final String category;
    private final float rating;
    private final String coverUrl; // để sau bạn load ảnh URL; tạm thời có thể để rỗng

    public Book(String id, String title, String category, float rating, String coverUrl) {
        this.id = id; this.title = title; this.category = category; this.rating = rating; this.coverUrl = coverUrl;
    }
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public float getRating() { return rating; }
    public String getCoverUrl() { return coverUrl; }
}
