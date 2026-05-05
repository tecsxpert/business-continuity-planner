package com.internship.tool.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Simple demo auth endpoint.
 * Java Developer 1 will replace this with real JWT (JwtUtil + SecurityConfig).
 * Demo credentials: admin / admin123
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Auth", description = "Login and register (demo — Java Dev 1 adds real JWT)")
public class AuthController {

    @PostMapping("/login")
    @Operation(summary = "Login — returns a demo token (replace with JWT)")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "");
        String password = body.getOrDefault("password", "");

        if ("admin".equals(username) && "admin123".equals(password)) {
            return ResponseEntity.ok(Map.of(
                    "token", "demo-token-" + System.currentTimeMillis(),
                    "username", "admin",
                    "role", "ADMIN",
                    "message", "Login successful"));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user (demo — always succeeds)")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "");
        if (username.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
        }
        return ResponseEntity.ok(Map.of(
                "message", "Registered successfully",
                "username", username));
    }
}
