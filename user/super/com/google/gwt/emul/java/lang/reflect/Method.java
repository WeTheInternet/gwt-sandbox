package java.lang.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.util.function.Supplier;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.UnsafeNativeLong;
import com.google.gwt.reflect.shared.ReflectUtil;

/**
 * A <code>Method</code> provides information about, and access to, a single method on a class or interface.
 * The reflected method may be a class method or an instance method (including an abstract method).
 * <p>
 * A <code>Method</code> permits widening conversions to occur when matching the actual parameters to invoke
 * with the underlying method's formal parameters, but it throws an <code>IllegalArgumentException</code> if a
 * narrowing conversion would occur.
 *
 * @see Member
 * @see java.lang.Class
 * @see java.lang.Class#getMethods()
 * @see java.lang.Class#getMethod(String, Class[])
 * @see java.lang.Class#getDeclaredMethods()
 * @see java.lang.Class#getDeclaredMethod(String, Class[])
 * @author Kenneth Russell
 * @author Nakul Saraiya
 */
public class Method extends Executable implements GenericDeclaration, Member {

  private Class returnType;
  // Generics and annotations support
  private transient String signature;

  private JavaScriptObject method;
  
  // Modifiers that can be applied to a method in source code
  private static final int LANGUAGE_MODIFIERS = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE |
    Modifier.ABSTRACT | Modifier.STATIC | Modifier.FINAL | Modifier.SYNCHRONIZED | Modifier.NATIVE;

  // Generics infrastructure

  private String getGenericSignature() {
    return signature;
  }

  public Method(Class<?> declaringClass, Class<?> returnType, String name, int modifiers, JavaScriptObject method, 
      final Supplier<Annotation[]> annos,
      Class<?>[] params, Class<?>[] exceptions) {
    super(declaringClass, name, modifiers, params, exceptions, annos);
    this.method = method;
    this.returnType = returnType;
    // TODO implement this
    this.signature = "";
  }
  
  /**
   * Returns a <code>Class</code> object that represents the formal return type of the method represented by
   * this <code>Method</code> object.
   *
   * @return the return type for the method this object represents
   */
  public Class<?> getReturnType() {
    return returnType;
  }

  /**
   */
  public Type getGenericReturnType() {
    // if (getGenericSignature() != null) {
    // return getGenericInfo().getReturnType();
    // } else {
    return getReturnType();
    // }
  }

  /**
   * Compares this <code>Method</code> against the specified object. Returns true if the objects are the same.
   * Two <code>Methods</code> are the same if they were declared by the same class and have the same name and
   * formal parameter types and return type.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj instanceof Method) {
      Method other = (Method)obj;
      if ((getDeclaringClass() == other.getDeclaringClass()) && (getName() == other.getName())) {
        if (!returnType.equals(other.getReturnType())) return false;
        Class[] params1 = getParameterTypes();
        Class[] params2 = other.getParameterTypes();
        return params1 == params2;
      }
    }
    return false;
  }

  /**
   * Returns a hashcode for this <code>Method</code>. The hashcode is computed as the exclusive-or of the
   * hashcodes for the underlying method's declaring class name and the method's name.
   */
  @Override
  public int hashCode() {
    return getDeclaringClass().getName().hashCode() ^ getName().hashCode();
  }

  /**
   */
  @Override
  public String toString() {
    try {
      StringBuffer sb = new StringBuffer();
      int mod = getModifiers() & LANGUAGE_MODIFIERS;
      if (mod != 0) {
        sb.append(Modifier.toString(mod) + " ");
      }
      sb.append(Field.getTypeName(getReturnType()) + " ");
      sb.append(Field.getTypeName(getDeclaringClass()) + ".");
      sb.append(getName() + "(");
      Class[] params = getParameterTypes();
      for (int j = 0; j < params.length; j++) {
        sb.append(Field.getTypeName(params[j]));
        if (j < (params.length - 1)) sb.append(",");
      }
      sb.append(")");
      Class[] exceptions = getExceptionTypes();
      if (exceptions != null && exceptions.length > 0) {
        sb.append(" throws ");
        for (int k = 0; k < exceptions.length; k++) {
          sb.append(exceptions[k].getName());
          if (k < (exceptions.length - 1)) sb.append(",");
        }
      }
      return sb.toString();
    } catch (Exception e) {
      return "<" + e + ">";
    }
  }

  /**
   */
  public String toGenericString() {
    return toString();
  }

  /**
   */
  @UnsafeNativeLong
  public Object invoke(Object obj, Object ... initargs) throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
    return call(method, obj, initargs);
  }

  @UnsafeNativeLong
  private native Object call(JavaScriptObject func, Object obj, Object[] args)
  /*-{
    // The method we're calling is "static", and takes obj as first parameter
    args.unshift(obj);
    // The rest of the args are accessed by generated param names A, B, C...
    return func.apply(this, args);
  }-*/;

  /**
   * Returns <tt>true</tt> if this method is a bridge method; returns <tt>false</tt> otherwise.
   *
   * @return true if and only if this method is a bridge method as defined by the Java Language Specification.
   * @since 1.5
   */
  public boolean isBridge() {
    return (getModifiers() & Modifier.BRIDGE) != 0;
  }

  /**
   */
  public Object getDefaultValue() {
    // if (annotationDefault == null)
    return null;
  }

}
