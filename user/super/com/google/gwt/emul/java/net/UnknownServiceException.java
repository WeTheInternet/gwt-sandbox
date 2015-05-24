package java.net;

import java.io.IOException;

public class UnknownServiceException extends IOException {

  public UnknownServiceException() {

  }

  public UnknownServiceException(String message) {
    super(message);
  }
}
