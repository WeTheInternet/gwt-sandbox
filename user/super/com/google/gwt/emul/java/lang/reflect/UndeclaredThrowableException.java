package java.lang.reflect;

public class UndeclaredThrowableException extends RuntimeException {

    private Throwable undeclared;

    public UndeclaredThrowableException(Throwable t) {
        super((Throwable)null);
        this.undeclared = t;
    }

    public UndeclaredThrowableException(Throwable t, String msg) {
        super(msg, (Throwable)null);
        this.undeclared = t;
    }

    public Throwable getUndeclaredThrowable() {
        return this.undeclared;
    }

    public Throwable getCause() {
        return this.undeclared;
    }
}
