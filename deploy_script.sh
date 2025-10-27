#!/bin/bash
echo "========================================"
echo "   LearnMate App Deployment Script"
echo "========================================"

echo ""
echo "[1/4] Cleaning previous builds..."
./gradlew clean

echo ""
echo "[2/4] Building debug APK..."
./gradlew assembleDebug

echo ""
echo "[3/4] Building release APK..."
./gradlew assembleRelease

echo ""
echo "[4/4] Building App Bundle for Play Store..."
./gradlew bundleRelease

echo ""
echo "========================================"
echo "   Build completed successfully!"
echo "========================================"
echo ""
echo "APK files location:"
echo "- Debug APK: app/build/outputs/apk/debug/app-debug.apk"
echo "- Release APK: app/build/outputs/apk/release/app-release.apk"
echo "- App Bundle: app/build/outputs/bundle/release/app-release.aab"
echo ""
echo "You can now:"
echo "1. Install debug APK on your device for testing"
echo "2. Upload release APK to Firebase App Distribution"
echo "3. Upload App Bundle to Google Play Store"
echo ""

