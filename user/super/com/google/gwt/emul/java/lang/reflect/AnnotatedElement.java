package java.lang.reflect;

import com.google.gwt.core.ext.UnableToCompleteException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import sun.reflect.annotation.AnnotationSupport;
import sun.reflect.annotation.AnnotationType;

/**
 * Emulation layer compatibility for AnnotatedElement
 * 
 * @originalauthor Josh Bloch
 * @author "James X. Nelson (james@wetheinter.net)"
 */
public interface AnnotatedElement {
  /**
   * Returns true if an annotation for the specified type is present on this
   * element, else false.
   */
  default boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
    for (Annotation annotation : getAnnotations()) {
      if (annotation.annotationType() == annotationClass) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns this element's annotation for the specified type if such an
   * annotation is present, else null.
   */
  <T extends Annotation> T getAnnotation(Class<T> annotationClass);

  /**
   * Returns all annotations present on this element. (Returns an array of
   * length zero if this element has no annotations.)
   */
  Annotation[] getAnnotations();

  Annotation[] getDeclaredAnnotations();

  default <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
    throw new UnsupportedOperationException();
  }

  default <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
    Objects.requireNonNull(annotationClass);
    for (Annotation annotation : getDeclaredAnnotations()) {
      if (annotation.annotationType() == annotationClass) {
        return (T)annotation;
      }
    }
    return null;
  }

  default <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
    throw new UnsupportedOperationException();
  }

}
