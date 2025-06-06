package com.tektechno.payout.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class BeneficiaryDetailsDto {

  private String statuscode;
  private String status;
  private List<Data> data;

  @lombok.Data
  public static class Data {
    @JsonProperty("BENEFICIARY_ID")
    private String beneficiaryId;

    @JsonProperty("BENE_TYPE")
    private String beneType;

    @JsonProperty("BENEFICIARY_VERIFICATION_STATUS")
    private String beneficiaryVerificationStatus;

    @JsonProperty("PAY_TYPE")
    private String payType;

    @JsonProperty("NAME_OF_ACCOUNT_HOLDER")
    private String accountHolderName;

    @JsonProperty("BANK_ACCOUNT_NUMBER")
    private String bankAccountNumber;

    @JsonProperty("BANK_IFSC_CODE")
    private String bankIfscCode;

    @JsonProperty("VPA")
    private String vpa;

    @JsonProperty("PAN")
    private String pan;

    @JsonProperty("AADHAR")
    private String aadhar;

    @JsonProperty("IS_AGREEMENT_WITH_BENEFICIARY")
    private String isAgreementWithBeneficiary;

    @JsonProperty("BENEFICIARY_ADDRESS")
    private String beneficiaryAddress; // Parse separately if needed

    @JsonProperty("LATLONG")
    private String latLong;

    @JsonProperty("ADDDATE")
    private String addDate;

    @JsonProperty("EMAIL")
    private String email;

    @JsonProperty("PHONE")
    private String phone;
  }

}
