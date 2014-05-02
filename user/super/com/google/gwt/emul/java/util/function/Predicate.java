package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface Predicate<T> {
  default Predicate<T> and(Predicate<? super T> other) {
    return (t) -> Predicate.this.test(t) && other.test(t);
  }

  static <T> Predicate<T> isEqual(Object targetRef) {
    return (null == targetRef) ? Objects::isNull : object -> targetRef.equals(object);
  }

  default Predicate<T> negate() {
    return (t) -> !Predicate.this.test(t);
  }

  default Predicate<T> or(Predicate<? super T> other) {
    assert other != null;
    return (t) -> Predicate.this.test(t) || other.test(t);
  }

  boolean test(T t);

}