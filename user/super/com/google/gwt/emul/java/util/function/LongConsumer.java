package java.util.function;

@FunctionalInterface
public interface LongConsumer {

  void accept(long value);

  default LongConsumer andThen(LongConsumer after) {
    assert after != null;
    return (long t) -> {
      LongConsumer.this.accept(t);
      after.accept(t);
    };
  }
}
