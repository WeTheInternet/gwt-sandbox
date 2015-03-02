package java.lang.reflect;

public interface InvocationHandler {

  public Object invoke(Object object, Method method, Object[] arguments) throws Throwable;

}
