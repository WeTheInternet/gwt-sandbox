package java.util.function;

@FunctionalInterface
public interface DoubleUnaryOperator {

  double applyAsDouble(double operand);

  default DoubleUnaryOperator compose(DoubleUnaryOperator before) {
    assert before != null;
    return (double v) -> DoubleUnaryOperator.this.applyAsDouble(before.applyAsDouble(v));
  }

  default DoubleUnaryOperator andThen(DoubleUnaryOperator after) {
    assert after != null;
    return (double t) -> after.applyAsDouble(DoubleUnaryOperator.this.applyAsDouble(t));
  }

  static DoubleUnaryOperator identity() {
    return t -> t;
  }
}
