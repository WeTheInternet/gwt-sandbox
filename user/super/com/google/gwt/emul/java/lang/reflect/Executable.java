package java.lang.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.function.Supplier;

import com.google.gwt.lang.Array;

public abstract class Executable <E extends Executable> extends AccessibleObject
    implements Member, GenericDeclaration {

  private final Class[] parameterTypes;
  private final Class[] exceptionTypes;

  Executable(Class<?> declaringClass, String name, int modifier, Class<?>[] params,
      Class<?>[] exceptions, Supplier<Annotation[]> annotations) {
    super(declaringClass, name, modifier, annotations);
    this.parameterTypes = params;
    this.exceptionTypes = exceptions;
  }

  boolean equalParamTypes(Class<?>[] params1, Class<?>[] params2) {
    // In our generated code, our parameter class arrays will actually be the
    // same instance
    return params1 == params2;
  }

  Type[] getAllGenericParameterTypes() {
    // In the JVM, this includes synthetic parameters, which we do not support.
    return getGenericParameterTypes();
  }

  /**
   * Returns a clone of this constructor's exception types Class[]
   */
  public Class<?>[] getExceptionTypes() {
    return Array.clone((Class<?>[]) exceptionTypes);
  }

  /**
   * We do not currently support generic exception type information
   */
  public Type[] getGenericExceptionTypes() {
    throw new UnsupportedOperationException();
  }

  /**
   * We do not currently support generic parameter types
   */
  public Type[] getGenericParameterTypes() {
    throw new UnsupportedOperationException();
  }

  public int getParameterCount() {
    return parameterTypes.length;
  }

  /**
   * Returns a clone of the array parameter types
   */
  public Class<?>[] getParameterTypes() {
    return Array.clone((Class<?>[]) parameterTypes);
  }

  public Parameter[] getParameters() {
    // We do not yet support the Parameter object
    throw new UnsupportedOperationException();
  }

  /**
   * We do not currently support type parameters correctly
   */
  public TypeVariable<E>[] getTypeParameters() {
    throw new UnsupportedOperationException();
  }

  public Annotation[][] getParameterAnnotations() {
    throw new UnsupportedOperationException();
  }

  public AnnotatedType getAnnotatedReturnType() {
    throw new UnsupportedOperationException();
  }

  public AnnotatedType getAnnotatedReceiverType() {
    throw new UnsupportedOperationException();
  }

  public AnnotatedType[] getAnnotatedParameterTypes() {
    throw new UnsupportedOperationException();
  }

  public AnnotatedType[] getAnnotatedExceptionTypes() {
    throw new UnsupportedOperationException();
  }

  /**
   * Currently returns the same value as toString() because we do not yet support generic type information.
   */
  public String toGenericString() {
    return toString();
  }
  
  /**
   * Returns true if our modifiers contains the {@link Modifier#VARARGS} bit
   */
  public boolean isVarArgs() {
    return (getModifiers() & Modifier.VARARGS) != 0;
  }

}
