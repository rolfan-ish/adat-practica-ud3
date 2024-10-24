package es.rolfan.menu;

import java.util.function.Supplier;

public abstract class LazyArgGetter<T> implements ArgGetter<Supplier<T>>, Supplier<T> {
    private String param;
    private String name;
    private boolean present = false;
    private T value;

    @Override
    public Supplier<T> get(String param, String name) {
        this.param = param;
        this.name = name;
        return this;
    }

    public abstract ArgGetter<T> instanceGetter();

    @Override
    public T get() {
        if (!present) {
            value = instanceGetter().get(param, name);
            present = true;
        }
        return value;
    }
}
