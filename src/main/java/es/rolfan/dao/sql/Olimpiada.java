package es.rolfan.dao.sql;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Olimpiada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_olimpiada")
    private int idOlimpiada;

    private String nombre;

    private int anio;

    private String temporada;

    public Olimpiada() {
    }

    private String ciudad;

    public Olimpiada(String nombre, int anio, String temporada, String ciudad) {
        this.nombre = nombre;
        this.anio = anio;
        this.temporada = temporada;
        this.ciudad = ciudad;
    }

    public int getIdOlimpiada() {
        return idOlimpiada;
    }

    public void setIdOlimpiada(int idOlimpiada) {
        this.idOlimpiada = idOlimpiada;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public String getTemporada() {
        return temporada;
    }

    public void setTemporada(String temporada) {
        this.temporada = temporada;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Olimpiada olimpiada)) return false;
        return Objects.equals(nombre, olimpiada.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nombre);
    }
}
