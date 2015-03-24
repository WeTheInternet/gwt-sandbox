package java.lang;

/**
 * Present for JRE support where users may want to catch this when running in
 * JVMs
 */
public class UnsatisfiedLinkError extends LinkageError {
  public UnsatisfiedLinkError() {
    super();
  }

  public UnsatisfiedLinkError(String s) {
    super(s);
  }
}
