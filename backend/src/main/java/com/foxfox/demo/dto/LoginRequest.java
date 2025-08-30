package com.foxfox.demo.dto;

public class LoginRequest {

    // 可传用户名或邮箱
    private String usernameOrEmail;
    private String password;

    public String getUsernameOrEmail() { return usernameOrEmail; }
    public void setUsernameOrEmail(String usernameOrEmail) { this.usernameOrEmail = usernameOrEmail; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}