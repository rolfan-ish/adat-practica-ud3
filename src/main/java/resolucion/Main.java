package resolucion;

import menu.Menu;
import menu.MenuEntry;

public class Main {
    static void salir() {
        System.exit(0);
    }

    public static void main(String[] args) {
        MenuEntry[] entries = {
                new MenuEntry(Main::salir, "Salir del programa.")
        };
        var menu = new Menu(i -> (i + 1) % entries.length, entries);
        while (true) menu.run();
    }
}
