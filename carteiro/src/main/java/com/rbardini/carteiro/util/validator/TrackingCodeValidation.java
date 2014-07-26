package com.rbardini.carteiro.util.validator;

public class TrackingCodeValidation {
  public static enum Result {
    EMPTY,
    WRONG_LENGTH,
    BAD_FORMAT,
    INVALID_CHECK_DIGIT,
    SUCCESS
  }

  private final String cod;
  private final Result res;

  public TrackingCodeValidation(String cod, Result res) {
    this.cod = cod;
    this.res = res;
  }

  public String getCod() { return this.cod; }
  public Result getResult() { return this.res; }

  public boolean isValid() { return this.res == Result.SUCCESS; }
}
