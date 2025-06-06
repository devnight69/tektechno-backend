package com.tektechno.payout.config;


import com.tektechno.payout.dto.error.CustomErrorResponse;
import com.tektechno.payout.response.ResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;


/**
 * CustomAuthenticationEntryPoint is a custom implementation of Spring Security's
 * AuthenticationEntryPoint interface. It is used to handle unauthorized access attempts.
 * This class sends a custom error response to the client when the user is not authenticated
 * or the provided token is invalid or expired.
 * The commence method is triggered whenever an authentication exception is thrown
 * during the request processing. It constructs a response in JSON format containing
 * the error details, status, and a user-friendly error message.
 *
 * @author kousik manik
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
                       AuthenticationException authException) throws IOException {

    CustomErrorResponse customErrorResponse = new CustomErrorResponse("TEK-TECHNO-401",
        "Unauthorized: The provided token is invalid or expired. Please log in again.");
    ResponseDto responseDto = new ResponseDto();
    responseDto.setResponse(false);
    responseDto.setStatus(HttpStatus.UNAUTHORIZED);
    responseDto.setData(customErrorResponse);
    responseDto.setMessage("You need to be authenticated to access this resource.");

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(responseDto.toJson());
  }
}
