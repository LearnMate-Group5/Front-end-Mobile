package com.example.LearnMate.reader;

public class Chapter {
    public final String title;
    public final String summary;   // 1-2 câu đầu show ở list
    public final int index;        // thứ tự chapter trong danh sách
    public final int startOffset;  // offset ký tự bắt đầu trong toàn văn
    public final int endOffset;    // offset ký tự kết thúc trong toàn văn

    public Chapter(String title, String summary, int index, int startOffset, int endOffset) {
        this.title = title;
        this.summary = summary;
        this.index = index;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }
}
