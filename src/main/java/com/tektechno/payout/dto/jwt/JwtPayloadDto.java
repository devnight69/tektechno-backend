package com.tektechno.payout.dto.jwt;

import lombok.Data;

@Data
public class JwtPayloadDto {

  private String userId;
  private String mobileNumber;
  private String emailId;
  private String fullName;
  private Boolean active;
  private String userType;
  private String tokenType;
  private String memberId;

}
