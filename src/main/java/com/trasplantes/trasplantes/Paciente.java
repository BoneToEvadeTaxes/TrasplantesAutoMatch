package com.trasplantes.trasplantes;

import java.util.Objects;

/**
 * Modelo de dominio que representa un paciente en lista de espera
 * de trasplante de organos.
 *
 * Incluye los campos necesarios para calcular distintos scores
 * de prioridad segun el organo requerido:
 * - HIGADO: score MELD (bilirrubina, creatinina, INR)
 * - CORAZON: urgencia UNOS (1A, 1B, 2)
 * - RINON: histocompatibilidad HLA (0 a 6 alelos compatibles)
 */
public class Paciente {

    public static final String ORGANO_HIGADO = "HIGADO";
    public static final String ORGANO_CORAZON = "CORAZON";
    public static final String ORGANO_RINON = "RINON";

    private int idPaciente;
    private String nombre;
    private String tipoSangre;
    private String organoRequerido; // HIGADO, CORAZON, RINON
    private String fechaInscripcion;
    private boolean activo;

    // Campos MELD (higado)
    private Double bilirrubina;
    private Double creatinina;
    private Double inr;

    // Urgencia UNOS (corazon): "1A", "1B", "2"
    private String urgenciaCorazon;

    // Histocompatibilidad HLA (rinon): 0 a 6 alelos compatibles
    private Integer hlaAlelos;

    // Dias en lista de espera, usado como criterio de desempate
    private int diasEspera;

    public Paciente() {
    }

    public Paciente(int idPaciente, String nombre, String tipoSangre, String organoRequerido,
                    String fechaInscripcion, boolean activo, Double bilirrubina, Double creatinina,
                    Double inr, String urgenciaCorazon, Integer hlaAlelos, int diasEspera) {
        this.idPaciente = idPaciente;
        this.nombre = nombre;
        this.tipoSangre = tipoSangre;
        this.organoRequerido = organoRequerido;
        this.fechaInscripcion = fechaInscripcion;
        this.activo = activo;
        this.bilirrubina = bilirrubina;
        this.creatinina = creatinina;
        this.inr = inr;
        this.urgenciaCorazon = urgenciaCorazon;
        this.hlaAlelos = hlaAlelos;
        this.diasEspera = diasEspera;
    }

    // ---------------- Getters y Setters ----------------

    public int getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(int idPaciente) {
        this.idPaciente = idPaciente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoSangre() {
        return tipoSangre;
    }

    public void setTipoSangre(String tipoSangre) {
        this.tipoSangre = tipoSangre;
    }

    public String getOrganoRequerido() {
        return organoRequerido;
    }

    public void setOrganoRequerido(String organoRequerido) {
        this.organoRequerido = organoRequerido;
    }

    public String getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(String fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Double getBilirrubina() {
        return bilirrubina;
    }

    public void setBilirrubina(Double bilirrubina) {
        this.bilirrubina = bilirrubina;
    }

    public Double getCreatinina() {
        return creatinina;
    }

    public void setCreatinina(Double creatinina) {
        this.creatinina = creatinina;
    }

    public Double getInr() {
        return inr;
    }

    public void setInr(Double inr) {
        this.inr = inr;
    }

    public String getUrgenciaCorazon() {
        return urgenciaCorazon;
    }

    public void setUrgenciaCorazon(String urgenciaCorazon) {
        this.urgenciaCorazon = urgenciaCorazon;
    }

    public Integer getHlaAlelos() {
        return hlaAlelos;
    }

    public void setHlaAlelos(Integer hlaAlelos) {
        this.hlaAlelos = hlaAlelos;
    }

    public int getDiasEspera() {
        return diasEspera;
    }

    public void setDiasEspera(int diasEspera) {
        this.diasEspera = diasEspera;
    }

    @Override
    public String toString() {
        return "Paciente{" +
                "idPaciente=" + idPaciente +
                ", nombre='" + nombre + '\'' +
                ", tipoSangre='" + tipoSangre + '\'' +
                ", organoRequerido='" + organoRequerido + '\'' +
                ", fechaInscripcion='" + fechaInscripcion + '\'' +
                ", activo=" + activo +
                ", bilirrubina=" + bilirrubina +
                ", creatinina=" + creatinina +
                ", inr=" + inr +
                ", urgenciaCorazon='" + urgenciaCorazon + '\'' +
                ", hlaAlelos=" + hlaAlelos +
                ", diasEspera=" + diasEspera +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Paciente)) return false;
        Paciente paciente = (Paciente) o;
        return idPaciente == paciente.idPaciente;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPaciente);
    }
}