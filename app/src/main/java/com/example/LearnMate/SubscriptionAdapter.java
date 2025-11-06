package com.example.LearnMate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.ViewHolder> {
    
    private List<SubscriptionActivity.SubscriptionPlan> plans;
    private OnPlanClickListener planClickListener;
    private OnCancelClickListener cancelClickListener;
    
    public interface OnPlanClickListener {
        void onPlanClick(SubscriptionActivity.SubscriptionPlan plan);
    }
    
    public interface OnCancelClickListener {
        void onCancelClick(SubscriptionActivity.SubscriptionPlan plan);
    }
    
    public SubscriptionAdapter(List<SubscriptionActivity.SubscriptionPlan> plans, 
                               OnPlanClickListener planListener,
                               OnCancelClickListener cancelListener) {
        this.plans = plans;
        this.planClickListener = planListener;
        this.cancelClickListener = cancelListener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_subscription_plan, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubscriptionActivity.SubscriptionPlan plan = plans.get(position);
        
        holder.tvPlanName.setText(plan.getName());
        holder.tvPlanPrice.setText(plan.getFormattedPrice());
        holder.tvPlanSubtitle.setText(plan.getSubtitle());
        holder.tvPlanFeatures.setText(plan.getFeatures());
        
        // Hiển thị originalPrice và discount nếu có
        if (plan.hasDiscount() && holder.tvOriginalPrice != null && holder.tvDiscount != null) {
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);
            holder.tvDiscount.setVisibility(View.VISIBLE);
            
            // Set originalPrice với gạch ngang (strikethrough)
            holder.tvOriginalPrice.setText(plan.getFormattedOriginalPrice());
            holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            
            // Set discount với màu đỏ
            holder.tvDiscount.setText(plan.getFormattedDiscount());
        } else {
            if (holder.tvOriginalPrice != null) {
                holder.tvOriginalPrice.setVisibility(View.GONE);
            }
            if (holder.tvDiscount != null) {
                holder.tvDiscount.setVisibility(View.GONE);
            }
        }
        
        boolean isCurrentPlan = plan.isCurrentPlan();
        
        // Hiển thị badge "Current Plan" nếu là current plan
        if (holder.tvCurrentBadge != null) {
            holder.tvCurrentBadge.setVisibility(isCurrentPlan ? View.VISIBLE : View.GONE);
        }
        
        // Setup Subscribe/Upgrade button
        if (holder.btnSubscribe != null) {
            if (isCurrentPlan) {
                // Ẩn Subscribe button cho current plan
                holder.btnSubscribe.setVisibility(View.GONE);
            } else {
                // Hiển thị Subscribe button cho các plans khác
                holder.btnSubscribe.setVisibility(View.VISIBLE);
                holder.btnSubscribe.setText("Subscribe");
                holder.btnSubscribe.setOnClickListener(v -> {
                    if (planClickListener != null) {
                        planClickListener.onPlanClick(plan);
                    }
                });
            }
        }
        
        // Setup Cancel button (chỉ hiển thị cho current plan)
        if (holder.btnCancel != null) {
            if (isCurrentPlan && plan.getSubscriptionId() != null) {
                // Hiển thị Cancel button cho current plan (nếu có subscriptionId)
                holder.btnCancel.setVisibility(View.VISIBLE);
                holder.btnCancel.setOnClickListener(v -> {
                    if (cancelClickListener != null) {
                        cancelClickListener.onCancelClick(plan);
                    }
                });
            } else {
                // Ẩn Cancel button cho các plans khác hoặc empty plan
                holder.btnCancel.setVisibility(View.GONE);
            }
        }
        
        // Highlight Premium plans
        if (plan.getPrice() > 0) {
            holder.cardView.setCardElevation(8);
        } else {
            holder.cardView.setCardElevation(2);
        }
    }
    
    @Override
    public int getItemCount() {
        return plans != null ? plans.size() : 0;
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        TextView tvPlanName;
        TextView tvPlanPrice;
        TextView tvOriginalPrice;
        TextView tvDiscount;
        TextView tvPlanSubtitle;
        TextView tvPlanFeatures;
        TextView tvCurrentBadge;
        MaterialButton btnSubscribe;
        MaterialButton btnCancel;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardPlan);
            tvPlanName = itemView.findViewById(R.id.tvPlanName);
            tvPlanPrice = itemView.findViewById(R.id.tvPlanPrice);
            tvOriginalPrice = itemView.findViewById(R.id.tvOriginalPrice);
            tvDiscount = itemView.findViewById(R.id.tvDiscount);
            tvPlanSubtitle = itemView.findViewById(R.id.tvPlanSubtitle);
            tvPlanFeatures = itemView.findViewById(R.id.tvPlanFeatures);
            tvCurrentBadge = itemView.findViewById(R.id.tvCurrentBadge);
            btnSubscribe = itemView.findViewById(R.id.btnSubscribe);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}
