package com.foxfox.demo.controller;

import com.foxfox.demo.dto.LoginRequest;
import com.foxfox.demo.dto.RegisterRequest;
import com.foxfox.demo.dto.UserResponse;
import com.foxfox.demo.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) { this.userService = userService; }

    @PostMapping("/register")
    public UserResponse register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }
}