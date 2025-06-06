package com.tektechno.payout.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tektechno.payout.dto.jwt.JwtPayloadDto;
import com.tektechno.payout.dto.request.LogInRequestDto;
import com.tektechno.payout.dto.request.UserRegistrationDto;
import com.tektechno.payout.dto.response.LogInUserResponseDto;
import com.tektechno.payout.dto.response.UserResponseDto;
import com.tektechno.payout.enums.TokenType;
import com.tektechno.payout.model.Users;
import com.tektechno.payout.repository.UserRepository;
import com.tektechno.payout.response.BaseResponse;
import com.tektechno.payout.service.AuthService;
import com.tektechno.payout.utilities.JwtAuthUtils;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

  @Autowired
  private JwtAuthUtils jwtAuthUtils;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private BaseResponse baseResponse;

  @Autowired
  private ObjectMapper objectMapper;

  private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

  @Override
  public ResponseEntity<?> createUser(UserRegistrationDto userRegistrationDto) {
    try {
      if (userRepository.existsByEmail(userRegistrationDto.getEmail())) {
        logger.info("Email already exists With This Email = {}", userRegistrationDto.getEmail());
        return baseResponse.errorResponse(HttpStatus.BAD_REQUEST, "Email already exists");
      }

      if (userRepository.existsByMobileNumber(userRegistrationDto.getMobileNumber())) {
        logger.info("Mobile Number already exists With This Mobile Number = {}", userRegistrationDto.getMobileNumber());
        return baseResponse.errorResponse(HttpStatus.BAD_REQUEST, "Mobile Number already exists");
      }

      Users users = objectMapper.convertValue(userRegistrationDto, Users.class);
      users.setPassword(passwordEncoder.encode(users.getPassword()));
      Users savedUser = userRepository.save(users);
      logger.info("User Created Successfully = {}", savedUser.getEmail());
      return baseResponse.successResponse("User Created Successfully");

    } catch (Exception ex) {
      logger.error("Error at creating user = {}", ex.getMessage());
      return baseResponse.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error at creating user");
    }
  }

  @Override
  public ResponseEntity<?> loginUser(LogInRequestDto logInRequestDto) {
    try {
      logger.info("Login attempt for email: {}", logInRequestDto.getEmail());

      Optional<Users> optionalUsers = userRepository.findByEmailAndStatus(logInRequestDto.getEmail(), true);

      if (optionalUsers.isEmpty()) {
        logger.warn("Login failed - user not found or inactive. Email: {}", logInRequestDto.getEmail());
        return baseResponse.errorResponse(HttpStatus.BAD_REQUEST, "User not found or account is inactive");
      }

      Users users = optionalUsers.get();

      if (!passwordEncoder.matches(logInRequestDto.getPassword(), users.getPassword())) {
        logger.warn("Login failed - incorrect password for email: {}", logInRequestDto.getEmail());
        return baseResponse.errorResponse(HttpStatus.BAD_REQUEST, "Incorrect email or password");
      }

      logger.info("Login successful for email: {}", logInRequestDto.getEmail());

      UserResponseDto userResponseDto = objectMapper.convertValue(users, UserResponseDto.class);

      LogInUserResponseDto logInUserResponseDto = new LogInUserResponseDto(
          generateJwtToken(createJwtPayloadDto(users, false)),
          generateJwtToken(createJwtPayloadDto(users, true)),
          userResponseDto
      );

      return baseResponse.successResponse(logInUserResponseDto);

    } catch (Exception e) {
      logger.error("Exception during login for email: {}. Error: {}", logInRequestDto.getEmail(), e.getMessage(), e);
      return baseResponse.errorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
          "An unexpected error occurred during login. Please try again later.");
    }
  }

  private JwtPayloadDto createJwtPayloadDto(Users users, boolean rt) {
    JwtPayloadDto jwtPayloadDto = new JwtPayloadDto();
    jwtPayloadDto.setUserId(String.valueOf(users.getId()));
    jwtPayloadDto.setEmailId(users.getEmail());
    jwtPayloadDto.setMobileNumber(users.getMobileNumber());
    jwtPayloadDto.setFullName(users.getFullName());
    jwtPayloadDto.setActive(users.isStatus());
    jwtPayloadDto.setUserType(users.getUserType().name());
    if (rt) {
      jwtPayloadDto.setTokenType(TokenType.RT.name());
    } else {
      jwtPayloadDto.setTokenType(TokenType.AT.name());
    }
    return jwtPayloadDto;
  }

  private String generateJwtToken(JwtPayloadDto jwtPayloadDto) throws JsonProcessingException {
    UsernamePasswordAuthenticationToken authenticationToken;
    authenticationToken = new UsernamePasswordAuthenticationToken(
        jwtPayloadDto.getMobileNumber(),
        null
    );
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    return jwtAuthUtils.generateRefreshToken(authenticationToken, jwtPayloadDto);
  }
}
