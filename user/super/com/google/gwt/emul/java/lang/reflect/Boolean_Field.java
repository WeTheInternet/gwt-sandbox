package java.lang.reflect;

import com.google.gwt.core.client.JavaScriptObject;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

/**
 * A field representing a primitive boolean.
 * 
 * @author "james@wetheinter.net"
 * 
 */
public final class Boolean_Field extends Field {

  public Boolean_Field(Class<?> declaringClass, String name, int modifiers, 
      JavaScriptObject accessor, Supplier<Annotation[]> annos) {
    super(boolean.class, declaringClass, name, modifiers, accessor, annos);
  }

  protected final Object nativeGet(Object obj) {
    return primitiveGet(obj) ? Boolean.TRUE : Boolean.FALSE;
  }

  protected final void nativeSet(Object obj, Object value) {
    primitiveSet(obj, (Boolean) value);
  }
  
  protected boolean isNotAssignable (Class<?> c) {
    return c != Boolean.class;
  }

  protected final native boolean primitiveGet(Object obj)
  /*-{
    return this.@java.lang.reflect.Field::accessor.getter(obj);
   }-*/;

  protected final native void primitiveSet(Object obj, boolean value)
  /*-{
    this.@java.lang.reflect.Field::accessor.setter(obj, value);
   }-*/;

  protected boolean nullNotAllowed() {
    return true;
  }

  public final boolean getBoolean(Object obj) throws IllegalArgumentException,
      IllegalAccessException {
    maybeThrowNull(obj);
    return primitiveGet(obj);
  }

  public final void setBoolean(Object obj, boolean z)
      throws IllegalArgumentException, IllegalAccessException {
    maybeThrowNull(obj);
    primitiveSet(obj, z);
  }

}
