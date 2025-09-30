package com.example.learnmate_frontend.model;

public class LoginRequest {
    private String email;
    private String password;

    private String idToken;

    public LoginRequest(String email, String password, String idToken) {
        this.email = email;
        this.password = password;
        this.idToken = idToken;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }
}
