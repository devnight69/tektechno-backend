package com.tektechno.payout.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AddBeneficiaryResponseDto {

  private String statuscode;
  private String status;
  private Data data;

  @lombok.Data
  public static class Data {
    @JsonProperty("STATUS")
    private String status;

    @JsonProperty("BENEFICIARY_ID")
    private String beneficiaryId;
  }

}
