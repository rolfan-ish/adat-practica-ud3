package es.rolfan.app;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;
// import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class Insertor<T> implements Iterable<T> {
    // private final ConcurrentHashMap<T, T> map = new ConcurrentHashMap<>(1024);
    private final HashMap<T, T> map = new HashMap<>(1024);
    public T add(T obj) {
        return Optional.ofNullable(map.putIfAbsent(obj, obj)).orElse(obj);
    }

    public Stream<T> stream() {
        return map.values().stream();
    }

    public Stream<T> parallelStream() {
        return map.values().parallelStream();
    }

    @Override
    public Iterator<T> iterator() {
        return map.values().iterator();
    }
}
