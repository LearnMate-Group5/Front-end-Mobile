package com.example.LearnMate.view;


import com.example.LearnMate.model.Book;

import java.util.List;

public interface HomeRepository {
    interface Callback {
        void onSuccess(List<Book> featured, List<Book> recommended);
        void onError(String message);
    }
    void loadHome(Callback cb);
}

