package java.util.function;

@FunctionalInterface
public interface IntConsumer {

  void accept(int value);

  default IntConsumer andThen(IntConsumer after) {
    assert after != null;
    return (int t) -> {
      IntConsumer.this.accept(t);
      after.accept(t);
    };
  }
}
