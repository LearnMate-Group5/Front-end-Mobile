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
    private OnPlanClickListener listener;
    
    public interface OnPlanClickListener {
        void onPlanClick(SubscriptionActivity.SubscriptionPlan plan);
    }
    
    public SubscriptionAdapter(List<SubscriptionActivity.SubscriptionPlan> plans, OnPlanClickListener listener) {
        this.plans = plans;
        this.listener = listener;
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
        
        boolean isCurrentPlan = plan.isCurrentPlan();
        
        // Hiển thị badge "Current Plan" nếu là current plan
        if (holder.tvCurrentBadge != null) {
            holder.tvCurrentBadge.setVisibility(isCurrentPlan ? android.view.View.VISIBLE : android.view.View.GONE);
        }
        
        // Ẩn nút Subscribe cho Current Plan, hiển thị cho Premium
        if (holder.btnSubscribe != null) {
            if (isCurrentPlan) {
                holder.btnSubscribe.setVisibility(android.view.View.GONE);
            } else {
                holder.btnSubscribe.setVisibility(android.view.View.VISIBLE);
                holder.btnSubscribe.setText("Upgrade");
                holder.btnSubscribe.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onPlanClick(plan);
                    }
                });
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
        TextView tvPlanSubtitle;
        TextView tvPlanFeatures;
        TextView tvCurrentBadge;
        MaterialButton btnSubscribe;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardPlan);
            tvPlanName = itemView.findViewById(R.id.tvPlanName);
            tvPlanPrice = itemView.findViewById(R.id.tvPlanPrice);
            tvPlanSubtitle = itemView.findViewById(R.id.tvPlanSubtitle);
            tvPlanFeatures = itemView.findViewById(R.id.tvPlanFeatures);
            tvCurrentBadge = itemView.findViewById(R.id.tvCurrentBadge);
            btnSubscribe = itemView.findViewById(R.id.btnSubscribe);
        }
    }
}


