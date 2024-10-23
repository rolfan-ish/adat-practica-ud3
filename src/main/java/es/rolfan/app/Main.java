package es.rolfan.app;

import es.rolfan.menu.Entry;
import es.rolfan.menu.Menu;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class Main extends Menu {
    private static final Scanner sc = new Scanner(System.in);

    private final Connection conn;

    public Main(String host, String port, String user, String password) throws SQLException {
        conn = DriverManager.getConnection(host + ":" + port, user, password);
    }

    public static void main(String[] args) throws Exception {
        new Main("localhost", "3306", "admin", "1234").runMenuForever();
    }

    @Entry(position = 0, description = "Salir del programa")
    private void salir() {
        System.exit(0);
    }

    @Entry(position = 1, description = "Crear BBDD MySQL")
    private void crearBBDDMySQL() {
        var url = getClass().getResource("olimpiadas.db.sql");
        if (url == null) {
            System.err.println("Falta el archivo sql");
            return;
        }

        System.out.println("Introduce el nombre del archivo");
        var csv = new File(sc.next());
        if (!csv.exists() || !csv.isFile()) {
            System.err.println("El archivo csv no existe");
            return;
        }

        try (var st = conn.createStatement()) {
            st.executeUpdate(Files.readString(new File(url.getFile()).toPath()));
            // TODO: Cargar archivo csv
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return;
        } catch (SQLException e) {
            System.err.println("Ha ocurrido algún error en la carga");
            return;
        }

        System.out.println("La carga de la información se ha realizado correctamente");
    }
}
