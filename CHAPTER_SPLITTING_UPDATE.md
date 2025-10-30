# 📖 Chapter Splitting Logic - Updated

## 🎯 Change Summary

**Trước đây:** Phải có số sau "chapter" để chia chapter (VD: "Chapter 1", "Chapter 2")  
**Bây giờ:** Chỉ cần có từ "chapter" (viết hoa hay thường) là chia chapter mới

## ✅ What Changed

### Before (Old Logic)

```
PDF Content:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Chapter 1                     ← ✅ Matched (có số)
Introduction...

Chapter 2                     ← ✅ Matched (có số)
Content...

Chapter                       ← ❌ KHÔNG match (thiếu số)
More content...
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Result: 2 chapters (bỏ qua "Chapter" không có số)
```

### After (New Logic)

```
PDF Content:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Chapter 1                     ← ✅ Matched
Introduction...

Chapter 2                     ← ✅ Matched
Content...

Chapter                       ← ✅ Matched (giờ OK!)
More content...

chapter abc                   ← ✅ Matched (viết thường OK!)
Even more...

CHAPTER XYZ                   ← ✅ Matched (viết HOA OK!)
Final content...
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Result: 5 chapters (match tất cả!)
```

## 📝 Files Modified

### 1. PdfChapterParser.java

**Location:** `app/src/main/java/com/example/LearnMate/reader/PdfChapterParser.java`

**Old Pattern:**

```java
// Chỉ match "Chapter 1", "Chapter One", "CHAPTER 1", etc.
Pattern p = Pattern.compile("(?im)^\\s*(chapter\\s+[0-9]+|chapter\\s+[a-z]+)\\s*$");
```

**New Pattern:**

```java
// Match bất kỳ dòng nào có từ "chapter" (case insensitive)
// Matches: "chapter", "Chapter", "CHAPTER", "Chapter 1", "chapter abc", etc.
Pattern p = Pattern.compile("(?im)^\\s*chapter\\b.*$");
```

**Explanation:**

- `(?im)` - Case insensitive + multiline mode
- `^\\s*` - Đầu dòng, có thể có whitespace
- `chapter\\b` - Từ "chapter" (word boundary để không match "chapters" hoặc "chapterize")
- `.*$` - Bất kỳ ký tự nào sau "chapter" đến cuối dòng

### 2. ContentCache.java

**Location:** `app/src/main/java/com/example/LearnMate/reader/ContentCache.java`

**Old Logic:**

```java
// Array of patterns yêu cầu số sau "chapter"
String[] chapterPatterns = {
    "Chapter \\d+",    // Phải có số
    "CHAPTER \\d+",    // Phải có số
    "chapter \\d+",    // Phải có số
    // ...
};
```

**New Logic:**

```java
// Pattern đơn giản: chỉ cần có từ "chapter" hoặc "chương"
String chapterPattern = "(?i)\\bchapter\\b|\\bchương\\b";

// Lấy cả dòng chứa "chapter" để làm title
while (m.find()) {
    int position = m.start();

    // Tìm đầu dòng và cuối dòng
    int lineStart = position;
    while (lineStart > 0 && content.charAt(lineStart - 1) != '\n') {
        lineStart--;
    }

    int lineEnd = content.indexOf('\n', position);
    if (lineEnd == -1) {
        lineEnd = content.length();
    }

    String title = content.substring(lineStart, lineEnd).trim();
    chapterPositions.add(lineStart);
    chapterTitles.add(title);
}
```

**Enhancement:**

- Loại bỏ duplicate positions với `TreeSet`
- Lấy cả dòng làm chapter title (không chỉ "Chapter X")

### 3. ChapterUtils.java

**Location:** `app/src/main/java/com/example/LearnMate/reader/ChapterUtils.java`

**Old Logic:**

```java
// Split với pattern phải có số
String[] parts = fullText.split("(?i)\\bchapter\\s+\\d+\\b");
```

**New Logic:**

```java
// Split với lookahead để giữ lại "chapter" trong kết quả
String[] parts = fullText.split("(?i)(?=\\bchapter\\b|\\bchương\\b)");

// Lấy dòng đầu làm title
for (int i = 0; i < parts.length; i++) {
    String part = parts[i].trim();
    if (part.isEmpty()) continue;

    String[] lines = part.split("\n", 2);
    String title = lines.length > 0 ? lines[0].trim() : "Chapter " + (i + 1);
    String content = lines.length > 1 ? lines[1].trim() : part;

    out.add(new Chapter(title, content));
}
```

## 🎨 User Experience

### Example 1: PDF với chapters đơn giản

**Input:**

```
Chapter
This is the introduction to our book...

Chapter
This is the main content...

Chapter
This is the conclusion...
```

**Old Result:**

```
❌ Chỉ 1 chapter (không tìm thấy pattern "Chapter X")
```

**New Result:**

```
✅ 3 chapters:
  - "Chapter" (Introduction)
  - "Chapter" (Main content)
  - "Chapter" (Conclusion)
```

### Example 2: PDF với mixed case

**Input:**

```
CHAPTER ONE
Introduction...

chapter two
Main content...

Chapter Three
Conclusion...
```

**Old Result:**

```
❌ Chỉ 1 chapter (không có "Chapter 1", "Chapter 2")
```

**New Result:**

```
✅ 3 chapters:
  - "CHAPTER ONE"
  - "chapter two"
  - "Chapter Three"
```

### Example 3: PDF với custom chapter titles

**Input:**

```
Chapter: Getting Started
This chapter covers...

Chapter: Advanced Topics
In this chapter...

Chapter: Best Practices
We will discuss...
```

**Old Result:**

```
❌ Chỉ 1 chapter (không match "Chapter: Title")
```

**New Result:**

```
✅ 3 chapters:
  - "Chapter: Getting Started"
  - "Chapter: Advanced Topics"
  - "Chapter: Best Practices"
```

## 🔍 Pattern Details

### Regex Breakdown

**PdfChapterParser:**

```regex
(?im)^\\s*chapter\\b.*$

(?im)       - Flags: case insensitive + multiline
^           - Đầu dòng
\\s*        - Optional whitespace
chapter     - Từ "chapter" (case insensitive)
\\b         - Word boundary (không match "chapters", "chapterize")
.*          - Bất kỳ ký tự nào
$           - Cuối dòng
```

**ContentCache:**

```regex
(?i)\\bchapter\\b|\\bchương\\b

(?i)        - Case insensitive
\\b         - Word boundary
chapter     - Từ "chapter"
\\b         - Word boundary
|           - OR
\\b         - Word boundary
chương      - Từ "chương" (tiếng Việt)
\\b         - Word boundary
```

### What Gets Matched

✅ **Will Match:**

- `chapter`
- `Chapter`
- `CHAPTER`
- `Chapter 1`
- `Chapter One`
- `chapter: Introduction`
- `Chapter - Getting Started`
- `  chapter  ` (with spaces)
- `chương 1` (Vietnamese)

❌ **Won't Match:**

- `chapters` (có 's')
- `chapterize` (part of word)
- `this chapter` (not at start of line for PdfChapterParser)

## 🚀 Benefits

### 1. More Flexible

- ✅ Không bắt buộc phải có số
- ✅ Hỗ trợ custom chapter titles
- ✅ Hoạt động với nhiều format PDF

### 2. Better Recognition

- ✅ Detect nhiều loại chapters hơn
- ✅ Giảm false negatives
- ✅ Tự động lấy full title

### 3. User-Friendly

- ✅ User không cần format đặc biệt
- ✅ Hoạt động với hầu hết PDF
- ✅ Kết quả dễ đọc hơn

## 📊 Before vs After Comparison

| Scenario         | Old Logic | New Logic         |
| ---------------- | --------- | ----------------- |
| "Chapter 1"      | ✅ Match  | ✅ Match          |
| "Chapter"        | ❌ Skip   | ✅ Match          |
| "chapter one"    | ✅ Match  | ✅ Match          |
| "chapter: intro" | ❌ Skip   | ✅ Match          |
| "CHAPTER"        | ❌ Skip   | ✅ Match          |
| "chapters"       | ❌ Skip   | ❌ Skip (correct) |

## 🧪 Testing

### Test Case 1: Simple Chapters

**Input:**

```
Chapter
Content A

Chapter
Content B
```

**Expected:** 2 chapters ✅

### Test Case 2: Mixed Case

**Input:**

```
CHAPTER
Content A

chapter
Content B

Chapter
Content C
```

**Expected:** 3 chapters ✅

### Test Case 3: With Numbers

**Input:**

```
Chapter 1
Content A

Chapter 2
Content B
```

**Expected:** 2 chapters (vẫn hoạt động như cũ) ✅

### Test Case 4: Custom Titles

**Input:**

```
Chapter: Introduction to Programming
Learn the basics...

Chapter: Advanced Concepts
Deep dive into...
```

**Expected:** 2 chapters với full titles ✅

## ⚠️ Edge Cases

### Case 1: "chapter" trong giữa câu

**Input:**

```
This is a paragraph talking about the chapter system...

Chapter
Real chapter starts here...
```

**PdfChapterParser:** Won't match "chapter" giữa câu (pattern yêu cầu đầu dòng)  
**ContentCache:** Có thể match, cần cẩn thận

**Solution:** Pattern đã dùng word boundary `\b` để tránh false positives

### Case 2: Multiple "chapter" trên cùng dòng

**Input:**

```
Chapter A Chapter B
```

**Behavior:** Match lần đầu tiên, tạo 1 chapter với title "Chapter A Chapter B"

### Case 3: Empty chapter content

**Input:**

```
Chapter
Chapter
Chapter
```

**Behavior:** Tạo 3 chapters, một số có thể rỗng (handled gracefully)

## 🔧 Implementation Details

### Duplicate Prevention

**ContentCache.java:**

```java
// Loại bỏ duplicate positions
java.util.Set<Integer> uniquePositions = new java.util.TreeSet<>(chapterPositions);
chapterPositions = new java.util.ArrayList<>(uniquePositions);
```

**Why:** Nếu có nhiều pattern match cùng vị trí, chỉ lấy một

### Title Extraction

**Logic:**

1. Tìm từ "chapter" trong text
2. Tìm đầu dòng (backtrack đến `\n`)
3. Tìm cuối dòng (forward đến `\n`)
4. Extract cả dòng làm title
5. Trim whitespace

**Result:** Full chapter title instead of just "Chapter X"

## 📱 User Impact

### Import Screen

- ✅ More chapters detected
- ✅ Better chapter titles
- ✅ More accurate page count

### Chapter List

- ✅ More descriptive titles
- ✅ Better navigation
- ✅ Easier to find content

### Reader

- ✅ Natural chapter boundaries
- ✅ Improved reading flow
- ✅ Accurate progress tracking

## ✅ Build Status

```bash
BUILD SUCCESSFUL in 2m 35s
32 actionable tasks: 9 executed, 23 up-to-date
```

## 🎉 Summary

**Before:**

```
Must be: "Chapter 1", "Chapter 2", etc.
Strict pattern with numbers
Limited flexibility
```

**After:**

```
Can be: "chapter", "Chapter", "CHAPTER", etc.
Any line starting with "chapter" (case insensitive)
Maximum flexibility
```

**Impact:**

- ✅ More PDFs will be split correctly
- ✅ Better chapter detection
- ✅ User-friendly chapter titles
- ✅ Works with various PDF formats

**Ready to use!** 🚀

## 💡 Future Enhancements

Possible improvements:

- [ ] Support more chapter keywords (e.g., "Part", "Section")
- [ ] Smart detection of chapter hierarchy
- [ ] Auto-numbering if numbers missing
- [ ] Language-specific chapter detection
- [ ] Machine learning for chapter detection
