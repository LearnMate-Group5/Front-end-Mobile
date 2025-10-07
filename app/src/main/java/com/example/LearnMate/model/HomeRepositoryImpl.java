package com.example.LearnMate.model;

import android.os.Handler;
import android.os.Looper;

import com.example.LearnMate.view.HomeRepository;

import java.util.Arrays;
import java.util.List;

public class HomeRepositoryImpl implements HomeRepository {
    @Override
    public void loadHome(Callback cb) {
        // giả lập API 500ms
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                List<Book> featured = Arrays.asList(
                        new Book("1","The Last Four Things","Fantasy",4.9f,""),
                        new Book("2","Robotic Book","Sci-Fi",4.9f,"")
                );
                List<Book> recommended = Arrays.asList(
                        new Book("3","Trace","Science, Horror",4.6f,""),
                        new Book("4","Lost Souls","Romance, Sorrow",4.7f,""),
                        new Book("5","Focus","Science",4.3f,"")
                );
                cb.onSuccess(featured, recommended);
            } catch (Exception e) {
                cb.onError(e.getMessage());
            }
        }, 500);
    }
}
