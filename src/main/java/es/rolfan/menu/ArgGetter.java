package es.rolfan.menu;

public interface ArgGetter<T> {
    T get(String param, String name);
}
