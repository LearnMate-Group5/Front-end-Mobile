package com.example.LearnMate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LearnMate.network.dto.PaymentHistoryResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentHistoryAdapter extends RecyclerView.Adapter<PaymentHistoryAdapter.PaymentHistoryViewHolder> {

    private List<PaymentHistoryResponse.PaymentHistoryItem> paymentHistoryList;

    public PaymentHistoryAdapter(List<PaymentHistoryResponse.PaymentHistoryItem> paymentHistoryList) {
        this.paymentHistoryList = paymentHistoryList;
    }

    @NonNull
    @Override
    public PaymentHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_history, parent, false);
        return new PaymentHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentHistoryViewHolder holder, int position) {
        PaymentHistoryResponse.PaymentHistoryItem item = paymentHistoryList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return paymentHistoryList != null ? paymentHistoryList.size() : 0;
    }

    static class PaymentHistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAmount;
        private TextView tvOrderInfo;
        private TextView tvStatus;
        private TextView tvPaymentGateway;
        private TextView tvTransactionId;
        private TextView tvCreatedAt;
        private TextView tvMessage;

        public PaymentHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvOrderInfo = itemView.findViewById(R.id.tvOrderInfo);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPaymentGateway = itemView.findViewById(R.id.tvPaymentGateway);
            tvTransactionId = itemView.findViewById(R.id.tvTransactionId);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }

        public void bind(PaymentHistoryResponse.PaymentHistoryItem item) {
            // Format amount
            if (item.amount != null) {
                String formattedAmount = formatCurrency(item.amount);
                tvAmount.setText(formattedAmount);
            } else {
                tvAmount.setText("0 đ");
            }

            // Order info
            if (item.orderInfo != null && !item.orderInfo.isEmpty() && !item.orderInfo.equals("string")) {
                tvOrderInfo.setText(item.orderInfo);
                tvOrderInfo.setVisibility(View.VISIBLE);
            } else {
                tvOrderInfo.setText("Thanh toán gói dịch vụ");
                tvOrderInfo.setVisibility(View.VISIBLE);
            }

            // Status
            if (item.status != null) {
                tvStatus.setText(getStatusText(item.status));
                tvStatus.setBackgroundResource(getStatusBackground(item.status));
                tvStatus.setTextColor(itemView.getContext().getColor(getStatusTextColor(item.status)));
            } else {
                tvStatus.setText("Không xác định");
            }

            // Payment gateway
            if (item.paymentGateway != null && !item.paymentGateway.isEmpty()) {
                tvPaymentGateway.setText(item.paymentGateway);
            } else {
                tvPaymentGateway.setText("N/A");
            }

            // Transaction ID
            if (item.transactionId != null && !item.transactionId.isEmpty()) {
                tvTransactionId.setText(item.transactionId);
            } else {
                tvTransactionId.setText("N/A");
            }

            // Created date
            if (item.createdAt != null && !item.createdAt.isEmpty()) {
                String formattedDate = formatDate(item.createdAt);
                tvCreatedAt.setText(formattedDate);
            } else {
                tvCreatedAt.setText("N/A");
            }

            // Message
            if (item.message != null && !item.message.isEmpty()) {
                tvMessage.setText(item.message);
                tvMessage.setVisibility(View.VISIBLE);
            } else {
                tvMessage.setVisibility(View.GONE);
            }
        }

        private String formatCurrency(Long amount) {
            // Format: 1,800,000 đ
            return String.format(Locale.getDefault(), "%,d đ", amount);
        }

        private String formatDate(String isoDateString) {
            if (isoDateString == null || isoDateString.isEmpty()) {
                return "";
            }
            
            try {
                // Parse ISO 8601 format: "2025-11-07T15:54:20.988806Z"
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                if (isoDateString.contains("Z")) {
                    inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                    isoDateString = isoDateString.substring(0, isoDateString.indexOf("Z"));
                } else if (isoDateString.contains(".")) {
                    isoDateString = isoDateString.substring(0, isoDateString.indexOf("."));
                }
                
                Date date = inputFormat.parse(isoDateString);
                if (date != null) {
                    // Display format: dd/MM/yyyy HH:mm
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    return outputFormat.format(date);
                }
            } catch (Exception e) {
                android.util.Log.e("PaymentHistoryAdapter", "Error formatting date", e);
            }
            
            return isoDateString;
        }

        private String getStatusText(String status) {
            if (status == null) return "Không xác định";
            
            switch (status.toLowerCase()) {
                case "success":
                    return "Thành công";
                case "expired":
                    return "Hết hạn";
                case "pending":
                    return "Đang xử lý";
                case "failed":
                    return "Thất bại";
                case "cancelled":
                    return "Đã hủy";
                default:
                    return status;
            }
        }

        private int getStatusBackground(String status) {
            if (status == null) return R.drawable.bg_chip_dark;
            
            switch (status.toLowerCase()) {
                case "success":
                    return R.drawable.bg_chip_success;
                case "expired":
                case "failed":
                case "cancelled":
                    return R.drawable.bg_chip_error;
                case "pending":
                    return R.drawable.bg_chip_warning;
                default:
                    return R.drawable.bg_chip_dark;
            }
        }

        private int getStatusTextColor(String status) {
            if (status == null) return R.color.text_primary;
            
            switch (status.toLowerCase()) {
                case "success":
                    return R.color.success;
                case "expired":
                case "failed":
                case "cancelled":
                    return R.color.error;
                case "pending":
                    return R.color.warning;
                default:
                    return R.color.text_primary;
            }
        }
    }
}

