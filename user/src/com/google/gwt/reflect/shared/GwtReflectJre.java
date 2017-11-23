package com.google.gwt.reflect.shared;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;

public final class GwtReflectJre {

  public static Package getPackage(final String name) {
    return getPackage(name, Thread.currentThread().getContextClassLoader());
  }

  public static Package getPackage(final String name, final ClassLoader cl) {
    Package pkg = Package.getPackage(name);
    if (pkg == null) {
      final String pkgInfo = name.replace('.', '/')+"/package-info.class";
      final URL loc = Thread.currentThread().getContextClassLoader().getResource(pkgInfo);
      if (loc != null) {
        try {
          cl.loadClass(name+".package-info");
          pkg = Package.getPackage(name);
        } catch (final ClassNotFoundException ignored) {}
      }
    }
    // TODO: try using reflection to get access to the constructor we would need to create-a-package.
    // (perhaps going as far as taking a byte[] template of a generic package,
    // and just swapping out the name attributes).
    return pkg;
  }

  /**
   * Do nefarious things to be able to call a default method without an instance.
   *
   * This is ONLY safe to do on methods without side effects,
   * i.e.: {@code default String getMyPrefix() { return "foo" }}
   * behavior is undefined for attempting to invoke other instance methods inside
   * default methods that are reflectively invoked with this method.
   *
   * Use at your own risk (be sure to test... Without using mocks, preferably).
   */
  public static Object invokeDefaultMethod(final Method method, final Object[] params) throws Throwable {
    @SuppressWarnings("Convert2Lambda") // we compile to java 1.7
    final Object t=  java.lang.reflect.Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
        new Class<?>[]{method.getDeclaringClass()}, new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method foreign, Object[] args) throws Throwable {
            // TODO: actually handle chained calls
            assert method.getName().equals(foreign.getName()) : "Calling unhandled method named " + foreign.toGenericString();
            assert method.toGenericString().equals(foreign.toGenericString()) : "Calling unhandled method named " + foreign.toGenericString();
            return null;
          }
        }
    );

    final Field field = java.lang.invoke.MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
    field.setAccessible(true);
    final java.lang.invoke.MethodHandles.Lookup lookup = (java.lang.invoke.MethodHandles.Lookup) field.get(null);
    final Object value = lookup
        .unreflectSpecial(method, method.getDeclaringClass())
        .bindTo(t)
        .invokeWithArguments();
    return value;
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] newArray(final Class<T> classLit, final int size) {
    return (T[])Array.newInstance(classLit, size);
  }


  @SuppressWarnings("unchecked")
  public static <T> T[][] newArray(final Class<T> classLit, final int dim1, final int dim2) {
    return (T[][])Array.newInstance(classLit, dim1, dim2);
  }

  private GwtReflectJre() {}
}
