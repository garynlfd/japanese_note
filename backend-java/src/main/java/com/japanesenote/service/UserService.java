package com.japanesenote.service;

import com.japanesenote.model.User;
import com.japanesenote.repository.UserRepository;
import com.japanesenote.util.JwtUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User save(User user) {
        // TODO: hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> update(Long id, User user) {
        return userRepository.update(id, user);
    }

    public boolean delete(Long id) {
        return userRepository.delete(id);
    }

    public Map<String, String> login(User user) {
        Map<String, String> res = new HashMap<>();

        Optional<User> foundUser = findByUsername(user.getUsername());
        if (!foundUser.isPresent()) {
            res.put("error", "username not found");
            return res;
        }
        
        // passwordEncoder.matches(plainpassword, hashedPassword);
        boolean passwordValidation = passwordEncoder.matches(user.getPassword(), foundUser.get().getPassword());
        if (!passwordValidation) {
            res.put("error", "password is not correct");
            return res;
        }

        String token = jwtUtil.generateToken(user.getUsername());
        res.put("token", token);
        return res;
    }
}