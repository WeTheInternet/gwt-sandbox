package java.util.function;

@FunctionalInterface
public interface BiConsumer<O, T> {

  void accept(O o, T t);

  default BiConsumer<O, T> andThen(BiConsumer<? super O, ? super T> after) {
    return (l, r) -> {
      BiConsumer.this.accept(l, r);
      after.accept(l, r);
    };
  }
}
