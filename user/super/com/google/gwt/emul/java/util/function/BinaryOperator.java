package java.util.function;

import java.util.Comparator;

@FunctionalInterface
public interface BinaryOperator<T> extends BiFunction<T, T, T> {

  public static <T> BinaryOperator<T> minBy(Comparator<? super T> comparator) {
    assert comparator != null;
    return (a, b) -> comparator.compare(a, b) <= 0 ? a : b;
  }

  public static <T> BinaryOperator<T> maxBy(Comparator<? super T> comparator) {
    assert comparator != null;
    return (a, b) -> comparator.compare(a, b) >= 0 ? a : b;
  }
}
