package com.example.LearnMate.presenter;

import android.util.Log;

import com.example.LearnMate.model.SubscriptionModel;
import com.example.LearnMate.model.SubscriptionPlan;
import com.example.LearnMate.network.dto.CurrentSubscriptionResponse;
import com.example.LearnMate.network.dto.SubscriptionPlanResponse;
import com.example.LearnMate.view.SubscriptionView;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionPresenter {
    private static final String TAG = "SubscriptionPresenter";

    private final SubscriptionView view;
    private final SubscriptionModel model;
    private CurrentSubscriptionResponse currentSubscription;
    private List<SubscriptionPlan> plansList = new ArrayList<>();

    public SubscriptionPresenter(SubscriptionView view, SubscriptionModel model) {
        this.view = view;
        this.model = model;
    }

    /**
     * Load subscription data (current subscription + plans)
     */
    public void loadSubscriptionData() {
        view.showLoading("Loading subscription...");
        
        // Load current subscription first
        model.loadCurrentSubscription(new SubscriptionModel.SubscriptionCallback() {
            @Override
            public void onCurrentSubscriptionLoaded(CurrentSubscriptionResponse subscription) {
                currentSubscription = subscription;
                
                // Then load plans
                loadPlans();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, message);
                currentSubscription = null;
                // Still load plans even if current subscription fails
                loadPlans();
            }
        });
    }

    /**
     * Load subscription plans
     */
    private void loadPlans() {
        view.showLoading("Loading plans...");
        
        model.loadPlans(new SubscriptionModel.PlansCallback() {
            @Override
            public void onPlansLoaded(List<SubscriptionPlanResponse> apiPlans) {
                view.hideLoading();
                
                plansList.clear();
                
                // Add Current Plan (if exists) or "Current Plan is Empty"
                if (currentSubscription != null) {
                    SubscriptionPlan currentPlan = new SubscriptionPlan(
                        currentSubscription.name,
                        currentSubscription.getFinalPrice(),
                        currentSubscription.originalPrice,
                        currentSubscription.discount,
                        "Status: " + currentSubscription.status,
                        SubscriptionModel.buildFeaturesFromType(currentSubscription.type),
                        currentSubscription.type,
                        true,
                        currentSubscription.subscriptionId
                    );
                    plansList.add(currentPlan);
                } else {
                    // Show "Current Plan is Empty"
                    SubscriptionPlan emptyPlan = new SubscriptionPlan(
                        "Current Plan",
                        0,
                        0,
                        0,
                        "Current Plan is Empty",
                        "• No active subscription\n• Choose a plan below to get started",
                        "FREE",
                        true,
                        null
                    );
                    plansList.add(emptyPlan);
                }
                
                // Add plans from API
                for (SubscriptionPlanResponse apiPlan : apiPlans) {
                    // Only add if not current plan
                    if (currentSubscription == null || !apiPlan.subscriptionId.equals(currentSubscription.subscriptionId)) {
                        SubscriptionPlan plan = new SubscriptionPlan(
                            apiPlan.name,
                            apiPlan.getFinalPrice(),
                            apiPlan.originalPrice,
                            apiPlan.discount,
                            "Type: " + apiPlan.type,
                            SubscriptionModel.buildFeaturesFromType(apiPlan.type),
                            apiPlan.type,
                            false,
                            apiPlan.subscriptionId
                        );
                        plansList.add(plan);
                    }
                }
                
                view.showPlans(plansList);
            }

            @Override
            public void onError(String message) {
                view.hideLoading();
                Log.e(TAG, message);
                view.showErrorMessage(message);
            }
        });
    }

    /**
     * Handle plan selection
     */
    public void onPlanSelected(SubscriptionPlan plan) {
        // If it's current plan, do nothing
        if (plan.isCurrentPlan()) {
            Log.d(TAG, "Plan is current plan, skipping");
            return;
        }
        
        if (plan.getSubscriptionId() == null) {
            view.showErrorMessage("Invalid plan");
            return;
        }
        
        Log.d(TAG, "=== Plan selected ===");
        Log.d(TAG, "Plan: " + plan.getName());
        Log.d(TAG, "SubscriptionId: " + plan.getSubscriptionId());
        
        // Find current plan
        SubscriptionPlan currentPlan = null;
        for (SubscriptionPlan p : plansList) {
            if (p.isCurrentPlan()) {
                currentPlan = p;
                break;
            }
        }
        
        // If no current plan found, create a default Free plan
        if (currentPlan == null) {
            currentPlan = new SubscriptionPlan(
                "Free", 0, 0, 0,
                "", "", "FREE", true, null
            );
        }
        
        // Navigate to payment
        view.navigateToPayment(plan, currentPlan);
    }

    /**
     * Handle cancel subscription
     */
    public void onCancelClicked(SubscriptionPlan plan) {
        if (!plan.isCurrentPlan() || plan.getSubscriptionId() == null) {
            return;
        }
        
        // Show confirmation dialog
        view.showCancelConfirmationDialog(() -> {
            cancelSubscription();
        });
    }

    /**
     * Cancel subscription
     */
    private void cancelSubscription() {
        view.showLoading("Cancelling subscription...");
        
        model.cancelSubscription(new SubscriptionModel.CancelCallback() {
            @Override
            public void onSuccess() {
                view.hideLoading();
                Log.d(TAG, "Subscription cancelled successfully");
                view.showSuccessMessage("Subscription cancelled successfully");
                
                // Reload subscription data
                loadSubscriptionData();
            }

            @Override
            public void onError(String message) {
                view.hideLoading();
                Log.e(TAG, message);
                view.showErrorMessage(message);
            }
        });
    }

    /**
     * Choose plan (called after payment)
     */
    public void choosePlan(String subscriptionId) {
        view.showLoading("Subscribing to plan...");
        
        model.choosePlan(subscriptionId, new SubscriptionModel.ChoosePlanCallback() {
            @Override
            public void onSuccess(com.example.LearnMate.network.dto.ChoosePlanResponse response) {
                view.hideLoading();
                Log.d(TAG, "Plan chosen successfully: " + response.name);
                view.showSuccessMessage("Successfully subscribed to " + response.name + "!");
                
                // Reload subscription data
                loadSubscriptionData();
            }

            @Override
            public void onError(String message) {
                view.hideLoading();
                Log.e(TAG, message);
                view.showErrorMessage(message);
            }
        });
    }
}

