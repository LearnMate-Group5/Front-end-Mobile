package com.example.LearnMate.view;

import com.example.LearnMate.model.SubscriptionPlan;

import java.util.List;

public interface SubscriptionView {
    void showLoading(String message);
    void hideLoading();
    void showPlans(List<SubscriptionPlan> plans);
    void showErrorMessage(String message);
    void showSuccessMessage(String message);
    void navigateToPayment(SubscriptionPlan selectedPlan, SubscriptionPlan currentPlan);
    void showCancelConfirmationDialog(Runnable onConfirm);
    void finish();
}

