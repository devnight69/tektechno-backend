package com.tektechno.payout.dto.response;

import com.tektechno.payout.enums.UserType;
import java.util.Date;
import lombok.Data;

@Data
public class UserResponseDto {

  private String fullName;

  private String email;

  private String mobileNumber;

  private UserType userType;

  private boolean status;

  private Date createdAt;

  private Date updatedAt;

}
