package com.example.LearnMate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.PaymentService;
import com.example.LearnMate.network.dto.PaymentHistoryResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentHistoryActivity extends AppCompatActivity {

    private static final String TAG = "PaymentHistoryActivity";

    private ImageButton btnBack;
    private RecyclerView recyclerViewPaymentHistory;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    
    private PaymentService paymentService;
    private PaymentHistoryAdapter adapter;
    private List<PaymentHistoryResponse.PaymentHistoryItem> paymentHistoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history);

        paymentService = RetrofitClient.getPaymentService(this);
        paymentHistoryList = new ArrayList<>();

        // Bind views
        btnBack = findViewById(R.id.btnBack);
        recyclerViewPaymentHistory = findViewById(R.id.recyclerViewPaymentHistory);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);

        // Setup RecyclerView
        adapter = new PaymentHistoryAdapter(paymentHistoryList);
        recyclerViewPaymentHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPaymentHistory.setAdapter(adapter);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Load payment history
        loadPaymentHistory();
    }

    private void loadPaymentHistory() {
        showLoading();
        
        // Call API với pageNumber=1, pageSize=10 (mặc định)
        // status và paymentGateway là optional (null)
        paymentService.getPaymentHistory(1, 10, null, null)
                .enqueue(new Callback<PaymentHistoryResponse>() {
                    @Override
                    public void onResponse(Call<PaymentHistoryResponse> call, Response<PaymentHistoryResponse> response) {
                        hideLoading();
                        
                        if (response.isSuccessful() && response.body() != null) {
                            PaymentHistoryResponse historyResponse = response.body();
                            
                            if (historyResponse.items != null && !historyResponse.items.isEmpty()) {
                                paymentHistoryList.clear();
                                paymentHistoryList.addAll(historyResponse.items);
                                adapter.notifyDataSetChanged();
                                
                                showContent();
                                Log.d(TAG, "Loaded " + paymentHistoryList.size() + " payment history items");
                            } else {
                                showEmpty();
                                Log.d(TAG, "No payment history found");
                            }
                        } else {
                            showEmpty();
                            String errorMsg = "Không thể tải lịch sử thanh toán";
                            if (response.code() == 401) {
                                errorMsg = "Vui lòng đăng nhập lại";
                            }
                            Toast.makeText(PaymentHistoryActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Failed to load payment history: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<PaymentHistoryResponse> call, Throwable t) {
                        hideLoading();
                        showEmpty();
                        Toast.makeText(PaymentHistoryActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error loading payment history", t);
                    }
                });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewPaymentHistory.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showContent() {
        recyclerViewPaymentHistory.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void showEmpty() {
        recyclerViewPaymentHistory.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
    }
}

