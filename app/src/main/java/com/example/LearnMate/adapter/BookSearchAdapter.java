package com.example.LearnMate.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.LearnMate.R;
import com.example.LearnMate.network.dto.BookResponse;
import com.example.LearnMate.util.MarkdownHelper;

import java.util.List;

/**
 * Adapter for displaying book search results
 */
public class BookSearchAdapter extends RecyclerView.Adapter<BookSearchAdapter.BookViewHolder> {
    
    private List<BookResponse> books;
    private OnBookClickListener listener;
    
    public interface OnBookClickListener {
        void onBookClick(BookResponse book);
    }
    
    public BookSearchAdapter(List<BookResponse> books, OnBookClickListener listener) {
        this.books = books;
        this.listener = listener;
    }
    
    public void updateBooks(List<BookResponse> newBooks) {
        this.books = newBooks;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book_search, parent, false);
        return new BookViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        BookResponse book = books.get(position);
        holder.bind(book, listener);
    }
    
    @Override
    public int getItemCount() {
        return books != null ? books.size() : 0;
    }
    
    static class BookViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivBookCover;
        private TextView tvBookTitle;
        private TextView tvBookAuthor;
        private TextView tvBookDescription;
        private TextView tvBookCategories;
        
        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.ivBookCover);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookAuthor = itemView.findViewById(R.id.tvBookAuthor);
            tvBookDescription = itemView.findViewById(R.id.tvBookDescription);
            tvBookCategories = itemView.findViewById(R.id.tvBookCategories);
        }
        
        public void bind(BookResponse book, OnBookClickListener listener) {
            if (book == null) return;
            
            // Helper method to get field value (try getter first, then direct field)
            String title = getFieldValue(book, "getTitle", "title");
            String author = getFieldValue(book, "getAuthor", "author");
            String description = getFieldValue(book, "getDescription", "description");
            List<String> categories = getCategories(book);
            
            // Set title
            if (tvBookTitle != null) {
                tvBookTitle.setText(title != null ? title : "Không có tiêu đề");
            }
            
            // Set author
            if (tvBookAuthor != null) {
                tvBookAuthor.setText(author != null ? author : "Không rõ tác giả");
            }
            
            // Set description with markdown rendering
            if (tvBookDescription != null) {
                if (description != null && !description.isEmpty()) {
                    // Render markdown for description
                    // For search results, we limit the length but still render markdown
                    String descriptionToRender = description;
                    if (description.length() > 200) {
                        // Truncate but keep complete words
                        int lastSpace = description.lastIndexOf(' ', 200);
                        if (lastSpace > 0) {
                            descriptionToRender = description.substring(0, lastSpace) + "...";
                        } else {
                            descriptionToRender = description.substring(0, 200) + "...";
                        }
                    }
                    // Render markdown
                    MarkdownHelper.renderMarkdown(tvBookDescription, descriptionToRender);
                    // Allow text selection
                    tvBookDescription.setTextIsSelectable(true);
                } else {
                    tvBookDescription.setText("Không có mô tả");
                }
            }
            
            // Set categories
            if (tvBookCategories != null && categories != null && !categories.isEmpty()) {
                StringBuilder categoriesText = new StringBuilder();
                for (int i = 0; i < categories.size(); i++) {
                    if (i > 0) categoriesText.append(", ");
                    categoriesText.append(categories.get(i));
                }
                tvBookCategories.setText(categoriesText.toString());
            } else if (tvBookCategories != null) {
                tvBookCategories.setText("Không có danh mục");
            }
            
            // Load book cover image
            if (ivBookCover != null) {
                // Try to get imageBase64 field (primary method used in this codebase)
                String imageBase64 = getFieldValue(book, "getImageBase64", "imageBase64");
                
                if (imageBase64 != null && !imageBase64.isEmpty()) {
                    // Decode base64 image
                    android.graphics.Bitmap bitmap = decodeBase64ToBitmap(imageBase64);
                    if (bitmap != null) {
                        ivBookCover.setImageBitmap(bitmap);
                        ivBookCover.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    } else {
                        ivBookCover.setImageResource(R.drawable.bg_placeholder);
                    }
                } else {
                    // Check if book has getImageUrl() method as fallback
                    String imageUrl = getFieldValue(book, "getImageUrl", null);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        // For URL, we would need to load it asynchronously
                        // Since Picasso is not available, we'll just show placeholder
                        // You can implement URL loading with other libraries if needed
                        ivBookCover.setImageResource(R.drawable.bg_placeholder);
                    } else {
                        ivBookCover.setImageResource(R.drawable.bg_placeholder);
                    }
                }
            }
            
            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookClick(book);
                }
            });
        }
        
        /**
         * Helper method to get field value using getter or direct field access
         */
        private String getFieldValue(BookResponse book, String getterName, String fieldName) {
            // Try getter method first
            try {
                java.lang.reflect.Method method = book.getClass().getMethod(getterName);
                Object result = method.invoke(book);
                if (result != null) {
                    return result.toString();
                }
            } catch (Exception e) {
                // Getter doesn't exist, try direct field access
            }
            
            // Try direct field access
            if (fieldName != null) {
                try {
                    java.lang.reflect.Field field = book.getClass().getField(fieldName);
                    Object result = field.get(book);
                    if (result != null) {
                        return result.toString();
                    }
                } catch (Exception e) {
                    // Field doesn't exist or is not accessible
                }
            }
            
            return null;
        }
        
        /**
         * Helper method to get categories list
         */
        @SuppressWarnings("unchecked")
        private List<String> getCategories(BookResponse book) {
            // Try getter method first
            try {
                java.lang.reflect.Method method = book.getClass().getMethod("getCategories");
                Object result = method.invoke(book);
                if (result instanceof List) {
                    return (List<String>) result;
                }
            } catch (Exception e) {
                // Getter doesn't exist, try direct field access
            }
            
            // Try direct field access
            try {
                java.lang.reflect.Field field = book.getClass().getField("categories");
                Object result = field.get(book);
                if (result instanceof List) {
                    return (List<String>) result;
                }
            } catch (Exception e) {
                // Field doesn't exist or is not accessible
            }
            
            return null;
        }
        
        /**
         * Decode base64 string to Bitmap
         */
        private android.graphics.Bitmap decodeBase64ToBitmap(String base64String) {
            try {
                // Remove data URL prefix if present (e.g., "data:image/png;base64,")
                String base64Image = base64String;
                if (base64String.contains(",")) {
                    base64Image = base64String.substring(base64String.indexOf(",") + 1);
                }
                
                byte[] decodedBytes = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
                return android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            } catch (Exception e) {
                android.util.Log.e("BookSearchAdapter", "Error decoding base64 image: " + e.getMessage());
                return null;
            }
        }
    }
}

