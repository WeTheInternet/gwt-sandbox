package java.lang.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

/**
 * An {@link AnnotatedElement} in which {@link Annotation}s are declared on a  {@link Type}.
 * The {@link AnnotatedType#getType()} method returns the type which contains annotations.
 */
public interface AnnotatedType extends AnnotatedElement {

    public Type getType();
    
}
