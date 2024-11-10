package es.rolfan.menu.argument;

public interface ArgGetter<T> {
    T get(String param, String name) throws Exception;
}
