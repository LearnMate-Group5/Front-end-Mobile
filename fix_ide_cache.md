# Fix IDE Cache Issue

## 🔧 Solution for "Cannot resolve symbol 'AiHighlightService'"

Lỗi này thường do IDE (Android Studio) chưa sync lại project. Code đã compile thành công.

### ✅ Quick Fixes:

**1. Sync Project with Gradle Files**
- Click `File` → `Sync Project with Gradle Files`
- Hoặc click icon 🔄 trong toolbar

**2. Invalidate Caches / Restart**
- `File` → `Invalidate Caches...`
- Check all options:
  - ✅ Clear file system cache and Local History
  - ✅ Clear downloaded shared indexes
- Click `Invalidate and Restart`

**3. Rebuild Project**
- `Build` → `Rebuild Project`
- Hoặc `Build` → `Clean Project` → `Build` → `Rebuild Project`

**4. Check Build Variants**
- `Build` → `Select Build Variant`
- Ensure `debug` is selected

**5. Manual Refresh (If needed)**
- Right-click on `app` module → `Open Module Settings`
- Click `OK` to refresh

### 📝 Verification:

Code đã được verify:
- ✅ `AiHighlightService.java` exists at correct path
- ✅ Package name: `com.example.LearnMate.network.api`
- ✅ Import statement: `import com.example.LearnMate.network.api.AiHighlightService;`
- ✅ Gradle compile: **SUCCESS**

### 🚀 After Fixing:

Run:
```bash
.\gradlew.bat assembleDebug
```

Should build successfully!

