package com.example.LearnMate.reader;

import android.net.Uri;

import java.util.List;

/** Bộ nhớ tạm cho nội dung đã parse/translate */
public final class ContentCache {

    /** Danh sách chương bản gốc (raw) và đã dịch (trans) */
    public static List<ChapterUtils.Chapter> RAW;
    public static List<ChapterUtils.Chapter> TRANS;

    /** Lưu lại Uri file PDF cuối cùng để ReaderActivity mở đúng */
    private static Uri lastPdfUri = Uri.EMPTY;

    private ContentCache() {}

    public static void setLastPdfUri(Uri uri) {
        lastPdfUri = (uri == null) ? Uri.EMPTY : uri;
    }

    public static Uri getLastPdfUri() {
        return lastPdfUri == null ? Uri.EMPTY : lastPdfUri;
    }
}
