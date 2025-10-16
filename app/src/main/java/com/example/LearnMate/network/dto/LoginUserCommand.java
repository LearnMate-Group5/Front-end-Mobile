package com.example.LearnMate.network.dto;

public class LoginUserCommand {
    public String email;
    public String password;
    public LoginUserCommand(String email, String password) {
        this.email = email; this.password = password;
    }
}
