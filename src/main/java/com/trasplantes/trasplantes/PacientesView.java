package com.trasplantes.trasplantes;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
 * Vista del modulo Pacientes.
 * Formulario CRUD completo + tabla para registrar y editar
 * las metricas clinicas de los pacientes (MELD, urgencia cardiaca, HLA).
 */
public class PacientesView {

    private final VBox vista;
    private final TableView<Paciente> tabla;
    private final ObservableList<Paciente> datosPacientes;

    private TextField txtId;
    private TextField txtNombre;
    private ComboBox<String> cmbTipoSangre;
    private ComboBox<String> cmbOrganoRequerido;
    private TextField txtFechaInscripcion;
    private CheckBox chkActivo;
    private TextField txtBilirrubina;
    private TextField txtCreatinina;
    private TextField txtInr;
    private ComboBox<String> cmbUrgenciaCorazon;
    private TextField txtHlaAlelos;
    private TextField txtDiasEspera;

    public PacientesView() {
        vista = new VBox(15);
        vista.setPadding(new Insets(25));

        Label titulo = new Label("Gestion de Pacientes");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1b263b;");

        GridPane formulario = construirFormulario();
        HBox botones = construirBotonesAccion();

        datosPacientes = FXCollections.observableArrayList();
        tabla = construirTabla();

        vista.getChildren().addAll(titulo, formulario, botones, tabla);

        cargarPacientes();
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

        txtNombre = new TextField();
        txtNombre.setPromptText("Nombre completo");

        cmbTipoSangre = new ComboBox<>(FXCollections.observableArrayList(
                "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
        cmbTipoSangre.setPromptText("Tipo de sangre");

        cmbOrganoRequerido = new ComboBox<>(FXCollections.observableArrayList(
                "HIGADO", "CORAZON", "RINON"));
        cmbOrganoRequerido.setPromptText("Organo requerido");

        txtFechaInscripcion = new TextField();
        txtFechaInscripcion.setPromptText("YYYY-MM-DD");

        chkActivo = new CheckBox("Activo");
        chkActivo.setSelected(true);

        txtBilirrubina = new TextField();
        txtBilirrubina.setPromptText("Bilirrubina (MELD)");

        txtCreatinina = new TextField();
        txtCreatinina.setPromptText("Creatinina (MELD)");

        txtInr = new TextField();
        txtInr.setPromptText("INR (MELD)");

        cmbUrgenciaCorazon = new ComboBox<>(FXCollections.observableArrayList("1A", "1B", "2"));
        cmbUrgenciaCorazon.setPromptText("Urgencia corazon");

        txtHlaAlelos = new TextField();
        txtHlaAlelos.setPromptText("HLA alelos (0-6)");

        txtDiasEspera = new TextField();
        txtDiasEspera.setPromptText("Dias en espera");

        grid.add(new Label("ID:"), 0, 0);
        grid.add(txtId, 1, 0);
        grid.add(new Label("Nombre:"), 2, 0);
        grid.add(txtNombre, 3, 0);

        grid.add(new Label("Tipo Sangre:"), 0, 1);
        grid.add(cmbTipoSangre, 1, 1);
        grid.add(new Label("Organo requerido:"), 2, 1);
        grid.add(cmbOrganoRequerido, 3, 1);

        grid.add(new Label("Fecha inscripcion:"), 0, 2);
        grid.add(txtFechaInscripcion, 1, 2);
        grid.add(chkActivo, 2, 2);

        grid.add(new Label("Bilirrubina:"), 0, 3);
        grid.add(txtBilirrubina, 1, 3);
        grid.add(new Label("Creatinina:"), 2, 3);
        grid.add(txtCreatinina, 3, 3);

        grid.add(new Label("INR:"), 0, 4);
        grid.add(txtInr, 1, 4);
        grid.add(new Label("Urgencia corazon:"), 2, 4);
        grid.add(cmbUrgenciaCorazon, 3, 4);

        grid.add(new Label("HLA alelos:"), 0, 5);
        grid.add(txtHlaAlelos, 1, 5);
        grid.add(new Label("Dias espera:"), 2, 5);
        grid.add(txtDiasEspera, 3, 5);

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
        btnGuardar.setOnAction(e -> guardarPaciente());
        btnActualizar.setOnAction(e -> actualizarPaciente());
        btnEliminar.setOnAction(e -> eliminarPaciente());
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        HBox contenedor = new HBox(10, btnNuevo, btnGuardar, btnActualizar, btnEliminar, btnLimpiar);
        return contenedor;
    }

    private void estilizarBoton(Button boton, String colorHex) {
        boton.setStyle(
                "-fx-background-color: " + colorHex + "; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 8 16 8 16;"
        );
    }

    @SuppressWarnings("unchecked")
    private TableView<Paciente> construirTabla() {
        TableView<Paciente> tv = new TableView<>();
        tv.setItems(datosPacientes);

        TableColumn<Paciente, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getIdPaciente()));

        TableColumn<Paciente, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));

        TableColumn<Paciente, String> colSangre = new TableColumn<>("Sangre");
        colSangre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipoSangre()));

        TableColumn<Paciente, String> colOrgano = new TableColumn<>("Organo Req.");
        colOrgano.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrganoRequerido()));

        TableColumn<Paciente, String> colUrgencia = new TableColumn<>("Urgencia");
        colUrgencia.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getUrgenciaCorazon() != null ? data.getValue().getUrgenciaCorazon() : "-"));

        TableColumn<Paciente, String> colHla = new TableColumn<>("HLA");
        colHla.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getHlaAlelos() != null ? String.valueOf(data.getValue().getHlaAlelos()) : "-"));

        TableColumn<Paciente, Number> colEspera = new TableColumn<>("Dias Espera");
        colEspera.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getDiasEspera()));

        tv.getColumns().addAll(colId, colNombre, colSangre, colOrgano, colUrgencia, colHla, colEspera);

        tv.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cargarFormularioDesdePaciente(newVal);
            }
        });

        tv.setPrefHeight(300);
        return tv;
    }

    private void cargarFormularioDesdePaciente(Paciente p) {
        txtId.setText(String.valueOf(p.getIdPaciente()));
        txtNombre.setText(p.getNombre());
        cmbTipoSangre.setValue(p.getTipoSangre());
        cmbOrganoRequerido.setValue(p.getOrganoRequerido());
        txtFechaInscripcion.setText(p.getFechaInscripcion());
        chkActivo.setSelected(p.isActivo());
        txtBilirrubina.setText(p.getBilirrubina() != null ? String.valueOf(p.getBilirrubina()) : "");
        txtCreatinina.setText(p.getCreatinina() != null ? String.valueOf(p.getCreatinina()) : "");
        txtInr.setText(p.getInr() != null ? String.valueOf(p.getInr()) : "");
        cmbUrgenciaCorazon.setValue(p.getUrgenciaCorazon());
        txtHlaAlelos.setText(p.getHlaAlelos() != null ? String.valueOf(p.getHlaAlelos()) : "");
        txtDiasEspera.setText(String.valueOf(p.getDiasEspera()));
    }

    private void limpiarFormulario() {
        txtId.clear();
        txtNombre.clear();
        cmbTipoSangre.setValue(null);
        cmbOrganoRequerido.setValue(null);
        txtFechaInscripcion.clear();
        chkActivo.setSelected(true);
        txtBilirrubina.clear();
        txtCreatinina.clear();
        txtInr.clear();
        cmbUrgenciaCorazon.setValue(null);
        txtHlaAlelos.clear();
        txtDiasEspera.clear();
        tabla.getSelectionModel().clearSelection();
    }

    private void guardarPaciente() {
        String sql = "INSERT INTO pacientes (nombre, tipo_sangre, organo_requerido, fecha_inscripcion, activo, "
                + "bilirrubina, creatinina, inr, urgencia_corazon, hla_alelos, dias_espera) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection con = ConexionBD.getConexion();
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                enlazarParametrosFormulario(stmt);
                stmt.executeUpdate();
            }
            cargarPacientes();
            limpiarFormulario();
            mostrarInfo("Paciente guardado correctamente.");
        } catch (SQLException e) {
            mostrarError("Error al guardar paciente: " + e.getMessage());
        }
    }

    private void actualizarPaciente() {
        if (txtId.getText() == null || txtId.getText().isBlank()) {
            mostrarError("Seleccione un paciente de la tabla para actualizar.");
            return;
        }

        String sql = "UPDATE pacientes SET nombre = ?, tipo_sangre = ?, organo_requerido = ?, "
                + "fecha_inscripcion = ?, activo = ?, bilirrubina = ?, creatinina = ?, inr = ?, "
                + "urgencia_corazon = ?, hla_alelos = ?, dias_espera = ? WHERE id_paciente = ?";

        try {
            Connection con = ConexionBD.getConexion();
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                enlazarParametrosFormulario(stmt);
                stmt.setInt(12, Integer.parseInt(txtId.getText()));
                stmt.executeUpdate();
            }
            cargarPacientes();
            limpiarFormulario();
            mostrarInfo("Paciente actualizado correctamente.");
        } catch (SQLException e) {
            mostrarError("Error al actualizar paciente: " + e.getMessage());
        }
    }

    private void enlazarParametrosFormulario(PreparedStatement stmt) throws SQLException {
        stmt.setString(1, txtNombre.getText());
        stmt.setString(2, cmbTipoSangre.getValue());
        stmt.setString(3, cmbOrganoRequerido.getValue());
        stmt.setString(4, txtFechaInscripcion.getText());
        stmt.setInt(5, chkActivo.isSelected() ? 1 : 0);

        setNullableDouble(stmt, 6, txtBilirrubina.getText());
        setNullableDouble(stmt, 7, txtCreatinina.getText());
        setNullableDouble(stmt, 8, txtInr.getText());

        stmt.setString(9, cmbUrgenciaCorazon.getValue());

        setNullableInt(stmt, 10, txtHlaAlelos.getText());

        int diasEspera = 0;
        try {
            diasEspera = Integer.parseInt(txtDiasEspera.getText().trim());
        } catch (Exception ignored) {
        }
        stmt.setInt(11, diasEspera);
    }

    private void setNullableDouble(PreparedStatement stmt, int index, String valor) throws SQLException {
        if (valor == null || valor.isBlank()) {
            stmt.setNull(index, java.sql.Types.REAL);
        } else {
            try {
                stmt.setDouble(index, Double.parseDouble(valor.trim()));
            } catch (NumberFormatException e) {
                stmt.setNull(index, java.sql.Types.REAL);
            }
        }
    }

    private void setNullableInt(PreparedStatement stmt, int index, String valor) throws SQLException {
        if (valor == null || valor.isBlank()) {
            stmt.setNull(index, java.sql.Types.INTEGER);
        } else {
            try {
                stmt.setInt(index, Integer.parseInt(valor.trim()));
            } catch (NumberFormatException e) {
                stmt.setNull(index, java.sql.Types.INTEGER);
            }
        }
    }

    private void eliminarPaciente() {
        Paciente seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Seleccione un paciente de la tabla para eliminar.");
            return;
        }

        String sql = "DELETE FROM pacientes WHERE id_paciente = ?";
        try {
            Connection con = ConexionBD.getConexion();
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, seleccionado.getIdPaciente());
                stmt.executeUpdate();
            }
            cargarPacientes();
            limpiarFormulario();
            mostrarInfo("Paciente eliminado correctamente.");
        } catch (SQLException e) {
            mostrarError("Error al eliminar paciente: " + e.getMessage());
        }
    }

    public void cargarPacientes() {
        datosPacientes.clear();
        String sql = "SELECT id_paciente, nombre, tipo_sangre, organo_requerido, fecha_inscripcion, activo, "
                + "bilirrubina, creatinina, inr, urgencia_corazon, hla_alelos, dias_espera FROM pacientes";

        try {
            Connection con = ConexionBD.getConexion();
            try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Paciente p = new Paciente();
                    p.setIdPaciente(rs.getInt("id_paciente"));
                    p.setNombre(rs.getString("nombre"));
                    p.setTipoSangre(rs.getString("tipo_sangre"));
                    p.setOrganoRequerido(rs.getString("organo_requerido"));
                    p.setFechaInscripcion(rs.getString("fecha_inscripcion"));
                    p.setActivo(rs.getInt("activo") == 1);

                    double bilirrubina = rs.getDouble("bilirrubina");
                    p.setBilirrubina(rs.wasNull() ? null : bilirrubina);

                    double creatinina = rs.getDouble("creatinina");
                    p.setCreatinina(rs.wasNull() ? null : creatinina);

                    double inr = rs.getDouble("inr");
                    p.setInr(rs.wasNull() ? null : inr);

                    p.setUrgenciaCorazon(rs.getString("urgencia_corazon"));

                    int hla = rs.getInt("hla_alelos");
                    p.setHlaAlelos(rs.wasNull() ? null : hla);

                    p.setDiasEspera(rs.getInt("dias_espera"));

                    datosPacientes.add(p);
                }
            }
        } catch (SQLException e) {
            mostrarError("Error al cargar pacientes: " + e.getMessage());
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