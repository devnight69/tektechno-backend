package com.tektechno.payout.dto.request;

import lombok.Data;

@Data
public class AddressRequestDto {
  private String line;
  private String area;
  private String city;
  private String district;
  private String state;
  private String pincode;

}
