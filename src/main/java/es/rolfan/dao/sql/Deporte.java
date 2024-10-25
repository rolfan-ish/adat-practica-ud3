package es.rolfan.dao.sql;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Deporte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_deporte")
    private int idDeporte;

    private String nombre;

    public Deporte() {}

    public Deporte(String nombre) {
        this.nombre = nombre;
    }

    public int getIdDeporte() {
        return idDeporte;
    }

    public void setIdDeporte(int idDeporte) {
        this.idDeporte = idDeporte;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Deporte deporte)) return false;
        return Objects.equals(nombre, deporte.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nombre);
    }
}
