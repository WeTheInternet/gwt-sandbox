package java.lang.reflect;

/**
 * Standard emulation for GenericArrayType
 */
public interface GenericArrayType extends Type {
    Type getGenericComponentType();
}
