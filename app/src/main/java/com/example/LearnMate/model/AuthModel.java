package com.example.LearnMate.model;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// >>> Thêm 2 import dưới đây để hết lỗi DatabaseReference / FirebaseDatabase
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AuthModel {

    private final FirebaseAuth mAuth;
    private final DatabaseReference mDatabase;

    public AuthModel() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(); // child(...) sẽ có trên kiểu này
    }

    public void login(@NonNull String email,
                      @NonNull String password,
                      @NonNull AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess("Login successful");
                    } else {
                        String msg = (task.getException() != null)
                                ? task.getException().getMessage() // getMessage() ở Exception
                                : "Login failed";
                        callback.onFailure(msg);
                    }
                });
    }

    public void signup(@NonNull String email,
                       @NonNull String password,
                       @NonNull String username,
                       @NonNull AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        String msg = (task.getException() != null)
                                ? task.getException().getMessage()
                                : "Signup failed";
                        callback.onFailure(msg);
                        return;
                    }

                    FirebaseUser current = mAuth.getCurrentUser();
                    if (current == null) {
                        callback.onFailure("User is null after signup");
                        return;
                    }

                    String userId = current.getUid();
                    // Pass email and userId to the User constructor
                    User user = new User(username, email, userId); 

                    // child(String) có sẵn trên DatabaseReference
                    mDatabase.child("users").child(userId).setValue(user)
                            .addOnSuccessListener(aVoid -> callback.onSuccess("Signup successful"))
                            .addOnFailureListener(e ->
                                    // Simplified error message
                                    callback.onFailure(e.getMessage()));
                });
    }

    public interface AuthCallback {
        void onSuccess(String message);
        void onFailure(String message);
    }
}
