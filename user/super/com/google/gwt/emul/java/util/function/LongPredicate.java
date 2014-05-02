package java.util.function;

@FunctionalInterface
public interface LongPredicate {

  boolean test(long value);

  default LongPredicate and(LongPredicate other) {
    assert other != null;
    return (value) -> LongPredicate.this.test(value) && other.test(value);
  }

  default LongPredicate negate() {
    return (value) -> !LongPredicate.this.test(value);
  }

  default LongPredicate or(LongPredicate other) {
    assert other != null;
    return (value) -> LongPredicate.this.test(value) || other.test(value);
  }
}
