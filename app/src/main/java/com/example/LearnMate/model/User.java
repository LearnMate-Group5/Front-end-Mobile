package com.example.LearnMate.model;

public class User {
    public String username;
    public String email;

    private String uid;

    public User() {

    }

    public User(String username, String email, String uid) {
        this.username = username;
        this.email = email;
        this.uid = uid;
    }

    public String getUid(){return uid;}

    public void setUid(String uid){this.uid = uid;}
    public String getUsername(){return username;}

    public void setUsername(String username){this.username = username;}

    public String getEmail(){return email;}

    public void setEmail(String email){this.email = email;}

}
