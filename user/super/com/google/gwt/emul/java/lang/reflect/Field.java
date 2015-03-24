package java.lang.reflect;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.reflect.shared.ReflectUtil;

/**
 * 
 */
public class Field extends AccessibleObject implements Member {

  private Class type;
  private transient String signature;

  private JavaScriptObject accessor;

  // Generics infrastructure

  private String getGenericSignature() {
    return signature;
  }

  public Field(Class returnType, Class declaringClass,
      String name, int modifiers, JavaScriptObject accessor, Supplier<Annotation[]> annos) {
    super(declaringClass, name, modifiers, annos);
    this.type = returnType;
    this.signature = "";
    this.accessor = accessor;
  }

  /**
   * Return true if the type of this field is that of an Enum
   */
  public boolean isEnumConstant() {
    return (getModifiers() & Modifier.ENUM) != 0;
  }

  /**
   */
  public Class<?> getType() {
    return type;
  }

  /**
   */
  public Type getGenericType() {
    return getType();
  }

  /**
   * Compares this <code>Field</code> against the specified object. Returns true
   * if the objects are the same. Two <code>Field</code> objects are the same if
   * they were declared by the same class and have the same name and type.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Field) {
      Field other = (Field) obj;
      return (getDeclaringClass() == other.getDeclaringClass())
          && (getName() == other.getName())
          && (getType() == other.getType());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
  }

  /**
   */
  @Override
  public String toString() {
    int mod = getModifiers();
    return (((mod == 0) ? "" : (Modifier.toString(mod) + " "))
        + getTypeName(getType()) + " "
        + getTypeName(getDeclaringClass()) + "."
        + getName());
  }

  public String toGenericString() {
    return toString();
  }

  protected native Object nativeGet(Object obj)
  /*-{
    return this.@java.lang.reflect.Field::accessor.getter(obj);
   }-*/;

  protected native void nativeSet(Object obj, Object value)
  /*-{
    this.@java.lang.reflect.Field::accessor.setter(obj, value);
   }-*/;

  protected void throwIllegalArg(Class received) throws IllegalArgumentException {
    throw new IllegalArgumentException("Cannot access field " + this + " as " + received.getName() + ", "
        + "as the underlying field is of type " + getType().getName());
  }

  protected void throwIllegalAccess(Object received) throws IllegalAccessException {
    throw new IllegalAccessException("Cannot set final " + this + " to " + received);
  }

  protected void throwNullNotAllowed() throws IllegalArgumentException {
    throw new IllegalArgumentException("Cannot set null values to " + this + ".");
  }

  protected void maybeThrowNullGet(Object o) throws IllegalArgumentException {
    maybeThrowNullGet(o, nullNotAllowed());
  }

  protected void maybeThrowNullGet(Object o, boolean noNull) throws IllegalArgumentException {
    if (noNull && o == null)
      throw new IllegalArgumentException("Cannot get null values from " + this + ".");
  }

  protected void maybeThrowNull(Object obj) {
    if (!Modifier.isStatic(getModifiers()) && obj == null)
      throw new NullPointerException();
  }

  protected void maybeThrowNull(Object obj, Object value) throws IllegalArgumentException {
    maybeThrowNull(obj);
    if (value == null) {
      if (nullNotAllowed()) {
        throw new IllegalArgumentException("Cannot set null to field " + this);
      }
    } else {
      maybeThrowNotAssignable(value);
    }
  }

  protected void maybeThrowNotAssignable(Object value) throws IllegalArgumentException {
    if (isNotAssignable(value.getClass())) {
      throw new IllegalArgumentException("Cannot assign object " + value + " of type " + value.getClass()
          + " to field " + this);
    }
  }

  protected void maybeThrowFinal(Object value) throws IllegalAccessException {
    if (Modifier.isFinal(getModifiers())) {
      throw new IllegalAccessException("Cannot assign object " + value + " of type " + value.getClass()
          + " to final field " + this);
    }
  }

  protected boolean isNotAssignable(Class<?> c) {
    // TODO remove the need for this .isPrimitive() using subclasses of Field
    return !getType().isPrimitive() && !getType().isAssignableFrom(c);
  }

  protected boolean nullNotAllowed() {
    return false;
  }

  public final Object get(Object obj)
      throws IllegalArgumentException, IllegalAccessException {
    maybeThrowNull(obj);
    return nativeGet(obj);
  }

  public boolean getBoolean(Object obj)
      throws IllegalArgumentException, IllegalAccessException
  {
    throwIllegalArg(boolean.class);
    return false;
  }

  public byte getByte(Object obj)
      throws IllegalArgumentException, IllegalAccessException {
    throwIllegalArg(byte.class);
    return 0;
  }

  public char getChar(Object obj)
      throws IllegalArgumentException, IllegalAccessException {
    throwIllegalArg(char.class);
    return 0;
  }

  public short getShort(Object obj)
      throws IllegalArgumentException, IllegalAccessException {
    throwIllegalArg(short.class);
    return 0;
  }

  public int getInt(Object obj)
      throws IllegalArgumentException, IllegalAccessException {
    throwIllegalArg(int.class);
    return 0;
  }

  public long getLong(Object obj)
      throws IllegalArgumentException, IllegalAccessException {
    throwIllegalArg(long.class);
    return 0;
  }

  public float getFloat(Object obj)
      throws IllegalArgumentException, IllegalAccessException {
    throwIllegalArg(float.class);
    return 0;
  }

  public double getDouble(Object obj)
      throws IllegalArgumentException, IllegalAccessException {
    throwIllegalArg(double.class);
    return 0;
  }

  public final void set(Object obj, Object value)
      throws IllegalArgumentException, IllegalAccessException {
    maybeThrowNull(obj, value);
    maybeThrowFinal(value);
    nativeSet(obj, value);
  }

  public void setBoolean(Object obj, boolean z)
      throws IllegalArgumentException, IllegalAccessException {
    throwIllegalArg(boolean.class);
  }

  public void setByte(Object obj, byte b)
      throws IllegalArgumentException, IllegalAccessException {
    throwIllegalArg(byte.class);
  }

  public void setChar(Object obj, char c)
      throws IllegalArgumentException, IllegalAccessException {
    throwIllegalArg(char.class);
  }

  public void setShort(Object obj, short s)
      throws IllegalArgumentException, IllegalAccessException {
    throwIllegalArg(short.class);
  }

  public void setInt(Object obj, int i)
      throws IllegalArgumentException, IllegalAccessException {
    throwIllegalArg(int.class);
  }

  public void setLong(Object obj, long l)
      throws IllegalArgumentException, IllegalAccessException {
    throwIllegalArg(long.class);
  }

  public void setFloat(Object obj, float f)
      throws IllegalArgumentException, IllegalAccessException {
    throwIllegalArg(float.class);
  }

  public void setDouble(Object obj, double d)
      throws IllegalArgumentException, IllegalAccessException {
    throwIllegalArg(double.class);
  }

  /*
   * Utility routine to paper over array type names
   */
  static String getTypeName(Class type) {
    if (type.isArray()) {
      try {
        Class cl = type;
        int dimensions = 0;
        while (cl.isArray()) {
          dimensions++;
          cl = cl.getComponentType();
        }
        StringBuffer sb = new StringBuffer();
        sb.append(cl.getName());
        for (int i = 0; i < dimensions; i++) {
          sb.append("[]");
        }
        return sb.toString();
      } catch (Throwable e) { /* FALLTHRU */
      }
    }
    return type.getName();
  }

}
