package es.rolfan.model.sql;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.Set;

@Entity
public class Deportista {
    @Id
    @Column(name = "id_deportista")
    private int idDeportista;

    private String nombre;

    private String sexo;
    private Integer peso;
    private Integer altura;
    @OneToMany(mappedBy = "deportista")
    private Set<Participacion> participaciones;

    public Deportista(int idDeportista, String nombre, String sexo, Integer peso, Integer altura) {
        this.idDeportista = idDeportista;
        this.nombre = nombre;
        this.sexo = sexo;
        this.peso = peso;
        this.altura = altura;
    }

    public Deportista() {
    }

    public Set<Participacion> getParticipaciones() {
        return participaciones;
    }

    public int getIdDeportista() {
        return idDeportista;
    }

    public void setIdDeportista(int idDeportista) {
        this.idDeportista = idDeportista;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public Integer getPeso() {
        return peso;
    }

    public void setPeso(int peso) {
        this.peso = peso;
    }

    public Integer getAltura() {
        return altura;
    }

    public void setAltura(int altura) {
        this.altura = altura;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Deportista that)) return false;
        return idDeportista == that.idDeportista;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(idDeportista);
    }
}
