package es.rolfan.menu.argument;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.HashMap;

public class DefaultArgGetterFactory implements ArgGetterFactory {
    private static final HashMap<Type, ArgGetter<?>> map = new HashMap<>();

    public static void registerDispatch(Type type, ArgGetter<?> rd) {
        map.put(type, rd);
    }

    static {
        var ig = new IntegerArgGetter();
        registerDispatch(int.class, ig);
        registerDispatch(Integer.class, ig);
        registerDispatch(String.class, new StringArgGetter());
        registerDispatch(FileReader.class, new FileReaderArgGetter());
    }

    public static <T> ArgGetter<T> get(Class<T> clazz) {
        var reader = map.get(clazz);
        if (reader == null)
            throw new RuntimeException("No dispatch for type " + clazz.getTypeName());
        return (ArgGetter<T>) reader;
    }

    @Override
    public <T> ArgGetter<T> build(Class<T> clazz) {
        return get(clazz);
    }
}
