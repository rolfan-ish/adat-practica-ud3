package menu;

import java.util.Scanner;
import java.util.function.Function;

public record Menu(Function<Integer, Integer> indexer, MenuEntry...entries) implements Runnable {
    private static Scanner sc = new Scanner(System.in);

    public Menu(MenuEntry...entries) {
        this(i -> i, entries);
    }

    @Override
    public void run() {
        for (var i = 0; i < entries.length; i++) {
            var index = indexer.apply(i);
            System.out.println(index + ". " + entries[index].desc());
        }
        var sel = indexer.apply(sc.nextInt());
        if (sel < 0 || sel > entries.length - 1)
            return;
        entries[sel].func().run();
    }
}
