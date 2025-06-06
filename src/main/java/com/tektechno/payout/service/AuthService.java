package com.tektechno.payout.service;

import com.tektechno.payout.dto.request.LogInRequestDto;
import com.tektechno.payout.dto.request.UserRegistrationDto;
import org.springframework.http.ResponseEntity;

public interface AuthService {

  public ResponseEntity<?> createUser(UserRegistrationDto userRegistrationDto);

  public ResponseEntity<?> loginUser(LogInRequestDto logInRequestDto);

}
