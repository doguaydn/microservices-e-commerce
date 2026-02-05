package com.dogu.auth.user.web;

public class LoginResponse {

    private String token;
    private String email;
    private String name;
    private String surname;

    public LoginResponse() {
    }

    public LoginResponse(String token, String email, String name, String surname) {
        this.token = token;
        this.email = email;
        this.name = name;
        this.surname = surname;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}
