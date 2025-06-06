package com.tektechno.payout.constant;

/**
 * A utility class that holds constant regular expressions for common patterns.
 * This class provides pre-defined regex patterns which can be used across applications
 * to validate specific types of strings such as mobile numbers and email addresses.
 * The constants in this class are immutable and thread-safe.
 *
 * @author kousik manik
 */
public class RegexConstant {

  public static final String MOBILE_NUMBER_REGEX = "^(\\+91[\\-\\s]?)?[0]?(91)?[6789]\\d{9}$";

  public static final String EMAIL_REGEX = "^[A-Za-z0-9._-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

  public static final String PASSWORD_REGEX =
      "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!])[A-Za-z\\d@#$%^&+=!]{8,}$";

  public static final String ABDM_PASSWORD_REGEX = "^(?=.[A-Z])(?=.\\d)(?=.[!@#$^-])[A-Za-z\\d!@#$%^&*-]{8,}$";
}
