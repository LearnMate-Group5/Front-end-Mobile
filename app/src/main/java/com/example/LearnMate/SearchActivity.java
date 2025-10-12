package com.example.LearnMate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.LearnMate.components.BottomNavigationComponent;
import com.google.android.material.textfield.TextInputEditText;

public class SearchActivity extends AppCompatActivity {

    private TextInputEditText etSearch;
    private Button btnFilterCategory;
    private Button btnFilterAuthor;
    private Button btnFilterRating;
    private ImageView btnSearchSettings;
    private BottomNavigationComponent bottomNavComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize views
        etSearch = findViewById(R.id.etSearch);
        btnFilterCategory = findViewById(R.id.btnFilterCategory);
        btnFilterAuthor = findViewById(R.id.btnFilterAuthor);
        btnFilterRating = findViewById(R.id.btnFilterRating);
        btnSearchSettings = findViewById(R.id.btnSearchSettings);
        bottomNavComponent = findViewById(R.id.bottomNavComponent);

        // Set click listeners
        btnFilterCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SearchActivity.this, "Filter by Category", Toast.LENGTH_SHORT).show();
            }
        });

        btnFilterAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SearchActivity.this, "Filter by Author", Toast.LENGTH_SHORT).show();
            }
        });

        btnFilterRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SearchActivity.this, "Filter by Rating", Toast.LENGTH_SHORT).show();
            }
        });

        btnSearchSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SearchActivity.this, "Search Settings", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup bottom navigation
        bottomNavComponent.setSelectedItem(R.id.nav_search);
        // Navigation is now handled automatically by BottomNavigationComponent
    }
}
