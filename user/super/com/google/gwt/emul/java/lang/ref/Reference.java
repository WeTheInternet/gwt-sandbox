package java.lang.ref;

// Extremely minimal emulation for references.
public abstract class Reference<T> {
    private T value;
    public T get() {
        return this.value;
    }
    public void clear() {
        this.value = null;
    }
    Reference(T referent) {
        this.value = referent;
    }

}
