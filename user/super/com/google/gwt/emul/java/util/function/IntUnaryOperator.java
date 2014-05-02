package java.util.function;

@FunctionalInterface
public interface IntUnaryOperator {

  int applyAsInt(int operand);

  default IntUnaryOperator compose(IntUnaryOperator before) {
    assert before != null;
    return (int v) -> IntUnaryOperator.this.applyAsInt(before.applyAsInt(v));
  }

  default IntUnaryOperator andThen(IntUnaryOperator after) {
    assert after != null;
    return (int t) -> after.applyAsInt(IntUnaryOperator.this.applyAsInt(t));
  }

  static IntUnaryOperator identity() {
    return t -> t;
  }
}
