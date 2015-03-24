package java.lang.reflect;

import com.google.gwt.core.client.JavaScriptObject;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

/**
 * A field representing a primitive char.
 * 
 * @author "james@wetheinter.net"
 * 
 */
public final class Char_Field extends Field {

  public Char_Field(Class<?> declaringClass, String name, int modifiers, 
      JavaScriptObject accessor, Supplier<Annotation[]> annos) {
    super(char.class, declaringClass, name, modifiers, accessor, annos);
  }

  protected final Object nativeGet(Object obj) {
    return new Character(primitiveGet(obj));
  }

  protected final void nativeSet(Object obj, Object value) {
    primitiveSet(obj, (Character) value);
  }
  
  protected boolean isNotAssignable (Class<?> c) {
    return c != Character.class;
  }

  protected final native char primitiveGet(Object obj)
  /*-{
    return this.@java.lang.reflect.Field::accessor.getter(obj);
   }-*/;

  protected final native void primitiveSet(Object obj, char value)
  /*-{
    this.@java.lang.reflect.Field::accessor.setter(obj, value);
   }-*/;

  protected boolean nullNotAllowed() {
    return true;
  }

  public final char getChar(Object obj) throws IllegalArgumentException,
      IllegalAccessException {
    maybeThrowNull(obj);
    return primitiveGet(obj);
  }

  public final void setChar(Object obj, char c)
      throws IllegalArgumentException, IllegalAccessException {
    maybeThrowNull(obj);
    primitiveSet(obj, c);
  }

}
