package es.rolfan.app;

import es.rolfan.menu.LazyCall;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileReader;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MainTest {
    static Main m;

    @BeforeAll
    static void setUp() {
        m = new Main();
        var archivoCsv = Main.class.getResource("athlete_events.csv");
        assertNotNull(archivoCsv, "No se encontro el archivo csv");
        m.crearBBDDMySQL(new LazyCall<>(() -> new FileReader(archivoCsv.getFile())));
    }

    @Test
    void testBuild() {}
}