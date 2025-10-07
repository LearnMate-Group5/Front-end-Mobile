package com.example.LearnMate.presenter;

import com.example.LearnMate.model.Book;
import com.example.LearnMate.model.HomeRepositoryImpl;
import com.example.LearnMate.view.HomeRepository;

import java.lang.ref.WeakReference;
import java.util.List;

public class HomePresenter implements HomeContract.Presenter {

    private final HomeRepository repo;
    private WeakReference<HomeContract.View> viewRef;

    public HomePresenter(HomeRepository repo) { this.repo = repo; }

    @Override public void attach(HomeContract.View v) { viewRef = new WeakReference<>(v); }
    @Override public void detach() { if (viewRef != null) viewRef.clear(); }
    private HomeContract.View v() { return viewRef == null ? null : viewRef.get(); }

    @Override
    public void onStart() {
        if (v()==null) return;
        v().showGreeting("Meo");
        v().showLoading(true);
        repo.loadHome(new HomeRepository.Callback() {
            @Override public void onSuccess(List<Book> featured, List<Book> recommended) {
                if (v()==null) return;
                v().showLoading(false);
                v().renderFeatured(featured);
                v().renderRecommended(recommended);
            }
            @Override public void onError(String message) {
                if (v()==null) return;
                v().showLoading(false);
                v().showMessage(message);
            }
        });
    }

    @Override public void onFeaturedClick(Book b) { if (v()!=null) v().openBookDetail(b); }
    @Override public void onRecommendedClick(Book b) { if (v()!=null) v().openBookDetail(b); }
    @Override public void onImportClick() { if (v()!=null) v().openImport(); }
}
