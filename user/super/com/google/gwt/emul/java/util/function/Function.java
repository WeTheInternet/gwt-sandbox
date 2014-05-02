package java.util.function;

@FunctionalInterface
public interface Function<T, R> {

  default <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
    assert after != null;
    return (T t) -> after.apply(Function.this.apply(t));
  }

  R apply(T t);

  default <V> Function<V, R> compose(Function<? super V, ? extends T> before) {
    assert before != null;
    return (V v) -> Function.this.apply(before.apply(v));
  }

  static <T> Function<T, T> identity() {
    return (T x) -> x;
  }
}