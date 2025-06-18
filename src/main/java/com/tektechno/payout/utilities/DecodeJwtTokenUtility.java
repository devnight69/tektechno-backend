package com.tektechno.payout.utilities;

import com.tektechno.payout.dto.jwt.JwtPayloadDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class for decoding information from a JWT (JSON Web Token) stored in the security context.
 * This class helps retrieve decrypted user-specific data such as ULID or user ID of the authenticated user.
 * that JWT payload is managed using {@link JwtPayloadDto}.
 * The utility ensures that the JWT payload is accessed from the current authentication context, validates
 * user authentication, and handles any null or invalid data gracefully.
 * The key methods provided by this class include:
 * - Retrieving and decrypting the authenticated user's ULID.
 * - Retrieving and decrypting the authenticated user's user ID.
 * This class is designed for use in token-based authentication systems where JWTs are utilized
 * for storing and transmitting user information securely.
 *
 * @author kousik manik
 */
@Component
public class DecodeJwtTokenUtility {

  /**
   * Retrieves the user ID of the authenticated user.
   * Extracts the user ID from the JWT payload stored in the security context, decrypts it,
   * and returns it as a Long. If authentication is null, not authenticated, or the user ID
   * cannot be retrieved or decrypted, it returns null.
   *
   * @return The decrypted user ID as a Long if available and successfully decrypted, otherwise null.
   */
  public Long getUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }

    JwtPayloadDto jwtPayloadDto = (JwtPayloadDto) authentication.getPrincipal();

    if (jwtPayloadDto == null) {
      return null;
    }

    try {
      return Long.parseLong(jwtPayloadDto.getUserId());
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Retrieves the member ID of the authenticated user.
   * Extracts the member ID from the JWT payload stored in the security context.
   * If authentication is null, not authenticated, or the JWT payload is null,
   * or any exception occurs, it returns null.
   *
   * @return The member ID as a String if available, otherwise null.
   */
  public String getMemberId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }

    JwtPayloadDto jwtPayloadDto = (JwtPayloadDto) authentication.getPrincipal();

    if (jwtPayloadDto == null) {
      return null;
    }

    try {
      return jwtPayloadDto.getMemberId();
    } catch (Exception e) {
      return null;
    }
  }

}
