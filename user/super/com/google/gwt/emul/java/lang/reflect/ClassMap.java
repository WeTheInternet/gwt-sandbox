package java.lang.reflect;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A very simple {@link JavaScriptObject} dictionary that uses classnames as keys.
 */
class ClassMap <T> extends JavaScriptObject {
  
  protected ClassMap() {}
  
  static <T> ClassMap<T> newMap() {
    return JavaScriptObject.createObject().cast();
  }
  
  final native T get(Class<?> c)
  /*-{
     return this[c.@java.lang.Class::getName()()];
  }-*/;

  final native void put(Class<?> c, T t)
  /*-{
     this[c.@java.lang.Class::getName()()] = t;
  }-*/;
  
}