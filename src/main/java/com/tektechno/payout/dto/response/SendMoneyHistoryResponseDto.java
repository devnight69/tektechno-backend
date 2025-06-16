package com.tektechno.payout.dto.response;

import lombok.Data;

import java.util.Date;

@Data
public class SendMoneyHistoryResponseDto {

  private Long id;

  private String status;

  private String beneficiaryId;

  private String beneficiaryName;

  private String orderId;

  private String cyrusOrderId;

  private String cyrusId;

  private String rrnNumber;

  private String openingBalance;

  private String lockedAmount;

  private String chargedAmount;

  private Date createdAt;

  private Date updatedAt;

}
