package com.trasplantes.trasplantes;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Vista del modulo Dashboard.
 * Muestra tarjetas con metricas rapidas del sistema:
 * total de pacientes, organos disponibles y trasplantes concretados.
 */
public class DashboardView {

    private final VBox vista;
    private Label lblTotalPacientes;
    private Label lblOrganosDisponibles;
    private Label lblTrasplantesConcretados;

    public DashboardView() {
        vista = new VBox(20);
        vista.setPadding(new Insets(30));

        Label titulo = new Label("Dashboard");
        titulo.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1b263b;");

        Label subtitulo = new Label("Resumen general del sistema de trasplantes");
        subtitulo.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        HBox contenedorTarjetas = new HBox(20);
        contenedorTarjetas.setAlignment(Pos.CENTER_LEFT);

        lblTotalPacientes = new Label("0");
        lblOrganosDisponibles = new Label("0");
        lblTrasplantesConcretados = new Label("0");

        VBox tarjetaPacientes = crearTarjeta("Total de Pacientes", lblTotalPacientes, "#3a5a9c");
        VBox tarjetaOrganos = crearTarjeta("Organos Disponibles", lblOrganosDisponibles, "#2a9d8f");
        VBox tarjetaTrasplantes = crearTarjeta("Trasplantes Concretados", lblTrasplantesConcretados, "#e76f51");

        HBox.setHgrow(tarjetaPacientes, Priority.ALWAYS);
        HBox.setHgrow(tarjetaOrganos, Priority.ALWAYS);
        HBox.setHgrow(tarjetaTrasplantes, Priority.ALWAYS);

        contenedorTarjetas.getChildren().addAll(tarjetaPacientes, tarjetaOrganos, tarjetaTrasplantes);

        vista.getChildren().addAll(titulo, subtitulo, contenedorTarjetas);

        cargarMetricas();
    }

    private VBox crearTarjeta(String etiqueta, Label valorLabel, String colorHex) {
        VBox tarjeta = new VBox(10);
        tarjeta.setPadding(new Insets(20));
        tarjeta.setPrefHeight(120);
        tarjeta.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-border-radius: 10; -fx-border-color: #e0e0e0; -fx-border-width: 1; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
        );

        Label lblTitulo = new Label(etiqueta);
        lblTitulo.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        valorLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + colorHex + ";");

        tarjeta.getChildren().addAll(lblTitulo, valorLabel);
        return tarjeta;
    }

    /**
     * Consulta la base de datos para obtener las metricas rapidas
     * y actualiza los labels de las tarjetas.
     */
    public void cargarMetricas() {
        try {
            Connection con = ConexionBD.getConexion();

            try (Statement stmt = con.createStatement()) {
                ResultSet rsPacientes = stmt.executeQuery(
                        "SELECT COUNT(*) AS total FROM pacientes WHERE activo = 1");
                if (rsPacientes.next()) {
                    lblTotalPacientes.setText(String.valueOf(rsPacientes.getInt("total")));
                }
            }

            try (Statement stmt = con.createStatement()) {
                ResultSet rsOrganos = stmt.executeQuery(
                        "SELECT COUNT(*) AS total FROM organos WHERE estado = 'DISPONIBLE'");
                if (rsOrganos.next()) {
                    lblOrganosDisponibles.setText(String.valueOf(rsOrganos.getInt("total")));
                }
            }

            try (Statement stmt = con.createStatement()) {
                ResultSet rsHistorial = stmt.executeQuery(
                        "SELECT COUNT(*) AS total FROM historial");
                if (rsHistorial.next()) {
                    lblTrasplantesConcretados.setText(String.valueOf(rsHistorial.getInt("total")));
                }
            }

        } catch (SQLException e) {
            lblTotalPacientes.setText("-");
            lblOrganosDisponibles.setText("-");
            lblTrasplantesConcretados.setText("-");
            System.err.println("Error al cargar metricas del dashboard: " + e.getMessage());
        }
    }

    public VBox getVista() {
        return vista;
    }
}