package org.alfredlibrary;

@SuppressWarnings("serial")
public class AlfredException extends RuntimeException {

  public AlfredException() {
    super();
  }

  public AlfredException(final String message) {
    super(message);
  }

  public AlfredException(final Throwable cause) {
    super(cause);
  }

  public AlfredException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
