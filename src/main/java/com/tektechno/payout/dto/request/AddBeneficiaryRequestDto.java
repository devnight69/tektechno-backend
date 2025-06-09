package com.tektechno.payout.dto.request;

import com.tektechno.payout.constant.RegexConstant;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddBeneficiaryRequestDto {

  @NotBlank(message = "Beneficiary name is required")
  @Size(max = 100, message = "Beneficiary name must be less than 100 characters")
  @Pattern(regexp = "^[A-Za-z ]+$", message = "Beneficiary name must contain only alphabets and spaces")
  private String beneficiaryName;

  @NotBlank(message = "Beneficiary mobile number is required")
  @Pattern(regexp = RegexConstant.MOBILE_NUMBER_REGEX, message = "Invalid mobile number")
  private String beneficiaryMobileNumber;

  @Size(max = 100, message = "Email must be less than 100 characters")
  @Pattern(regexp = RegexConstant.EMAIL_REGEX, message = "Invalid email address")
  private String beneficiaryEmail;

  @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}", message = "Invalid PAN number")
  private String beneficiaryPanNumber;

  @Pattern(regexp = "\\d{12}", message = "Aadhaar number must be 12 digits")
  private String beneficiaryAadhaarNumber;

  @NotBlank(message = "Bank name is required")
  @Size(max = 100, message = "Bank name must be less than 100 characters")
  private String beneficiaryBankName;

  @NotBlank(message = "Account number is required")
  @Pattern(regexp = "\\d{9,18}", message = "Account number must be between 9 to 18 digits")
  private String beneficiaryAccountNumber;

  @NotBlank(message = "IFSC code is required")
  @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code")
  private String beneficiaryIfscCode;

  @NotBlank(message = "Beneficiary type is required")
  @NotNull(message = "Beneficiary type cannot be null")
  @NotEmpty(message = "Beneficiary type cannot be empty")
  private String beneType;

  @NotNull(message = "Latitude is required")
  @Min(value = -90, message = "Latitude cannot be less than -90")
  @Max(value = 90, message = "Latitude cannot be more than 90")
  private Long latitude;

  @NotNull(message = "Longitude is required")
  @Min(value = -180, message = "Longitude cannot be less than -180")
  @Max(value = 180, message = "Longitude cannot be more than 180")
  private Long longitude;

  private AddressRequestDto address;
}
