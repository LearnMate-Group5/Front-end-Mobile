package com.example.LearnMate.presenter;

import com.example.LearnMate.model.Book;
import java.util.List;

public interface HomeContract {
    interface View {
        void showGreeting(String name);
        void showLoading(boolean show);
        void renderFeatured(List<Book> items);
        void renderRecommended(List<Book> items);
        void openBookDetail(Book book);
        void openImport();
        void showMessage(String msg);
    }
    interface Presenter {
        void attach(View v);
        void detach();
        void onStart();                     // gọi khi View sẵn sàng
        void onFeaturedClick(Book b);
        void onRecommendedClick(Book b);
        void onImportClick();
    }
}
