package java.lang.ref;

// Extremely minimal emulation for references...
public class WeakReference<T> extends Reference<T> {

    public WeakReference(T referent) {
        super(referent);
    }

}
