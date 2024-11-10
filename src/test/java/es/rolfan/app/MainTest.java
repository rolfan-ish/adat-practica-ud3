package es.rolfan.app;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;

class MainTest {
    static Main m;

    @BeforeAll
    static void setUp() {
        m = new Main();
        var archivoCsv = Main.class.getResource("athlete_events.csv");
        m.crearBBDDMySQL(() -> {
            if (archivoCsv == null)
                throw new FileNotFoundException();
            return new FileReader(archivoCsv.getFile());
        });
    }

    @Test
    void testBuild() {}
}