package com.tektechno.payout.service;

import org.springframework.http.ResponseEntity;

public interface UserService {

  public ResponseEntity<?> getUserDetails(Long userId);

}
