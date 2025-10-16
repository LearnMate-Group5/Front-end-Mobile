package com.example.LearnMate.network.dto;

public class RegisterUserCommand {
    public String name;
    public String email;
    public String password;
    public RegisterUserCommand(String name, String email, String password) {
        this.name = name; this.email = email; this.password = password;
    }
}
