package java.io;

import java.util.Objects;
import java.util.Locale;

/**
 * Emulated PrintWriter with minimal support
 */

public class PrintWriter extends Writer {

  protected Writer out;

  private final boolean autoFlush;
  private boolean trouble = false;
  private PrintStream psOut = null;

  private final String lineSeparator;

  public PrintWriter (Writer out) {
    this(out, false);
  }

  public PrintWriter(Writer out,
                     boolean autoFlush) {
    super(out);
    this.out = out;
    this.autoFlush = autoFlush;
    lineSeparator = "/";
  }

  public PrintWriter(OutputStream out) {
    this(out, false);
  }

  public PrintWriter(OutputStream out, boolean autoFlush) {
    throw new UnsupportedOperationException();
  }

  public PrintWriter(String fileName) throws FileNotFoundException {
    throw new UnsupportedOperationException();
  }

  public PrintWriter(String fileName, String csn)
      throws FileNotFoundException, UnsupportedEncodingException
  {
    throw new UnsupportedOperationException();
  }

  private void ensureOpen() throws IOException {
    if (out == null)
      throw new IOException("Stream closed");
  }

  public void flush() {
    try {
      ensureOpen();
      out.flush();
    }
    catch (IOException x) {
      trouble = true;
    }
  }

  public void close() {
    try {
      if (out == null)
        return;
      out.close();
      out = null;
    }
    catch (IOException x) {
      trouble = true;
    }
  }

  public boolean checkError() {
    if (out != null) {
      flush();
    }
    if (out instanceof java.io.PrintWriter) {
      PrintWriter pw = (PrintWriter) out;
      return pw.checkError();
    } else if (psOut != null) {
      return psOut.checkError();
    }
    return trouble;
  }

  protected void setError() {
    trouble = true;
  }

  protected void clearError() {
    trouble = false;
  }

  public void write(int c) {
    try {
      ensureOpen();
      out.write(c);
    }
    catch (IOException x) {
      trouble = true;
    }
  }

  public void write(char buf[], int off, int len) {
    try {
      ensureOpen();
      out.write(buf, off, len);
    }
    catch (IOException x) {
      trouble = true;
    }
  }

  public void write(char buf[]) {
    write(buf, 0, buf.length);
  }

  public void write(String s, int off, int len) {
    try {
      ensureOpen();
      out.write(s, off, len);
    }
    catch (IOException x) {
      trouble = true;
    }
  }

  public void write(String s) {
    write(s, 0, s.length());
  }

  private void newLine() {
    try {
      ensureOpen();
      out.write(lineSeparator);
      if (autoFlush)
        out.flush();
    }
    catch (IOException x) {
      trouble = true;
    }
  }

  public void print(boolean b) {
    write(b ? "true" : "false");
  }

  public void print(char c) {
    write(c);
  }

  public void print(int i) {
    write(String.valueOf(i));
  }

  public void print(long l) {
    write(String.valueOf(l));
  }

  public void print(float f) {
    write(String.valueOf(f));
  }

  public void print(double d) {
    write(String.valueOf(d));
  }

  public void print(char s[]) {
    write(s);
  }

  public void print(String s) {
    if (s == null) {
      s = "null";
    }
    write(s);
  }

  public void print(Object obj) {
    write(String.valueOf(obj));
  }

  public void println() {
    newLine();
  }

  public void println(boolean x) {
    print(x);
    println();
  }

  public void println(char x) {
    print(x);
    println();
  }

  public void println(int x) {
    print(x);
    println();
  }

  public void println(long x) {
    print(x);
    println();
  }

  public void println(float x) {
    print(x);
    println();
  }

  public void println(double x) {
    print(x);
    println();
  }

  public void println(char x[]) {
    print(x);
    println();
  }

  public void println(String x) {
    print(x);
    println();
  }

  public void println(Object x) {
    String s = String.valueOf(x);
    print(s);
    println();
  }

  public PrintWriter printf(String format, Object ... args) {
    return format(format, args);
  }

  public PrintWriter printf(Locale l, String format, Object ... args) {
    throw new UnsupportedOperationException();
  }

  public PrintWriter format(String format, Object ... args) {
    try {
      ensureOpen();
      write(String.format(format, args));
      if (autoFlush)
        out.flush();
    } catch (IOException x) {
      trouble = true;
    }
    return this;
  }

  public PrintWriter format(Locale l, String format, Object ... args) {
    throw new UnsupportedOperationException();
  }

  public PrintWriter append(CharSequence csq) {
    if (csq == null)
      write("null");
    else
      write(csq.toString());
    return this;
  }

  public PrintWriter append(CharSequence csq, int start, int end) {
    CharSequence cs = (csq == null ? "null" : csq);
    write(cs.subSequence(start, end).toString());
    return this;
  }

  public PrintWriter append(char c) {
    write(c);
    return this;
  }
}
