package com.tektechno.payout.controller;

import com.tektechno.payout.service.UserService;
import com.tektechno.payout.utilities.DecodeJwtTokenUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

  @Autowired
  private UserService userService;

  @Autowired
  private DecodeJwtTokenUtility decodeJwtTokenUtility;

  @GetMapping("/details")
  public ResponseEntity<?> getUserDetails() {
    Long userId = decodeJwtTokenUtility.getUserId();
    return userService.getUserDetails(userId);
  }

}
