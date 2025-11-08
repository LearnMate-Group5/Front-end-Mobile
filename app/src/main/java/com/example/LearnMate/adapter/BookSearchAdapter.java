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
import com.squareup.picasso.Picasso;

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
            
            // Set title
            if (tvBookTitle != null) {
                tvBookTitle.setText(book.getTitle() != null ? book.getTitle() : "Không có tiêu đề");
            }
            
            // Set author
            if (tvBookAuthor != null) {
                tvBookAuthor.setText(book.getAuthor() != null ? book.getAuthor() : "Không rõ tác giả");
            }
            
            // Set description with markdown rendering
            if (tvBookDescription != null) {
                String description = book.getDescription();
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
            if (tvBookCategories != null && book.getCategories() != null && !book.getCategories().isEmpty()) {
                StringBuilder categoriesText = new StringBuilder();
                for (int i = 0; i < book.getCategories().size(); i++) {
                    if (i > 0) categoriesText.append(", ");
                    categoriesText.append(book.getCategories().get(i));
                }
                tvBookCategories.setText(categoriesText.toString());
            } else if (tvBookCategories != null) {
                tvBookCategories.setText("Không có danh mục");
            }
            
            // Load book cover image
            if (ivBookCover != null) {
                if (book.getImageUrl() != null && !book.getImageUrl().isEmpty()) {
                    Picasso.get()
                            .load(book.getImageUrl())
                            .placeholder(R.drawable.bg_placeholder)
                            .error(R.drawable.bg_placeholder)
                            .into(ivBookCover);
                } else {
                    ivBookCover.setImageResource(R.drawable.bg_placeholder);
                }
            }
            
            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookClick(book);
                }
            });
        }
    }
}

