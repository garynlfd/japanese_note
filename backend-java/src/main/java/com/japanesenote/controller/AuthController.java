package com.japanesenote.controller;

import com.japanesenote.model.User;
import com.japanesenote.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    // constructor
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        // TODO: hash password, save user, return 201
        User registerdUser = userService.save(user);

        // don not show password back to user(even the hashed one)
        registerdUser.setPassword(null);
        
        return ResponseEntity.status(201).body(registerdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        // TODO: find user by username
        //       verify password with passwordEncoder.matches(rawPassword, hashedPassword)
        //       if valid → generate token with jwtUtil.generateToken(username)
        //       return 200 with { "token": "..." }
        //       if invalid → return 401

        // move the business logic to service: passwordEncoder & jwtUtil operations

        Map<String, String> res = userService.login(user);
        if (res.containsKey("error")) {
            String msg = res.get("error");
            if("username not found".equals(msg)) {
                return ResponseEntity.notFound().build();
            }
            else if ("password is not correct".equals(msg)) {
                return ResponseEntity.status(401).build();
            }
        }

        return ResponseEntity.status(200).body(res);
    }
}