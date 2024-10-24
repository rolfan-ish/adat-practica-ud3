package es.rolfan.app;

import com.opencsv.bean.CsvToBeanBuilder;
import es.rolfan.dao.csv.EntradaAtleta;
import es.rolfan.dao.sql.*;
import es.rolfan.menu.Entry;
import es.rolfan.menu.LazyCall;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Supplier;

public class Main {
    private static final StandardServiceRegistry registry;
    private static final SessionFactory factory;

    static {
        registry = new StandardServiceRegistryBuilder().build();
        try {
            factory = new MetadataSources(registry)
                    .addAnnotatedClasses(Deporte.class, Deportista.class, Equipo.class, Evento.class, Olimpiada.class, Participacion.class)
                    .buildMetadata()
                    .buildSessionFactory();
        } catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw e;
        }
    }

    public static void main(String[] args) {
        // new Menu(new Main()).runMenuForever();
        var archivoCsv = Main.class.getResource("athlete_events.csv");
        new Main().crearBBDDMySQL(new LazyCall<>(() -> new FileReader(archivoCsv.getFile())));
    }

    @Entry(position = 0, description = "Salir del programa")
    private void salir() {
        System.exit(0);
    }

    @Entry(position = 1, description = "Crear BBDD MySQL")
    private void crearBBDDMySQL(Supplier<FileReader> csv) {
        var url = getClass().getResource("olimpiadas.sql");
        if (url == null) {
            System.err.println("Falta el archivo sql");
            return;
        }
        if (csv.get() == null) {
            System.err.println("El archivo csv no existe");
            return;
        }

        factory.inTransaction(s -> {
            try {
                // Crear las tablas
                var query = Files.readString(new File(url.getPath()).toPath()).split(";");
                for (var q : query) {
                    if (q.trim().isEmpty()) continue;
                    s.createNativeMutationQuery(q).executeUpdate();
                }

                // Extraer los datos del csv
                var elems = new CsvToBeanBuilder<EntradaAtleta>(csv.get()).withType(EntradaAtleta.class).withFilter(fields -> {
                    for (var i = 0; i < fields.length; i++)
                        if (fields[i].equals("NA"))
                            fields[i] = null;
                    return true;
                }).build();

                // Queries para tomar uno de cada
                var deportistaQuery = s.createQuery("FROM Deportista WHERE idDeportista = ?1", Deportista.class);
                var deporteQuery = s.createQuery("FROM Deporte WHERE nombre = ?1", Deporte.class);
                var equipoQuery = s.createQuery("FROM Equipo WHERE nombre = ?1", Equipo.class);
                var olimpiadaQuery = s.createQuery("FROM Olimpiada WHERE nombre = ?1", Olimpiada.class);
                var eventoQuery = s.createQuery(
                        "FROM Evento WHERE olimpiada = :olimpiada"
                        + " AND deporte = :deporte"
                        + " AND nombre = :nombre", Evento.class);
                var participacionQuery = s.createQuery(
                        "FROM Participacion WHERE deportista = :deportista"
                        +" AND evento = :evento", Participacion.class);
                for (var e : elems) {
                    // Insertar deportista
                    var deportista = deportistaQuery.setParameter(1, e.getId()).uniqueResult();
                    if (deportista == null) {
                        deportista = new Deportista(
                                e.getId(),
                                e.getNombre(),
                                e.getSexo(),
                                e.getPeso() == null ? null : e.getPeso().intValue(),
                                e.getAltura());
                        s.persist(deportista);
                    }
                    // Insertar deporte
                    var deporte = deporteQuery.setParameter(1, e.getDeporte()).uniqueResult();
                    if (deporte == null) {
                        deporte = new Deporte(e.getDeporte());
                        s.persist(deporte);
                    }
                    // Insertar equipo
                    var equipo = equipoQuery.setParameter(1, e.getEquipo()).uniqueResult();
                    if (equipo == null) {
                        equipo = new Equipo(e.getEquipo(), e.getNoc());
                        s.persist(equipo);
                    }
                    // Insertar olimpiada
                    var olimpiada = olimpiadaQuery.setParameter(1, e.getJuegos()).uniqueResult();
                    if (olimpiada == null) {
                        olimpiada = new Olimpiada(
                                e.getJuegos(),
                                e.getAnio(),
                                e.getTemporada(),
                                e.getCiudad());
                        s.persist(olimpiada);
                    }
                    // Insertar evento
                    var evento = eventoQuery
                            .setParameter("nombre", e.getEvento())
                            .setParameter("olimpiada", olimpiada)
                            .setParameter("deporte", deporte).uniqueResult();
                    if (evento == null) {
                        evento = new Evento(e.getEvento(), olimpiada, deporte);
                        s.persist(evento);
                    }
                    // Insertar participacion
                    var participacion = participacionQuery
                            .setParameter("deportista", deportista)
                            .setParameter("evento", evento).uniqueResult();
                    if (participacion == null) {
                        participacion = new Participacion(
                                deportista,
                                evento,
                                equipo.getIdEquipo(),
                                e.getEdad(),
                                e.getMedalla());
                        s.persist(participacion);
                    }
                }

            } catch (IOException e) {
                System.err.println("Ha ocurrido algun error en la carga");
                return;
            }
            System.out.println("La carga de la información se ha realizado correctamente");
        });
    }
}
