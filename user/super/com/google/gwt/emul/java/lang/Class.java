/*
 * Copyright 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package java.lang;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.impl.DoNotInline;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.security.ProtectionDomain;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.UnsafeNativeLong;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.reflect.shared.AnnotationMap;
import com.google.gwt.reflect.shared.ClassMap;
import com.google.gwt.reflect.shared.JsMemberPool;
import com.google.gwt.reflect.shared.ReflectUtil;

/**
 * Generally unsupported. This class is provided so that the GWT compiler can
 * choke down class literal references.
 * <p>
 * NOTE: The code in this class is very sensitive and should keep its dependencies upon other
 * classes to a minimum.
 *
 * @param <T> the type of the object
 */
@SuppressWarnings("serial")
public final class Class<T>
implements java.io.Serializable, 
java.lang.reflect.GenericDeclaration, 
java.lang.reflect.Type,
java.lang.reflect.AnnotatedElement 
{
  private static final int PRIMITIVE = 0x00000001;
  private static final int INTERFACE = 0x00000002;
  private static final int ARRAY = 0x00000004;
  private static final int ENUM = 0x00000008;
  private static final String NOT_IMPLEMENTED_CORRECTLY = "You cannot call this method in gwt from a normal class" +
    " object.  You must wrap your class literal with GwtReflect.magicClass(MyClass.class) first.";
  private static final String NOT_FOUND = "Did you forget to annotate with @ReflectionStrategy methods, " +
  		"or to call GwtReflect.magicClass on your class literal?.";
  private static int index;
  
  private static final JavaScriptObject CONSTS;
  static {
    if (GWT.isClient()) {
      CONSTS = initConstPool();
    }
    else {
      CONSTS = null;
    }
  }

  private static native JavaScriptObject initConstPool()
  /*-{
    $wnd.Reflect = {
      $:[],// enhanced classes
      $$:[],// class members
      a:[],// annotations
      c:[],// classes
      d:[],// doubles (includes float)
      e:[],// enums
      i:[],// ints (includes short, char, byte)
      l:[],// longs
      n:{},// class by name
      s:[],// strings
      p:{},// packages
      _a:[],// annotation arrays
      _b:[],// byte arrays
      _c:[],// char arrays
      _d:[],// double arrays
      _e:[],// enum arrays
      _f:[],// float arrays
      _i:[],// int arrays
      _j:[],// long arrays
      _l:[],// Class (type) arrays
      _o:[],// Object arrays
      _s:[],// short arrays
      _t:[],// String arrays
      _z:[]// boolan arrays
    };
    return $wnd.Reflect;
  }-*/;

  /**
   * Create a Class object for an array.<p>
   *
   * Arrays are not registered in the prototype table and get the class literal explicitly at
   * construction.<p>
   *
   * NOTE: this method is accessed through JSNI (Violator pattern) to avoid changing the public API.
   */
  private static native <T> Class<T> getClassLiteralForArray(Class<?> leafClass,
      int dimensions) /*-{
    var arrayLiterals = leafClass.@Class::arrayLiterals = leafClass.@Class::arrayLiterals || [];
    return arrayLiterals[dimensions] || (arrayLiterals[dimensions] =
            leafClass.@Class::createClassLiteralForArray(I)(dimensions));
  }-*/;

  private <T> Class<T> createClassLiteralForArray(int dimensions) {
    Class<T> clazz = new Class<T>();
    clazz.modifiers = ARRAY;
    clazz.superclass = Object.class;
    if (dimensions > 1) {
      clazz.componentType = getClassLiteralForArray(this, dimensions - 1);
    } else {
      clazz.componentType = this;
    }
    return clazz;
  }

  /**
   * Create a Class object for a class.
   *
   * @skip
   */
  @DoNotInline
  static <T> Class<T> createForClass(String packageName, String compoundClassName,
      JavaScriptObject typeId, Class<? super T> superclass) {
    Class<T> clazz = createClassObject(packageName, compoundClassName, typeId);
    maybeSetClassLiteral(typeId, clazz);
    clazz.superclass = superclass;
    return clazz;
  }

  /**
   * Create a Class object for an enum.
   *
   * @skip
   */
  @DoNotInline
  static <T> Class<T> createForEnum(String packageName, String compoundClassName,
      JavaScriptObject typeId, Class<? super T> superclass,
      JavaScriptObject enumConstantsFunc, JavaScriptObject enumValueOfFunc) {
    Class<T> clazz = createClassObject(packageName, compoundClassName, typeId);
    maybeSetClassLiteral(typeId, clazz);
    clazz.modifiers = (enumConstantsFunc != null) ? ENUM : 0;
    clazz.superclass = clazz.enumSuperclass = superclass;
    clazz.enumConstantsFunc = enumConstantsFunc;
    clazz.enumValueOfFunc = enumValueOfFunc;
    return clazz;
  }

  /**
   * Create a Class object for an interface.
   *
   * @skip
   */
  @DoNotInline
  static <T> Class<T> createForInterface(String packageName, String compoundClassName) {
    Class<T> clazz = createClassObject(packageName, compoundClassName, null);
    clazz.modifiers = INTERFACE;
    return clazz;
  }

  /**
   * Create a Class object for a primitive.
   *
   * @skip
   */
  @DoNotInline
  static Class<?> createForPrimitive(String className, JavaScriptObject primitiveTypeId) {
    Class<?> clazz = createClassObject("", className, primitiveTypeId);
    clazz.typeId = primitiveTypeId;
    clazz.modifiers = PRIMITIVE;
    return clazz;
  }

  /**
    * Used by {@link com.google.gwt.rpc.server.WebModePayloadSink} to create uninitialized instances.
    */
   @DoNotInline
   static native JavaScriptObject getPrototypeForClass(Class<?> clazz) /*-{
     if (clazz.@Class::isPrimitive()()) {
       return null;
     }
     var typeId = clazz.@Class::typeId;
     var prototype = @com.google.gwt.lang.JavaClassHierarchySetupUtil::prototypesByTypeId[typeId];
     return prototype;
   }-*/;

  public static boolean isClassMetadataEnabled() {
    // This body may be replaced by the compiler
    return false;
  }

  /**
   * null implies non-instantiable type, with no entries in
   * {@link JavaClassHierarchySetupUtil::prototypesByTypeId}.
   */
  private static native boolean isInstantiable(JavaScriptObject typeId) /*-{
    return !!typeId;
  }-*/;

  /**
   * Creates the class object for a type and initiliazes its fields.
   */
  private static <T> Class<T> createClassObject(String packageName, String compoundClassName,
      JavaScriptObject typeId) {
    Class<T> clazz = new Class<T>();
    if (isClassMetadataEnabled()) {
      clazz.packageName = packageName;
      clazz.compoundName = compoundClassName;
    } else {
      synthesizeClassNamesFromTypeId(clazz, typeId);
    }
    return clazz;
  }

  /**
   * Install class literal into prototype.clazz field (if type is instantiable) such that
   * Object.getClass() returning this.clazz returns the literal. Also stores typeId on class literal
   * for looking up prototypes given a literal. This is used for deRPC at the moment, but may be
   * used to implement Class.newInstance() in the future.
   *
   * If the prototype for typeId has not yet been created, then install the literal into a
   * placeholder array to differentiate the two cases.
   */
  private static native void maybeSetClassLiteral(JavaScriptObject typeId, Class<?> clazz) /*-{
    var proto;
    if (!typeId) {
      // Type is not instantiable, hence not registered in the metadata table.
      return;
    }
    clazz.@Class::typeId = typeId;
    // Guarantees virtual method won't be pruned by using a JSNI ref
    // This is required because deRPC needs to call it.
    var prototype  = @Class::getPrototypeForClass(Ljava/lang/Class;)(clazz);
    // A class literal may be referenced prior to an async-loaded vtable setup
    // For example, class literal lives in inital fragment,
    // but type is instantiated in another fragment
    if (!prototype) {
      // Leave a place holder for now to be filled in by __defineClass__ later.
      // TODO(rluble): Do not rely on the fact that if the entry is an array it is a placeholder.
      @com.google.gwt.lang.JavaClassHierarchySetupUtil::prototypesByTypeId[typeId] = [clazz];
      return;
    }
    // Type already registered in the metadata table, install the class literal in the appropriate
    // prototype field.
    prototype.@java.lang.Object::___clazz = clazz;
  }-*/;

  /**
   * Initiliazes {@code clazz} names from metadata.
   * <p>
   * Written in JSNI to minimize dependencies (on String.+).
   */
  private static native void initializeNames(Class<?> clazz) /*-{
    if (clazz.@Class::isArray(*)()) {
      var componentType = clazz.@Class::componentType;
      if (componentType.@Class::isPrimitive()()) {
        clazz.@Class::typeName = "[" + componentType.@Class::typeId;
      } else  if (!componentType.@Class::isArray()()) {
        clazz.@Class::typeName = "[L" + componentType.@Class::getName()() + ";";
      } else {
        clazz.@Class::typeName = "[" + componentType.@Class::getName()();
      }
      clazz.@Class::canonicalName = componentType.@Class::getCanonicalName()() + "[]";
      clazz.@Class::simpleName = componentType.@Class::getSimpleName()() + "[]";
      return;
    }

    var packageName = clazz.@Class::packageName;
    var compoundName = clazz.@Class::compoundName;
    compoundName =  compoundName.split("/");
    clazz.@Class::typeName =
        @Class::join(*)('.', [packageName , @Class::join(*)("$", compoundName)]);
    clazz.@Class::canonicalName =
        @Class::join(*)('.', [packageName , @Class::join(*)(".", compoundName)]);
    clazz.@Class::simpleName = compoundName[compoundName.length - 1];
    clazz.@Class::constId = clazz.@Class::remember()();
  }-*/;

  /**
   * Joins an array of strings with the specified separator.
   * <p>
   * Written in JSNI to minimize dependencies (on String.+).
   */
  private static native String join(String separator, JavaScriptObject strings) /*-{
    var i = 0;
    while (!strings[i] || strings[i] == "") {
      i++;
    }
    var result = strings[i++];
    for (; i < strings.length; i++) {
      if  (!strings[i] || strings[i] == "") {
        continue;
      }
      result += separator + strings[i];
    }
    return result;
  }-*/;

  /**
   * Initiliazes {@code clazz} names from typeIds.
   * <p>
   * Only called if metadata IS disabled.
   * <p>
   * Written in JSNI to minimize dependencies (on toString() and String.+).
   */
  static native void synthesizeClassNamesFromTypeId(Class<?> clazz, JavaScriptObject typeId) /*-{
     // The initial "" + in the below code is to prevent clazz.getAnonymousId from
     // being autoboxed. The class literal creation code is run very early
     // during application start up, before class Integer has been initialized.

    clazz.@Class::typeName = "Class$" +
        (!!typeId ? "S" + typeId : "" + clazz.@Class::sequentialId);
    clazz.@Class::canonicalName = clazz.@Class::typeName;
    clazz.@Class::simpleName = clazz.@Class::typeName;
  }-*/;

  /**
   * Sets the class object for primitives.
   * <p>
   * Written in JSNI to minimize dependencies (on (String)+).
   */
  static native void synthesizePrimitiveNamesFromTypeId(Class<?> clazz,
      JavaScriptObject primitiveTypeId) /*-{
    clazz.@Class::typeName = "Class$" + primitiveTypeId;
    clazz.@Class::canonicalName = clazz.@Class::typeName;
    clazz.@Class::simpleName = clazz.@Class::typeName;
  }-*/;
  
  /**
   * This is a magic-method hook used by Package.java; it is wired up the same as 
   * GwtReflect.magicClass, except it does not require a dependency on com.google.gwt.reflect.shared
   */
  static Class<?> magicClass(Class<?> c) {return c;}
  
  
  @SuppressWarnings("rawtypes")
  public static Class forName(String name)
    throws ClassNotFoundException{
    Class c = findClass(name);
    if (c == null) {
      throw new ClassNotFoundException("No class found for "+name);
    }
    return c;
  }
  
  private static native Class findClass(String name)
  /*-{
    return @java.lang.Class::CONSTS.n[className];
   }-*/;
  
  @SuppressWarnings("rawtypes")
  public static Class forName(String name, boolean initialize, ClassLoader loader) 
    throws ClassNotFoundException{
    return forName(name);
  }

  JavaScriptObject enumValueOfFunc;

  int modifiers;

  protected JavaScriptObject enumConstantsFunc;
  protected String pkgName;
  protected String typeName;
  protected Class<?> componentType;
  protected Class<? super T> enumSuperclass;
  protected Class<? super T> superclass;
  private AnnotationMap annotations;
  public ClassMap<T> classData;
  public JsMemberPool<T> members;
  private int constId;

  public static <T> boolean needsEnhance(Class<T> cls) {
    if (cls.members == null) {
      // might as well init here; as soon as we return true, the class is enhanced.
      cls.members = JsMemberPool.getMembers(cls);
      return true;
    }
    return false;
  }

  private String canonicalName;

  private String packageName;

  private String compoundName;

  private JavaScriptObject typeId;

  private JavaScriptObject arrayLiterals;

  // Assign a sequential id to each class literal to avoid calling hashCode which bring Impl as
  // a dependency.
  private int sequentialId = nextSequentialId++;

  private static int nextSequentialId = 1;

  protected static boolean isRememberClassByName() {
    // TODO replace System.getProperty calls such that they become JStringLiterals
    return "true".equals(System.getProperty("gwt.reflect.remember.names", "true"));
  }
  
  /**
   * Not publicly instantiable.
   *
   * @skip
   */
  protected Class() {
    // Initialize in constructor to avoid V8 invalidating hidden classes.
    typeName = null;
    simpleName = null;
    packageName = null;
    compoundName = null;
    canonicalName = null;
    typeId = null;
    arrayLiterals = null;
  }
  
  private native int remember()
  /*-{
    var pos = @java.lang.Class::CONSTS.c.length;
    @java.lang.Class::CONSTS.c[pos] = this;
    if (@java.lang.Class::isRememberClassByName()()) {
      var n = this.@java.lang.Class::getName()();
      @java.lang.Class::CONSTS.n[n] = this;
    }
    return pos;
  }-*/;
  
  public boolean desiredAssertionStatus() {
    // This body is ignored by the JJS compiler and a new one is
    // synthesized at compile-time based on the actual compilation arguments.
    return false;
  }

  private void ensureNamesAreInitialized() {
    if (typeName != null) {
      return;
    }
    initializeNames(this);
  }

  public String getCanonicalName() {
    ensureNamesAreInitialized();
    return canonicalName;
  }

  public Class<?> getComponentType() {
    return componentType;
  }

  public native T[] getEnumConstants() /*-{
    return this.@Class::enumConstantsFunc
        && (this.@Class::enumConstantsFunc)();
  }-*/;

  public String getName() {
    ensureNamesAreInitialized();
    return typeName;
  }

  public String getSimpleName() {
    ensureNamesAreInitialized();
    return simpleName;
  }

  public Class<? super T> getSuperclass() {
    if (isClassMetadataEnabled()) {
      return superclass;
    } else {
      return null;
    }
  }
  
  public Package getPackage(){
    // We don't trust our package field, because it may be elided in production compiles.
    // Using the reflection subsystem allows most types to be elided,
    // but allow selective installment of metadata like package names.
    return Package.getPackage(this);
  }

  public boolean isArray() {
    return (modifiers & ARRAY) != 0;
  }

  public boolean isEnum() {
    return (modifiers & ENUM) != 0;
  }
  
  public boolean isAnonymousClass() {
    return "".equals(getSimpleName());
  }
  

  public boolean isInterface() {
    return (modifiers & INTERFACE) != 0;
  }

  public boolean isPrimitive() {
    return (modifiers & PRIMITIVE) != 0;
  }

  public String toString() {
    return (isInterface() ? "interface " : (isPrimitive() ? "" : "class "))
        + getName();
  }

  public T newInstance()
  throws IllegalAccessException, InstantiationException {
    return classData.newInstance();
  }
  
  /**
   * Used by Enum to allow getSuperclass() to be pruned.
   */
  Class<? super T> getEnumSuperclass() {
    return enumSuperclass;
  }

  public ClassLoader getClassLoader() {
    return ClassLoader.getCallerClassLoader();
  }
  
  @SuppressWarnings("unchecked")
  public T cast(Object obj) {
    return (T) obj;
  }

  public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
    if (annotations == null)
      throw new NoSuchMethodError(NOT_IMPLEMENTED_CORRECTLY);
    return annotations.hasAnnotation(annotationClass);
  }

  public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
    if (annotations == null)
      throw new NoSuchMethodError(NOT_IMPLEMENTED_CORRECTLY);
    return annotations.getAnnotation(annotationClass);
  }

  public Annotation[] getAnnotations() {
    if (annotations == null)
      throw new NoSuchMethodError(NOT_IMPLEMENTED_CORRECTLY);
    return annotations.getAnnotations();
  }

  public Annotation[] getDeclaredAnnotations() {
    if (annotations == null)
      throw new NoSuchMethodError(NOT_IMPLEMENTED_CORRECTLY);
    return annotations.getDeclaredAnnotations();
  }
  
  public ProtectionDomain getProtectionDomain() {
    if (classData == null)
      throw new NoSuchMethodError(NOT_IMPLEMENTED_CORRECTLY);
    return classData.getProtectionDomain();
  }
  
  public Method getDeclaredMethod(String name, Class<?> ... parameterTypes)
    throws NoSuchMethodException{
    if (members == null)
      // Throw a helpful error message suggesting the client forgot to initialize this class
      // Note, we throw NoSuchMethodERROR if the method repo is null, as this means
      // we _might_ actually support this class, but it simply wasn't enhanded yet
      throw new NoSuchMethodError("Could not find "+getName()+"#"+ name+
          ": ("+ReflectUtil.joinClasses(", ", parameterTypes)+"); "+NOT_FOUND);
    // Call into our method repo; it will throw NoSuchMethodEXCEPTION,
    // as this is the correct behavior when our metho repo IS initialized,
    // but the method is legitimately missing
    return members.getDeclaredMethod(name, parameterTypes);
  }
  
  public Method getMethod(String name, Class<?> ... parameterTypes) 
    throws NoSuchMethodException{
    if (members == null)
      // Throw a helpful error message suggesting the client forgot to initialize this class
      // Note, we throw NoSuchMethodERROR if the method repo is null, as this means
      // we _might_ actually support this class, but it simply wasn't enhanced yet
      throw new NoSuchMethodError("Could not find "+getName()+"#"+ name+
          ": ("+ReflectUtil.joinClasses(", ", parameterTypes)+"); "+NOT_FOUND);
    // Call into our method repo; it will throw NoSuchMethodEXCEPTION,
    // as this is the correct behavior when our metho repo IS initialized,
    // but the method is legitimately missing

    return members.getMethod(name, parameterTypes);
  }

  public Method[] getDeclaredMethods() {
    if (members == null)
      throw new NoSuchMethodError(NOT_IMPLEMENTED_CORRECTLY);
    return members.getDeclaredMethods();
  }
  
  public Method[] getMethods() {
    if (members == null)
      throw new NoSuchMethodError(NOT_IMPLEMENTED_CORRECTLY);
    return members.getMethods();
  }
  
  public Field getDeclaredField(String name) 
  throws NoSuchFieldException
  {
    if (members == null)
      // Throw a helpful error message suggesting the client forgot to initialize this class
      // Note, we throw NoSuchFieldERROR is the field repo is null, as this means
      // we _might_ actually support this class, but it simply wasn't enhanded yet
      throw new NoSuchFieldError("Could not find "+getName()+"%"+ name+
          " "+NOT_FOUND);
    // Call into our field repo; it will throw NoSuchFieldEXCEPTION,
    // as this is the correct behavior when our field repo IS initialized,
    // but the field is legitimately missing
    return members.getDeclaredField(name);
  }
  
  public Field getField(String name) 
  throws NoSuchFieldException {
    if (members == null)
      // Throw a helpful error message suggesting the client forgot to initialize this class
      // Note, we throw NoSuchFieldERROR is the method repo is null, as this means
      // we _might_ actually support this class, but it simply wasn't enhanded yet
      throw new NoSuchFieldError("Could not find "+getName()+"%"+ name+
          " "+NOT_FOUND);
    // Call into our field repo; it will throw NoSuchFieldEXCEPTION,
    // as this is the correct behavior when our field repo IS initialized,
    // but the field is legitimately missing
    return members.getField(name);
  }
  
  public Field[] getDeclaredFields()
  {
    if (members == null)
      throw new NoSuchFieldError(NOT_IMPLEMENTED_CORRECTLY);
    return members.getDeclaredFields();
  }
  
  public Field[] getFields() {
    if (members == null)
      throw new NoSuchFieldError(NOT_IMPLEMENTED_CORRECTLY);
    return members.getFields();
  }
  
  public Constructor<T> getConstructor(Class<?> ... parameterTypes) 
  throws NoSuchMethodException {
    if (members == null)
      // Throw a helpful error message suggesting the client forgot to initialize this class
      // Note, we throw NoSuchMethodERROR is the constructor repo is null, as this means
      // we _might_ actually support this class, but it simply wasn't enhanded yet
      throw new NoSuchMethodError("Could not find "+getName()+"#<init>(" +
      		ReflectUtil.joinClasses(", ", parameterTypes)+") "+NOT_FOUND);
    // Call into our constructor repo; it will throw NoSuchMethodEXCEPTION,
    // as this is the correct behavior when our constructor repo IS initialized,
    // but the method is legitimately missing
    return members.getConstructor(parameterTypes);
  }
  
  public Constructor<T>[] getConstructors() {
    if (members == null)
      throw new NoSuchMethodError(NOT_IMPLEMENTED_CORRECTLY);
    return members.getConstructors();
  }
  
  public Constructor<T> getDeclaredConstructor(Class<?> ... parameterTypes) 
      throws NoSuchMethodException {
    if (members == null)
      // Throw a helpful error message suggesting the client forgot to initialize this class
      // Note, we throw NoSuchMethodERROR is the constructor repo is null, as this means
      // we _might_ actually support this class, but it simply wasn't enhanded yet
      throw new NoSuchMethodError("Could not find "+getName()+"#<init>(" +
          ReflectUtil.joinClasses(", ", parameterTypes)+") "+NOT_FOUND);
    // Call into our constructor repo; it will throw NoSuchMethodEXCEPTION,
    // as this is the correct behavior when our constructor repo IS initialized,
    // but the method is legitimately missing
    return members.getDeclaredConstructor(parameterTypes);
  }
  
  public Constructor<T>[] getDeclaredConstructors() {
    if (members == null)
      throw new NoSuchMethodError(NOT_IMPLEMENTED_CORRECTLY);
    return members.getDeclaredConstructors();
  }
  
  public Class<?>[] getClasses() {
    Class<?>[] list = new Class<?>[0];
    Class<?> cls = this;
    while (cls != null) {
      for (Class<?> c : cls.getDeclaredClasses()) {
        list[list.length] = c;
      }
      cls = cls.getSuperclass();
    }
    return list;
  }

  public Class<?>[] getDeclaredClasses() {
    if (classData == null) return new Class<?>[0];
    return classData.getDeclaredClasses();
  }
  
  public Class<?>[] getInterfaces() {
    if (classData == null) return new Class<?>[0];
    return classData.getInterfaces();
  }
  
  @SuppressWarnings("unchecked")
  public TypeVariable<Class<T>>[] getTypeParameters() {
//    if (getGenericSignature() != null) 
//      return (TypeVariable<Class<T>>[])getGenericInfo().getTypeParameters();
//    else
      return (TypeVariable<Class<T>>[])new TypeVariable[0];
  }
  
  @SuppressWarnings("unchecked")
  public <U> Class<? extends U> asSubclass(Class<U> clazz) {
    if (clazz.isAssignableFrom(this))
        return (Class<? extends U>) this;
    else
        throw new ClassCastException(this.toString());
  }
  
  public boolean isInstance(Object cls) {
    if (cls == null) {
      return false;
    }
    return isAssignableFrom(cls.getClass());
  }
  
  public boolean isAssignableFrom(Class<?> cls) {
    if (cls == this)return true;
    if (isInterface()) {
      while (cls != null) {
        for (Class<?> cl : cls.getInterfaces()) {
          if (cl == this)return true;
        }
        cls = cls.getSuperclass();
      }
    }else {
      while (cls != null) {
        cls = cls.getSuperclass();
        if (cls == this)return true;
      }
    }
    return false;
  }
  
  public Method getEnclosingMethod() {
    return classData.getEnclosingMethod();
  }
  
  public Class<?> getEnclosingClass() {
    return classData.getEnclosingClass();
  }
  
  protected static native int isNumber(Class<?> cls)
  /*-{
    // yup, switch case on classes works in jsni ;)
    switch(cls) {
      case @java.lang.Byte::class:
      return 1;
      case @java.lang.Short::class:
      return 2;
      case @java.lang.Integer::class:
      return 3;
      case @java.lang.Long::class:
      return 4;
      case @java.lang.Float::class:
      return 5;
      case @java.lang.Double::class:
      return 6;
    }
    return 0;
  }-*/;

  @UnsafeNativeLong
  protected static Long boxLong(long o) {
    return new Long(o);
  }
  
  @UnsafeNativeLong
  protected static long unboxLong(Long o) {
    return o.longValue();
  }
}
