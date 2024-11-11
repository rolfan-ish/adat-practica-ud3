package es.rolfan.app;

import com.opencsv.bean.CsvToBeanBuilder;
import es.rolfan.menu.Entry;
import es.rolfan.menu.Menu;
import es.rolfan.menu.argument.Arg;
import es.rolfan.menu.argument.FileArgGetter;
import es.rolfan.menu.argument.IntegerArgGetter;
import es.rolfan.menu.argument.StringArgGetter;
import es.rolfan.model.csv.EntradaAtleta;
import es.rolfan.model.sql.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.MutationQuery;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

public class Main {
    public static final String SCRIPT_SQL_INIT = "olimpiadas.sql";
    public static final SessionFactory factory;

    static {
        var registry = new StandardServiceRegistryBuilder().build();
        try {
            factory = new MetadataSources(registry).addAnnotatedClasses(Deporte.class, Deportista.class, Equipo.class, Evento.class, Olimpiada.class, Participacion.class).buildMetadata().buildSessionFactory();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        new Menu(new Main()).runMenuForever();
    }

    @Entry(key = "0", pos = -1, desc = "Salir del programa")
    public void salir() {
        System.exit(0);
    }

    @Entry(key = "2", pos = 2, desc = "Listado deportistas")
    public void listadoDeportistas() {
        factory.inTransaction(s -> s.createSelectionQuery("FROM Deportista", Deportista.class).stream().forEach(d -> {
            System.out.format("Nombre: %s, Sexo: %s, Altura: %s, Peso: %s\n",
                    d.getNombre(), d.getSexo(), d.getAltura(), d.getPeso());
            d.getParticipaciones().forEach(p ->
                    System.out.format("\tDeporte: %s, Edad: %d, Evento: %s, Equipo: %s, Juegos: %s, Medalla: %s\n",
                            p.getEvento().getDeporte().getNombre(), p.getEdad(), p.getEvento().getNombre(),
                            p.getEquipo().getNombre(), p.getEvento().getOlimpiada().getNombre(), p.getMedalla()));
        }));
    }

    private static <T> T getCodigo(Session s, Callable<?> codigoGetter, Class<T> clazz) {
        Object cod = null;
        try {
            cod = codigoGetter.call();
        } catch (Exception e) {
            System.err.println("Codigo invalido");
            return null;
        }
        var res = s.find(clazz, cod);
        if (res == null)
            System.err.println("Codigo invalido");
        return res;
    }

    @Entry(key = "3", pos = 3, desc = "Listado de deportistas participantes")
    public void listadoDeporPart(@Arg(getter = StringArgGetter.class) Callable<String> temporada,
                                 @Arg(getter = IntegerArgGetter.class) Callable<Integer> codigoOlimpiada,
                                 @Arg(getter = IntegerArgGetter.class) Callable<Integer> codigoDeporte,
                                 @Arg(getter = IntegerArgGetter.class) Callable<Integer> codigoEvento) {
        String tempo;
        try {
            var res = temporada.call().toLowerCase();
            tempo = "winter".startsWith(res) ? "Winter"
                    : "summer".startsWith(res) ? "Summer"
                    : null;
        } catch (Exception e) {
            tempo = null;
        }
        if (tempo == null) {
            System.err.println("Temporada invalida");
            return;
        }
        var temporadaString = tempo;
        factory.inTransaction(s -> {
            s.createSelectionQuery("FROM Olimpiada WHERE temporada = :temporada", Olimpiada.class)
                    .setParameter("temporada", temporadaString)
                    .stream()
                    .forEach(o -> System.out.println(o.getIdOlimpiada() + ". " + o.getNombre()));

            var olim = getCodigo(s, codigoOlimpiada, Olimpiada.class);
            if (olim == null)
                return;

            olim.getEventos().stream()
                    .map(Evento::getDeporte)
                    .forEach(d -> System.out.println(d.getIdDeporte() + ". " + d.getNombre()));
            var depor = getCodigo(s, codigoDeporte, Deporte.class);
            if (depor == null)
                return;

            depor.getEventos().forEach(e ->
                    System.out.println(e.getIdEvento() + ". " + e.getNombre()));
            var even = getCodigo(s, codigoEvento, Evento.class);
            if (even == null)
                return;

            System.out.println("Temporada: " + temporadaString);
            System.out.println("Edición olimpica: " + olim.getNombre());
            System.out.println("Deporte: " + depor.getNombre());
            System.out.println("Evento: " + even.getNombre());
            System.out.println("Deportistas: ");
            even.getParticipaciones().stream()
                    .map(Participacion::getDeportista)
                    .forEach(d -> System.out.println("\t" + d.getNombre()));
        });
    }

    @Entry(key = "4", pos = 4, desc = "Modificar medalla deportista")
    public void modificarMedalla(@Arg(getter = StringArgGetter.class) Callable<String> textoBusqueda,
                                 @Arg(getter = IntegerArgGetter.class) Callable<Integer> deportistaCodigo,
                                 @Arg(getter = IntegerArgGetter.class) Callable<Integer> eventoCodigo,
                                 @Arg(getter = StringArgGetter.class) Callable<String> medalla) {
        String texto;
        try {
            texto = textoBusqueda.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        factory.inTransaction(s -> {
            s.createSelectionQuery("FROM Deportista d WHERE d.nombre LIKE :texto", Deportista.class)
                    .setParameter("texto", texto)
                    .stream()
                    .forEach(d -> System.out.println(d.getIdDeportista() + ". " + d.getNombre()));
            var deportista = getCodigo(s, deportistaCodigo, Deportista.class);
            if (deportista == null)
                return;

            deportista.getParticipaciones()
                    .stream()
                    .map(Participacion::getEvento)
                    .forEach(e -> System.out.println(e.getIdEvento() + ". " + e.getNombre()));
            var participacion = getCodigo(s, () -> new ParticipacionId(deportista.getIdDeportista(), eventoCodigo.call()), Participacion.class);
            if (participacion == null)
                return;

            try {
                participacion.setMedalla(medalla.call());
                s.merge(participacion);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Entry(key = "5", pos = 5, desc = "Añadir deportista/participación")
    public void aniadirDeportistaParticipacion(@Arg(getter = StringArgGetter.class) Callable<String> textoBusqueda,
                                               @Arg(getter = IntegerArgGetter.class) Callable<Integer> deportistaCodigo,
                                               @Arg(getter = StringArgGetter.class) Callable<String> temporada,
                                               @Arg(getter = IntegerArgGetter.class) Callable<Integer> codigoOlimpiada,
                                               @Arg(getter = IntegerArgGetter.class) Callable<Integer> codigoDeporte,
                                               @Arg(getter = IntegerArgGetter.class) Callable<Integer> codigoEvento,
                                               @Arg(getter = StringArgGetter.class) Callable<String> nombreDeportista,
                                               @Arg(getter = StringArgGetter.class) Callable<String> sexoDeportista) {
        String texto;
        try {
            texto = textoBusqueda.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        factory.inTransaction(s -> {
            var res = s.createSelectionQuery("FROM Deportista d WHERE d.nombre LIKE :texto", Deportista.class)
                    .setParameter("texto", texto)
                    .getResultList();
            Deportista deportista;
            if (res.isEmpty()) {
                try {
                    var sexo = sexoDeportista.call().toLowerCase();
                    var sexoEnum = "masculino".startsWith(sexo) ? "M"
                            : "femenino".startsWith(sexo) ? "F"
                            : null;
                    if (sexoEnum == null) {
                        System.out.println("Sexo invalido");
                        return;
                    }
                    deportista = new Deportista(nombreDeportista.call(), sexoEnum, null, null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                s.persist(deportista);
            } else {
                res.forEach(d -> System.out.println(d.getIdDeportista() + ". " + d.getNombre()));
                deportista = getCodigo(s, deportistaCodigo, Deportista.class);
                if (deportista == null)
                    return;
            }

            String tempo;
            try {
                var tmp = temporada.call().toLowerCase();
                tempo = "winter".startsWith(tmp) ? "Winter"
                        : "summer".startsWith(tmp) ? "Summer"
                        : null;
            } catch (Exception e) {
                tempo = null;
            }
            if (tempo == null) {
                System.err.println("Temporada invalida");
                return;
            }

            s.createSelectionQuery("FROM Olimpiada WHERE temporada = :temporada", Olimpiada.class)
                    .setParameter("temporada", tempo)
                    .stream()
                    .forEach(o -> System.out.println(o.getIdOlimpiada() + ". " + o.getNombre()));
            var olim = getCodigo(s, codigoOlimpiada, Olimpiada.class);
            if (olim == null)
                return;

            olim.getEventos().stream()
                    .map(Evento::getDeporte)
                    .forEach(d -> System.out.println(d.getIdDeporte() + ". " + d.getNombre()));
            var depor = getCodigo(s, codigoDeporte, Deporte.class);
            if (depor == null)
                return;

            depor.getEventos().forEach(e ->
                    System.out.println(e.getIdEvento() + ". " + e.getNombre()));
            var even = getCodigo(s, codigoEvento, Evento.class);
            if (even == null)
                return;

            var part = new Participacion(deportista, even, , null, null);
            s.persist(part);
        });
    }

    @Entry(key = "6", pos = 6, desc = "Eliminar participación")
    public void eliminarParticipacion(@Arg(getter = StringArgGetter.class) Callable<String> textoBusqueda,
                                      @Arg(getter = IntegerArgGetter.class) Callable<Integer> codigoDeportista,
                                      @Arg(getter = IntegerArgGetter.class) Callable<Integer> codigoParticipacion) {
        factory.inTransaction(s -> {
            String texto;
            try {
                texto = textoBusqueda.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            s.createSelectionQuery("FROM Deportista d WHERE d.nombre LIKE :texto", Deportista.class)
                    .setParameter("texto", texto)
                    .stream().forEach(d -> System.out.println(d.getIdDeportista() + ". " + d.getNombre()));
            var depor = getCodigo(s, codigoDeportista, Deportista.class);
            if (depor == null)
                return;

            depor.getParticipaciones()
                    .stream()
                    .map(Participacion::getEvento)
                    .forEach(e -> System.out.println(e.getIdEvento() + ". " + e.getNombre()));
            var part = getCodigo(s, () -> new ParticipacionId(depor.getIdDeportista(), codigoParticipacion.call()), Participacion.class);
            if (part == null)
                return;
            s.remove(part);
            depor.getParticipaciones().remove(part);
            if (depor.getParticipaciones().isEmpty()) {
                s.remove(depor);
            }
        });
    }

    @Entry(key = "1", pos = 1, desc = "Crear BBDD MySQL")
    public void crearBBDDMySQL(@Arg(getter = FileArgGetter.class) Callable<FileReader> archivoCsv) {
        var url = getClass().getResource(SCRIPT_SQL_INIT);
        if (url == null) {
            System.err.println("Falta el archivo sql");
            return;
        }

        System.out.println("Creando tablas");
        try {
            var sc = new Scanner(new FileReader(url.getFile())).useDelimiter(";");
            factory.inTransaction(s ->
                    sc.tokens().filter(q -> !q.isBlank())
                            .map(s::createNativeMutationQuery)
                            .forEach(MutationQuery::executeUpdate));
        } catch (FileNotFoundException _) {
            System.err.println("El archivo sql no se encontro");
        }

        System.out.println("Borrando contenidos de las tablas");
        factory.inTransaction(s -> {
            Stream.of(Participacion.class, Evento.class, Olimpiada.class, Deporte.class, Deportista.class, Equipo.class)
                    .map(c -> "DELETE FROM " + c.getSimpleName())
                    .forEach(q -> s.createMutationQuery(q).executeUpdate());
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
                        if (fields[i].equals("NA")) fields[i] = null;
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
            var deporte = deportes.add(new Deporte(
                    e.getDeporte()));
            var equipo = equipos.add(new Equipo(
                    e.getEquipo(),
                    e.getNoc()));
            var olimpiada = olimpiadas.add(new Olimpiada(
                    e.getJuegos(),
                    e.getAnio(),
                    e.getTemporada(),
                    e.getCiudad()));
            var evento = eventos.add(new Evento(
                    e.getEvento(),
                    olimpiada,
                    deporte));
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
