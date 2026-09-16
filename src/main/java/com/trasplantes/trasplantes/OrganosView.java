package com.trasplantes.trasplantes;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Vista del modulo Organos.
 * Formulario CRUD para registrar organos donantes disponibles.
 */
public class OrganosView {

    private final VBox vista;
    private final TableView<Organo> tabla;
    private final ObservableList<Organo> datosOrganos;

    private TextField txtId;
    private ComboBox<String> cmbTipoOrgano;
    private ComboBox<String> cmbTipoSangre;
    private TextField txtDonanteNombre;
    private TextField txtFechaDisponible;
    private TextField txtTiempoIsquemia;
    private TextField txtHlaAlelosDonante;
    private ComboBox<String> cmbEstado;

    public OrganosView() {
        vista = new VBox(15);
        vista.setPadding(new Insets(25));

        Label titulo = new Label("Gestion de Organos Donantes");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1b263b;");

        GridPane formulario = construirFormulario();
        HBox botones = construirBotonesAccion();

        datosOrganos = FXCollections.observableArrayList();
        tabla = construirTabla();

        vista.getChildren().addAll(titulo, formulario, botones, tabla);

        cargarOrganos();
    }

    private GridPane construirFormulario() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");

        txtId = new TextField();
        txtId.setDisable(true);
        txtId.setPromptText("Auto");

        cmbTipoOrgano = new ComboBox<>(FXCollections.observableArrayList("HIGADO", "CORAZON", "RINON"));
        cmbTipoOrgano.setPromptText("Tipo de organo");

        cmbTipoSangre = new ComboBox<>(FXCollections.observableArrayList(
                "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        cmbTipoSangre.setPromptText("Tipo de sangre");

        txtDonanteNombre = new TextField();
        txtDonanteNombre.setPromptText("Nombre del donante");

        txtFechaDisponible = new TextField();
        txtFechaDisponible.setPromptText("YYYY-MM-DD");

        txtTiempoIsquemia = new TextField();
        txtTiempoIsquemia.setPromptText("Tiempo isquemia max (horas)");

        txtHlaAlelosDonante = new TextField();
        txtHlaAlelosDonante.setPromptText("HLA alelos donante (0-6, solo RINON)");

        cmbEstado = new ComboBox<>(FXCollections.observableArrayList("DISPONIBLE", "ASIGNADO", "DESCARTADO"));
        cmbEstado.setValue("DISPONIBLE");

        grid.add(new Label("ID:"), 0, 0);
        grid.add(txtId, 1, 0);
        grid.add(new Label("Tipo organo:"), 2, 0);
        grid.add(cmbTipoOrgano, 3, 0);

        grid.add(new Label("Tipo sangre:"), 0, 1);
        grid.add(cmbTipoSangre, 1, 1);
        grid.add(new Label("Donante:"), 2, 1);
        grid.add(txtDonanteNombre, 3, 1);

        grid.add(new Label("Fecha disponible:"), 0, 2);
        grid.add(txtFechaDisponible, 1, 2);
        grid.add(new Label("Isquemia max (h):"), 2, 2);
        grid.add(txtTiempoIsquemia, 3, 2);

        grid.add(new Label("HLA alelos donante:"), 0, 3);
        grid.add(txtHlaAlelosDonante, 1, 3);
        grid.add(new Label("Estado:"), 2, 3);
        grid.add(cmbEstado, 3, 3);

        return grid;
    }

    private HBox construirBotonesAccion() {
        Button btnNuevo = new Button("Nuevo");
        Button btnGuardar = new Button("Guardar");
        Button btnActualizar = new Button("Actualizar");
        Button btnEliminar = new Button("Eliminar");
        Button btnLimpiar = new Button("Limpiar");

        estilizarBoton(btnNuevo, "#6c757d");
        estilizarBoton(btnGuardar, "#2a9d8f");
        estilizarBoton(btnActualizar, "#3a5a9c");
        estilizarBoton(btnEliminar, "#e63946");
        estilizarBoton(btnLimpiar, "#adb5bd");

        btnNuevo.setOnAction(e -> limpiarFormulario());
        btnGuardar.setOnAction(e -> guardarOrgano());
        btnActualizar.setOnAction(e -> actualizarOrgano());
        btnEliminar.setOnAction(e -> eliminarOrgano());
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        return new HBox(10, btnNuevo, btnGuardar, btnActualizar, btnEliminar, btnLimpiar);
    }

    private void estilizarBoton(Button boton, String colorHex) {
        boton.setStyle(
                "-fx-background-color: " + colorHex + "; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 16 8 16;"
        );
    }

    private TableView<Organo> construirTabla() {
        TableView<Organo> tv = new TableView<>();
        tv.setItems(datosOrganos);

        TableColumn<Organo, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getIdOrgano()));

        TableColumn<Organo, String> colTipo = new TableColumn<>("Tipo Organo");
        colTipo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipoOrgano()));

        TableColumn<Organo, String> colSangre = new TableColumn<>("Sangre");
        colSangre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipoSangre()));

        TableColumn<Organo, String> colDonante = new TableColumn<>("Donante");
        colDonante.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDonanteNombre() != null ? data.getValue().getDonanteNombre() : "-"));

        TableColumn<Organo, String> colFecha = new TableColumn<>("Fecha Disp.");
        colFecha.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFechaDisponible()));

        TableColumn<Organo, Number> colIsquemia = new TableColumn<>("Isquemia Max (h)");
        colIsquemia.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getTiempoIsquemiaMax()));

        TableColumn<Organo, String> colHla = new TableColumn<>("HLA Donante");
        colHla.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getHlaAlelosDonante() != null ? String.valueOf(data.getValue().getHlaAlelosDonante()) : "-"));

        TableColumn<Organo, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEstado()));

        tv.getColumns().addAll(colId, colTipo, colSangre, colDonante, colFecha, colIsquemia, colHla, colEstado);

        tv.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cargarFormularioDesdeOrgano(newVal);
            }
        });

        tv.setPrefHeight(300);
        return tv;
    }

    private void cargarFormularioDesdeOrgano(Organo o) {
        txtId.setText(String.valueOf(o.getIdOrgano()));
        cmbTipoOrgano.setValue(o.getTipoOrgano());
        cmbTipoSangre.setValue(o.getTipoSangre());
        txtDonanteNombre.setText(o.getDonanteNombre());
        txtFechaDisponible.setText(o.getFechaDisponible());
        txtTiempoIsquemia.setText(String.valueOf(o.getTiempoIsquemiaMax()));
        txtHlaAlelosDonante.setText(o.getHlaAlelosDonante() != null ? String.valueOf(o.getHlaAlelosDonante()) : "");
        cmbEstado.setValue(o.getEstado());
    }

    private void limpiarFormulario() {
        txtId.clear();
        cmbTipoOrgano.setValue(null);
        cmbTipoSangre.setValue(null);
        txtDonanteNombre.clear();
        txtFechaDisponible.clear();
        txtTiempoIsquemia.clear();
        txtHlaAlelosDonante.clear();
        cmbEstado.setValue("DISPONIBLE");
        tabla.getSelectionModel().clearSelection();
    }

    private void guardarOrgano() {
        String sql = "INSERT INTO organos (tipo_organo, tipo_sangre, donante_nombre, fecha_disponible, "
                + "tiempo_isquemia_max, hla_alelos_donante, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection con = ConexionBD.getConexion();
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                enlazarParametrosFormulario(stmt);
                stmt.executeUpdate();
            }
            cargarOrganos();
            limpiarFormulario();
            mostrarInfo("Organo guardado correctamente.");
        } catch (SQLException e) {
            mostrarError("Error al guardar organo: " + e.getMessage());
        }
    }

    private void actualizarOrgano() {
        if (txtId.getText() == null || txtId.getText().isBlank()) {
            mostrarError("Seleccione un organo de la tabla para actualizar.");
            return;
        }

        String sql = "UPDATE organos SET tipo_organo = ?, tipo_sangre = ?, donante_nombre = ?, "
                + "fecha_disponible = ?, tiempo_isquemia_max = ?, hla_alelos_donante = ?, estado = ? "
                + "WHERE id_organo = ?";

        try {
            Connection con = ConexionBD.getConexion();
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                enlazarParametrosFormulario(stmt);
                stmt.setInt(8, Integer.parseInt(txtId.getText()));
                stmt.executeUpdate();
            }
            cargarOrganos();
            limpiarFormulario();
            mostrarInfo("Organo actualizado correctamente.");
        } catch (SQLException e) {
            mostrarError("Error al actualizar organo: " + e.getMessage());
        }
    }

    private void enlazarParametrosFormulario(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, cmbTipoOrgano.getValue());
        stmt.setString(2, cmbTipoSangre.getValue());
        stmt.setString(3, txtDonanteNombre.getText());
        stmt.setString(4, txtFechaDisponible.getText());

        int isquemia = 0;
        try {
            isquemia = Integer.parseInt(txtTiempoIsquemia.getText().trim());
        } catch (Exception ignored) {
        }
        stmt.setInt(5, isquemia);

        String hlaTexto = txtHlaAlelosDonante.getText();
        if (hlaTexto == null || hlaTexto.isBlank()) {
            stmt.setNull(6, java.sql.Types.INTEGER);
        } else {
            try {
                stmt.setInt(6, Integer.parseInt(hlaTexto.trim()));
            } catch (NumberFormatException e) {
                stmt.setNull(6, java.sql.Types.INTEGER);
            }
        }

        stmt.setString(7, cmbEstado.getValue() != null ? cmbEstado.getValue() : "DISPONIBLE");
    }

    private void eliminarOrgano() {
        Organo seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Seleccione un organo de la tabla para eliminar.");
            return;
        }

        String sql = "DELETE FROM organos WHERE id_organo = ?";
        try {
            Connection con = ConexionBD.getConexion();
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, seleccionado.getIdOrgano());
                stmt.executeUpdate();
            }
            cargarOrganos();
            limpiarFormulario();
            mostrarInfo("Organo eliminado correctamente.");
        } catch (SQLException e) {
            mostrarError("Error al eliminar organo: " + e.getMessage());
        }
    }

    public void cargarOrganos() {
        datosOrganos.clear();
        String sql = "SELECT id_organo, tipo_organo, tipo_sangre, donante_nombre, fecha_disponible, "
                + "tiempo_isquemia_max, hla_alelos_donante, estado FROM organos";

        try {
            Connection con = ConexionBD.getConexion();
            try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Organo o = new Organo();
                    o.setIdOrgano(rs.getInt("id_organo"));
                    o.setTipoOrgano(rs.getString("tipo_organo"));
                    o.setTipoSangre(rs.getString("tipo_sangre"));
                    o.setDonanteNombre(rs.getString("donante_nombre"));
                    o.setFechaDisponible(rs.getString("fecha_disponible"));
                    o.setTiempoIsquemiaMax(rs.getInt("tiempo_isquemia_max"));

                    int hla = rs.getInt("hla_alelos_donante");
                    o.setHlaAlelosDonante(rs.wasNull() ? null : hla);

                    o.setEstado(rs.getString("estado"));

                    datosOrganos.add(o);
                }
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar organos: " + e.getMessage());
        }
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, mensaje);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensaje);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public VBox getVista() {
        return vista;
    }
}