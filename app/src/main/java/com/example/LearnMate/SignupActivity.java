package com.example.LearnMate;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.LearnMate.managers.FirebaseAuthManager;
import com.example.LearnMate.presenter.SignupPresenter;
import com.example.LearnMate.view.SignupView;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SignupActivity extends AppCompatActivity implements SignupView {

    private ImageButton btnBack;
    private EditText inputName, inputEmailSignUp, inputPasswordSignUp;
    private EditText inputPhoneNumber, inputDateOfBirth;
    private Spinner spinnerGender;
    private TextView privacyPolicy, goToSignIn;
    private CheckBox privacyCheckbox;
    private Button btnSignUp;
    private AppCompatButton btnGoogleSignUp;
    private FirebaseAuthManager firebaseAuthManager;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private Calendar selectedDate;

    private SignupPresenter presenter;
    private boolean isSubmitting = false;  // Flag to prevent double submission

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        btnBack = findViewById(R.id.btnBack);
        inputName = findViewById(R.id.inputName);
        inputEmailSignUp = findViewById(R.id.inputEmailSignUp);
        inputPasswordSignUp = findViewById(R.id.inputPasswordSignUp);
        inputPhoneNumber = findViewById(R.id.inputPhoneNumber);
        inputDateOfBirth = findViewById(R.id.inputDateOfBirth);
        spinnerGender = findViewById(R.id.spinnerGender);
        privacyPolicy = findViewById(R.id.privacyPolicy);
        privacyCheckbox = findViewById(R.id.privacyCheckbox);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnGoogleSignUp = findViewById(R.id.btnGoogleSignUp);
        goToSignIn = findViewById(R.id.goToSignIn);

        // Setup Gender Spinner
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        // Setup Date Picker
        selectedDate = Calendar.getInstance();
        inputDateOfBirth.setOnClickListener(v -> showDatePicker());

        // Presenter
        presenter = new SignupPresenter(this, this);
        
        // Initialize Firebase Auth Manager
        firebaseAuthManager = new FirebaseAuthManager(this);

        // Register for activity result
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getData() == null) { 
                            Toast.makeText(SignupActivity.this, "Đăng nhập Google đã bị hủy", Toast.LENGTH_SHORT).show(); 
                            return; 
                        }
                        Task<GoogleSignInAccount> task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        firebaseAuthManager.handleGoogleSignInResult(task, new FirebaseAuthManager.FirebaseAuthCallback() {
                            @Override
                            public void onSuccess(String idToken, String email, String displayName) {
                                // Gọi API backend với Firebase ID token
                                presenter.performFirebaseSignup(idToken, email, displayName);
                            }

                            @Override
                            public void onError(String errorMessage) {
                                showSignupError(errorMessage);
                            }
                        });
                    }
                }
        );

        btnBack.setOnClickListener(v -> finish());
        privacyPolicy.setOnClickListener(v -> Toast.makeText(this, "Mở Chính Sách Bảo Mật", Toast.LENGTH_SHORT).show());

        btnSignUp.setOnClickListener(v -> {
            // Prevent double submission
            if (isSubmitting) {
                Log.d("SignupActivity", "Already submitting, ignoring click");
                return;
            }
            
            String fullName = inputName.getText().toString().trim();
            String email = inputEmailSignUp.getText().toString().trim();
            String password = inputPasswordSignUp.getText().toString().trim();
            String phoneNumber = inputPhoneNumber.getText().toString().trim();
            String dateOfBirth = inputDateOfBirth.getText().toString().trim();
            String gender = spinnerGender.getSelectedItem().toString();

            if (!privacyCheckbox.isChecked()) { showSignupError("Vui lòng đồng ý với Privacy Policy"); return; }
            if (TextUtils.isEmpty(fullName)) { showSignupError("Vui lòng nhập Full Name"); return; }
            if (TextUtils.isEmpty(email)) { showSignupError("Vui lòng nhập Email"); return; }
            if (TextUtils.isEmpty(password) || password.length() < 6) { showSignupError("Mật khẩu phải ≥ 6 ký tự"); return; }

            // Disable button and set flag
            isSubmitting = true;
            btnSignUp.setEnabled(false);
            btnSignUp.setText("Đang đăng ký...");
            
            // Convert empty optional fields to null
            String finalPhoneNumber = phoneNumber.isEmpty() ? null : phoneNumber;
            String finalDateOfBirth = dateOfBirth.isEmpty() ? null : formatDateForApi(dateOfBirth);
            // Check for Vietnamese or English default value
            String finalGender = (gender.equals("Chọn giới tính (Tùy chọn)") || gender.equals("Select Gender (Optional)")) ? null : gender;

            presenter.performSignup(email, password, password, fullName, finalPhoneNumber, finalDateOfBirth, finalGender);
        });

        goToSignIn.setOnClickListener(v -> presenter.onLoginClicked());
        btnGoogleSignUp.setOnClickListener(v -> {
            Log.d("SignupActivity", "Google Sign-In button clicked");
            Intent intent = firebaseAuthManager.getGoogleSignInClient().getSignInIntent();
            Log.d("SignupActivity", "Launching Google Sign-In intent");
            googleSignInLauncher.launch(intent);
        });

        inputPasswordSignUp.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (inputPasswordSignUp.getRight()
                        - inputPasswordSignUp.getCompoundDrawables()[2].getBounds().width())) {
                    int type = inputPasswordSignUp.getInputType();
                    if ((type & InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                            == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                        inputPasswordSignUp.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        inputPasswordSignUp.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0);
                    } else {
                        inputPasswordSignUp.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        inputPasswordSignUp.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye, 0);
                    }
                    inputPasswordSignUp.setSelection(inputPasswordSignUp.getText().length());
                    return true;
                }
            }
            return false;
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    // Display format: dd/MM/yyyy
                    SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    inputDateOfBirth.setText(displayFormat.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set max date to today (user must be born before today)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private String formatDateForApi(String displayDate) {
        if (displayDate == null || displayDate.isEmpty()) {
            return null;
        }
        
        try {
            // Parse display format (dd/MM/yyyy) and convert to ISO 8601
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            apiFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            
            return apiFormat.format(displayFormat.parse(displayDate));
        } catch (Exception e) {
            Log.e("SignupActivity", "Date format error", e);
            return null;
        }
    }

    @Override public void showSignupSuccess(String message) { 
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); 
    }
    
    @Override public void showSignupError(String error) { 
        // Re-enable button on error
        isSubmitting = false;
        btnSignUp.setEnabled(true);
        btnSignUp.setText("GET STARTED");
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show(); 
    }
    
    @Override public void navigateToLogin() { 
        startActivity(new Intent(this, LoginActivity.class)); 
        finish(); 
    }
}
