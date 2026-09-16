package com.trasplantes.trasplantes;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Modelo que representa el resultado de un proceso de matching (asignacion)
 * entre un paciente y un organo disponible.
 *
 * Almacena el paciente ganador, el organo asignado, la fecha del match
 * y el desglose detallado de la puntuacion utilizada para la decision.
 */
public class MatchResult {

    private Paciente pacienteGanador;
    private Organo organoAsignado;
    private LocalDateTime fechaMatch;
    private double puntuacionTotal;

    // Desglose de la puntuacion: nombre del criterio -> valor calculado
    // Ej: "scoreMELD" -> 18.4, "compatibilidadSangre" -> 100.0, "diasEspera" -> 300.0
    private Map<String, Double> desglosePuntuacion;

    public MatchResult() {
        this.desglosePuntuacion = new LinkedHashMap<>();
        this.fechaMatch = LocalDateTime.now();
    }

    public MatchResult(Paciente pacienteGanador, Organo organoAsignado, LocalDateTime fechaMatch,
                       double puntuacionTotal, Map<String, Double> desglosePuntuacion) {
        this.pacienteGanador = pacienteGanador;
        this.organoAsignado = organoAsignado;
        this.fechaMatch = fechaMatch;
        this.puntuacionTotal = puntuacionTotal;
        this.desglosePuntuacion = (desglosePuntuacion != null)
                ? desglosePuntuacion
                : new LinkedHashMap<>();
    }

    // ---------------- Getters y Setters ----------------

    public Paciente getPacienteGanador() {
        return pacienteGanador;
    }

    public void setPacienteGanador(Paciente pacienteGanador) {
        this.pacienteGanador = pacienteGanador;
    }

    public Organo getOrganoAsignado() {
        return organoAsignado;
    }

    public void setOrganoAsignado(Organo organoAsignado) {
        this.organoAsignado = organoAsignado;
    }

    public LocalDateTime getFechaMatch() {
        return fechaMatch;
    }

    public void setFechaMatch(LocalDateTime fechaMatch) {
        this.fechaMatch = fechaMatch;
    }

    public double getPuntuacionTotal() {
        return puntuacionTotal;
    }

    public void setPuntuacionTotal(double puntuacionTotal) {
        this.puntuacionTotal = puntuacionTotal;
    }

    public Map<String, Double> getDesglosePuntuacion() {
        return desglosePuntuacion;
    }

    public void setDesglosePuntuacion(Map<String, Double> desglosePuntuacion) {
        this.desglosePuntuacion = desglosePuntuacion;
    }

    /**
     * Agrega un criterio individual al desglose de puntuacion.
     *
     * @param criterio nombre del criterio evaluado
     * @param valor    valor numerico calculado para ese criterio
     */
    public void agregarCriterio(String criterio, double valor) {
        if (this.desglosePuntuacion == null) {
            this.desglosePuntuacion = new LinkedHashMap<>();
        }
        this.desglosePuntuacion.put(criterio, valor);
    }

    /**
     * Convierte el desglose de puntuacion a una representacion simple tipo JSON,
     * util para persistir en la columna detalle_puntuacion de la tabla historial.
     *
     * @return cadena en formato JSON con el desglose
     */
    public String desgloseComoJson() {
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        int total = desglosePuntuacion.size();
        for (Map.Entry<String, Double> entry : desglosePuntuacion.entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
            if (++i < total) {
                sb.append(",");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "MatchResult{" +
                "pacienteGanador=" + (pacienteGanador != null ? pacienteGanador.getNombre() : "null") +
                ", organoAsignado=" + (organoAsignado != null ? organoAsignado.getTipoOrgano() : "null") +
                ", fechaMatch=" + fechaMatch +
                ", puntuacionTotal=" + puntuacionTotal +
                ", desglosePuntuacion=" + desglosePuntuacion +
                '}';
    }
}