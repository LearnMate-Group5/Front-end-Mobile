package com.example.LearnMate.model;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.SubscriptionService;
import com.example.LearnMate.network.dto.ChoosePlanResponse;
import com.example.LearnMate.network.dto.CurrentSubscriptionResponse;
import com.example.LearnMate.network.dto.SubscriptionPlanResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscriptionModel {
    private final SubscriptionService service;

    public SubscriptionModel(Context context) {
        this.service = RetrofitClient.getSubscriptionService(context);
    }

    /**
     * Load current subscription
     */
    public void loadCurrentSubscription(@NonNull SubscriptionCallback callback) {
        service.getCurrentSubscription().enqueue(new Callback<List<CurrentSubscriptionResponse>>() {
            @Override
            public void onResponse(Call<List<CurrentSubscriptionResponse>> call, Response<List<CurrentSubscriptionResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onCurrentSubscriptionLoaded(response.body().get(0));
                } else {
                    callback.onCurrentSubscriptionLoaded(null);
                }
            }

            @Override
            public void onFailure(Call<List<CurrentSubscriptionResponse>> call, Throwable t) {
                callback.onError("Error loading current subscription: " + (t.getMessage() != null ? t.getMessage() : "Unknown error"));
            }
        });
    }

    /**
     * Load subscription plans
     */
    public void loadPlans(@NonNull PlansCallback callback) {
        service.getPlans().enqueue(new Callback<List<SubscriptionPlanResponse>>() {
            @Override
            public void onResponse(Call<List<SubscriptionPlanResponse>> call, Response<List<SubscriptionPlanResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onPlansLoaded(response.body());
                } else {
                    callback.onError("Failed to load plans: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<SubscriptionPlanResponse>> call, Throwable t) {
                callback.onError("Error loading plans: " + (t.getMessage() != null ? t.getMessage() : "Unknown error"));
            }
        });
    }

    /**
     * Choose plan
     */
    public void choosePlan(@NonNull String subscriptionId, @NonNull ChoosePlanCallback callback) {
        service.choosePlan(subscriptionId).enqueue(new Callback<ChoosePlanResponse>() {
            @Override
            public void onResponse(Call<ChoosePlanResponse> call, Response<ChoosePlanResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to choose plan: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ChoosePlanResponse> call, Throwable t) {
                callback.onError("Error choosing plan: " + (t.getMessage() != null ? t.getMessage() : "Unknown error"));
            }
        });
    }

    /**
     * Cancel subscription
     */
    public void cancelSubscription(@NonNull CancelCallback callback) {
        service.cancelSubscription().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Failed to cancel subscription: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Error cancelling subscription: " + (t.getMessage() != null ? t.getMessage() : "Unknown error"));
            }
        });
    }

    /**
     * Build features string from plan type
     */
    public static String buildFeaturesFromType(String type) {
        if (type == null) return "";
        
        type = type.toLowerCase();
        if (type.contains("premium")) {
            return "• Unlimited PDF imports\n• Advanced AI features\n• Priority support\n• Cloud storage\n• Ad-free experience\n• Early access to new features";
        } else if (type.contains("standard")) {
            return "• 20 PDF imports per month\n• Standard AI features\n• Email support\n• Limited cloud storage";
        } else {
            return "• 5 PDF imports per month\n• Basic AI features\n• Limited storage";
        }
    }

    public interface SubscriptionCallback {
        void onCurrentSubscriptionLoaded(CurrentSubscriptionResponse subscription);
        void onError(String message);
    }

    public interface PlansCallback {
        void onPlansLoaded(List<SubscriptionPlanResponse> plans);
        void onError(String message);
    }

    public interface ChoosePlanCallback {
        void onSuccess(ChoosePlanResponse response);
        void onError(String message);
    }

    public interface CancelCallback {
        void onSuccess();
        void onError(String message);
    }
}

