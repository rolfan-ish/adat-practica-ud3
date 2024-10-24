package es.rolfan.dao.sql;

import jakarta.persistence.*;

@Entity
public class Equipo {
    public Equipo() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipo")
    private int idEquipo;

    private String nombre;

    private String iniciales;

    public Equipo(String nombre, String iniciales) {
        this.nombre = nombre;
        this.iniciales = iniciales;
    }

    public int getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(int idEquipo) {
        this.idEquipo = idEquipo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIniciales() {
        return iniciales;
    }

    public void setIniciales(String iniciales) {
        this.iniciales = iniciales;
    }
}
