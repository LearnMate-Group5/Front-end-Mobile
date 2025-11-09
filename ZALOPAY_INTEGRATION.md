# ZaloPay Payment Integration Summary

## Overview

Added ZaloPay payment method alongside MoMo payment, using the ZaloPay SDK (zpdk) and zpTransToken for payment processing. Both payment methods now redirect to a unified deep link: `learnmate://payment/success`.

## Changes Made

### 1. New DTO Classes

**Files Created:**

- `app/src/main/java/com/example/LearnMate/payment/dto/ZaloPayOrderRequest.java`
  - Fields: `orderId`, `description`, `redirectUrl`
- `app/src/main/java/com/example/LearnMate/payment/dto/ZaloPayOrderResponse.java`
  - Fields: `orderUrl`, `appTransId`, `zpTransToken`, `orderToken`, `qrCode`

### 2. API Service

**File Created:**

- `app/src/main/java/com/example/LearnMate/network/api/ZaloPayService.java`
  - Endpoint: `POST /api/Payment/zalopay/create`
  - Uses `ZaloPayOrderRequest` and returns `ZaloPayOrderResponse`

### 3. Retrofit Client Update

**File Modified:** `app/src/main/java/com/example/LearnMate/network/RetrofitClient.java`

- Added `ZaloPayService` import
- Added `cachedZaloPayService` field
- Added `getZaloPayService(Context)` method (with authentication)
- Added cache clearing for ZaloPayService

### 4. Payment Helper

**File Created:**

- `app/src/main/java/com/example/LearnMate/payment/ZaloPayPaymentHelper.java`
  - Initializes ZaloPay SDK with app ID 2554 in SANDBOX mode
  - Implements full payment flow:
    1. Choose subscription plan
    2. Create ZaloPay order
    3. Pay using ZaloPay SDK with `zpTransToken`
  - Handles SDK callbacks: success, failure, cancelled
  - Deep link handling for return from ZaloPay app
  - Return URL: `learnmate://payment/success`

### 5. MoMo Deep Link Update

**File Modified:** `app/src/main/java/com/example/LearnMate/payment/MoMoPaymentHelper.java`

- Changed `MOMO_RETURN_URL` from `learnmate://payment/momo/return` to `learnmate://payment/success`

### 6. Gradle Dependencies

**File Modified:** `app/build.gradle`

- Added ZaloPay SDK: `implementation files('../zpdk-release-v3.1.aar')`

### 7. AndroidManifest Update

**File Modified:** `app/src/main/AndroidManifest.xml`

- Updated `SubscriptionPaymentActivity` intent-filter
- Changed from specific path `/momo` to generic host `payment`
- Now handles all payment deep links: `learnmate://payment/*`

### 8. Payment Activity Updates

**File Modified:** `app/src/main/java/com/example/LearnMate/SubscriptionPaymentActivity.java`

- Added `ZaloPayPaymentHelper` instance
- Added `setupZaloPayListener()` method
- Renamed `btnPay` to `btnPayMoMo` and added `btnPayZaloPay`
- Updated `handleMoMoReturn()` to `handlePaymentReturn()` (handles both MoMo and ZaloPay)
- Updated loading states for both buttons
- Updated button text to "Thanh toán MoMo" and "Thanh toán ZaloPay"

### 9. Layout Update

**File Modified:** `app/src/main/res/layout/activity_subscription_payment.xml`

- Replaced single "Pay" button with two buttons:
  - `btnPayMoMo` - Pink/magenta color (#D82D8B)
  - `btnPayZaloPay` - Blue color (#0068FF)
- Added header text: "Chọn phương thức thanh toán"

## Payment Flow

### MoMo Payment Flow:

1. User clicks "Thanh toán MoMo"
2. Choose subscription plan → get `userSubscriptionId`
3. Create MoMo order → get deeplink
4. Open MoMo app via deeplink
5. User completes payment in MoMo app
6. Return to app via `learnmate://payment/success`
7. Handle success/failure

### ZaloPay Payment Flow:

1. User clicks "Thanh toán ZaloPay"
2. Choose subscription plan → get `userSubscriptionId`
3. Create ZaloPay order → get `zpTransToken`
4. Call ZaloPay SDK with `zpTransToken`
5. SDK handles payment (opens ZaloPay app or web)
6. SDK callback with success/failure/cancelled
7. Optional: Return via deep link `learnmate://payment/success`

## API Endpoints Used

### ZaloPay API

```
POST https://d2j7q4aa2pvgbz.cloudfront.net/api/Payment/zalopay/create
Headers: Authorization: Bearer <token>
Body: {
  "orderId": "uuid",
  "description": "string",
  "redirectUrl": "learnmate://payment/success"
}
Response: {
  "orderUrl": "string",
  "appTransId": "string",
  "zpTransToken": "string",
  "orderToken": "string",
  "qrCode": "string"
}
```

### MoMo API (unchanged)

```
POST https://d2j7q4aa2pvgbz.cloudfront.net/api/Payment/momo/create
Headers: Authorization: Bearer <token>
Body: {
  "orderId": "uuid",
  "orderInfo": "string",
  "redirectUrl": "learnmate://payment/success",
  "extraData": "string",
  "lang": "vi"
}
```

## Deep Link Schema

```
learnmate://payment/success?status=...&orderId=...&message=...&transactionId=...
```

## Testing Notes

- ZaloPay SDK is initialized in **SANDBOX** mode
- Change to `Environment.PRODUCTION` for production
- App ID: 2554
- Both payment methods now use the same success deep link
- Payment return can be handled via deep link or SDK callback

## Next Steps for Production

1. Change ZaloPay SDK environment from SANDBOX to PRODUCTION
2. Test both payment methods thoroughly
3. Handle edge cases (network errors, timeouts)
4. Add analytics tracking for payment events
5. Consider adding payment method icons to buttons
6. Add loading progress indicator during payment
