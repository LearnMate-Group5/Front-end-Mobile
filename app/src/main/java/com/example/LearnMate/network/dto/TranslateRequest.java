package com.example.LearnMate.network.dto;

public class TranslateRequest {
    private String text;

    public TranslateRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
