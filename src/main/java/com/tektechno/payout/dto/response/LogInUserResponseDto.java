package com.tektechno.payout.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogInUserResponseDto {

  private String token;

  private String refreshToken;

  private UserResponseDto user;

}
