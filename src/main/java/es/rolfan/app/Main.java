package es.rolfan.app;

import com.opencsv.bean.CsvToBeanBuilder;
import es.rolfan.dao.csv.EntradaAtleta;
import es.rolfan.dao.sql.*;
import es.rolfan.menu.Entry;
import es.rolfan.menu.Menu;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.MutationQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Supplier;
import java.util.stream.Stream;

class Insertor<T> {
    private final HashMap<T, T> map = new HashMap<>(1024);

    public T add(T obj) {
        var put = map.putIfAbsent(obj, obj);
        return put == null ? obj : put;
    }

    public Stream<T> stream() {
        return map.values().stream();
    }
}

public class Main {
    private static final String SCRIPT_SQL_INIT = "olimpiadas.sql";
    private static final StandardServiceRegistry registry;
    private static final SessionFactory factory;
    private static final Logger log = LoggerFactory.getLogger(Main.class);

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
        new Menu(new Main()).runMenuForever();
    }

    @Entry(position = 0, description = "Salir del programa")
    private void salir() {
        System.exit(0);
    }

    @Entry(position = 1, description = "Crear BBDD MySQL")
    public void crearBBDDMySQL(Supplier<FileReader> csv) {
        var url = getClass().getResource(SCRIPT_SQL_INIT);
        if (url == null) {
            log.error("Falta el archivo sql");
            return;
        }
        if (csv.get() == null) {
            log.error("El archivo csv no existe");
            return;
        }

        log.info("Creando tablas");
        try {
            var sc = new Scanner(new FileReader(url.getFile())).useDelimiter(";");
            factory.inTransaction(s -> {
                sc.tokens()
                        .filter(q -> !q.isBlank())
                        .map(s::createNativeMutationQuery)
                        .forEach(MutationQuery::executeUpdate);
            });
        } catch (FileNotFoundException _) {
            log.error("El archivo sql no se encontro");
        }

        log.info("Borrando contenidos de las tablas");
        factory.inTransaction(s -> {
            Stream.of(Deporte.class, Deportista.class, Equipo.class, Evento.class, Olimpiada.class, Participacion.class)
                    .map(Class::getSimpleName)
                    .map(n -> "DELETE FROM " + n)
                    .map(s::createMutationQuery)
                    .forEach(MutationQuery::executeUpdate);
        });

        log.info("Extrallendo datos del csv");
        var elems = new CsvToBeanBuilder<EntradaAtleta>(new BufferedReader(csv.get()))
                .withOrderedResults(false)
                .withType(EntradaAtleta.class)
                .withFilter(fields -> {
                    for (var i = 0; i < fields.length; i++)
                        if (fields[i].equals("NA"))
                            fields[i] = null;
                    return true;
                }).build();

        var deportistas = new Insertor<Deportista>();
        var deportes = new Insertor<Deporte>();
        var equipos = new Insertor<Equipo>();
        var olimpiadas = new Insertor<Olimpiada>();
        var eventos = new Insertor<Evento>();
        var participaciones = new Insertor<Participacion>();

        elems.stream().forEach(e -> {
            var deportista = deportistas.add(new Deportista(
                    e.getId(),
                    e.getNombre(),
                    e.getSexo(),
                    e.getPeso().map(Double::intValue).orElse(null),
                    e.getAltura()));
            var deporte = deportes.add(new Deporte(e.getDeporte()));
            var equipo = equipos.add(new Equipo(e.getEquipo(), e.getNoc()));
            var olimpiada = olimpiadas.add(new Olimpiada(
                    e.getJuegos(),
                    e.getAnio(),
                    e.getTemporada(),
                    e.getCiudad()));
            var evento = eventos.add(new Evento(e.getEvento(), olimpiada, deporte));
            participaciones.add(new Participacion(
                    deportista,
                    evento,
                    equipo,
                    e.getEdad(),
                    e.getMedalla()));
        });

        log.info("Escribiendo a base de datos");
        factory.inTransaction(s ->
                Stream.of(deportistas, deportes, equipos, olimpiadas, eventos, participaciones)
                        .map(Insertor::stream)
                        .reduce(Stream.of(), Stream::concat)
                        .forEach(s::persist));

        log.info("Terminado exitosamente");
    }
}
