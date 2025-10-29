package com.example.LearnMate.managers;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.example.LearnMate.R;

public class FirebaseAuthManager {
    private static final String TAG = "FirebaseAuthManager";
    
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private Context context;
    
    public interface FirebaseAuthCallback {
        void onSuccess(String idToken, String email, String displayName);
        void onError(String errorMessage);
    }
    
    public FirebaseAuthManager(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }
    
    public GoogleSignInClient getGoogleSignInClient() {
        return mGoogleSignInClient;
    }
    
    public void handleGoogleSignInResult(Task<GoogleSignInAccount> task, FirebaseAuthCallback callback) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account, callback);
            } else {
                callback.onError("Google account is null");
            }
        } catch (ApiException e) {
            Log.e(TAG, "Google sign-in failed", e);
            callback.onError("Google sign-in failed: " + e.getStatusCode());
        }
    }
    
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct, FirebaseAuthCallback callback) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        
        // Log Google ID token
        String googleIdToken = acct.getIdToken();
        Log.d(TAG, "Google ID Token: " + googleIdToken);
        
        AuthCredential credential = GoogleAuthProvider.getCredential(googleIdToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Get fresh ID token
                            user.getIdToken(true).addOnCompleteListener(tokenTask -> {
                                if (tokenTask.isSuccessful()) {
                                    String idToken = tokenTask.getResult().getToken();
                                    Log.d(TAG, "Firebase ID Token: " + idToken);
                                    Log.d(TAG, "User ID: " + user.getUid());
                                    Log.d(TAG, "User Email: " + user.getEmail());
                                    Log.d(TAG, "User Display Name: " + user.getDisplayName());
                                    callback.onSuccess(idToken, user.getEmail(), user.getDisplayName());
                                } else {
                                    Log.e(TAG, "Failed to get Firebase ID token", tokenTask.getException());
                                    callback.onError("Failed to get ID token");
                                }
                            });
                        } else {
                            Log.e(TAG, "Firebase user is null");
                            callback.onError("Firebase user is null");
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        callback.onError("Firebase authentication failed: " + 
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }
    
    public void signOut() {
        // Firebase sign out
        mAuth.signOut();
        
        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Log.d(TAG, "Google sign out completed");
        });
    }
    
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
    
    public boolean isUserSignedIn() {
        return mAuth.getCurrentUser() != null;
    }
}
