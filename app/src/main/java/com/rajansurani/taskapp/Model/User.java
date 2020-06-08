package com.rajansurani.taskapp.Model;

public class User {

    private String name,email;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
    public User(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
