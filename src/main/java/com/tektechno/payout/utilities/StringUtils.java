package com.tektechno.payout.utilities;

import org.springframework.stereotype.Component;

/**
 * String Utility class.
 *
 * @author Kousik Manik
 */
@Component
public class StringUtils {
  public static boolean isNotNullAndNotEmpty(String s) {
    return s != null && !s.isEmpty();
  }

  public static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }

}
