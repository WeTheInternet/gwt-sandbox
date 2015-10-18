package java.io;

  public class InputStream {

    private static final int SKIP_BUFFER_SIZE = 2048;
    private static byte[] skipBuffer;
    /**
     * The next position to read.
     */
    private int position;

    /**
     * The text to stream.
     */
    protected String text;

    public InputStream() {
      super();
    }

    /**
     * Constructor.
     *
     * @param text
     */
    public InputStream(String text) {
      super();
      this.position = 0;
      this.text = text;
    }

    /**
     * @return
     * @throws IOException
     */
    public int available() throws IOException {
      if (text != null) {
        return text.length();
      }

      return 0;
    }

    public String getText() {
      return text;
    }

    public void setText(String text) {
      this.text = text;
    }

    public void close() throws IOException {

    }

    /**
     * Reads the next character in the source text.
     *
     * @return The next character or -1 if end of text is reached.
     * @throws IOException
     */
    public int read() throws IOException {
      return (this.position == this.text.length()) ? -1 : this.text
          .charAt(this.position++);
    }

    /**
     * @param cbuf
     * @return
     * @throws IOException
     */
    public int read(char[] cbuf) throws IOException {
      return read(cbuf, 0, cbuf.length);
    }

    /**
     * @param cbuf
     * @param off
     * @param len
     * @return
     * @throws IOException
     */
    public int read(char[] cbuf, int off, int len) throws IOException {
      if (position >= text.length())
        return -1;
      int n = Math.min(text.length() - position, len);
      text.getChars(position, position + n, cbuf, off);
      position += n;
      return n;
    }

    public boolean markSupported() {
      return false;
    }

    public synchronized void reset() throws IOException {
      throw new IOException("mark/reset not supported");
    }

    public synchronized void mark(int readlimit) {}

    public int read(byte b[]) throws IOException {
      return read(b, 0, b.length);
    }

    public int read(byte b[], int off, int len) throws IOException {
      if (b == null) {
        throw new NullPointerException();
      } else if (off < 0 || len < 0 || len > b.length - off) {
        throw new IndexOutOfBoundsException();
      } else if (len == 0) {
        return 0;
      }

      int c = read();
      if (c == -1) {
        return -1;
      }
      b[off] = (byte) c;

      int i = 1;
      try {
        for (; i < len; i++) {
          c = read();
          if (c == -1) {
            break;
          }
          b[off + i] = (byte) c;
        }
      } catch (IOException ee) {
      }
      return i;
    }

    public long skip(long n) throws IOException {

      long remaining = n;
      int nr;
      if (skipBuffer == null)
        skipBuffer = new byte[SKIP_BUFFER_SIZE];

      byte[] localSkipBuffer = skipBuffer;

      if (n <= 0) {
        return 0;
      }

      while (remaining > 0) {
        nr = read(
            localSkipBuffer, 0,
            (int) Math.min(SKIP_BUFFER_SIZE, remaining)
        );
        if (nr < 0) {
          break;
        }
        remaining -= nr;
      }

      return n - remaining;
    }

  }
