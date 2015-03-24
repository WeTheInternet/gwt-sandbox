package java.lang.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.UnsafeNativeLong;

/**
 * This emulated constructor is designed to encapsulate the metadata normally
 * available from a JVM Constructor. It accepts a {@link JavaScriptObject} which
 * is a function that wraps a JSNI <code>@com.foo.pkg.Type::new()</code>
 * function, thus enabling the newInstance method.
 * <p>
 * In order to reduce the number of classes generated, this class is final and
 * accepts all metadata (Class[] parameters, modifier, signature, etc.) as a
 * constructor parameter.
 * <p>
 * Currently, not all metadata is accurately captured; type parameters / generic
 * info is lost, as are parameter annotations. These may be added in the future,
 * however, for now, we are only implementing the more commonly used bits of
 * metadata.
 */
public final class Constructor<T> extends Executable<Constructor<T>>
    implements GenericDeclaration, Member {

  private transient String signature;
  private JavaScriptObject method;

  private static final int LANGUAGE_MODIFIERS =
      Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE;

  /**
   * Public constructor to allow gwt to create constructors anywhere
   */
  public Constructor(Class<T> declaringClass, int modifiers, JavaScriptObject method, final Supplier<Annotation[]> annos,
      Class<?>[] params, Class<?>[] exceptions) {
    super(declaringClass, declaringClass.getSimpleName(), modifiers, params, exceptions, annos);
    this.method = method;
    // TODO implement this
    this.signature = "";
  }

  /**
   * Equality comparison checks if both constructors have the same decalaring class,
   * and the exact same instance of parameterTypes; because of the way our code generator
   * works, the parameterTypes arrays will point to the same reference.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Constructor) {
      Constructor other = (Constructor) obj;
      if (getDeclaringClass() == other.getDeclaringClass()) {
        // We can do referential equality here because our code generator emits
        // singletons for every possible Class[] that can be used.
        return getParameterTypes() == other.getParameterTypes();
      }
    }
    return false;
  }

  /**
   * @return our pre-computed signature.
   */
  String getSignature() {
    return signature;
  }

  /**
   * Invokes the javascript function pointing to the <code>@com.foo.pkg.Type::new</code> sent
   * to this constructor.  Parameter validation is ignored.  None of the declared exception
   * types are thrown, they are only there for conformance with JRE APIs (so you don't get
   * compile errors when you try to catch them).
   * <p>
   * The only declared exception we will throw is InvocationTargetException, as that is what
   * the JVM will do to wrap exceptions, and we want to match that behavior so client code
   * does not have to make special assumptions about how exceptions will be sent to them.
   */
  @UnsafeNativeLong
  public T newInstance(Object... initargs)
      throws InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException
  {
    try {
      return create(method, initargs);
    } catch (Throwable e) {
      throw new InvocationTargetException(e);
    }
  }

  /**
   * Returns a string representation of this constructor.
   */
  @Override
  public String toString() {
    // In javascript, string += is the faster way to build a string
    String s = "";
    int mod = getModifiers() & LANGUAGE_MODIFIERS;
    if (mod != 0) {
      s += Modifier.toString(mod) + " ";
    }
    s += Field.getTypeName(getDeclaringClass()) + "(";
    Class[] params = getParameterTypes(); 
    for (int i = 0; i < params.length; i++) {
      s += Field.getTypeName(params[i]);
      if (i < (params.length - 1))
        s += ",";
    }
    s += ")";
    Class[] exceptions = getExceptionTypes(); 
    if (exceptions.length > 0) {
      s += " throws ";
      for (int i = 0; i < exceptions.length; i++) {
        s += exceptions[i].getName();
        if (i < (exceptions.length - 1))
          s += ",";
      }
    }
    return s;
  }

  /**
   * Invokes our javascript function that wraps the <init> Constructor method we wrap.
   */
  @UnsafeNativeLong
  private static native <T> T create(JavaScriptObject func, Object[] args)
  /*-{
    return func.apply(null, args);
   }-*/;

}
