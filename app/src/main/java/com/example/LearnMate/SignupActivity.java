package com.example.LearnMate;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.LearnMate.presenter.SignupPresenter;
import com.example.LearnMate.view.SignupView;

public class SignupActivity extends AppCompatActivity implements SignupView {

    private ImageButton btnBack;
    private EditText inputName, inputEmailSignUp, inputPasswordSignUp;
    private TextView privacyPolicy, goToSignIn;
    private CheckBox privacyCheckbox;
    private Button btnSignUp;
    private AppCompatButton btnGoogleSignUp;

    private SignupPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        btnBack = findViewById(R.id.btnBack);
        inputName = findViewById(R.id.inputName);
        inputEmailSignUp = findViewById(R.id.inputEmailSignUp);
        inputPasswordSignUp = findViewById(R.id.inputPasswordSignUp);
        privacyPolicy = findViewById(R.id.privacyPolicy);
        privacyCheckbox = findViewById(R.id.privacyCheckbox);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnGoogleSignUp = findViewById(R.id.btnGoogleSignUp);
        goToSignIn = findViewById(R.id.goToSignIn);

        // ✅ Sửa ở đây
        presenter = new SignupPresenter(this, this);

        btnBack.setOnClickListener(v -> finish());
        privacyPolicy.setOnClickListener(v -> Toast.makeText(this, "Open Privacy Policy", Toast.LENGTH_SHORT).show());

        btnSignUp.setOnClickListener(v -> {
            String username = inputName.getText().toString().trim();
            String email = inputEmailSignUp.getText().toString().trim();
            String password = inputPasswordSignUp.getText().toString().trim();

            if (!privacyCheckbox.isChecked()) { showSignupError("Vui lòng đồng ý với Privacy Policy"); return; }
            if (TextUtils.isEmpty(username)) { showSignupError("Vui lòng nhập Username"); return; }
            if (TextUtils.isEmpty(email)) { showSignupError("Vui lòng nhập Email"); return; }
            if (TextUtils.isEmpty(password) || password.length() < 6) { showSignupError("Mật khẩu phải ≥ 6 ký tự"); return; }

            presenter.performSignup(email, password, password, username);
        });

        goToSignIn.setOnClickListener(v -> presenter.onLoginClicked());
        btnGoogleSignUp.setOnClickListener(v -> Toast.makeText(this, "Google Sign-Up clicked", Toast.LENGTH_SHORT).show());

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

    @Override public void showSignupSuccess(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
    @Override public void showSignupError(String error) { Toast.makeText(this, error, Toast.LENGTH_SHORT).show(); }
    @Override public void navigateToLogin() { startActivity(new Intent(this, LoginActivity.class)); finish(); }
}
