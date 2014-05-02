package java.util.function;

@FunctionalInterface
public interface BiPredicate<T, U> {

  boolean test(T t, U u);

  default BiPredicate<T, U> and(BiPredicate<? super T, ? super U> other) {
    assert other != null;
    return (T t, U u) -> BiPredicate.this.test(t, u) && other.test(t, u);
  }

  default BiPredicate<T, U> negate() {
    return (T t, U u) -> !BiPredicate.this.test(t, u);
  }

  default BiPredicate<T, U> or(BiPredicate<? super T, ? super U> other) {
    assert other != null;
    return (T t, U u) -> BiPredicate.this.test(t, u) || other.test(t, u);
  }
}
