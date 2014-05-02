package java.util.function;

@FunctionalInterface
public interface LongBinaryOperator {
  long applyAsLong(long zero, long one);
}