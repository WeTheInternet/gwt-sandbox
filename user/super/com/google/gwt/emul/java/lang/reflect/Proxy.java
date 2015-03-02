package java.lang.reflect;

public class Proxy {

  public static Object newProxyInstance(ClassLoader cl,
                                        Class<?>[] interfaces,
                                        InvocationHandler handler)
        throws IllegalArgumentException
    {
        throw new UnsupportedOperationException("Proxy Instances do not work in GWT");
    }


}
