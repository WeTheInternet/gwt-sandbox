
package java.lang.reflect;

import com.google.gwt.core.client.JavaScriptObject;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

/** 
 * A field representing a Float object.
 * 
 * @author "james@wetheinter.net"
 *
 */
public class FloatField extends Field{

  public FloatField(Class<?> declaringClass, String name, int modifiers,
      JavaScriptObject accessor, Supplier<Annotation[]> annos) {
    super(Float.class, declaringClass, name, modifiers, accessor, annos);
  }
  
    public float getFloat(Object obj)
  throws IllegalArgumentException, IllegalAccessException
  {
    Object o = get(obj);
    maybeThrowNullGet(o, true);
    return (Float)o;
  }
    
    public void setFloat(Object obj, float f)
  throws IllegalArgumentException, IllegalAccessException {
      set(obj, new Float(f));
    }

}
