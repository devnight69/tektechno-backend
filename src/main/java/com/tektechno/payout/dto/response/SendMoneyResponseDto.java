package com.tektechno.payout.dto.response;

import lombok.Data;

@Data
public class SendMoneyResponseDto {

  private String statuscode;
  private String status;
  private Data data;

  @lombok.Data
  public static class Data {
    private String orderId;
    private String cyrusOrderId;
    private String cyrus_id;
    private String opening_bal;
    private String locked_amt;
    private String charged_amt;
    private String rrn;
  }

}
