package com.example.LearnMate.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.AiService;
import com.example.LearnMate.network.dto.ChaptersResponse;
import com.example.LearnMate.reader.ContentCache;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChapterPollingService {
    private static final int POLLING_INTERVAL = 5000; // 5 giây
    private static final int MAX_ATTEMPTS = 60; // Tối đa 60 lần thử (5 phút)

    private final Context context;
    private final Handler handler;
    private int attemptCount = 0;

    public ChapterPollingService(Context context) {
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void startPolling(String jobId) {
        attemptCount = 0;
        pollChapters(jobId);
    }

    private void pollChapters(String jobId) {
        if (attemptCount >= MAX_ATTEMPTS) {
            Toast.makeText(context, "Timeout: Không thể lấy chapters sau " + MAX_ATTEMPTS + " lần thử",
                    Toast.LENGTH_LONG).show();
            return;
        }

        attemptCount++;

        // Log để debug
        android.util.Log.d("ChapterPolling",
                "Polling attempt " + attemptCount + "/" + MAX_ATTEMPTS + " for jobId: " + jobId);

        AiService service = RetrofitClient.get().create(AiService.class);
        service.getChapters(jobId).enqueue(new Callback<ChaptersResponse>() {
            @Override
            public void onResponse(Call<ChaptersResponse> call, Response<ChaptersResponse> response) {
                android.util.Log.d("ChapterPolling", "Response received: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ChaptersResponse chaptersResponse = response.body();
                    android.util.Log.d("ChapterPolling",
                            "Status: " + chaptersResponse.status + ", Chapters: " + chaptersResponse.totalChapters);

                    if ("completed".equals(chaptersResponse.status)) {
                        // Processing hoàn tất, lưu chapters và thay thế dữ liệu mẫu
                        ContentCache.setChaptersFromApi(chaptersResponse);
                        Toast.makeText(context, "✅ Đã lấy được " + chaptersResponse.totalChapters + " chapters từ API!",
                                Toast.LENGTH_SHORT).show();
                    } else if ("failed".equals(chaptersResponse.status)) {
                        Toast.makeText(context, "❌ Lỗi xử lý PDF: " + chaptersResponse.status, Toast.LENGTH_LONG)
                                .show();
                    } else {
                        // Vẫn đang processing, tiếp tục polling
                        android.util.Log.d("ChapterPolling",
                                "Still processing, retrying in " + POLLING_INTERVAL + "ms");
                        handler.postDelayed(() -> pollChapters(jobId), POLLING_INTERVAL);
                    }
                } else {
                    // Lỗi API, thử lại
                    android.util.Log.e("ChapterPolling", "API Error: " + response.code() + " - " + response.message());
                    handler.postDelayed(() -> pollChapters(jobId), POLLING_INTERVAL);
                }
            }

            @Override
            public void onFailure(Call<ChaptersResponse> call, Throwable t) {
                // Lỗi network, thử lại
                android.util.Log.e("ChapterPolling", "Network Error: " + t.getMessage());
                handler.postDelayed(() -> pollChapters(jobId), POLLING_INTERVAL);
            }
        });
    }
}
