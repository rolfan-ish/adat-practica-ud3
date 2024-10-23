package es.rolfan.dao.csv;

import com.opencsv.bean.CsvBindByName;

public class EntradaAtleta {
    @CsvBindByName(column = "ID", required = true)
    private int id;

    @CsvBindByName(column = "Name")
    private String nombre;

    @CsvBindByName(column = "Age")
    private Integer edad;

    @CsvBindByName(column = "Sex")
    private String sexo;

    @CsvBindByName(column = "Height")
    private Integer altura;

    @CsvBindByName(column = "Weight")
    private Double peso;

    @CsvBindByName(column = "Team")
    private String equipo;

    @CsvBindByName(column = "NOC")
    private String noc;

    @CsvBindByName(column = "Games")
    private String juegos;

    @CsvBindByName(column = "Year")
    private Integer anio;

    @CsvBindByName(column = "Season")
    private String temporada;

    @CsvBindByName(column = "City")
    private String ciudad;

    @CsvBindByName(column = "Sport")
    private String deporte;

    @CsvBindByName(column = "Event")
    private String evento;

    @CsvBindByName(column = "Medal")
    private String medalla;

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Integer getEdad() {
        return edad;
    }

    public Integer getAltura() {
        return altura;
    }

    public Double getPeso() {
        return peso;
    }

    public String getEquipo() {
        return equipo;
    }

    public String getNoc() {
        return noc;
    }

    public String getJuegos() {
        return juegos;
    }

    public Integer getAnio() {
        return anio;
    }

    public String getTemporada() {
        return temporada;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getDeporte() {
        return deporte;
    }

    public String getEvento() {
        return evento;
    }

    public String getMedalla() {
        return medalla;
    }

    public String getSexo() {
        return sexo;
    }
}
