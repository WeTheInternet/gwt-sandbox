
package java.lang.reflect;

import com.google.gwt.core.client.JavaScriptObject;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

/** 
 * A field representing a Boolean object.
 * 
 * @author "james@wetheinter.net"
 *
 */
public class BooleanField extends Field{

  public BooleanField(Class<?> declaringClass, String name, int modifiers, 
      JavaScriptObject accessor, Supplier<Annotation[]> annos) {
    super(Boolean.class, declaringClass, name, modifiers, accessor, annos);
  }
  
    public boolean getBoolean(Object obj)
  throws IllegalArgumentException, IllegalAccessException
  {
    Object o = get(obj);
    maybeThrowNullGet(o, true);
    return (Boolean)o;
  }
    
    public void setBoolean(Object obj, boolean z)
  throws IllegalArgumentException, IllegalAccessException {
      set(obj, z ? Boolean.TRUE : Boolean.FALSE);
    }

}
