package java.util.function;

@FunctionalInterface
public interface BiFunction<T, U, R> {

  R apply(T t, U u);

  default <V> BiFunction<T, U, V> andThen(Function<? super R, ? extends V> after) {
    assert after != null;
    return (T t, U u) -> after.apply(BiFunction.this.apply(t, u));
  }
}
