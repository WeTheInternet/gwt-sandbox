package com.google.gwt.reflect.shared;

import java.lang.reflect.Method;

public final class GwtReflectJre {

  public static Package getPackage(final String name) {
    throw new UnsupportedOperationException();
  }

  public static Package getPackage(final String name, final ClassLoader cl) {
    throw new UnsupportedOperationException();
  }

  public static Object invokeDefaultMethod(final Method method, final Object[] params) throws Throwable {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  public static <T> T[] newArray(final Class<T> classLit, final int size) {
    throw new UnsupportedOperationException();
  }


  @SuppressWarnings("unchecked")
  public static <T> T[][] newArray(final Class<T> classLit, final int dim1, final int dim2) {
    throw new UnsupportedOperationException();
  }

  private GwtReflectJre() {}
}
