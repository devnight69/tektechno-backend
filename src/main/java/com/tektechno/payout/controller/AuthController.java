package com.tektechno.payout.controller;

import com.tektechno.payout.dto.request.LogInRequestDto;
import com.tektechno.payout.dto.request.UserRegistrationDto;
import com.tektechno.payout.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  @Autowired
  private AuthService authService;

  @PostMapping("/register/user")
  public ResponseEntity<?> createUser(@Valid @RequestBody UserRegistrationDto userRegistrationDto) {
    return authService.createUser(userRegistrationDto);
  }

  @PostMapping("/log-in")
  public ResponseEntity<?> loginUser(@Valid @RequestBody LogInRequestDto logInRequestDto) {
    return authService.loginUser(logInRequestDto);
  }

}
