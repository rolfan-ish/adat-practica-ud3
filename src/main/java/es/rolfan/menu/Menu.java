package es.rolfan.menu;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public abstract class Menu {
    private final ArrayList<Method> ms;
    private final String msg;
    private final Scanner sc = new Scanner(System.in);

    public Menu() {
        ms = Arrays.stream(getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Entry.class))
                .sorted(Comparator.comparingInt(m -> m.getAnnotation(Entry.class).position()))
                .collect(Collectors.toCollection(ArrayList::new));
        msg = ms.stream().map(m -> m.getAnnotation(Entry.class))
                .map(e -> e.position() + ". " + e.description())
                .collect(Collectors.joining("\n"));
    }

    private void runMenu() throws Exception {
        System.out.println(msg);
        var sel = sc.nextInt();
        if (sel < 0 || sel > ms.size() - 1) return;
        ms.get(sel).setAccessible(true);
        ms.get(sel).invoke(this);
    }

    public final void start() throws Exception {
        runMenu();
    }

    public final void runMenuForever() throws Exception {
        while (true) runMenu();
    }
}
