package es.rolfan.dao.sql;

import jakarta.persistence.*;

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
}
