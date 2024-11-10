package es.rolfan.menu;

import es.rolfan.menu.argument.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class Menu {
    private final Map<String, Method> ms;
    private final String msg;
    private final Scanner sc = new Scanner(System.in);

    private final Object runner;

    public Menu(Object runner) {
        this.runner = runner;
        var msgBuilder = new StringBuilder();
        ms = Arrays.stream(runner.getClass().getMethods())
                .filter(m -> m.isAnnotationPresent(Entry.class))
                // unsigned sort
                .sorted((m, n) -> Integer.compareUnsigned(
                        m.getAnnotation(Entry.class).pos(),
                        n.getAnnotation(Entry.class).pos()))
                // Build prompt
                .peek(m -> {
                    m.setAccessible(true);
                    var e = m.getAnnotation(Entry.class);
                    msgBuilder.append(e.key()).append(". ").append(e.desc()).append('\n');
                })
                .collect(Collectors.toMap(m -> m.getAnnotation(Entry.class).key(), m -> m));
        msg = msgBuilder.toString();
    }

    public void runMenu() {
        System.out.println(msg);
        var m = ms.get(sc.nextLine());
        if (m == null) return;

        // Get arguments for the method
        var params = m.getParameters();
        var objs = new Callable[params.length];
        for (var i = 0; i < params.length; i++) {
            var p = params[i];

            // Get type parameter
            var ptype = (ParameterizedType)p.getType().getGenericSuperclass();
            var type = (Class<?>)ptype.getActualTypeArguments()[0];

            ArgGetter<?> argGetter;
            String value;
            if (p.isAnnotationPresent(Arg.class)) {
                var a = p.getAnnotation(Arg.class);
                value = a.value();
                try {
                    argGetter = a.getter().getConstructor().newInstance().build(type);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            } else {
                argGetter = DefaultArgGetterFactory.get(type);
                value = "";
            }
            objs[i] = () -> argGetter.get(value, p.getName());
        }

        try {
            m.invoke(runner, (Object[]) objs);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void runMenuForever() {
        while (true) runMenu();
    }
}
