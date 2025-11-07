package com.example.LearnMate;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.LearnMate.managers.FirebaseAuthManager;
import com.example.LearnMate.presenter.LoginPresenter;
import com.example.LearnMate.view.LoginView;
import com.google.android.material.button.MaterialButton;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity implements LoginView {

    private EditText inputEmail, inputPassword;
    private MaterialButton btnSignIn;
    private TextView goToSignUp, forgotPassword;
    private ImageButton btnBack;
    private AppCompatButton btnGoogle;
    
    private FirebaseAuthManager firebaseAuthManager;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private LoginPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize Views
        btnBack = findViewById(R.id.btnBack);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnGoogle = findViewById(R.id.btnGoogle);
        forgotPassword = findViewById(R.id.forgotPassword);
        goToSignUp = findViewById(R.id.goToSignUp);

        // Initialize Presenter with Context
        presenter = new LoginPresenter(this, this);
        
        // Initialize Firebase Auth Manager
        firebaseAuthManager = new FirebaseAuthManager(this);

        // Register for activity result
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getData() == null) { 
                            Toast.makeText(LoginActivity.this, "Đăng nhập Google đã bị hủy", Toast.LENGTH_SHORT).show(); 
                            return; 
                        }
                        Task<GoogleSignInAccount> task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        firebaseAuthManager.handleGoogleSignInResult(task, new FirebaseAuthManager.FirebaseAuthCallback() {
                            @Override
                            public void onSuccess(String idToken, String email, String displayName) {
                                // Gọi API backend với Firebase ID token
                                presenter.performFirebaseLogin(idToken);
                            }

                            @Override
                            public void onError(String errorMessage) {
                                showErrorMessage(errorMessage);
                            }
                        });
                    }
                }
        );

        // Set Click Listeners
        btnBack.setOnClickListener(v -> finish());

        btnSignIn.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();
            presenter.performLogin(email, password);
        });

        goToSignUp.setOnClickListener(v -> {
            navigateToSignup();
        });

        // TODO: Implement these features later
        forgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Chức năng quên mật khẩu đang được phát triển", Toast.LENGTH_SHORT).show()
        );

        btnGoogle.setOnClickListener(v -> {
            Log.d("LoginActivity", "Google Sign-In button clicked");
            Intent intent = firebaseAuthManager.getGoogleSignInClient().getSignInIntent();
            Log.d("LoginActivity", "Launching Google Sign-In intent");
            googleSignInLauncher.launch(intent);
        });
    }

    // ---- LoginView Implementation ----

    @Override
    public void showSuccessMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        // Call the new navigation method
        navigateToHome();
    }

    @Override
    public void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void navigateToHome() {
        // Navigate to the main screen of the app
        startActivity(new Intent(this, HomeActivity.class));
        finishAffinity(); // Finish this activity and all parent activities
    }

    @Override
    public void navigateToSignup() {
        startActivity(new Intent(this, SignupActivity.class));
        finish();
    }
}
