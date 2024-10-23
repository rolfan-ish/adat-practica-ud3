package resolucion;

import menu.Entry;
import menu.Menu;

public class Main extends Menu {
    @Entry(position = 0, description = "Salir del programa")
    private void salir() {
        System.exit(0);
    }

    @Entry(position = 1, description = "Crear BBDD MySQL")
    private void crearBBDDMySQL() {
        System.out.println(getClass().getResource("resources/olimpiadas.db.sql"));
    }

    public static void main(String[] args) throws Exception {
        new Main().runMenuForever();
    }
}
