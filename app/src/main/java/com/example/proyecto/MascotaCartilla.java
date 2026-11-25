package com.example.proyecto;

public class MascotaCartilla {
    private int idMascota;
    private String nombre;
    private String raza;
    private String edadTexto;
    private String fotoUri;
    private int vacunasAplicadas;
    private int vacunasTotales;

    public MascotaCartilla(int idMascota, String nombre, String raza, String edadTexto,
                           String fotoUri, int vacunasAplicadas, int vacunasTotales) {
        this.idMascota = idMascota;
        this.nombre = nombre;
        this.raza = raza;
        this.edadTexto = edadTexto;
        this.fotoUri = fotoUri;
        this.vacunasAplicadas = vacunasAplicadas;
        this.vacunasTotales = vacunasTotales;
    }

    public int getIdMascota() { return idMascota; }
    public String getNombre() { return nombre; }
    public String getRaza() { return raza; }
    public String getEdadTexto() { return edadTexto; }
    public String getFotoUri() { return fotoUri; }
    public int getVacunasAplicadas() { return vacunasAplicadas; }
    public int getVacunasTotales() { return vacunasTotales; }
}