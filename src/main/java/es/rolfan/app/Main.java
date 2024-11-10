package es.rolfan.app;

import com.opencsv.bean.CsvToBeanBuilder;
import es.rolfan.menu.Entry;
import es.rolfan.menu.Menu;
import es.rolfan.model.csv.EntradaAtleta;
import es.rolfan.model.sql.*;
import jakarta.persistence.Id;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.processing.Find;
import org.hibernate.annotations.processing.HQL;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.MutationQuery;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

public class Main {
    private static final String SCRIPT_SQL_INIT = "olimpiadas.sql";
    private static final SessionFactory factory;

    static {
        var registry = new StandardServiceRegistryBuilder().build();
        try {
            factory = new MetadataSources(registry)
                    .addAnnotatedClasses(Deporte.class, Deportista.class, Equipo.class, Evento.class, Olimpiada.class, Participacion.class)
                    .buildMetadata()
                    .buildSessionFactory();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        new Menu(new Main()).runMenuForever();
    }

    @Entry(key = "0", desc = "Salir del programa")
    private void salir() {
        System.exit(0);
    }

    @Entry(key = "2", pos = 2, desc = "Listado deportistas")
    public void listadoDeportistas() {
        factory.inTransaction(s ->
                s.createSelectionQuery("FROM Deportista", Deportista.class)
                        .stream()
                        .forEach(d -> {
                            System.out.format("Nombre: %s, Sexo: %s, Altura: %s, Peso: %s\n",
                                    d.getNombre(), d.getSexo(), d.getAltura(), d.getPeso());
                            d.getParticipaciones()
                                    .forEach(p -> {
                                        System.out.format("\tDeporte: %s, Edad: %d, Evento: %s, Equipo: %s, Juegos: %s, Medalla: %s\n",
                                                p.getEvento().getDeporte().getNombre(), p.getEdad(), p.getEvento().getNombre(),
                                                p.getEquipo().getNombre(), p.getEvento().getOlimpiada().getNombre(), p.getMedalla());
                                    });
                        }));
    }

    private static <T> T getCodigo(Callable<Integer> codigoGetter, Class<T> clazz) {
        try (var em = factory.createEntityManager()) {
            int cod = codigoGetter.call();
            var fields = clazz.getDeclaredFields();
            var id = Arrays.stream(fields)
                    .filter(f -> f.isAnnotationPresent(jakarta.persistence.Id.class))
                    .map(f -> f.getAnnotation(jakarta.persistence.Column.class).name())
                    .findFirst().orElseThrow();
            return em.createQuery("FROM Olimpiada WHERE " + id + " = :id", clazz)
                    .setParameter("id", cod)
                    .getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Entry(key = "3", pos = 3, desc = "Listado de deportistas participantes")
    public void listadoDeDeportistasParticipantes(Callable<String> temporada, Callable<Integer> codigoNumerico) {
        String tempo;
        try {
            tempo = temporada.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String temp;
        if ("winter".startsWith(tempo.toLowerCase())) {
            temp = "Winter";
        } else if ("summer".startsWith(tempo.toLowerCase())) {
            temp = "Summer";
        } else {
            System.err.println("Temporada invalida");
            return;
        }

        factory.inTransaction(s -> {
            var olimpiadaList = s.createSelectionQuery("FROM Olimpiada WHERE temporada = :temporada", Olimpiada.class)
                    .setParameter("temporada", temp)
                    .getResultList();

            for (var o : olimpiadaList) {
                System.out.println(o.getIdOlimpiada() + ". " + o.getNombre());
            }
            var olim = getCodigo(codigoNumerico, Olimpiada.class);
            if (olim == null) {
                System.err.println("Codigo invalido");
                return;
            }

            for (var e : olim.getEventos()) {
                System.out.println(e.getDeporte().getIdDeporte() + ". " + e.getDeporte().getNombre());
            }
            var depor = getCodigo(codigoNumerico, Deporte.class);
            if (depor == null) {
                System.err.println("Codigo invalido");
                return;
            }

            for (var e : depor.getEventos()) {
                System.out.println(e.getIdEvento()+ ". " + e.getNombre());
            }
            var even = getCodigo(codigoNumerico, Evento.class);
            if (even == null) {
                System.err.println("Codigo invalido");
                return;
            }



        }

    }


    @Entry(key = "1", pos = 1, desc = "Crear BBDD MySQL")
    public void crearBBDDMySQL(Callable<FileReader> archivoCsv) {
        var url = getClass().getResource(SCRIPT_SQL_INIT);
        if (url == null) {
            System.err.println("Falta el archivo sql");
            return;
        }

        System.out.println("Creando tablas");
        try {
            var sc = new Scanner(new FileReader(url.getFile())).useDelimiter(";");
            factory.inTransaction(s -> {
                sc.tokens()
                        .filter(q -> !q.isBlank())
                        .map(s::createNativeMutationQuery)
                        .forEach(MutationQuery::executeUpdate);
            });
        } catch (FileNotFoundException _) {
            System.err.println("El archivo sql no se encontro");
        }

        System.out.println("Borrando contenidos de las tablas");
        factory.inTransaction(s -> {
            Stream.of(Deporte.class, Deportista.class, Equipo.class, Evento.class, Olimpiada.class, Participacion.class)
                    .map(Class::getSimpleName)
                    .map(n -> "DELETE FROM " + n)
                    .map(s::createMutationQuery)
                    .forEach(MutationQuery::executeUpdate);
        });


        FileReader fichero;
        try {
            fichero = archivoCsv.call();
        } catch (FileNotFoundException e) {
            System.err.println("El fichero csv no se encontro");
            return;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("Extrallendo datos del csv");
        var elems = new CsvToBeanBuilder<EntradaAtleta>(new BufferedReader(fichero))
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

        elems.forEach(e -> {
            var deportista = deportistas.add(new Deportista(
                    e.getId(),
                    e.getNombre(),
                    e.getSexo(),
                    e.getPeso() == null ? null : e.getPeso().intValue(),
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

        System.out.println("Escribiendo a base de datos");
        factory.inStatelessTransaction(s ->
                Stream.of(deportistas, deportes, equipos, olimpiadas, eventos, participaciones)
                        .map(Insertor::stream)
                        .reduce(Stream::concat)
                        .get().forEach(s::insert));


        System.out.println("Terminado exitosamente");
    }
}
