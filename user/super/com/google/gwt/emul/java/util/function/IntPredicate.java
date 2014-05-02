package java.util.function;

@FunctionalInterface
public interface IntPredicate {

  boolean test(int value);

  default IntPredicate and(IntPredicate other) {
    assert other != null;
    return (value) -> IntPredicate.this.test(value) && other.test(value);
  }

  default IntPredicate negate() {
    return (value) -> !IntPredicate.this.test(value);
  }

  default IntPredicate or(IntPredicate other) {
    assert other != null;
    return (value) -> IntPredicate.this.test(value) || other.test(value);
  }
}
