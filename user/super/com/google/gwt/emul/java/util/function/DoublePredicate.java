package java.util.function;

@FunctionalInterface
public interface DoublePredicate {
  boolean test(double value);

  default DoublePredicate and(DoublePredicate other) {
    assert other != null;
    return (value) -> DoublePredicate.this.test(value) && other.test(value);
  }

  default DoublePredicate negate() {
    return (value) -> !DoublePredicate.this.test(value);
  }

  default DoublePredicate or(DoublePredicate other) {
    assert other != null;
    return (value) -> DoublePredicate.this.test(value) || other.test(value);
  }
}
