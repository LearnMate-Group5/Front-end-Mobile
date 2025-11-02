package com.example.LearnMate.components;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.LearnMate.HomeActivity;
import com.example.LearnMate.SettingsActivity;
import com.example.LearnMate.SearchActivity;
import com.example.LearnMate.ImportActivity;
import com.example.LearnMate.ProfileActivity;
import com.example.LearnMate.AiChatBotActivity;
import com.example.LearnMate.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigationComponent extends FrameLayout {

    private BottomNavigationView bottomNav;
    private OnNavigationItemSelectedListener listener;

    public interface OnNavigationItemSelectedListener {
        void onNavigationItemSelected(int itemId);
    }

    public BottomNavigationComponent(@NonNull Context context) {
        super(context);
        init();
    }

    public BottomNavigationComponent(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BottomNavigationComponent(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.component_bottom_navigation, this, true);
        bottomNav = findViewById(R.id.bottomNav);
        
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            // Handle navigation automatically
            handleNavigation(itemId);
            
            // Also call the listener if set
            if (listener != null) {
                listener.onNavigationItemSelected(itemId);
            }
            return true;
        });

        // Handle system insets for gesture navigation
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
            Insets sb = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), sb.bottom);
            return insets;
        });
    }
    
    private void handleNavigation(int itemId) {
        Context context = getContext();
        Intent intent = null;
        
        if (itemId == R.id.nav_home) {
            if (!(context instanceof HomeActivity)) {
                intent = new Intent(context, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
        } else if (itemId == R.id.nav_search) {
            if (!(context instanceof SearchActivity)) {
                intent = new Intent(context, SearchActivity.class);
            }
        } else if (itemId == R.id.nav_import) {
            if (!(context instanceof ImportActivity)) {
                intent = new Intent(context, ImportActivity.class);
            }
        } else if (itemId == R.id.nav_ai_bot) {
            // Allow navigation to ChatBot - UI will handle locked state
            if (!(context instanceof AiChatBotActivity)) {
                intent = new Intent(context, AiChatBotActivity.class);
            }
        } else if (itemId == R.id.nav_profile) {
            // nav_profile is actually "Settings" in the menu
            if (!(context instanceof SettingsActivity)) {
                intent = new Intent(context, SettingsActivity.class);
            }
        }
        
        if (intent != null) {
            context.startActivity(intent);
        }
    }

    public void setOnNavigationItemSelectedListener(OnNavigationItemSelectedListener listener) {
        this.listener = listener;
    }

    public void setSelectedItem(int itemId) {
        bottomNav.setSelectedItemId(itemId);
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        bottomNav.setVisibility(visibility);
    }
}
