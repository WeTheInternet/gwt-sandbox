package java.security;

/**
 * Here for JRE emulation only
 */
public class PrivilegedActionException extends Exception {
    // use serialVersionUID from JDK 1.2.2 for interoperability
    private static final long serialVersionUID = 4724086851538908602L;

    /**
     * @serial
     */
    private Exception exception;

    /**
     * Constructs a new PrivilegedActionException &quot;wrapping&quot;
     * the specific Exception.
     *
     * @param exception The exception thrown
     */
    public PrivilegedActionException(Exception exception) {
        super((Throwable)null);  // Disallow initCause
        this.exception = exception;
    }

    /**
     * Returns the exception thrown by the privileged computation that
     * resulted in this <code>PrivilegedActionException</code>.
     *
     * <p>This method predates the general-purpose exception chaining facility.
     * The {@link Throwable#getCause()} method is now the preferred means of
     * obtaining this information.
     *
     * @return the exception thrown by the privileged computation that
     *         resulted in this <code>PrivilegedActionException</code>.
     * @see PrivilegedExceptionAction
     * @see AccessController#doPrivileged(PrivilegedExceptionAction)
     * @see AccessController#doPrivileged(PrivilegedExceptionAction,
     *                                            AccessControlContext)
     */
    public Exception getException() {
        return exception;
    }

    /**
     * Returns the cause of this exception (the exception thrown by
     * the privileged computation that resulted in this
     * <code>PrivilegedActionException</code>).
     *
     * @return  the cause of this exception.
     * @since   1.4
     */
    public Throwable getCause() {
        return exception;
    }

    public String toString() {
        String s = getClass().getName();
        return (exception != null) ? (s + ": " + exception.toString()) : s;
    }
}

