package com.tektechno.payout.service;


public interface CallbackService {

  public void callbackForPayout(String statuscode, String status, String data);

}
