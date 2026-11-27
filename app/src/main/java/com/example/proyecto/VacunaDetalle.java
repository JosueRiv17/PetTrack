package com.example.proyecto;

public class VacunaDetalle {
    private final String nombre;
    private final Integer periodicidadDias;
    private final String folioLote;
    private final String numeroRegistro;
    private final String descripcion;
    private final String fechaAplicacion;
    private final String proximaAplicacion;

    public VacunaDetalle(String nombre, Integer periodicidadDias, String folioLote,
                         String numeroRegistro, String descripcion,
                         String fechaAplicacion, String proximaAplicacion) {
        this.nombre = nombre;
        this.periodicidadDias = periodicidadDias;
        this.folioLote = folioLote;
        this.numeroRegistro = numeroRegistro;
        this.descripcion = descripcion;
        this.fechaAplicacion = fechaAplicacion;
        this.proximaAplicacion = proximaAplicacion;
    }

    public String getNombre() { return nombre; }
    public Integer getPeriodicidadDias() { return periodicidadDias; }
    public String getFolioLote() { return folioLote; }
    public String getNumeroRegistro() { return numeroRegistro; }
    public String getDescripcion() { return descripcion; }
    public String getFechaAplicacion() { return fechaAplicacion; }
    public String getProximaAplicacion() { return proximaAplicacion; }
}