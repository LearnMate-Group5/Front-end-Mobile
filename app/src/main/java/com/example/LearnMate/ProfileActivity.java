package com.example.LearnMate;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.LearnMate.components.BottomNavigationComponent;
import com.example.LearnMate.managers.SessionManager;
import com.example.LearnMate.managers.UserManager;
import com.example.LearnMate.network.RetrofitClient;
import com.example.LearnMate.network.api.AuthService;
import com.example.LearnMate.network.dto.ApiResult;
import com.example.LearnMate.network.dto.UserRolesMeResponse;
import com.example.LearnMate.util.FileUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final int PICK_IMAGE_REQUEST = 1001;

    private ImageButton btnBack;
    private ShapeableImageView imgAvatar;
    private MaterialButton btnChangeAvatar;
    private EditText etUsername;
    private EditText etEmail;
    private EditText etPhoneNumber;
    private EditText etBirth;
    private Spinner spinnerGender;
    private MaterialButton btnEditProfile;
    private MaterialButton btnLogout;
    private BottomNavigationComponent bottomNavComponent;
    private SessionManager sessionManager;
    private UserManager userManager;

    private boolean isEditMode = false;
    private Uri selectedAvatarUri;
    private Calendar selectedDate = Calendar.getInstance();

    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(getApplicationContext());
        authService = RetrofitClient.getAuthService(getApplicationContext());
        userManager = UserManager.getInstance(this);

        // bind views
        btnBack = findViewById(R.id.btnBack);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etBirth = findViewById(R.id.etBirth);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavComponent = findViewById(R.id.bottomNavComponent);

        setupGenderSpinner();
        setEditMode(false);
        loadUserProfile();

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnChangeAvatar.setOnClickListener(v -> {
            if (isEditMode) {
                openImagePicker();
            }
        });

        etBirth.setOnClickListener(v -> {
            if (isEditMode) {
                showDatePicker();
            }
        });

        btnEditProfile.setOnClickListener(v -> {
            if (isEditMode) saveProfileData();
            else setEditMode(true);
        });

        btnLogout.setOnClickListener(v -> sessionManager.logout(ProfileActivity.this));
    }

    private void setupGenderSpinner() {
        String[] genders = {"Giới tính (Tùy chọn)", "Nam", "Nữ", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void setEditMode(boolean editMode) {
        isEditMode = editMode;
        etUsername.setEnabled(editMode);
        etEmail.setEnabled(editMode);
        etPhoneNumber.setEnabled(editMode);
        etBirth.setEnabled(editMode);
        etBirth.setFocusable(editMode);
        etBirth.setClickable(editMode);
        spinnerGender.setEnabled(editMode);
        btnChangeAvatar.setEnabled(editMode);

        if (editMode) {
            btnEditProfile.setText("Lưu");
            btnEditProfile.setIconResource(android.R.drawable.ic_menu_save);
        } else {
            btnEditProfile.setText("Chỉnh Sửa Hồ Sơ");
            btnEditProfile.setIconResource(android.R.drawable.ic_menu_edit);
        }
    }

    /**
     * Load user profile từ UserManager (đã có cache từ /api/User/roles/me)
     */
    private void loadUserProfile() {
        // Load từ cache trước
        UserRolesMeResponse userProfile = userManager.getUserProfile();
        if (userProfile != null) {
            displayUserProfile(userProfile);
        } else {
            // Nếu không có cache, refresh từ API
            userManager.refreshUserProfile(profile -> {
                if (profile != null) {
                    displayUserProfile(profile);
                } else {
                    // Fallback to session data
                    loadFromSession();
                }
            });
        }
    }

    /**
     * Hiển thị thông tin user profile
     */
    private void displayUserProfile(UserRolesMeResponse userProfile) {
        // Hiển thị name
        if (userProfile.name != null && !userProfile.name.isEmpty()) {
            etUsername.setText(userProfile.name);
        } else {
            etUsername.setText("");
        }
        
        // Hiển thị email
        if (userProfile.email != null && !userProfile.email.isEmpty()) {
            etEmail.setText(userProfile.email);
        } else {
            etEmail.setText("");
        }
        
        // Hiển thị phone number
        if (userProfile.phoneNumber != null && !userProfile.phoneNumber.isEmpty()) {
            etPhoneNumber.setText(userProfile.phoneNumber);
        } else {
            etPhoneNumber.setText("");
        }
        
        // Hiển thị date of birth
        if (userProfile.dateOfBirth != null && !userProfile.dateOfBirth.isEmpty()) {
            String formattedDate = formatDateForDisplay(userProfile.dateOfBirth);
            etBirth.setText(formattedDate);
            // Parse date để set selectedDate
            parseDateToCalendar(userProfile.dateOfBirth);
        } else {
            etBirth.setText("");
        }
        
        // Hiển thị gender
        if (userProfile.gender != null && !userProfile.gender.isEmpty()) {
            String[] genders = {"Giới tính (Tùy chọn)", "Nam", "Nữ", "Khác"};
            for (int i = 0; i < genders.length; i++) {
                if (genders[i].equals(userProfile.gender)) {
                    spinnerGender.setSelection(i);
                    break;
                }
            }
        } else {
            spinnerGender.setSelection(0);
        }
        
        // Hiển thị avatar từ Base64
        if (userProfile.avatarUrl != null && !userProfile.avatarUrl.isEmpty()) {
            loadAvatarFromBase64(userProfile.avatarUrl);
        } else {
            imgAvatar.setImageResource(R.drawable.logo_learnmate);
        }
    }

    /**
     * Load avatar từ Base64 string
     */
    private void loadAvatarFromBase64(String base64String) {
        try {
            // Xử lý Base64 string - có thể có prefix "data:image/..." hoặc không
            String base64Image = base64String;
            if (base64String.contains(",")) {
                // Nếu có prefix, lấy phần sau dấu phẩy
                base64Image = base64String.substring(base64String.indexOf(",") + 1);
            }
            
            // Decode Base64 string thành byte array
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            
            // Tạo Bitmap từ byte array
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            
            if (bitmap != null) {
                imgAvatar.setImageBitmap(bitmap);
            } else {
                Log.w(TAG, "Failed to decode Base64 image");
                imgAvatar.setImageResource(R.drawable.logo_learnmate);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading avatar from Base64", e);
            imgAvatar.setImageResource(R.drawable.logo_learnmate);
        }
    }

    /**
     * Fallback: Load từ session nếu không có profile
     */
    private void loadFromSession() {
        android.content.SharedPreferences sp = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String name = sp.getString("user_name", "");
        String email = sp.getString("user_email", "");
        etUsername.setText(name);
        etEmail.setText(email);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedAvatarUri = data.getData();
            if (selectedAvatarUri != null) {
                imgAvatar.setImageURI(selectedAvatarUri);
            }
        }
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
                    etBirth.setText(displayFormat.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set max date to today (user must be born before today)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    /**
     * Format date từ ISO 8601 sang dd/MM/yyyy để hiển thị
     */
    private String formatDateForDisplay(String isoDateString) {
        if (isoDateString == null || isoDateString.isEmpty()) {
            return "";
        }
        
        try {
            // Parse ISO 8601 format: "2025-11-02T00:00:00" hoặc "2025-11-02T00:00:00Z"
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            if (isoDateString.endsWith("Z")) {
                inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                isoDateString = isoDateString.substring(0, isoDateString.length() - 1);
            }
            
            java.util.Date date = inputFormat.parse(isoDateString);
            if (date != null) {
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return outputFormat.format(date);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date for display", e);
        }
        
        return isoDateString;
    }

    /**
     * Parse date từ ISO 8601 sang Calendar
     */
    private void parseDateToCalendar(String isoDateString) {
        if (isoDateString == null || isoDateString.isEmpty()) {
            return;
        }
        
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            if (isoDateString.endsWith("Z")) {
                inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                isoDateString = isoDateString.substring(0, isoDateString.length() - 1);
            }
            
            java.util.Date date = inputFormat.parse(isoDateString);
            if (date != null) {
                selectedDate.setTime(date);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date to calendar", e);
        }
    }

    /**
     * Format date từ dd/MM/yyyy sang ISO 8601 để gửi API
     */
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
            Log.e(TAG, "Date format error", e);
            return null;
        }
    }

    private void saveProfileData() {
        String name = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String dateOfBirth = etBirth.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Tên và Email là bắt buộc", Toast.LENGTH_SHORT).show();
            setEditMode(true);
            return;
        }

        // Format date for API
        String formattedDateOfBirth = formatDateForApi(dateOfBirth);
        
        // Handle gender - convert to null if default option
        String finalGender = (gender.equals("Giới tính (Tùy chọn)") || gender.isEmpty()) ? null : gender;
        
        // Handle phone number - convert to null if empty
        String finalPhoneNumber = phoneNumber.isEmpty() ? null : phoneNumber;

        setEditMode(false);
        btnEditProfile.setEnabled(false);

        try {
            // Create RequestBody parts
            RequestBody namePart = RequestBody.create(okhttp3.MediaType.parse("text/plain"), name);
            RequestBody emailPart = RequestBody.create(okhttp3.MediaType.parse("text/plain"), email);
            RequestBody dateOfBirthPart = formattedDateOfBirth != null 
                ? RequestBody.create(okhttp3.MediaType.parse("text/plain"), formattedDateOfBirth)
                : RequestBody.create(okhttp3.MediaType.parse("text/plain"), "");
            RequestBody genderPart = finalGender != null
                ? RequestBody.create(okhttp3.MediaType.parse("text/plain"), finalGender)
                : RequestBody.create(okhttp3.MediaType.parse("text/plain"), "");
            RequestBody phoneNumberPart = finalPhoneNumber != null
                ? RequestBody.create(okhttp3.MediaType.parse("text/plain"), finalPhoneNumber)
                : RequestBody.create(okhttp3.MediaType.parse("text/plain"), "");

            // Handle avatar file
            MultipartBody.Part avatarFilePart = null;
            if (selectedAvatarUri != null) {
                avatarFilePart = FileUtils.uriToPdfPart(this, selectedAvatarUri, "AvatarFile");
            } else {
                // Create empty part if no avatar selected (backend expects this field)
                RequestBody emptyBody = RequestBody.create(okhttp3.MediaType.parse("application/octet-stream"), "");
                avatarFilePart = MultipartBody.Part.createFormData("AvatarFile", "", emptyBody);
            }

            authService.updateUserProfile(
                    namePart,
                    emailPart,
                    dateOfBirthPart,
                    genderPart,
                    phoneNumberPart,
                    avatarFilePart
            ).enqueue(new Callback<ApiResult<Object>>() {
                @Override
                public void onResponse(Call<ApiResult<Object>> call, Response<ApiResult<Object>> response) {
                    btnEditProfile.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess) {
                        Toast.makeText(ProfileActivity.this, "Hồ sơ đã được cập nhật", Toast.LENGTH_SHORT).show();

                        // Refresh user profile từ API sau khi update thành công
                        userManager.refreshUserProfile(userProfile -> {
                            if (userProfile != null) {
                                displayUserProfile(userProfile);
                            }
                        });

                        setEditMode(false);
                    } else {
                        setEditMode(true);
                        String msg = "Cập nhật thất bại";
                        if (response.body() != null && response.body().error != null
                                && response.body().error.description != null) {
                            msg = response.body().error.description;
                        }
                        Toast.makeText(ProfileActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResult<Object>> call, Throwable t) {
                    btnEditProfile.setEnabled(true);
                    setEditMode(true);
                    Toast.makeText(ProfileActivity.this, "Lỗi mạng: " + (t.getMessage() == null ? "" : t.getMessage()), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Update profile failed", t);
                }
            });
        } catch (Exception e) {
            btnEditProfile.setEnabled(true);
            setEditMode(true);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error preparing profile update", e);
        }
    }
}
