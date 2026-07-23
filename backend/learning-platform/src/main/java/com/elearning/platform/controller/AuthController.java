package com.elearning.platform.controller;

import com.elearning.platform.dto.request.LoginRequest;
import com.elearning.platform.dto.request.RegisterRequest;
import com.elearning.platform.dto.response.AuthResponse;
import com.elearning.platform.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // Registrar usuario
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    // Iniciar sesión
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

}