package com.foxfox.demo.service;

import com.foxfox.demo.dto.LoginRequest;
import com.foxfox.demo.dto.RegisterRequest;
import com.foxfox.demo.dto.UserResponse;

public interface UserService {

    UserResponse register(RegisterRequest request);

    UserResponse login(LoginRequest request);
}