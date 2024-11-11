package es.rolfan.model.sql;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Participacion {
    @EmbeddedId
    private ParticipacionId id;

    @ManyToOne
    @MapsId("idDeportista")
    @JoinColumn(name = "id_deportista")
    private Deportista deportista;

    @ManyToOne
    @MapsId("idEvento")
    @JoinColumn(name = "id_evento")
    private Evento evento;

    public Participacion() {
    }

    @ManyToOne
    @JoinColumn(name = "id_equipo")
    private Equipo equipo;

    private Integer edad;

    private String medalla;

    public Participacion(Deportista deportista, Evento evento, Equipo equipo, Integer edad, String medalla) {
        this.deportista = deportista;
        this.evento = evento;
        this.equipo = equipo;
        this.edad = edad;
        this.medalla = medalla;
    }

    public ParticipacionId getId() {
        return id;
    }

    public void setId(ParticipacionId id) {
        this.id = id;
    }

    public Deportista getDeportista() {
        return deportista;
    }

    public void setDeportista(Deportista deportista) {
        this.deportista = deportista;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getMedalla() {
        return medalla;
    }

    public void setMedalla(String medalla) {
        this.medalla = medalla;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Participacion that)) return false;
        return Objects.equals(deportista, that.deportista) && Objects.equals(evento, that.evento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deportista, evento);
    }
}
