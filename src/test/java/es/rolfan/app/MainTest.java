package es.rolfan.app;

import es.rolfan.model.sql.*;
import org.hibernate.query.MutationQuery;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Scanner;
import java.util.stream.Stream;

class MainTest {
    static Main m;

    @BeforeAll
    static void init() {
        m = new Main();

        // Crear tablas
        var sc = new Scanner(Main.class.getResourceAsStream(Main.SCRIPT_SQL_INIT)).useDelimiter(";");
        Main.factory.inTransaction(s ->
                sc.tokens().filter(q -> !q.isBlank())
                        .map(s::createNativeMutationQuery)
                        .forEach(MutationQuery::executeUpdate));
    }

    Deportista deportista;
    Deporte deporte;
    Olimpiada olimpiada;
    Evento evento1;
    Evento evento2;
    Equipo equipo;
    Participacion participacion1;
    Participacion participacion2;

    @BeforeEach
    void cargarTestDatos() {
        // Borrar datos
        Main.factory.inTransaction(s ->
                Stream.of(Participacion.class, Evento.class, Olimpiada.class, Deporte.class, Deportista.class, Equipo.class)
                        .map(c -> "DELETE FROM " + c.getSimpleName())
                        .forEach(q -> s.createMutationQuery(q).executeUpdate()));

        // cargar datos de testeo
        deportista = new Deportista("DEPORTISTA", "M", null, null);
        deporte = new Deporte("DEPORTE");
        olimpiada = new Olimpiada("OLIMPIADA", 0, "Winter", "");
        evento1 = new Evento("EVENTO1", olimpiada, deporte);
        evento2 = new Evento("EVENTO2", olimpiada, deporte);
        equipo = new Equipo("EQUIPO", "EQU");
        participacion1 = new Participacion(deportista, evento1, equipo, null, null);
        participacion2 = new Participacion(deportista, evento2, equipo, null, null);

        Main.factory.inTransaction(s ->
            Stream.of(deportista, deporte, olimpiada, equipo, evento1, evento2, participacion1, participacion2)
                    .forEach(s::persist));
    }

    @Test
    void cargarDatos() {
        var archivoCsv = Main.class.getResourceAsStream("athlete_events.csv");
        m.crearBBDDMySQL(() -> archivoCsv);
    }

    @Test
    void testListado() {
        m.listadoDeportistas();
    }

    @Test
    void testListadoDeporPart() {
        m.listadoDeporPart(() -> "W", () -> olimpiada.getIdOlimpiada(), () -> deporte.getIdDeporte(), () -> evento1.getIdEvento());
    }

    @Test
    void testAniadirNuevoDeportistaParticipacion() {
        var nuevoEvento = new Evento("nuevo", olimpiada, deporte);
        long siz;
        try (var em = Main.factory.createEntityManager()) {
            em.getTransaction().begin();
            siz = em.createQuery("SELECT COUNT(*) FROM Deportista", Long.class).getSingleResult();
            em.persist(nuevoEvento);
            em.getTransaction().commit();
        }
        m.aniadirDeportistaParticipacion(() -> "", () -> 0, () -> "w",
                olimpiada::getIdOlimpiada, deporte::getIdDeporte, nuevoEvento::getIdEvento, equipo::getIdEquipo,
                () -> "juan", () -> "m");
        Main.factory.inTransaction(s -> {
            var count = s.createSelectionQuery("SELECT COUNT(*) FROM Deportista", Long.class).getSingleResult();
            assertEquals(siz + 1, count);
        });
    }

    @Test
    void testAniadirParticipacion() {
        var nuevoEvento = new Evento("nuevo", olimpiada, deporte);
        int siz;
        try (var em = Main.factory.createEntityManager()) {
            em.getTransaction().begin();
            em.refresh(deportista);
            siz = deportista.getParticipaciones().size();
            em.persist(nuevoEvento);
            em.getTransaction().commit();
        }
        m.aniadirDeportistaParticipacion(() -> "%", deportista::getIdDeportista, () -> "w",
                olimpiada::getIdOlimpiada, deporte::getIdDeporte, nuevoEvento::getIdEvento, equipo::getIdEquipo,
                () -> null, () -> null);
        Main.factory.inTransaction(s -> {
            s.refresh(deportista);
            assertEquals(siz + 1, deportista.getParticipaciones().size());
        });
    }

    @Test
    void modificarMedalla() {
        assertNotEquals("gold", participacion1.getMedalla());
        m.modificarMedalla(() -> deportista.getNombre(), () -> deportista.getIdDeportista(), () -> evento1.getIdEvento(), () -> "gold");
        Main.factory.inTransaction(s -> s.refresh(participacion1));
        assertEquals("gold", participacion1.getMedalla());
    }

    @Test
    void eliminarParticipacion() {
        m.eliminarParticipacion(deportista::getNombre, deportista::getIdDeportista, participacion1.getId()::idEvento);
        m.eliminarParticipacion(deportista::getNombre, deportista::getIdDeportista, participacion2.getId()::idEvento);
        Main.factory.inTransaction(s -> {
            var deportitas = s.createSelectionQuery("SELECT COUNT(*) FROM Deportista", Long.class).getSingleResult();
            var participaciones = s.createSelectionQuery("SELECT COUNT(*) FROM Participacion", Long.class).getSingleResult();
            assertEquals(0, deportitas);
            assertEquals(0, participaciones);
        });
    }

    @Test
    void eliminarParticipacionUnica() {
        m.eliminarParticipacion(deportista::getNombre, deportista::getIdDeportista, participacion1.getId()::idEvento);
        Main.factory.inTransaction(s -> {
            var deportitas = s.createSelectionQuery("SELECT COUNT(*) FROM Deportista", Long.class).getSingleResult();
            var participaciones = s.createSelectionQuery("SELECT COUNT(*) FROM Participacion", Long.class).getSingleResult();
            assertEquals(1, deportitas);
            assertEquals(1, participaciones);
        });
    }
}