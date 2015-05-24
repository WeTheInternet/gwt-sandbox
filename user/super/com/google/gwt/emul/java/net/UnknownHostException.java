package java.net;

import java.io.IOException;

public class UnknownHostException extends IOException {
    public UnknownHostException(String message) {
        super(message);
    }
    public UnknownHostException() {
    }
}
