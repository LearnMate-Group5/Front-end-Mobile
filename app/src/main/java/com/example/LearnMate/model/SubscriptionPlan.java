package com.example.LearnMate.model;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Model class cho subscription plan
 */
public class SubscriptionPlan {
    private String name;
    private long price; // VND - giá sau discount
    private long originalPrice; // VND - giá gốc
    private int discount; // Phần trăm discount (0-100)
    private String subtitle;
    private String features;
    private String type; // FREE, PREMIUM, etc.
    private boolean isCurrentPlan;
    private String subscriptionId; // UUID của subscription plan
    
    public SubscriptionPlan(String name, long price, long originalPrice, int discount, String subtitle, String features, String type, boolean isCurrentPlan, String subscriptionId) {
        this.name = name;
        this.price = price;
        this.originalPrice = originalPrice;
        this.discount = discount;
        this.subtitle = subtitle;
        this.features = features;
        this.type = type;
        this.isCurrentPlan = isCurrentPlan;
        this.subscriptionId = subscriptionId;
    }
    
    public String getName() { return name; }
    public long getPrice() { return price; }
    public long getOriginalPrice() { return originalPrice; }
    public int getDiscount() { return discount; }
    public String getSubtitle() { return subtitle; }
    public String getFeatures() { return features; }
    public String getType() { return type; }
    public boolean isCurrentPlan() { return isCurrentPlan; }
    public String getSubscriptionId() { return subscriptionId; }
    
    public String getFormattedPrice() {
        if (price == 0) return isCurrentPlan ? "Active" : "Free";
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        return formatter.format(price) + " VND";
    }
    
    public String getFormattedOriginalPrice() {
        if (originalPrice == 0) return "";
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        return formatter.format(originalPrice) + " VND";
    }
    
    public String getFormattedDiscount() {
        if (discount <= 0) return "";
        return "-" + discount + "%";
    }
    
    public boolean hasDiscount() {
        return discount > 0 && originalPrice > 0;
    }
}

