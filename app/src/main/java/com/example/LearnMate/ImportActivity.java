package com.example.LearnMate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.LearnMate.components.BottomNavigationComponent;
import com.google.android.material.card.MaterialCardView;

public class ImportActivity extends AppCompatActivity {

    private MaterialCardView cardCameraImport;
    private MaterialCardView cardFileImport;
    private MaterialCardView cardUrlImport;
    private ImageView btnImportSettings;
    private BottomNavigationComponent bottomNavComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        // Initialize views
        cardCameraImport = findViewById(R.id.cardCameraImport);
        cardFileImport = findViewById(R.id.cardFileImport);
        cardUrlImport = findViewById(R.id.cardUrlImport);
        btnImportSettings = findViewById(R.id.btnImportSettings);
        bottomNavComponent = findViewById(R.id.bottomNavComponent);

        // Set click listeners
        cardCameraImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ImportActivity.this, "Open Camera to Scan", Toast.LENGTH_SHORT).show();
                // TODO: Implement camera scanning functionality
            }
        });

        cardFileImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ImportActivity.this, "Select File to Import", Toast.LENGTH_SHORT).show();
                // TODO: Implement file picker functionality
            }
        });

        cardUrlImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ImportActivity.this, "Enter URL to Import", Toast.LENGTH_SHORT).show();
                // TODO: Implement URL import functionality
            }
        });

        btnImportSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ImportActivity.this, "Import Settings", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup bottom navigation
        bottomNavComponent.setSelectedItem(R.id.nav_import);
        // Navigation is now handled automatically by BottomNavigationComponent
    }
}
