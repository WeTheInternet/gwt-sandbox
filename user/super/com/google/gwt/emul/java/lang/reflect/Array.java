package java.lang.reflect;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.UnsafeNativeLong;
import com.google.gwt.reflect.shared.GwtReflect;

public final class Array {

  private static void assertIsArray(Object o) {
    assert o != null : new NullPointerException();
    assert o.getClass().getComponentType() != null : "Not an array class: "+o.getClass()+" '"+o+"'";
  }
  @UnsafeNativeLong
  private static void assertIsArrayType(Object o, Class<?> component) {
    assertIsArray(o);
    assert o.getClass().getComponentType() == component : "Array class "+o.getClass().getName()+" does not have " +
      "claimed component type "+component.getName();
  }
  
  private static JavaScriptObject factories = JavaScriptObject.createArray();
  private static JavaScriptObject classes = JavaScriptObject.createArray();
  
  public static <T> T[] register(T[] array, Class componentType) {
    if (array == null) {
      return null;
    }
    if (componentType.isArray()) {
      componentType.getComponentType().getName();// init the constId...
      int seedId = constId(componentType.getComponentType());
      initFactory(seedId, array);
      saveType(seedId, componentType);
      if (array.length > 0 && componentType.getComponentType().isArray()) {
        // avoid casting issues with primitive arrays by using jsni
        // This will skip the cast checking and just do what we want.
        nativeRegister(array[0], componentType.getComponentType());
      }
    }
    return array;
  }
  
  private static native void nativeRegister(Object o, Class c)
  /*-{
     @java.lang.reflect.Array::register([Ljava/lang/Object;Ljava/lang/Class;)(o, c);
   }-*/;
  
  private static native int constId(Class<?> cls)
  /*-{
     return cls.@java.lang.Class::constId;
  }-*/;
  private static native <T> boolean initFactory(int id, T[] seed)
  /*-{
     if (!@java.lang.reflect.Array::factories[id]) {
       @java.lang.reflect.Array::factories[id] = @com.google.gwt.lang.Array::createFrom([Ljava/lang/Object;I)(seed, 0);
     }
   }-*/;

  private static native void saveType(int id, Class<?> arrayClass)
  /*-{
     @java.lang.reflect.Array::classes[id] = arrayClass;
  }-*/;
  
  private static native Class<?> findType(int id, int length)
  /*-{
     var cls = @java.lang.reflect.Array::classes[id];
     while(length-->0) {
       if (!cls) {
         return null;
       }
       cls = @java.lang.reflect.Array::classes[cls.@java.lang.Class::constId];
     }
     return cls;
   }-*/;
  
    /**
     * Constructor.  Class Array is not instantiable.
     */
    private Array() {}

    /**
     * Creates a new array with the specified component type and
     * length.
     * Invoking this method is equivalent to creating an array
     * as follows:
     * <blockquote>
     * <pre>
     * int[] x = {length};
     * Array.newInstance(componentType, x);
     * </pre>
     * </blockquote>
     *
     * @param componentType the <code>Class</code> object representing the
     * component type of the new array
     * @param length the length of the new array
     * @return the new array
     * @exception NullPointerException if the specified
     * <code>componentType</code> parameter is null
     * @exception IllegalArgumentException if componentType is {@link Void#TYPE}
     * @exception NegativeArraySizeException if the specified <code>length</code> 
     * is negative
     */
    public static Object newInstance(Class<?> componentType, int length) 
        throws NegativeArraySizeException {
      // We defer to a different method so if the magic-method injector does not
      // receive a class literal, it can just rewrite the call to #newArray()
      return newSingleDimArray(componentType, length);
    }

    public static Object newSingleDimArray(Class<?> componentType, int length) 
        throws NegativeArraySizeException {
      int seedId = constId(componentType);
      Object result = newArray(seedId, length);
      if (result == null) {
        throw new UnsupportedOperationException("Array for type "+componentType+" not initialized. "
            +"Call Array.newInstance("+componentType.getName()+".class, 0) to register this type");

      }
      return result;
    }
    
    private static native <T> T[] newArray(int id, int length)
    /*-{
       if (@java.lang.reflect.Array::factories[id]) {
         var from = @java.lang.reflect.Array::factories[id];
         return @com.google.gwt.lang.Array::createFrom([Ljava/lang/Object;I)(from, length);
       }
       return null;
     }-*/;


    /**
     * Creates a new array
     * with the specified component type and dimensions. 
     * If <code>componentType</code>
     * represents a non-array class or interface, the new array
     * has <code>dimensions.length</code> dimensions and
     * <code>componentType</code> as its component type. If
     * <code>componentType</code> represents an array class, the
     * number of dimensions of the new array is equal to the sum
     * of <code>dimensions.length</code> and the number of
     * dimensions of <code>componentType</code>. In this case, the
     * component type of the new array is the component type of
     * <code>componentType</code>.
     * 
     * <p>The number of dimensions of the new array must not
     * exceed the number of array dimensions supported by the
     * implementation (typically 255).
     *
     * @param componentType the <code>Class</code> object representing the component
     * type of the new array
     * @param dimensions an array of <code>int</code> representing the dimensions of
     * the new array
     * @return the new array
     * @exception NullPointerException if the specified 
     * <code>componentType</code> argument is null
     * @exception IllegalArgumentException if the specified <code>dimensions</code> 
     * argument is a zero-dimensional array, or if the number of
     * requested dimensions exceeds the limit on the number of array dimensions 
     * supported by the implementation (typically 255), or if componentType 
     * is {@link Void#TYPE}.
     * @exception NegativeArraySizeException if any of the components in
     * the specified <code>dimensions</code> argument is negative.
     */
    public static Object newInstance(Class<?> componentType, int... dimensions)
        throws IllegalArgumentException, NegativeArraySizeException {
      return newMultiDimArray(componentType, dimensions);
    }

    public static Object newMultiDimArray(Class<?> componentType, int[] dimensions)
        throws IllegalArgumentException, NegativeArraySizeException {
      Class<?> arrayType = findType(constId(componentType), dimensions.length);
      int size = dimensions.length > 0 ? dimensions[0] : 0;
      Object result = newArray(constId(arrayType), size);
      fillArray(result, arrayType, dimensions, 0);
      return result;
    }
    
    private static void fillArray(Object array, Class<?> arrayType, int[] dimensions, int position) {
      if (position < dimensions.length) {
        int size = dimensions[position];
        Class<?> component = arrayType.getComponentType();
        while (size-->0) {
          Object child = newArray(constId(component), 0);
          setUnsafe(array, size, child);
          fillArray(child, component, dimensions, position+1);
        }
      }
    }
    
    

    /**
     * Returns the length of the specified array object, as an <code>int</code>.
     *
     * @param array the array
     * @return the length of the array
     * @exception IllegalArgumentException if the object argument is not
     * an array
     */
    public static native int getLength(Object array)
  throws IllegalArgumentException
  /*-{
     @java.lang.reflect.Array::assertIsArray(Ljava/lang/Object;)(array);
     return array.length;
   }-*/
  ;

    /**
     * Returns the value of the indexed component in the specified
     * array object.  The value is automatically wrapped in an object
     * if it has a primitive type.
     *
     * @param array the array
     * @param index the index
     * @return the (possibly wrapped) value of the indexed component in
     * the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array
     * @exception ArrayIndexOutOfBoundsException If the specified <code>index</code> 
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     */
    public static native Object get(Object array, int index)
  throws IllegalArgumentException, ArrayIndexOutOfBoundsException
  /*-{
     @java.lang.reflect.Array::assertIsArray(Ljava/lang/Object;)(array);
     return array[index];
   }-*/
  ;

    /**
     * Returns the value of the indexed component in the specified
     * array object, as a <code>boolean</code>.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified <code>index</code> 
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
    public static native boolean getBoolean(Object array, int index)
  throws IllegalArgumentException, ArrayIndexOutOfBoundsException
  /*-{
     @java.lang.reflect.Array::assertIsArrayType(Ljava/lang/Object;Ljava/lang/Class;)(array, @boolean::class);
     return array[index];
   }-*/
  ;

    /**
     * Returns the value of the indexed component in the specified
     * array object, as a <code>byte</code>.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified <code>index</code> 
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
    public static native byte getByte(Object array, int index)
  throws IllegalArgumentException, ArrayIndexOutOfBoundsException
  /*-{
     @java.lang.reflect.Array::assertIsArrayType(Ljava/lang/Object;Ljava/lang/Class;)(array, @byte::class);
     return array[index];
   }-*/
  ;

    /**
     * Returns the value of the indexed component in the specified
     * array object, as a <code>char</code>.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified <code>index</code> 
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
    public static native char getChar(Object array, int index)
  throws IllegalArgumentException, ArrayIndexOutOfBoundsException
  /*-{
    @java.lang.reflect.Array::assertIsArrayType(Ljava/lang/Object;Ljava/lang/Class;)(array, @char::class);
    return array[index];
  }-*/
  ;

    /**
     * Returns the value of the indexed component in the specified
     * array object, as a <code>short</code>.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified <code>index</code> 
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
    public static native short getShort(Object array, int index)
  throws IllegalArgumentException, ArrayIndexOutOfBoundsException
  /*-{
    @java.lang.reflect.Array::assertIsArrayType(Ljava/lang/Object;Ljava/lang/Class;)(array, @short::class);
    return array[index];
  }-*/
  ;

    /**
     * Returns the value of the indexed component in the specified
     * array object, as an <code>int</code>.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified <code>index</code> 
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
    public static native int getInt(Object array, int index)
  throws IllegalArgumentException, ArrayIndexOutOfBoundsException
  /*-{
    @java.lang.reflect.Array::assertIsArrayType(Ljava/lang/Object;Ljava/lang/Class;)(array, @int::class);
    return array[index];
  }-*/
  ;

    /**
     * Returns the value of the indexed component in the specified
     * array object, as a <code>long</code>.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified <code>index</code> 
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
    @UnsafeNativeLong
    public static native long getLong(Object array, int index)
  throws IllegalArgumentException, ArrayIndexOutOfBoundsException
  /*-{
    @java.lang.reflect.Array::assertIsArrayType(Ljava/lang/Object;Ljava/lang/Class;)(array, @long::class);
    return array[index];
  }-*/
  ;

    /**
     * Returns the value of the indexed component in the specified
     * array object, as a <code>float</code>.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified <code>index</code> 
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
    public static native float getFloat(Object array, int index)
  throws IllegalArgumentException, ArrayIndexOutOfBoundsException
  /*-{
    @java.lang.reflect.Array::assertIsArrayType(Ljava/lang/Object;Ljava/lang/Class;)(array, @float::class);
    return array[index];
  }-*/
  ;

    /**
     * Returns the value of the indexed component in the specified
     * array object, as a <code>double</code>.
     *
     * @param array the array
     * @param index the index
     * @return the value of the indexed component in the specified array
     * @exception NullPointerException If the specified object is null
     * @exception IllegalArgumentException If the specified object is not
     * an array, or if the indexed element cannot be converted to the
     * return type by an identity or widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified <code>index</code> 
     * argument is negative, or if it is greater than or equal to the
     * length of the specified array
     * @see Array#get
     */
    public static native double getDouble(Object array, int index)
  throws IllegalArgumentException, ArrayIndexOutOfBoundsException
  /*-{
    @java.lang.reflect.Array::assertIsArrayType(Ljava/lang/Object;Ljava/lang/Class;)(array, @double::class);
    return array[index];
  }-*/
  ;

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified new value.  The new value is first
     * automatically unwrapped if the array has a primitive component
     * type.
     * @param array the array
     * @param index the index into the array
     * @param value the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the array component type is primitive and
     * an unwrapping conversion fails
     * @exception ArrayIndexOutOfBoundsException If the specified <code>index</code> 
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     */
    public static native void set(Object array, int index, Object value)
  throws IllegalArgumentException, ArrayIndexOutOfBoundsException
  /*-{
    @java.lang.reflect.Array::assertIsArray(Ljava/lang/Object;)(array);
    array[index] = value;
  }-*/;
    
  private static native void setUnsafe(Object array, int index, Object value)
      throws IllegalArgumentException, ArrayIndexOutOfBoundsException
      /*-{
        array[index] = value;
      }-*/
  ;

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified <code>boolean</code> value.
     * @param array the array
     * @param index the index into the array
     * @param z the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified <code>index</code> 
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
    public static native void setBoolean(Object array, int index, boolean z)
  throws IllegalArgumentException, ArrayIndexOutOfBoundsException
  /*-{
    @java.lang.reflect.Array::assertIsArrayType(Ljava/lang/Object;Ljava/lang/Class;)(array, @boolean::class);
    array[index] = z;
  }-*/
  ;

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified <code>byte</code> value.
     * @param array the array
     * @param index the index into the array
     * @param b the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified <code>index</code> 
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
    public static native void setByte(Object array, int index, byte b)
  throws IllegalArgumentException, ArrayIndexOutOfBoundsException
  /*-{
    @java.lang.reflect.Array::assertIsArrayType(Ljava/lang/Object;Ljava/lang/Class;)(array, @byte::class);
    array[index] = b;
  }-*/
  ;

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified <code>char</code> value.
     * @param array the array
     * @param index the index into the array
     * @param c the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified <code>index</code> 
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
    public static native void setChar(Object array, int index, char c)
  throws IllegalArgumentException, ArrayIndexOutOfBoundsException
  /*-{
    @java.lang.reflect.Array::assertIsArrayType(Ljava/lang/Object;Ljava/lang/Class;)(array, @char::class);
    array[index] = c;
  }-*/
  ;

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified <code>short</code> value.
     * @param array the array
     * @param index the index into the array
     * @param s the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified <code>index</code> 
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
    public static native void setShort(Object array, int index, short s)
  throws IllegalArgumentException, ArrayIndexOutOfBoundsException
  /*-{
    @java.lang.reflect.Array::assertIsArrayType(Ljava/lang/Object;Ljava/lang/Class;)(array, @short::class);
    array[index] = s;
  }-*/
  ;

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified <code>int</code> value.
     * @param array the array
     * @param index the index into the array
     * @param i the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified <code>index</code> 
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
    public static native void setInt(Object array, int index, int i)
  throws IllegalArgumentException, ArrayIndexOutOfBoundsException
  /*-{
    @java.lang.reflect.Array::assertIsArrayType(Ljava/lang/Object;Ljava/lang/Class;)(array, @int::class);
    array[index] = i;
  }-*/
  ;

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified <code>long</code> value.
     * @param array the array
     * @param index the index into the array
     * @param l the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified <code>index</code> 
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
    @UnsafeNativeLong
    public static native void setLong(Object array, int index, long l)
  throws IllegalArgumentException, ArrayIndexOutOfBoundsException
  /*-{
    @java.lang.reflect.Array::assertIsArrayType(Ljava/lang/Object;Ljava/lang/Class;)(array, @long::class);
    array[index] = l;
  }-*/
  ;

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified <code>float</code> value.
     * @param array the array
     * @param index the index into the array
     * @param f the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified <code>index</code> 
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
    public static native void setFloat(Object array, int index, float f)
  throws IllegalArgumentException, ArrayIndexOutOfBoundsException
  /*-{
    @java.lang.reflect.Array::assertIsArrayType(Ljava/lang/Object;Ljava/lang/Class;)(array, @float::class);
    array[index] = f;
  }-*/
  ;

    /**
     * Sets the value of the indexed component of the specified array
     * object to the specified <code>double</code> value.
     * @param array the array
     * @param index the index into the array
     * @param d the new value of the indexed component
     * @exception NullPointerException If the specified object argument
     * is null
     * @exception IllegalArgumentException If the specified object argument
     * is not an array, or if the specified value cannot be converted
     * to the underlying array's component type by an identity or a
     * primitive widening conversion
     * @exception ArrayIndexOutOfBoundsException If the specified <code>index</code> 
     * argument is negative, or if it is greater than or equal to
     * the length of the specified array
     * @see Array#set
     */
    public static native void setDouble(Object array, int index, double d)
  throws IllegalArgumentException, ArrayIndexOutOfBoundsException
  /*-{
    @java.lang.reflect.Array::assertIsArrayType(Ljava/lang/Object;Ljava/lang/Class;)(array, @double::class);
    array[index] = d;
  }-*/
  ;

  public static <T> T[] clone(T[] array) {
    return com.google.gwt.lang.Array.clone(array);
  }
  
  public static native boolean[] clone(boolean[] array)
  /*-{
    return @com.google.gwt.lang.Array::clone([Ljava/lang/Object;)(array);
  }-*/;
  
  public static native byte[] clone(byte[] array)
  /*-{
    return @com.google.gwt.lang.Array::clone([Ljava/lang/Object;)(array);
  }-*/;
  
  public static native short[] clone(short[] array)
  /*-{
    return @com.google.gwt.lang.Array::clone([Ljava/lang/Object;)(array);
  }-*/;
  
  public static native char[] clone(char[] array)
  /*-{
    return @com.google.gwt.lang.Array::clone([Ljava/lang/Object;)(array);
  }-*/;
  
  public static native int[] clone(int[] array)
  /*-{
    return @com.google.gwt.lang.Array::clone([Ljava/lang/Object;)(array);
  }-*/;
  
  @UnsafeNativeLong
  public static native long[] clone(long[] array)
  /*-{
    return @com.google.gwt.lang.Array::clone([Ljava/lang/Object;)(array);
  }-*/;
  
  public static native float[] clone(float[] array)
  /*-{
    return @com.google.gwt.lang.Array::clone([Ljava/lang/Object;)(array);
  }-*/;
  
  public static native double[] clone(double[] array)
  /*-{
    return @com.google.gwt.lang.Array::clone([Ljava/lang/Object;)(array);
  }-*/;
  
  
  public static native String join(Object array)
  /*-{
     // No type checking because the code that knows about this method will
     // do it's own checking if required.  For now, this is only used by
     // the annotation support, who can guarantee the value is never null,
     // and always a native array.
     return array.join(", ");
   }-*/;
  
  public static String join(long[] array) {
    String value = "";
    for (int i = 0; i < array.length; i++) {
      if (i > 0) {
        value += ", ";
      }
      value += array[i]+"L";
    }
    return value;
  }

  public static String join(Class[] array) {
    String value = "";
    for (int i = 0; i < array.length; i++) {
      if (i > 0) {
        value += ", ";
      }
      value += array[i].getCanonicalName()+".class";
    }
    return value;
  }
  
  public static String join(String[] array) {
    String value = "";
    for (int i = 0; i < array.length; i++) {
      if (i > 0) {
        value += ", ";
      }
      value += "\"" + escape(array[i]) + "\"";
    }
    return value;
  }
  
  private native static String escape(String unescaped)
  /*-{
    return unescaped.replace(/(["'\\])/g, "\\$1").replace(/\n/g,"\\n");
  }-*/;

  public static <E extends Enum<E>> String join(E[] array) {
    String value = "";
    for (int i = 0; i < array.length; i++) {
      if (i > 0) {
        value += ", ";
      }
      value += array[i].getDeclaringClass().getCanonicalName()+"."+array[i].name();
    }
    return value;
  }

}
