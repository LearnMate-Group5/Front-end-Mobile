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

import com.example.LearnMate.presenter.LoginPresenter;
import com.example.LearnMate.view.LoginView;
import com.google.android.material.button.MaterialButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import android.content.SharedPreferences;

public class LoginActivity extends AppCompatActivity implements LoginView {

    private EditText inputEmail, inputPassword;
    private MaterialButton btnSignIn;
    private TextView goToSignUp, forgotPassword;
    private ImageButton btnBack;
    private AppCompatButton btnGoogle;
    private GoogleSignInClient googleClient;
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

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();
        googleClient = GoogleSignIn.getClient(this, gso);

        // Register for activity result
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getData() == null) { Toast.makeText(LoginActivity.this, "Google sign-in canceled", Toast.LENGTH_SHORT).show(); return; }
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            onGoogleLoginSuccess(account);
                        } catch (ApiException e) {
                            Log.e("GoogleLogin", "Sign-in failed", e);
                            showErrorMessage("Google sign-in failed: " + e.getStatusCode());
                        }
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
                Toast.makeText(this, "TODO: Forgot password flow", Toast.LENGTH_SHORT).show()
        );

        btnGoogle.setOnClickListener(v -> {
            Intent intent = googleClient.getSignInIntent();
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

    private void onGoogleLoginSuccess(GoogleSignInAccount account) {
        if (account == null) { showErrorMessage("Google account is null"); return; }
        String idToken = account.getIdToken();
        String email = account.getEmail();
        String name = account.getDisplayName();

        // TODO: Gửi idToken lên backend để xác thực. Tạm thời lưu local để vào Home.
        SharedPreferences sp = getApplicationContext().getSharedPreferences("user_prefs", MODE_PRIVATE);
        sp.edit()
                .putString("token", idToken != null ? idToken : "google_dummy_token")
                .putString("user_email", email)
                .putString("user_name", name)
                .putBoolean("is_logged_in", true)
                .apply();

        showSuccessMessage("Signed in with Google" + (email != null ? (": " + email) : ""));
        navigateToHome();
    }
}
