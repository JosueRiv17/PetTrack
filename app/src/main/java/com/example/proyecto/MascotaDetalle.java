package com.example.proyecto;

import java.util.List;

public class MascotaDetalle {
    private final int idMascota;
    private final String foto;
    private final String nombre;
    private final String raza;
    private final int anoNacimiento;
    private final double peso;
    private final int esterilizado;
    private final int sexo;
    private final List<VacunaDetalle> vacunas;

    public MascotaDetalle(int idMascota, String foto, String nombre, String raza,int anoNacimiento, double peso, int esterilizado, int sexo, List<VacunaDetalle> vacunas) {
        this.idMascota = idMascota;
        this.foto = foto;
        this.nombre = nombre;
        this.raza = raza;
        this.anoNacimiento = anoNacimiento;
        this.peso = peso;
        this.esterilizado = esterilizado;
        this.sexo = sexo;
        this.vacunas = vacunas;
    }

    public int getIdMascota() { return idMascota; }
    public String getFoto() { return foto; }
    public String getNombre() { return nombre; }
    public String getRaza() { return raza; }
    public int getAnoNacimiento() { return anoNacimiento; }
    public double getPeso() { return peso; }
    public int getEsterilizado() { return esterilizado; }
    public int getSexo() { return sexo; }
    public List<VacunaDetalle> getVacunas() { return vacunas; }
}