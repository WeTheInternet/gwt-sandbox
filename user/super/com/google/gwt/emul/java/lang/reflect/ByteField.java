
package java.lang.reflect;

import com.google.gwt.core.client.JavaScriptObject;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

/** 
 * A field representing a Byte object.
 * 
 * @author "james@wetheinter.net"
 *
 */
public class ByteField extends Field{

  public ByteField(Class<?> declaringClass, String name, int modifiers, 
      JavaScriptObject accessor, Supplier<Annotation[]> annos) {
    super(Byte.class, declaringClass, name, modifiers, accessor, annos);
  }
  
//    public void setByte(Object obj, byte b)
//  throws IllegalArgumentException, IllegalAccessException {
//      set(obj, new Byte(b));
//    }

}
