package com.tektechno.payout.dto.request;

import com.tektechno.payout.constant.RegexConstant;
import com.tektechno.payout.enums.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDto {

  @NotNull(message = "fullName cannot be null")
  @NotBlank(message = "fullName cannot be blank")
  @NotEmpty(message = "fullName cannot be empty")
  @Size(min = 3, max = 100, message = "fullName must be between 3 and 100 characters")
  @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "fullName must contain only letters, spaces, hyphens, or apostrophes")
  private String fullName;

  @NotNull(message = "email cannot be null")
  @NotBlank(message = "email cannot be blank")
  @NotEmpty(message = "email cannot be empty")
  @Pattern(regexp = RegexConstant.EMAIL_REGEX, message = "email must be a valid email address")
  private String email;

  @NotNull(message = "mobileNumber cannot be null")
  @NotBlank(message = "mobileNumber cannot be blank")
  @NotEmpty(message = "mobileNumber cannot be empty")
  @Pattern(regexp = RegexConstant.MOBILE_NUMBER_REGEX, message = "mobileNumber must be a valid mobile number")
  private String mobileNumber;

  @NotNull(message = "password cannot be null")
  @NotBlank(message = "password cannot be blank")
  @NotEmpty(message = "password cannot be empty")
  @Pattern(regexp = RegexConstant.PASSWORD_REGEX, message = "password must contain at least one uppercase letter,"
      + " one lowercase letter, one digit, and one special character")
  private String password;

  @NotNull(message = "userType cannot be null")
  private UserType userType;

}
