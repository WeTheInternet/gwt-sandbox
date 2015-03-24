package java.lang.reflect;

import com.google.gwt.core.client.JavaScriptObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

/**
 * The AccessibleObject class is the superclass for Methods, Constructors and
 * Fields. It handles the mapping and returning of annotations in a standardized
 * way.
 * <p>
 * It receives, as a constructor parameter, a {@link Supplier} of
 * {@link Annotation} objects.
 * <p>
 * This allows us to defer actually initializing the annotations until they are
 * requested.
 * <p>
 * This also allows us to defer mapping the annotations by class until they are
 * requested by class.
 * <p>
 * Note that this class does a bit more than the JVM version of
 * AccessibleObject; in addition to handling annotations and accessibility, we
 * also handle all metadata that is shared between {@link Field}, {@link Method}
 * and {@link Constructor}. Note that functionality share between only Method
 * and Constructor will go into the {@link Executable} class instead.
 */
public class AccessibleObject implements AnnotatedElement {

  /**
   * No-op in gwt; isAccessible returns true.
   */
  public static void setAccessible(AccessibleObject[] array, boolean flag)
  {
    // no-op in gwt. Here for api compatibility;
  }

  /**
   * No-op in gwt; isAccessible returns true.
   */
  public final void setAccessible(boolean flag) /* throws SecurityException */{
    // provided only for emulated compatibility.
    // everything is accessible in gwt.
  }

  /**
   * All objects are accessible in gwt.
   */
  public final boolean isAccessible() {
    return true;
  }

  private final Supplier<Annotation[]> annotationSupplier;
  private final Class<?> declaringClass;
  private final int modifiers;
  private final String name;
  private JavaScriptObject annotationMap;

//  /**
//   * This constructor should be deleted once Contructor, Method and Field send
//   * Supplier<Annotion[]> to us.
//   */
//  protected AccessibleObject() {
//    this.annotationSupplier = null;
//    this.declaringClass = null;
//    this.name = null;
//    this.modifiers = 0;
//  }

  protected AccessibleObject(Class<?> declaringClass, String name, int modifiers, Supplier<Annotation[]> annotationSupplier) {
    this.annotationSupplier = annotationSupplier;
    this.declaringClass = declaringClass;
    this.modifiers = modifiers;
    this.name = name;
  }

  public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
    nullCheck(annotationClass);
    initAnnoMap();
    return getFromMap(annotationClass.getName(), annotationMap);
  }

  public Annotation[] getAnnotations() {
    return annotationSupplier.get();
  }

  public Annotation[] getDeclaredAnnotations() {
    return annotationSupplier.get();
  }
  
  /**
   * Returns the {@link Class} object representing the class or interface that
   * declares this executable
   */
  public Class<?> getDeclaringClass() {
    return declaringClass;
  }

  /**
   * Returns the Java language modifiers for the constructor represented by this AccesibleOject,
   * as an integer. The {@link Modifier} class should be used to decode the modifiers.
   *
   * @see Modifier
   */
  public int getModifiers() {
    return modifiers;
  }
  

  /**
   * Returns the name of this constructor, as a string. This is always the same
   * as the simple name of the constructor's declaring class.
   */
  public String getName() {
    return name;
  }
  
  /**
   * Returns a hashcode for this AccessibleObject. The default hashcode is the
   * same as the hashcode for the declaring class name.
   */
  @Override
  public int hashCode() {
    return getDeclaringClass().getName().hashCode();
  }
  
  public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
    nullCheck(annotationClass);
    initAnnoMap();
    return isInMap(annotationClass.getName(), annotationMap);
  }

  /**
   * Returns true if our modifiers contains the synthetic bit.
   */
  public boolean isSynthetic() {
    return Modifier.isSynthetic(getModifiers());
  }

  private final native <T extends Annotation> T getFromMap(String name, JavaScriptObject map)
  /*-{
     return map[name];
   }-*/;

  private final void initAnnoMap() {
    if (annotationMap == null) {
      annotationMap = fillMap(annotationSupplier.get(), JavaScriptObject.createObject());
    }
  }

  private final native boolean isInMap(String name, JavaScriptObject map)
  /*-{
     return map[name] !== undefined;
   }-*/;

  private final void nullCheck(Class<?> clazz) {
    if (clazz == null) {
      throw new NullPointerException();
    }
  }

  private final native JavaScriptObject fillMap(Annotation[] annos, JavaScriptObject into)
  /*-{
     for (var i = annos.length; i --> 0; ) {
       var anno = annos[i];
       var name = anno.@java.lang.annotation.Annotation::annotationType()().@java.lang.Class::getName()();
       into[name] = anno;
     }
     return into;
  }-*/;

}
