# 🔍 AI Highlight Feature - User Guide

## 📖 Overview

Tính năng **AI Highlight** cho phép bạn highlight text trong PDF và nhận thông tin chi tiết về từ/cụm từ đó từ AI dictionary assistance.

## ✨ Features

### 1. **Text Selection & Highlighting**
- ✅ Select text bằng cách giữ và kéo
- ✅ Long press để tự động highlight và lấy thông tin
- ✅ Menu option "AI Highlight" trong action menu khi select text

### 2. **Smart Popup Display**
- ✅ Popup hiển thị ngay cạnh text được highlight
- ✅ Tự động điều chỉnh vị trí để không bị che text
- ✅ Scrollable content cho thông tin dài
- ✅ Loading indicator khi đang fetch data

### 3. **AI Dictionary Information**
- ✅ Definition và meaning
- ✅ Synonym và antonym
- ✅ Related information
- ✅ Example use cases
- ✅ Extra context information

## 🎯 How to Use

### Method 1: Long Press (Recommended)

1. **Đọc PDF** trong ReaderActivity
2. **Long press** vào từ hoặc cụm từ muốn highlight
3. **Popup tự động hiện** với loading indicator
4. **Xem thông tin** từ AI dictionary

```
📖 PDF Content
└── Long Press on "architecture"
    └── 🤖 AI Processing...
        └── 📋 Popup với thông tin:
            • Definition
            • Synonym/Antonym
            • Examples
            • Related info
```

### Method 2: Text Selection Menu

1. **Select text** bằng cách giữ và kéo
2. **Action menu** xuất hiện
3. **Tap "AI Highlight"** trong menu
4. **Popup hiển thị** thông tin

```
📖 PDF Content
└── Select "machine learning"
    └── Action Menu
        └── [Copy] [AI Highlight] ← Tap here
            └── 📋 Popup với thông tin
```

### Method 3: Menu Button

1. **Select text** trước
2. **Tap menu button** (3 dots) ở header
3. **Tap "Highlight"** trong menu
4. **Popup hiển thị** thông tin

```
📖 Header Menu
└── [📖] [⭐] [⋮] ← Tap here
    └── Menu Options
        └── [Translate] [Highlight] [Note]
                           ↑ Tap here
            └── 📋 Popup với thông tin
```

## 📱 UI Components

### Popup Layout

```
┌─────────────────────────────┐
│      architecture            │ ← Highlighted word
├─────────────────────────────┤
│  📊 Definition:              │
│  Architecture refers to...   │
│                              │
│  📝 Synonyms:                │
│  • Structure                 │
│  • Design                    │
│                              │
│  💡 Examples:                │
│  • Software architecture     │
│  • Building architecture    │
│                              │
│  📚 Related:                 │
│  • Framework                 │
│  • System design             │
└─────────────────────────────┘
         [✕] ← Close button
```

### States

**Loading State:**
```
┌─────────────────────────────┐
│      architecture            │
├─────────────────────────────┤
│        ⏳ Loading...         │
└─────────────────────────────┘
```

**Error State:**
```
┌─────────────────────────────┐
│      architecture            │
├─────────────────────────────┤
│  ❌ Network error.           │
│  Please check connection.    │
└─────────────────────────────┘
```

**Success State:**
```
┌─────────────────────────────┐
│      architecture            │
├─────────────────────────────┤
│  📋 [Scrollable Content]     │
│  Definition, synonyms,       │
│  examples, etc.              │
└─────────────────────────────┘
```

## 🔧 Technical Details

### API Integration

**Endpoint:** `POST /webhook/ai-highlight`

**Base URL:** `http://10.0.2.2:5678/` (n8n webhook)

**Request Format:**
```json
{
  "message": "architecture",
  "sessionId": "uuid-here",
  "userId": "user-id-here"
}
```

**Response Format:**
```json
[
  {
    "output": "Definition: Architecture refers to...\n\nSynonyms: Structure, Design..."
  }
]
```

### N8n Workflow

Workflow sử dụng:
- **Webhook Node:** Nhận request từ app
- **Set Fields Node:** Map request data
- **AI Agent Node:** Google Gemini với system prompt về dictionary
- **Postgres Memory:** Lưu conversation history
- **Pinecone Vector Store:** Semantic search trong knowledge base
- **Respond to Webhook:** Trả về response

### Code Architecture

**ReaderActivity.java:**
```java
// Setup text selection
setupTextSelection()
  ├── Enable text selection
  ├── Long press listener
  └── Action mode callback

// Highlight flow
highlightCurrentText()
  └── fetchHighlightInfo()
      ├── Show loading popup
      ├── Call API
      └── Show result popup

// Popup display
showHighlightPopup()
  ├── Calculate position
  ├── Handle screen bounds
  └── Show popup
```

### Files Structure

```
app/src/main/
├── java/com/example/LearnMate/
│   ├── reader/
│   │   └── ReaderActivity.java          ← Main logic
│   └── network/
│       ├── api/
│       │   └── AiHighlightService.java  ← API interface
│       └── dto/
│           ├── AiHighlightRequest.java  ← Request DTO
│           └── AiHighlightResponse.java ← Response DTO
└── res/
    └── layout/
        └── popup_highlight_info.xml      ← Popup UI
```

## 🎨 Popup Positioning Logic

### Smart Positioning Algorithm

1. **Calculate text position:**
   - Get line number for selected text
   - Calculate X center of selection
   - Get Y position of line bottom

2. **Initial position:**
   - `popupX = textXCenter - popupWidth/2`
   - `popupY = lineBottom + 16dp`

3. **Screen bounds check:**
   - If `popupX < 0` → Align to left margin
   - If `popupX + width > screenWidth` → Align to right margin
   - If `popupY + height > screenHeight` → Show above text

4. **Final position:**
   - Adjust to fit screen
   - Add margins for better UX

### Example Positions

**Text ở giữa màn hình:**
```
     ┌─────────────┐
     │   Text      │
     └─────────────┘
          ↓
  ┌───────────────────┐
  │  Popup Info       │
  └───────────────────┘
```

**Text ở cuối màn hình:**
```
          ┌─────────────┐
          │   Text      │
          └─────────────┘
     ┌───────────────────┐
     │  Popup Info       │ ← Shown above
     └───────────────────┘
```

**Text ở cạnh màn hình:**
```
┌─────────────┐
│ Text        │
└─────────────┘
┌───────────────────┐ ← Aligned to margin
│ Popup Info        │
└───────────────────┘
```

## 🚀 Features Breakdown

### 1. Long Press Auto-Highlight

**Implementation:**
```java
tvContent.setOnLongClickListener(v -> {
    String selectedText = getSelectedText();
    if (!selectedText.isEmpty()) {
        fetchHighlightInfo(selectedText);
        return true;
    }
    return false;
});
```

**User Experience:**
- ✅ Instant highlight without menu
- ✅ One-step action
- ✅ Natural gesture

### 2. Action Menu Integration

**Implementation:**
```java
tvContent.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        menu.add(0, android.R.id.copy, 0, "AI Highlight");
        return true;
    }
});
```

**User Experience:**
- ✅ Visible in standard text selection menu
- ✅ Consistent with Android UX
- ✅ Easy to discover

### 3. Improved Popup Positioning

**Features:**
- ✅ Position calculation based on text selection
- ✅ Automatic screen bounds checking
- ✅ Smart placement (above/below text)
- ✅ Margin adjustments

**Code:**
```java
// Calculate center of selected text
float xCenter = (xStart + xEnd) / 2;

// Position below text
int popupY = location[1] + lineBottom + 16dp;

// Check if fits on screen
if (popupY + maxHeight > screenHeight) {
    // Show above instead
    popupY = lineTop - maxHeight - 16dp;
}
```

### 4. Error Handling

**Network Errors:**
- ✅ Connection timeout
- ✅ Server errors (500+)
- ✅ Authentication errors (401)
- ✅ Generic network failures

**User Messages:**
```java
"Network error. Please check your connection."
"Request timeout. Please try again."
"Server error. Please try again later."
"Authentication required. Please login."
```

### 5. Response Parsing

**Flexible Parsing:**
```java
// Try to extract output
String info = response.getOutput();

// Fallback: Try direct string
if (info == null) {
    info = response.toString();
}

// Final fallback: Default message
if (info == null || info.isEmpty()) {
    info = "No information available for: " + selectedText;
}
```

## 📊 User Flow Diagram

```
User reads PDF
    │
    ├─→ Select text
    │   │
    │   ├─→ Method 1: Long press
    │   │   └─→ Auto highlight
    │   │
    │   ├─→ Method 2: Action menu
    │   │   └─→ Tap "AI Highlight"
    │   │
    │   └─→ Method 3: Header menu
    │       └─→ Tap "Highlight"
    │
    └─→ App sends request to n8n
        │
        ├─→ Loading popup appears
        │
        ├─→ API processes request
        │   ├─→ AI Agent analyzes
        │   ├─→ Vector store search
        │   └─→ Memory retrieval
        │
        └─→ Result popup appears
            │
            ├─→ Success: Show info
            └─→ Error: Show message
```

## 🎯 Best Practices

### For Users

1. **Select meaningful text:**
   - Full words or phrases
   - Context-aware selections
   - Avoid single characters

2. **Use appropriate method:**
   - Quick lookup: Long press
   - Precise selection: Action menu
   - Menu access: Header menu

3. **Read popup content:**
   - Scroll for long content
   - Close when done
   - Try different selections

### For Developers

1. **Error handling:**
   - Always handle network failures
   - Provide user-friendly messages
   - Log errors for debugging

2. **UI/UX:**
   - Position popup near text
   - Handle screen bounds
   - Loading states

3. **Performance:**
   - Cache sessions
   - Debounce rapid requests
   - Optimize popup rendering

## 🐛 Troubleshooting

### Popup không hiện

**Causes:**
- Text không được select đúng
- Layout chưa được render
- Screen bounds calculation lỗi

**Solutions:**
- Kiểm tra text selection
- Wait for layout completion
- Check Logcat for errors

### API không trả về data

**Causes:**
- Network connection
- n8n workflow không chạy
- Authentication issues

**Solutions:**
- Check internet connection
- Verify n8n server running
- Check API logs

### Popup vị trí sai

**Causes:**
- Text position calculation
- Screen size mismatch
- Layout not ready

**Solutions:**
- Check calculation logic
- Use screen metrics
- Wait for layout

## ✅ Testing Checklist

- [ ] Long press triggers highlight
- [ ] Action menu shows "AI Highlight"
- [ ] Menu button works
- [ ] Popup appears near text
- [ ] Popup positions correctly
- [ ] Loading state shows
- [ ] Success state shows info
- [ ] Error state shows message
- [ ] Close button works
- [ ] Scroll works in popup
- [ ] Network errors handled
- [ ] Response parsing works
- [ ] Multiple selections work
- [ ] Popup dismisses correctly

## 📝 Future Enhancements

Possible improvements:
- [ ] Highlight multiple selections
- [ ] Save highlight history
- [ ] Export highlight data
- [ ] Share highlights
- [ ] Custom highlight colors
- [ ] Voice readout
- [ ] Offline dictionary
- [ ] Translation integration
- [ ] Note-taking from highlights
- [ ] Search in highlights

## 🎉 Summary

**AI Highlight** feature provides:
- ✅ Quick access to word definitions
- ✅ Comprehensive information from AI
- ✅ Smart popup positioning
- ✅ Multiple interaction methods
- ✅ Error handling
- ✅ User-friendly UX

**Ready to use!** 🚀

