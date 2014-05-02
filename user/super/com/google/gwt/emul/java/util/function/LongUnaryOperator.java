package java.util.function;

@FunctionalInterface
public interface LongUnaryOperator {

  long applyAsLong(long operand);

  default LongUnaryOperator compose(LongUnaryOperator before) {
    assert before != null;
    return (long v) -> LongUnaryOperator.this.applyAsLong(before.applyAsLong(v));
  }

  default LongUnaryOperator andThen(LongUnaryOperator after) {
    assert after != null;
    return (long t) -> after.applyAsLong(LongUnaryOperator.this.applyAsLong(t));
  }

  static LongUnaryOperator identity() {
    return t -> t;
  }
}
