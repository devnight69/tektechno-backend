package com.tektechno.payout.controller;

import com.tektechno.payout.service.CallbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/callback")
public class CallbackController {

  @Autowired
  private CallbackService callbackService;

  @GetMapping("/payout")
  public void callbackForPayout(@RequestParam String statuscode,
                                @RequestParam String status,
                                @RequestParam String data) {
    callbackService.callbackForPayout(statuscode, status, data);
  }

}
