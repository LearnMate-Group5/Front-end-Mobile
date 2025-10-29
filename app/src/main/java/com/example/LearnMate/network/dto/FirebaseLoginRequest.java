package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

public class FirebaseLoginRequest {
    @SerializedName("idToken")
    private String idToken;

    public FirebaseLoginRequest(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
