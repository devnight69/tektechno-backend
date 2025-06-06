package com.tektechno.payout.dto.request;

import lombok.Data;

@Data
public class SendMoneyRequestDto {

  private String beneficiaryId;
  private String beneficiaryName;
  private String beneficiaryMobileNumber;
  private String comment;
  private String remarks;
  private Long amount;
  private String transferType;

}
