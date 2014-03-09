package com.rbardini.carteiro.util.validator;

import java.util.Locale;

import com.rbardini.carteiro.util.validator.TrackingCodeValidation.Result;

public class TrackingCodeValidator {
  private static final int LENGTH = 13;
  private static final String FORMAT_REGEX = "^[A-Z]{2}[0-9]{9}[A-Z]{2}$";
  private static final int[] NUMERIC_WEIGHTS = {8, 6, 4, 2, 3, 5, 9, 7};

  public static boolean isValid(String cod) {
    return validate(cod).getResult() == Result.SUCCESS;
  }

  public static TrackingCodeValidation validate(String cod) {
    if (cod == null) {
      return new TrackingCodeValidation(cod, Result.EMPTY);
    }

    cod = cod.trim();
    if (cod.length() == 0) {
      return new TrackingCodeValidation(cod, Result.EMPTY);
    }

    if (cod.length() != LENGTH) {
      return new TrackingCodeValidation(cod, Result.WRONG_LENGTH);
    }

    cod = cod.toUpperCase(Locale.getDefault());
    if (!cod.matches(FORMAT_REGEX)) {
      return new TrackingCodeValidation(cod, Result.BAD_FORMAT);
    }

    if (!validateCheckDigit(cod)) {
      return new TrackingCodeValidation(cod, Result.INVALID_CHECK_DIGIT);
    }

    return new TrackingCodeValidation(cod, Result.SUCCESS);
  }

  private static boolean validateCheckDigit(String cod) {
    int sum = 0, mod, dv;

    for (int i=0; i<8; i++) {
      int numericValue = Character.getNumericValue(cod.charAt(i+2));
      sum += numericValue * NUMERIC_WEIGHTS[i];
    }

    mod = sum % 11;
    dv = (mod == 0) ? 5 : (mod == 1) ? 0 : 11 - mod;

    return dv == Character.getNumericValue(cod.charAt(10));
  }
}
