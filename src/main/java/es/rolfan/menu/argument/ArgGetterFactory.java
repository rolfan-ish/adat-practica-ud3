package es.rolfan.menu.argument;

public interface ArgGetterFactory {
    <T> ArgGetter<T> build(Class<T> clazz);
}
