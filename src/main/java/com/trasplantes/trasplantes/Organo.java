package com.trasplantes.trasplantes;

import java.util.Objects;

/**
 * Modelo de dominio que representa un organo disponible para trasplante.
 */
public class Organo {

    public static final String ESTADO_DISPONIBLE = "DISPONIBLE";
    public static final String ESTADO_ASIGNADO = "ASIGNADO";
    public static final String ESTADO_DESCARTADO = "DESCARTADO";

    private int idOrgano;
    private String tipoOrgano; // HIGADO, CORAZON, RINON
    private String tipoSangre;
    private String donanteNombre;
    private String fechaDisponible;
    private int tiempoIsquemiaMax; // horas maximas viables
    private Integer hlaAlelosDonante; // solo aplica para RINON
    private String estado;

    public Organo() {
    }

    public Organo(int idOrgano, String tipoOrgano, String tipoSangre, String donanteNombre,
                  String fechaDisponible, int tiempoIsquemiaMax, Integer hlaAlelosDonante, String estado) {
        this.idOrgano = idOrgano;
        this.tipoOrgano = tipoOrgano;
        this.tipoSangre = tipoSangre;
        this.donanteNombre = donanteNombre;
        this.fechaDisponible = fechaDisponible;
        this.tiempoIsquemiaMax = tiempoIsquemiaMax;
        this.hlaAlelosDonante = hlaAlelosDonante;
        this.estado = estado;
    }

    // ---------------- Getters y Setters ----------------

    public int getIdOrgano() {
        return idOrgano;
    }

    public void setIdOrgano(int idOrgano) {
        this.idOrgano = idOrgano;
    }

    public String getTipoOrgano() {
        return tipoOrgano;
    }

    public void setTipoOrgano(String tipoOrgano) {
        this.tipoOrgano = tipoOrgano;
    }

    public String getTipoSangre() {
        return tipoSangre;
    }

    public void setTipoSangre(String tipoSangre) {
        this.tipoSangre = tipoSangre;
    }

    public String getDonanteNombre() {
        return donanteNombre;
    }

    public void setDonanteNombre(String donanteNombre) {
        this.donanteNombre = donanteNombre;
    }

    public String getFechaDisponible() {
        return fechaDisponible;
    }

    public void setFechaDisponible(String fechaDisponible) {
        this.fechaDisponible = fechaDisponible;
    }

    public int getTiempoIsquemiaMax() {
        return tiempoIsquemiaMax;
    }

    public void setTiempoIsquemiaMax(int tiempoIsquemiaMax) {
        this.tiempoIsquemiaMax = tiempoIsquemiaMax;
    }

    public Integer getHlaAlelosDonante() {
        return hlaAlelosDonante;
    }

    public void setHlaAlelosDonante(Integer hlaAlelosDonante) {
        this.hlaAlelosDonante = hlaAlelosDonante;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Organo{" +
                "idOrgano=" + idOrgano +
                ", tipoOrgano='" + tipoOrgano + '\'' +
                ", tipoSangre='" + tipoSangre + '\'' +
                ", donanteNombre='" + donanteNombre + '\'' +
                ", fechaDisponible='" + fechaDisponible + '\'' +
                ", tiempoIsquemiaMax=" + tiempoIsquemiaMax +
                ", hlaAlelosDonante=" + hlaAlelosDonante +
                ", estado='" + estado + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Organo)) return false;
        Organo organo = (Organo) o;
        return idOrgano == organo.idOrgano;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idOrgano);
    }
}