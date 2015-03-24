package java.lang.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Parameters contain information about method and constructor parameters.
 * It has the name, modifiers, index of the parameter, and the {@link Executable}
 * which owns the Parameter.
 *
 */
public final class Parameter implements AnnotatedElement {

    private final String name;
    private final int modifiers;
    private final Executable executable;
    private final int index;

    private AnnotatedType annotatedType = null;
    private Type type = null;
    private Class<?> clazz = null;
    private ClassMap<Annotation> annotations;

    Parameter(String name,
              int modifiers,
              Executable executable,
              int index) {
        this.name = name;
        this.modifiers = modifiers;
        this.executable = executable;
        this.index = index;
    }

    public boolean equals(Object o) {
        if(o instanceof Parameter) {
            Parameter obj = (Parameter)o;
            return (obj.executable.equals(executable) &&
                    obj.index == index);
        }
        return false;
    }

    public int hashCode() {
        return executable.hashCode() ^ index;
    }

    public boolean isNamePresent() {
        return name != null;
    }

    public String toString() {
        String s = "";
        final Type type = getType();// getParameterizedType();
        final String typename = type.getTypeName();

        s += Modifier.toString(getModifiers());

        if(0 != modifiers) {
          s += " ";
        }

        if(isVarArgs()) {
          s += typename.replaceFirst("\\[\\]$", "...");
        }
        else {
          s += typename;
        }

        s += " ";
        s += getName();

        return s;
    }

    /**
     * Return the {@code Executable} that owns this parameter.
     */
    public Executable getDeclaringExecutable() {
        return executable;
    }

    public int getModifiers() {
        return modifiers;
    }

    public String getName() {
      return name == null ? "arg" + index : name;
    }

    public Type getParameterizedType() {
        if (type == null) {
          type = executable.getAllGenericParameterTypes()[index];
        }
        return type;
    }

    public Class<?> getType() {
        if (clazz == null) {
            clazz = executable.getParameterTypes()[index];
        }
        return clazz;
    }

    public AnnotatedType getAnnotatedType() {
      if (annotatedType == null) {
        annotatedType = executable.getAnnotatedParameterTypes()[index];
      }
      return annotatedType;
    }

    public boolean isImplicit() {
        return Modifier.isMandated(getModifiers());
    }

    public boolean isSynthetic() {
        return Modifier.isSynthetic(getModifiers());
    }

    public boolean isVarArgs() {
        return executable.isVarArgs() && index == executable.getParameterCount() - 1;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        Objects.requireNonNull(annotationClass);
        initAnnotationMap();
        return (T)annotations.get(annotationClass);
    }

    public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
      // When we have the ability to lookup repeated annotation containers, we will implement this
      throw new UnsupportedOperationException();
    }

    
    public Annotation[] getDeclaredAnnotations() {
        return executable.getParameterAnnotations()[index];
    }

    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return getAnnotation(annotationClass);
    }

    @Override
    public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
        return getAnnotationsByType(annotationClass);
    }

    public Annotation[] getAnnotations() {
        return getDeclaredAnnotations();
    }

    private void initAnnotationMap() {
      if (annotations == null) {
        annotations = ClassMap.newMap();
        for (Annotation annotation : getAnnotations()) {
          annotations.put(annotation.annotationType(), annotation);
        }
      }
    }
}
