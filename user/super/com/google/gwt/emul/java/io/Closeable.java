package java.io;

import java.io.IOException;

public interface Closeable extends AutoCloseable {

  /**
   * Does nothing in GWT
   */
  default void close() throws IOException {
  }

}
