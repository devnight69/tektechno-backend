package com.tektechno.payout.dto.request;

import com.tektechno.payout.constant.RegexConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LogInRequestDto {

  @NotNull(message = "email cannot be null")
  @NotBlank(message = "email cannot be blank")
  @NotEmpty(message = "email cannot be empty")
  @Pattern(regexp = RegexConstant.EMAIL_REGEX, message = "email must be a valid email address")
  private String email;

  @NotNull(message = "password cannot be null")
  @NotBlank(message = "password cannot be blank")
  @NotEmpty(message = "password cannot be empty")
  @Pattern(regexp = RegexConstant.PASSWORD_REGEX, message = "password must contain at least one uppercase letter,"
      + " one lowercase letter, one digit, and one special character")
  private String password;

}
