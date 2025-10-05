package com.example.learnmate_frontend.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.learnmate_frontend.R;
import com.example.learnmate_frontend.model.GoogleLoginRequest;
import com.example.learnmate_frontend.model.LoginRequest;
import com.example.learnmate_frontend.model.LoginResponse;
import com.example.learnmate_frontend.model.User;
import com.example.learnmate_frontend.network.ApiService;
import com.example.learnmate_frontend.network.RetrofitClient;
import com.example.learnmate_frontend.util.SharedPrefManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;

public class SignInActivity extends AppCompatActivity {
    private static final String TAG = "SignInActivity";

    private EditText emailInput, passwordInput;
    private Button signInBtn, googleBtn, facebookBtn;
    private TextView goToSignUp, forgotPassword;
    private ImageButton btnBack;

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        Log.d(TAG, "Google Sign-In successful, ID Token: " + (account.getIdToken() != null ? account.getIdToken().substring(0, 20) + "..." : "null"));
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Log.e(TAG, "Google Sign-In failed", e);
                        Toast.makeText(this, "Đăng nhập Google thất bại: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.w(TAG, "Google Sign-In canceled or data null, resultCode: " + result.getResultCode());
                    Toast.makeText(this, "Đăng nhập Google bị hủy", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SharedPrefManager.getInstance(this).getToken() != null) {
            Log.d(TAG, "Existing token found, redirecting to HomeActivity");
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_sign_in);

        emailInput = findViewById(R.id.inputEmail);
        passwordInput = findViewById(R.id.inputPassword);
        signInBtn = findViewById(R.id.btnSignIn);
        googleBtn = findViewById(R.id.btnGoogle);
        facebookBtn = findViewById(R.id.btnFacebook);
        goToSignUp = findViewById(R.id.goToSignUp);
        forgotPassword = findViewById(R.id.forgotPassword);
        btnBack = findViewById(R.id.btnBack);

        // Cấu hình Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Khởi tạo Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        signInBtn.setOnClickListener(view -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            loginUser(email, password);
        });

        googleBtn.setOnClickListener(view -> startGoogleSignIn());
        
        facebookBtn.setOnClickListener(view ->
                Toast.makeText(this, "Facebook login chưa được triển khai", Toast.LENGTH_SHORT).show()
        );

        goToSignUp.setOnClickListener(view ->
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class))
        );

        btnBack.setOnClickListener(view -> {
            Intent intent = new Intent(SignInActivity.this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        });

        if (forgotPassword != null) {
            forgotPassword.setOnClickListener(view ->
                    Toast.makeText(this, "Chức năng quên mật khẩu chưa được triển khai", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void startGoogleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase sign-in with Google successful");
                        sendIdTokenToBackend(idToken);
                    } else {
                        Log.e(TAG, "Firebase sign-in with Google failed", task.getException());
                        Toast.makeText(SignInActivity.this, "Đăng nhập Firebase thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendIdTokenToBackend(String idToken) {
        GoogleLoginRequest request = new GoogleLoginRequest(idToken);
        ApiService apiService = RetrofitClient.getInstance(this);
        Log.d(TAG, "Sending request to /api/login/google-login with idToken: " + (idToken != null ? idToken.substring(0, 20) + "..." : "null"));

        apiService.googleLogin(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    String token = loginResponse.getToken();
                    int userId = loginResponse.getUserId();
                    Log.d(TAG, "Received userId from LoginResponse: " + userId);
                    String role = loginResponse.getRole(); // Có thể null
                    if (role == null) {
                        fetchUserProfileAndRole(token, userId);
                    } else {
                        saveLoginData(token, userId, role, null);
                    }
                } else {
                    int statusCode = response.code();
                    String errorMsg = "Lỗi đăng nhập Google: HTTP " + statusCode;
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            errorMsg += " - " + errorBody;
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (IOException e) {
                            errorMsg += " - Không thể đọc lỗi chi tiết";
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                    Log.e(TAG, errorMsg);
                    Toast.makeText(SignInActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "Google login error: " + t.getMessage(), t);
                Toast.makeText(SignInActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loginUser(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password, null);
        ApiService apiService = RetrofitClient.getInstance(this);
        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    String token = loginResponse.getToken();
                    int userId = loginResponse.getUserId();
                    Log.d(TAG, "Received userId from LoginResponse: " + userId);
                    String role = loginResponse.getRole(); // Có thể null
                    if (role == null) {
                        fetchUserProfileAndRole(token, userId);
                    } else {
                        saveLoginData(token, userId, role, null);
                    }
                } else {
                    Toast.makeText(SignInActivity.this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(SignInActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchUserProfileAndRole(String token, int userId) {
        ApiService apiService = RetrofitClient.getInstance(this);
        Log.d(TAG, "Fetching user profile and role with token: " + (token != null ? token.substring(0, Math.min(token.length(), 20)) + "..." : "null") + ", userId: " + userId);
        apiService.getUserById( userId,"Bearer " + token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    int apiUserId = user.getId();
                    Log.d(TAG, "Received userId from User: " + apiUserId + ", roleId: " + user.getRoleId());
                    SharedPrefManager.getInstance(SignInActivity.this).saveUser(user); // Lưu roleId qua User
                    saveLoginData(token, apiUserId, null, user);
                    Log.d(TAG, "User profile fetched: " + (user.getEmail() != null ? user.getEmail() : "null") + ", roleId: " + user.getRoleId());
                } else {
                    Log.e(TAG, "Failed to fetch user profile: HTTP " + response.code() + " - " + response.message());
                    saveLoginData(token, userId, "user", null); // Fallback với role mặc định
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "Fetch user profile error: " + t.getMessage(), t);
                saveLoginData(token, userId, "user", null); // Fallback với role mặc định
            }
        });
    }

    private void saveLoginData(String token, int userId, String role, User user) {
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(this);
        if (sharedPrefManager == null) {
            Log.e(TAG, "SharedPrefManager is null, cannot save login data");
            Toast.makeText(this, "Lỗi lưu dữ liệu, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = false;
        if (user != null) {
            success = sharedPrefManager.saveUser(user); // Lưu roleId qua User
            Log.d(TAG, "Saved User: userId=" + user.getId() + ", roleId=" + user.getRoleId());
        } else {
            success = sharedPrefManager.saveToken(token) && sharedPrefManager.saveUserId(userId);
            if (role != null) {
                sharedPrefManager.saveRole(role); // Lưu role nếu có
                Log.d(TAG, "Saved role: " + role);
            }
            Log.d(TAG, "Saved token and userId: token=" + (token != null ? token.substring(0, 20) + "..." : "null") + ", userId=" + userId);
        }
        if (success) {
            Log.d(TAG, "Login data saved successfully");
            Toast.makeText(SignInActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            Log.e(TAG, "Failed to save login data, preventing navigation to HomeActivity");
            Toast.makeText(SignInActivity.this, "Đăng nhập thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show();
        }
    }

}
