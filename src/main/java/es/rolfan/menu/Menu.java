package es.rolfan.menu;

import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public final class Menu {
    private static class ArgType {
        private Class<?> clazz;

        public ArgType(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ArgType a)) return false;
            return clazz.isAssignableFrom(a.clazz);
        }

        @Override
        public int hashCode() {
            return clazz.hashCode();
        }
    }

    private final ArrayList<Method> ms;
    private final String msg;
    private final Scanner sc = new Scanner(System.in);
    private static final HashMap<ArgType, ArgGetter<?>> getters = new HashMap<>();
    private final Object runner;

    static {
        registerGetter(Integer.class, new IntegerArgGetter());
        registerGetter(String.class, new StringArgGetter());
        registerGetter(FileReader.class, new FileArgGetter());
    }

    public Menu(Object runner) {
        this.runner = runner;
        var msgBuilder = new StringBuilder();
        ms = Arrays.stream(runner.getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Entry.class))
                .sorted(Comparator.comparingInt(m -> m.getAnnotation(Entry.class).position()))
                // Build prompt
                .peek(m -> {
                    m.setAccessible(true);
                    var e = m.getAnnotation(Entry.class);
                    msgBuilder.append(e.position()).append(". ").append(e.description()).append('\n');
                })
                .collect(Collectors.toCollection(ArrayList::new));
        msg = msgBuilder.toString();
    }

    public static <T> void registerGetter(Class<T> clazz, ArgGetter<T> getter) {
        getters.put(new ArgType(clazz), getter);
    }

    public void runMenu() throws Throwable {
        System.out.println(msg);
        var sel = sc.nextInt();
        if (sel < 0 || sel > ms.size() - 1) return;
        var m = ms.get(sel);

        // Get arguments for the method
        var params = m.getParameters();
        var objs = new Object[params.length];
        for (var i = 0; i < params.length; i++) {
            var p = params[i];
            if (p.isAnnotationPresent(Arg.class)) {
                var a = p.getAnnotation(Arg.class);
                var getter = a.getter().getConstructor().newInstance();
                objs[i] = getter.get(a.parameter(), p.getName());
            } else {
                var getter = getters.get(new ArgType(p.getType()));
                if (getter == null)
                    throw new RuntimeException("MENU ARGUMENT READER: No se puede leer el parametro \"" + p.getName() + "\" pues no tiene lector");
                objs[i] = getter.get("", p.getName());
            }
        }

        m.invoke(runner, objs);
    }

    public void runMenuForever() throws Throwable {
        while (true) runMenu();
    }
}
