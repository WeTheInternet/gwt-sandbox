package java.io;


/**
 * Present for Gwt compatibility
 */
public class FileNotFoundException extends IOException {
  private static final long serialVersionUID = -897856973823710492L;

  public FileNotFoundException() {
    super();
  }

  public FileNotFoundException(String s) {
    super(s);
  }

  private FileNotFoundException(String path, String reason) {
    super(path + ((reason == null)
              ? ""
              : " (" + reason + ")"));
  }

}
