
package java.lang.reflect;

import com.google.gwt.core.client.JavaScriptObject;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

/** 
 * A field representing a Long object.
 * 
 * @author "james@wetheinter.net"
 *
 */
public class LongField extends Field{

  public LongField(Class<?> declaringClass, String name, int modifiers,
      JavaScriptObject accessor, Supplier<Annotation[]> annos) {
    super(Long.class, declaringClass, name, modifiers, accessor, annos);
  }
  
    public long getLong(Object obj)
  throws IllegalArgumentException, IllegalAccessException
  {
    Object o = get(obj);
    maybeThrowNullGet(o, true);
    return (Long)o;
  }
    
    public void setLong(Object obj, long l)
  throws IllegalArgumentException, IllegalAccessException {
      set(obj, new Long(l));
    }

}
