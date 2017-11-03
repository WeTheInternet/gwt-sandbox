/*
 * Copyright 2017 Google Inc.
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
package java.lang.reflect;

import static javaemul.internal.InternalPreconditions.checkArgument;
import static javaemul.internal.InternalPreconditions.checkNotNull;

import javaemul.internal.ArrayHelper;
import javaemul.internal.NativeArray;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.UnsafeNativeLong;

/**
 * See <a
 * href="http://java.sun.com/javase/6/docs/api/java/lang/reflect/Array.html">the
 * official Java API doc</a> for details.
 */
public final class Array {

  public static Object get(Object array, int index) {
    if (array instanceof boolean[]) {
      return getBooleanImpl(array, index);
    } else if (array instanceof byte[]) {
      return getByteImpl(array, index);
    } else if (array instanceof char[]) {
      return getCharImpl(array, index);
    } else if (array instanceof double[]) {
      return getDoubleImpl(array, index);
    } else if (array instanceof float[]) {
      return getFloatImpl(array, index);
    } else if (array instanceof int[]) {
      return getIntImpl(array, index);
    } else if (array instanceof long[]) {
      return getLongImpl(array, index);
    } else if (array instanceof short[]) {
      return getShortImpl(array, index);
    } else  {
      checkArgument(array instanceof Object[]);
      Object[] typedArray = (Object[]) array;
      return typedArray[index];
    }
  }

  public static byte getBoolean(Object array, int index) {
    checkArgument(array instanceof boolean[]);
    return getByteImpl(array, index);
  }

  private static boolean getBooleanImpl(Object array, int index) {
    boolean[] typedArray = (boolean[]) array;
    return typedArray[index];
  }

  public static byte getByte(Object array, int index) {
    checkArgument(array instanceof byte[]);
    return getByteImpl(array, index);
  }

  private static byte getByteImpl(Object array, int index) {
    byte[] typedArray = (byte[]) array;
    return typedArray[index];
  }

  public static char getChar(Object array, int index) {
    checkArgument(array instanceof char[]);
    return getCharImpl(array, index);
  }

  private static char getCharImpl(Object array, int index) {
    char[] typedArray = (char[]) array;
    return typedArray[index];
  }

  public static double getDouble(Object array, int index) {
    if (array instanceof double[]) {
      return getDoubleImpl(array, index);
    } else if (array instanceof byte[]) {
      return getByteImpl(array, index);
    } else if (array instanceof char[]) {
      return getCharImpl(array, index);
    } else if (array instanceof float[]) {
      return getFloatImpl(array, index);
    } else if (array instanceof int[]) {
      return getIntImpl(array, index);
    } else if (array instanceof long[]) {
      return getLongImpl(array, index);
    } else if (array instanceof short[]) {
      return getShortImpl(array, index);
    } else {
      checkArgument(false);
      return 0;
    }
  }

  private static double getDoubleImpl(Object array, int index) {
    double[] typedArray = (double[]) array;
    return typedArray[index];
  }

  public static float getFloat(Object array, int index) {
    if (array instanceof float[]) {
      return getFloatImpl(array, index);
    } else if (array instanceof byte[]) {
      return getByteImpl(array, index);
    } else if (array instanceof char[]) {
      return getCharImpl(array, index);
    } else if (array instanceof int[]) {
      return getIntImpl(array, index);
    } else if (array instanceof long[]) {
      return getLongImpl(array, index);
    } else if (array instanceof short[]) {
      return getShortImpl(array, index);
    } else {
      checkArgument(false);
      return 0;
    }
  }

  private static float getFloatImpl(Object array, int index) {
    float[] typedArray = (float[]) array;
    return typedArray[index];
  }

  public static int getInt(Object array, int index) {
    if (array instanceof int[]) {
      return getIntImpl(array, index);
    } else if (array instanceof byte[]) {
      return getByteImpl(array, index);
    } else if (array instanceof char[]) {
      return getCharImpl(array, index);
    } else if (array instanceof short[]) {
      return getShortImpl(array, index);
    } else {
      checkArgument(false);
      return 0;
    }
  }

  private static int getIntImpl(Object array, int index) {
    int[] typedArray = (int[]) array;
    return typedArray[index];
  }

  public static int getLength(Object array) {
    checkNotNull(array);
    return ArrayHelper.getLength(array);
  }

  public static long getLong(Object array, int index) {
    if (array instanceof long[]) {
      return getLongImpl(array, index);
    } else if (array instanceof byte[]) {
      return getByteImpl(array, index);
    } else if (array instanceof char[]) {
      return getCharImpl(array, index);
    } else if (array instanceof int[]) {
      return getIntImpl(array, index);
    } else if (array instanceof short[]) {
      return getShortImpl(array, index);
    } else {
      checkArgument(false);
      return 0;
    }
  }

  private static long getLongImpl(Object array, int index) {
    long[] typedArray = (long[]) array;
    return typedArray[index];
  }

  public static short getShort(Object array, int index) {
    if (array instanceof short[]) {
      return getShortImpl(array, index);
    } else if (array instanceof byte[]) {
      return getByteImpl(array, index);
    } else {
      checkArgument(false);
      return 0;
    }
  }

  private static short getShortImpl(Object array, int index) {
    short[] typedArray = (short[]) array;
    return typedArray[index];
  }

  public static void set(Object array, int index, Object value) {
    if (array instanceof Object[]) {
      Object[] typedArray = (Object[]) array;
      typedArray[index] = value;
    } else {
      if (value instanceof Boolean) {
        setBoolean(array, index, ((Boolean) value).booleanValue());
      } else if (value instanceof Byte) {
        setByte(array, index, ((Byte) value).byteValue());
      } else if (value instanceof Character) {
        setChar(array, index, ((Character) value).charValue());
      } else if (value instanceof Short) {
        setShort(array, index, ((Short) value).shortValue());
      } else if (value instanceof Integer) {
        setInt(array, index, ((Integer) value).intValue());
      } else if (value instanceof Long) {
        setLong(array, index, ((Long) value).longValue());
      } else if (value instanceof Float) {
        setFloat(array, index, ((Float) value).floatValue());
      } else if (value instanceof Double) {
        setDouble(array, index, ((Double) value).doubleValue());
      } else {
        checkArgument(false);
      }
    }
  }

  public static void setBoolean(Object array, int index, boolean value) {
    checkArgument(array instanceof boolean[]);
    setBooleanImpl(array, index, value);
  }

  private static void setBooleanImpl(Object array, int index, boolean value) {
    boolean[] typedArray = (boolean[]) array;
    typedArray[index] = value;
  }

  public static void setByte(Object array, int index, byte value) {
    if (array instanceof byte[]) {
      setByteImpl(array, index, value);
    } else if (array instanceof double[]) {
      setDoubleImpl(array, index, value);
    } else if (array instanceof float[]) {
      setFloatImpl(array, index, value);
    } else if (array instanceof int[]) {
      setIntImpl(array, index, value);
    } else if (array instanceof long[]) {
      setLongImpl(array, index, value);
    } else if (array instanceof short[]) {
      setShortImpl(array, index, value);
    } else {
      checkArgument(false);
    }
  }

  private static void setByteImpl(Object array, int index, byte value) {
    byte[] typedArray = (byte[]) array;
    typedArray[index] = value;
  }

  public static void setChar(Object array, int index, char value) {
    if (array instanceof char[]) {
      setCharImpl(array, index, value);
    } else if (array instanceof double[]) {
      setDoubleImpl(array, index, value);
    } else if (array instanceof float[]) {
      setFloatImpl(array, index, value);
    } else if (array instanceof int[]) {
      setIntImpl(array, index, value);
    } else if (array instanceof long[]) {
      setLongImpl(array, index, value);
    } else {
      checkArgument(false);
    }
  }

  private static void setCharImpl(Object array, int index, char value) {
    char[] typedArray = (char[]) array;
    typedArray[index] = value;
  }

  public static void setDouble(Object array, int index, double value) {
    checkArgument(array instanceof double[]);
    setDoubleImpl(array, index, value);
  }

  private static void setDoubleImpl(Object array,int index, double value) {
    double[] typedArray = (double[]) array;
    typedArray[index] = value;
  }

  public static void setFloat(Object array, int index, float value) {
    if (array instanceof float[]) {
      setFloatImpl(array, index, value);
    } else if (array instanceof double[]) {
      setDoubleImpl(array, index, value);
    } else {
      checkArgument(false);
    }
  }

  private static void setFloatImpl(Object array, int index, float value) {
    float[] typedArray = (float[]) array;
    typedArray[index] = value;
  }

  public static void setInt(Object array, int index, int value) {
    if (array instanceof int[]) {
      setIntImpl(array, index, value);
    } else if (array instanceof double[]) {
      setDoubleImpl(array, index, value);
    } else if (array instanceof float[]) {
      setFloatImpl(array, index, value);
    } else if (array instanceof long[]) {
      setLongImpl(array, index, value);
    } else {
      checkArgument(false);
    }
  }

  private static void setIntImpl(Object array, int index, int value) {
    int[] typedArray = (int[]) array;
    typedArray[index] = value;
  }

  public static void setLong(Object array, int index, long value) {
    if (array instanceof long[]) {
      setLongImpl(array, index, value);
    } else if (array instanceof double[]) {
      setDoubleImpl(array, index, value);
    } else if (array instanceof float[]) {
      setFloatImpl(array, index, value);
    } else {
      checkArgument(false);
    }
  }

  private static void setLongImpl(Object array, int index, long value) {
    long[] typedArray = (long[]) array;
    typedArray[index] = value;
  }

  public static void setShort(Object array, int index, short value) {
    if (array instanceof short[]) {
      setShortImpl(array, index, value);
    } else if (array instanceof double[]) {
      setDoubleImpl(array, index, value);
    } else if (array instanceof float[]) {
      setFloatImpl(array, index, value);
    } else if (array instanceof int[]) {
      setIntImpl(array, index, value);
    } else if (array instanceof long[]) {
      setLongImpl(array, index, value);
    } else {
      checkArgument(false);
    }
  }

  private static void setShortImpl(Object array, int index, short value) {
    short[] typedArray = (short[]) array;
    typedArray[index] = value;
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
       @java.lang.reflect.Array::factories[id] = @javaemul.internal.ArrayHelper::createFrom([Ljava/lang/Object;I)(seed, 0);
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
   * Creates a new sigle-dimensional array.
   *
   * Uses the same semantics as java, expects a non-array component type,
   * followed by the length of the single-dimensional array returned.
   *
   * newInstance(String.class, 1) -> new String[1]
   *
   * All instances of this method call must be replaced by the GWT compiler.
   *
   * TODO: consider hooking up some runtime backup wiring, in case this is called outside of GWT
   * (like in J2CL), such that other transpilers can fill in that wiring dynamically, and have
   * a real method body here.
   */
  public static Object newInstance(Class<?> componentType, int length)
  throws NegativeArraySizeException{
    // We defer to a different method so if the magic-method injector does not
    // receive a class literal, it can just rewrite the call to #newArray()
    return newSingleDimArray(componentType,length);
  }


  public static Object newSingleDimArray(Class<?> componentType, int length)
        throws NegativeArraySizeException {
    int seedId = constId(componentType);
    Object result = newArray(seedId, length);
    if (result == null) {
      throw new UnsupportedOperationException("Array for type " + componentType + " not initialized. "
          + "Call Array.newInstance(" + componentType.getName() + ".class, 0) to register this type");
    }
    return result;
  }

  /**
   * Creates a new multi-dimensional array.
   *
   * Uses the same semantics as java, expects a non-array component type,
   * followed by the length each dimension of the array returned.
   *
   * newInstance(String.class, 1) -> new String[1]
   * newInstance(int.class, 1, 2, 3) -> new int[1][2][3]
   *
   * All instances of this method call must be replaced by the GWT compiler.
   *
   * TODO: consider hooking up some runtime backup wiring, in case this is called outside of GWT
   * (like in J2CL), such that other transpilers can fill in that wiring dynamically, and have
   * a real method body here.
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

      NativeArray arr = ArrayHelper.asNativeArray(array);

      while (size-->0) {
        Object child = newArray(constId(component), 0);
        arr.concat(child);
        fillArray(child, component, dimensions, position+1);
      }
    }
  }

  private static native <T> T[] newArray(int id, int length)
  /*-{
     if (@java.lang.reflect.Array::factories[id]) {
       var from = @java.lang.reflect.Array::factories[id];
       return @javaemul.internal.ArrayHelper::createFrom([Ljava/lang/Object;I)(from, length);
     }
     return null;
   }-*/;

  public static <T> T[] clone(T[] array) {
    return ArrayHelper.clone(array, 0, array.length);
  }

  public static native boolean[] clone(boolean[] array)
  /*-{
    return @javaemul.internal.ArrayHelper::clone([Ljava/lang/Object;II)(array, 0, array.length);
  }-*/;

  public static native byte[] clone(byte[] array)
  /*-{
    return @javaemul.internal.ArrayHelper::clone([Ljava/lang/Object;II)(array, 0, array.length);
  }-*/;

  public static native short[] clone(short[] array)
  /*-{
    return @javaemul.internal.ArrayHelper::clone([Ljava/lang/Object;II)(array, 0, array.length);
  }-*/;

  public static native char[] clone(char[] array)
  /*-{
    return @javaemul.internal.ArrayHelper::clone([Ljava/lang/Object;II)(array, 0, array.length);
  }-*/;

  public static native int[] clone(int[] array)
  /*-{
    return @javaemul.internal.ArrayHelper::clone([Ljava/lang/Object;II)(array, 0, array.length);
  }-*/;

  @UnsafeNativeLong
  public static native long[] clone(long[] array)
  /*-{
    return @javaemul.internal.ArrayHelper::clone([Ljava/lang/Object;II)(array, 0, array.length);
  }-*/;

  public static native float[] clone(float[] array)
  /*-{
    return @javaemul.internal.ArrayHelper::clone([Ljava/lang/Object;II)(array, 0, array.length);
  }-*/;

  public static native double[] clone(double[] array)
  /*-{
    return @javaemul.internal.ArrayHelper::clone([Ljava/lang/Object;II)(array, 0, array.length);
  }-*/;


  public static native String join(Object array)
  /*-{
     // No type checking because the code that knows about this method will
     // do it's own checking if required.  For now, this hidden method is only used by
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
