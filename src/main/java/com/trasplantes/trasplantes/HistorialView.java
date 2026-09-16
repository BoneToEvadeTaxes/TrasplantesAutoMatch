package com.trasplantes.trasplantes;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Vista del modulo Historial.
 * Tabla con el registro de trasplantes confirmados y guardados
 * en la base de datos (tabla historial), con datos enlazados
 * de paciente y organo.
 */
public class HistorialView {

    private final VBox vista;
    private final TableView<RegistroHistorial> tabla;
    private final ObservableList<RegistroHistorial> datosHistorial;

    public HistorialView() {
        vista = new VBox(15);
        vista.setPadding(new Insets(25));

        Label titulo = new Label("Historial de Trasplantes");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1b263b;");

        HBox panelBotones = new HBox(10);
        Button btnRefrescar = new Button("Refrescar historial");
        btnRefrescar.setStyle(
                "-fx-background-color: #3a5a9c; -fx-text-fill: white; -fx-background-radius: 6; " +
                        "-fx-cursor: hand; -fx-padding: 8 16 8 16;"
        );
        btnRefrescar.setOnAction(e -> cargarHistorial());
        panelBotones.getChildren().add(btnRefrescar);

        datosHistorial = FXCollections.observableArrayList();
        tabla = construirTabla();

        vista.getChildren().addAll(titulo, panelBotones, tabla);

        cargarHistorial();
    }

    private TableView<RegistroHistorial> construirTabla() {
        TableView<RegistroHistorial> tv = new TableView<>();
        tv.setItems(datosHistorial);
        tv.setPrefHeight(500);

        TableColumn<RegistroHistorial, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getIdHistorial()));

        TableColumn<RegistroHistorial, String> colPaciente = new TableColumn<>("Paciente");
        colPaciente.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombrePaciente()));
        colPaciente.setPrefWidth(180);

        TableColumn<RegistroHistorial, String> colOrgano = new TableColumn<>("Organo");
        colOrgano.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipoOrgano()));

        TableColumn<RegistroHistorial, String> colDonante = new TableColumn<>("Donante");
        colDonante.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreDonante()));

        TableColumn<RegistroHistorial, String> colFecha = new TableColumn<>("Fecha Match");
        colFecha.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFechaMatch()));
        colFecha.setPrefWidth(150);

        TableColumn<RegistroHistorial, String> colPuntuacion = new TableColumn<>("Puntuacion Total");
        colPuntuacion.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.2f", data.getValue().getPuntuacionTotal())));

        TableColumn<RegistroHistorial, String> colDetalle = new TableColumn<>("Detalle de Puntuacion");
        colDetalle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDetallePuntuacion()));
        colDetalle.setPrefWidth(350);

        tv.getColumns().addAll(colId, colPaciente, colOrgano, colDonante, colFecha, colPuntuacion, colDetalle);

        return tv;
    }

    public void cargarHistorial() {
        datosHistorial.clear();

        String sql = "SELECT h.id_historial, h.fecha_match, h.puntuacion_total, h.detalle_puntuacion, "
                + "p.nombre AS nombre_paciente, o.tipo_organo, o.donante_nombre "
                + "FROM historial h "
                + "JOIN pacientes p ON h.id_paciente = p.id_paciente "
                + "JOIN organos o ON h.id_organo = o.id_organo "
                + "ORDER BY h.id_historial DESC";

        try {
            Connection con = ConexionBD.getConexion();
            try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    RegistroHistorial registro = new RegistroHistorial(
                            rs.getInt("id_historial"),
                            rs.getString("nombre_paciente"),
                            rs.getString("tipo_organo"),
                            rs.getString("donante_nombre"),
                            rs.getString("fecha_match"),
                            rs.getDouble("puntuacion_total"),
                            rs.getString("detalle_puntuacion")
                    );
                    datosHistorial.add(registro);
                }
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar el historial: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensaje);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public VBox getVista() {
        return vista;
    }

    /**
     * Clase auxiliar interna que representa una fila enriquecida
     * del historial, combinando datos de historial, pacientes y organos
     * para su presentacion directa en la TableView.
     */
    public static class RegistroHistorial {
        private final int idHistorial;
        private final String nombrePaciente;
        private final String tipoOrgano;
        private final String nombreDonante;
        private final String fechaMatch;
        private final double puntuacionTotal;
        private final String detallePuntuacion;

        public RegistroHistorial(int idHistorial, String nombrePaciente, String tipoOrgano,
                                 String nombreDonante, String fechaMatch, double puntuacionTotal,
                                 String detallePuntuacion) {
            this.idHistorial = idHistorial;
            this.nombrePaciente = nombrePaciente;
            this.tipoOrgano = tipoOrgano;
            this.nombreDonante = nombreDonante;
            this.fechaMatch = fechaMatch;
            this.puntuacionTotal = puntuacionTotal;
            this.detallePuntuacion = detallePuntuacion;
        }

        public int getIdHistorial() {
            return idHistorial;
        }

        public String getNombrePaciente() {
            return nombrePaciente;
        }

        public String getTipoOrgano() {
            return tipoOrgano;
        }

        public String getNombreDonante() {
            return nombreDonante;
        }

        public String getFechaMatch() {
            return fechaMatch;
        }

        public double getPuntuacionTotal() {
            return puntuacionTotal;
        }

        public String getDetallePuntuacion() {
            return detallePuntuacion;
        }
    }
}