
package java.lang.reflect;

import com.google.gwt.core.client.JavaScriptObject;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

/** 
 * A field representing a Character object.
 * 
 * @author "james@wetheinter.net"
 *
 */
public class CharacterField extends Field{

  public CharacterField(Class<?> declaringClass, String name, int modifiers,
      JavaScriptObject accessor, Supplier<Annotation[]> annos) {
    super(Character.class, declaringClass, name, modifiers, accessor, annos);
  }
  
    public char getChar(Object obj)
  throws IllegalArgumentException, IllegalAccessException
  {
    Object o = get(obj);
    maybeThrowNullGet(o, true);
    return (Character)o;
  }
    
    public void setChar(Object obj, char c)
  throws IllegalArgumentException, IllegalAccessException {
      set(obj, new Character(c));
    }

}
