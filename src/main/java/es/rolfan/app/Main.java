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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.stream.Collectors;

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

    public static void main(String[] args) throws Throwable {
        // new Menu(new Main()).runMenuForever();
        new Main().crearBBDDMySQL(new FileReader("/home/aimar/Downloads/ficheros_csv_xml_sql/athlete_events.csv"));
    }

    @Entry(position = 0, description = "Salir del programa")
    private void salir() {
        System.exit(0);
    }

    @Entry(position = 1, description = "Crear BBDD MySQL")
    private void crearBBDDMySQL(FileReader csv) {
        if (csv == null) {
            System.err.println("El archivo csv no existe");
            return;
        }
        var url = getClass().getResource("olimpiadas.sql");
        if (url == null) {
            System.err.println("Falta el archivo sql");
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
                var elems = new CsvToBeanBuilder<EntradaAtleta>(csv).withType(EntradaAtleta.class).withFilter(fields -> {
                    for (var i = 0; i < fields.length; i++)
                        if (fields[i].equals("NA"))
                            fields[i] = null;
                    return true;
                }).build();

                // Insertar los datos en la BBDD
                for (var e : elems) {
                    var sel = s.createQuery("FROM deportista", Deportista.class).stream().filter(d -> d.getIdDeportista() == e.getId()).collect(Collectors.toCollection(ArrayList::new));
                    Deportista deportista;
                    if (sel.isEmpty()) {
                        deportista = new Deportista(
                                e.getId(),
                                e.getNombre(),
                                e.getSexo(),
                                e.getPeso().intValue(),
                                e.getAltura());
                        s.persist(deportista);
                    } else {
                        deportista = sel.getFirst();
                    }
                    var deporte = new Deporte(
                            e.getDeporte());
                    s.persist(deporte);
                    var equipo = new Equipo(
                            e.getEquipo(),
                            e.getNoc());
                    s.persist(equipo);
                    var olimpiada = new Olimpiada(
                            e.getJuegos(),
                            e.getAnio(),
                            e.getTemporada(),
                            e.getCiudad());
                    s.persist(olimpiada);
                    var evento = new Evento(
                            e.getEvento(),
                            olimpiada,
                            deporte);
                    s.persist(evento);
                    var participacion = new Participacion(
                            deportista,
                            evento,
                            equipo.getIdEquipo(),
                            e.getEdad(),
                            e.getMedalla());
                    s.persist(participacion);
                }

            } catch (IOException e) {
                System.err.println("Ha ocurrido algun error en la carga");
                return;
            }
            System.out.println("La carga de la información se ha realizado correctamente");
        });
    }
}
