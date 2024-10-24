package es.rolfan.dao.sql;

import jakarta.persistence.*;

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

    @Column(name = "id_equipo")
    private int idEquipo;

    private Integer edad;

    private String medalla;

    public Participacion(Deportista deportista, Evento evento, int idEquipo, Integer edad, String medalla) {
        this.deportista = deportista;
        this.evento = evento;
        this.idEquipo = idEquipo;
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

    public int getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(int idEquipo) {
        this.idEquipo = idEquipo;
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
}
