package com.tektechno.payout.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressRequestDto {
  private String line;
  private String area;
  private String city;
  private String district;
  private String state;
  private String pincode;

}
