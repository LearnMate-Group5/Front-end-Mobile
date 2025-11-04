# 🐛 Debug Subscription Click Issue

## ✅ Đã fix:

1. ✅ Thêm logging vào SettingsActivity
2. ✅ Set click listener cho cả LinearLayout và CardView
3. ✅ Thêm `clickable`, `focusable`, `focusableInTouchMode` cho LinearLayout
4. ✅ Thêm logging vào SubscriptionActivity onCreate

## 🔍 Để debug:

### Bước 1: Xem Logcat

Khi click vào Subscription, bạn sẽ thấy logs:
```
D/SettingsActivity: btnSubscription: LinearLayout{...}
D/SettingsActivity: cardSubscription: MaterialCardView{...}
D/SettingsActivity: Set click listener for btnSubscription
D/SettingsActivity: Set click listener for cardSubscription
```

Khi click:
```
D/SettingsActivity: Subscription clicked!
D/SettingsActivity: Started SubscriptionActivity
D/SubscriptionActivity: onCreate called
D/SubscriptionActivity: Setup completed
```

### Bước 2: Kiểm tra lỗi

Nếu thấy:
- `btnSubscription is NULL!` → Layout chưa được build đúng
- `Error starting SubscriptionActivity` → Check AndroidManifest hoặc Activity class

### Bước 3: Clean & Rebuild

```bash
./gradlew clean
./gradlew assembleDebug
```

### Bước 4: Check trong code

Nếu vẫn không hoạt động, thử:

1. **Check xem view có được tìm thấy không:**
   - Xem logcat output khi mở SettingsActivity
   - Nếu NULL → Layout chưa được sync

2. **Test bằng Toast:**
   ```java
   Toast.makeText(this, "Subscription clicked", Toast.LENGTH_SHORT).show();
   ```

3. **Check AndroidManifest:**
   - Đảm bảo SubscriptionActivity đã được khai báo

4. **Sync project:**
   - File → Sync Project with Gradle Files
   - Build → Clean Project
   - Build → Rebuild Project

## 🎯 Quick Fix:

Nếu vẫn không hoạt động, hãy thử cách này trong SettingsActivity:

```java
// Đặt listener sau khi layout đã load xong
findViewById(R.id.btnSubscription).post(() -> {
    LinearLayout btnSubscription = findViewById(R.id.btnSubscription);
    if (btnSubscription != null) {
        btnSubscription.setOnClickListener(v -> {
            startActivity(new Intent(this, SubscriptionActivity.class));
        });
    }
});
```




