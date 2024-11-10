package es.rolfan.model.sql;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private int idEvento;

    private String nombre;

    public Evento() {
    }

    @ManyToOne
    @JoinColumn(name = "id_olimpiada")
    private Olimpiada olimpiada;

    @ManyToOne
    @JoinColumn(name = "id_deporte")
    private Deporte deporte;

    public Evento(String nombre, Olimpiada olimpiada, Deporte deporte) {
        this.nombre = nombre;
        this.olimpiada = olimpiada;
        this.deporte = deporte;
    }

    public int getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(int idEvento) {
        this.idEvento = idEvento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Olimpiada getOlimpiada() {
        return olimpiada;
    }

    public void setOlimpiada(Olimpiada olimpiada) {
        this.olimpiada = olimpiada;
    }

    public Deporte getDeporte() {
        return deporte;
    }

    public void setDeporte(Deporte deporte) {
        this.deporte = deporte;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Evento evento)) return false;
        return Objects.equals(nombre, evento.nombre) && Objects.equals(olimpiada, evento.olimpiada) && Objects.equals(deporte, evento.deporte);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, olimpiada, deporte);
    }
}
