package es.rolfan.menu;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class LazyCall<T> implements Supplier<T> {
    private Callable<T> arg;

    public LazyCall(Callable<T> arg) {
        this.arg = arg;
    }

    @Override
    public T get() {
        try {
            return arg.call();
        } catch (Exception e) {
            return null;
        }
    }
}
