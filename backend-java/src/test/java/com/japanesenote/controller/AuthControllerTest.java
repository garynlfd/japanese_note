package com.japanesenote.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.HashMap;
import java.util.Map;

import com.japanesenote.controller.AuthController;
import com.japanesenote.config.SecurityConfig;
import com.japanesenote.model.User;
import com.japanesenote.service.UserService;
import com.japanesenote.util.JwtUtil;

import org.springframework.context.annotation.Import;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService; // fake service injected into controller

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void register_valid_returns201() throws Exception {
        // 1. ARRANGE
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("gary");

        when(userService.save(any(User.class))).thenReturn(mockUser);

        // 2. ACT + 3. ASSERT
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"gary\",\"password\":\"test\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void login_valid_returns200withToken() throws Exception {
        // 1. ARRANGE
        Map<String, String> mockRes = new HashMap<>();
        mockRes.put("token", "mockToken");

        when(userService.login(any(User.class))).thenReturn(mockRes);

        // 2. ACT + 3. ASSERT
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"gary\",\"password\":\"mockPassword\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        // 1. ARRANGE
        Map<String, String> mockRes = new HashMap<>();
        mockRes.put("error", "password is not correct");

        when(userService.login(any(User.class))).thenReturn(mockRes);

        // 2. ACT + 3. ASSERT
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"gary\",\"password\":\"mockWrongPassword\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_userNotFound_returns404() throws Exception {
        // 1. ARRANGE
        Map<String, String> mockRes = new HashMap<>();
        mockRes.put("error", "username not found");

        when(userService.login(any(User.class))).thenReturn(mockRes);

        // 2. ACT + 3. ASSERT
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"nobody\",\"password\":\"mockPassword\"}"))
                .andExpect(status().isNotFound());
    }
}