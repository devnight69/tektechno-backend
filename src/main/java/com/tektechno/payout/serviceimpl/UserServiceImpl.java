package com.tektechno.payout.serviceimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tektechno.payout.dto.response.UserResponseDto;
import com.tektechno.payout.model.Users;
import com.tektechno.payout.repository.UserRepository;
import com.tektechno.payout.response.BaseResponse;
import com.tektechno.payout.service.UserService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private BaseResponse baseResponse;

  private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

  @Override
  public ResponseEntity<?> getUserDetails(Long userId) {
    try {
      logger.info("Fetching user details for userId: {}", userId);

      Optional<Users> optionalUsers = userRepository.findById(userId);

      if (optionalUsers.isEmpty()) {
        logger.warn("User not found for userId: {}", userId);
        return baseResponse.errorResponse(HttpStatus.BAD_REQUEST, "User not found with the provided ID");
      }

      UserResponseDto userResponseDto = objectMapper.convertValue(optionalUsers.get(), UserResponseDto.class);

      logger.info("Successfully fetched user details for userId: {}", userId);
      return baseResponse.successResponse(userResponseDto);

    } catch (Exception e) {
      logger.error("Exception occurred while fetching user details for userId: {}. Error: {}", userId, e.getMessage(), e);
      return baseResponse.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
          "An unexpected error occurred while fetching user details");
    }
  }

}
