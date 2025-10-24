package com.example.LearnMate.network;

import com.google.gson.annotations.SerializedName;

public class OcrTranslateResponse {
    // nội dung OCR (raw)
    @SerializedName(value="markdown",      alternate={"doc","original","text","ocr_markdown","raw"})
    public String markdown;

    // nội dung đã dịch
    @SerializedName(value="translated",    alternate={"translated_text","vi","translation"})
    public String translated;

    @SerializedName(value="filename",      alternate={"fileName","name"})
    public String filename;
}
