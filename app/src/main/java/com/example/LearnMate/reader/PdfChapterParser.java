package com.example.LearnMate.reader;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Đọc PDF → trích text → tách Chapter theo pattern “Chapter X” */
public class PdfChapterParser {

    public static class Result {
        public final String fullText;
        public final List<Chapter> chapters;
        public Result(String fullText, List<Chapter> chapters) {
            this.fullText = fullText; this.chapters = chapters;
        }
    }

    public static Result parse(Context ctx, Uri uri) throws Exception {
        ContentResolver cr = ctx.getContentResolver();
        String text;
        try (InputStream is = cr.openInputStream(uri);
             PDDocument doc = PDDocument.load(is)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            text = stripper.getText(doc);
        }

        // Chuẩn hoá nhẹ
        String full = text.replace("\r", "").replaceAll("[ \\t]+", " ").trim();

        // Regex nhận “Chapter 1”, “CHAPTER 1”, “Chapter One”, …
        Pattern p = Pattern.compile("(?im)^\\s*(chapter\\s+[0-9]+|chapter\\s+[a-z]+)\\s*$");
        Matcher m = p.matcher(full);

        List<int[]> marks = new ArrayList<>();
        while (m.find()) {
            marks.add(new int[]{m.start(), m.end()});
        }

        List<Chapter> out = new ArrayList<>();
        if (marks.isEmpty()) {
            // Không tìm thấy tiêu đề Chapter → coi toàn bộ là 1 chương
            String summary = firstSentence(full);
            out.add(new Chapter("Chapter 1", summary, 0, 0, full.length()));
        } else {
            for (int i = 0; i < marks.size(); i++) {
                int start = marks.get(i)[0];
                int nextStart = (i == marks.size()-1) ? full.length() : marks.get(i+1)[0];
                String title = full.substring(marks.get(i)[0], marks.get(i)[1]).trim();
                String body = full.substring(marks.get(i)[1], nextStart).trim();
                String summary = firstSentence(body);
                out.add(new Chapter(capFirst(title), summary, i, start, nextStart));
            }
        }
        return new Result(full, out);
    }

    private static String firstSentence(String s) {
        if (s == null) return "";
        s = s.trim();
        int cut = s.indexOf('.');
        if (cut < 0) cut = Math.min(160, s.length());
        else cut = Math.min(cut + 1, s.length());
        return s.substring(0, cut).trim();
    }

    private static String capFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }
}
