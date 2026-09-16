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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Vista del modulo AutoMatch Engine.
 * Panel central con selector de organo, boton "Ejecutar AutoMatch",
 * tabla con el receptor ganador y desglose visual de su puntuacion.
 */
public class AutoMatchView {

    private final VBox vista;
    private final AutoMatchService autoMatchService;

    private ComboBox<Organo> cmbOrganoDisponible;
    private TableView<Map.Entry<String, Double>> tablaDesglose;
    private ObservableList<Map.Entry<String, Double>> datosDesglose;

    private Label lblPacienteGanador;
    private Label lblOrganoAsignado;
    private Label lblPuntuacionTotal;
    private Label lblFechaMatch;

    private MatchResult ultimoResultado;

    public AutoMatchView() {
        this.autoMatchService = new AutoMatchService();

        vista = new VBox(20);
        vista.setPadding(new Insets(25));

        Label titulo = new Label("AutoMatch Engine");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1b263b;");

        HBox panelControl = construirPanelControl();
        VBox panelResultado = construirPanelResultado();

        datosDesglose = FXCollections.observableArrayList();
        tablaDesglose = construirTablaDesglose();

        vista.getChildren().addAll(titulo, panelControl, panelResultado, tablaDesglose);

        cargarOrganosDisponibles();
    }

    private HBox construirPanelControl() {
        HBox panel = new HBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");

        Label lblSelector = new Label("Organo disponible:");
        lblSelector.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        cmbOrganoDisponible = new ComboBox<>();
        cmbOrganoDisponible.setPrefWidth(400);
        cmbOrganoDisponible.setPromptText("Seleccione un organo");

        Button btnEjecutar = new Button("Ejecutar AutoMatch");
        btnEjecutar.setStyle(
                "-fx-background-color: #e76f51; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 20 10 20;"
        );
        btnEjecutar.setOnAction(e -> ejecutarAutoMatch());

        Button btnRefrescar = new Button("Refrescar organos");
        btnRefrescar.setStyle(
                "-fx-background-color: #6c757d; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 16 10 16;"
        );
        btnRefrescar.setOnAction(e -> cargarOrganosDisponibles());

        panel.getChildren().addAll(lblSelector, cmbOrganoDisponible, btnEjecutar, btnRefrescar);
        return panel;
    }

    private VBox construirPanelResultado() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(20));
        panel.setStyle(
                "-fx-background-color: #1b263b; -fx-background-radius: 8;"
        );

        Label tituloResultado = new Label("Receptor Ganador");
        tituloResultado.setStyle("-fx-font-size: 14px; -fx-text-fill: #8d99ae; -fx-font-weight: bold;");

        lblPacienteGanador = new Label("Sin resultado aun");
        lblPacienteGanador.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        lblOrganoAsignado = new Label("-");
        lblOrganoAsignado.setStyle("-fx-font-size: 13px; -fx-text-fill: #8d99ae;");

        lblPuntuacionTotal = new Label("Puntuacion total: -");
        lblPuntuacionTotal.setStyle("-fx-font-size: 16px; -fx-text-fill: #2a9d8f; -fx-font-weight: bold;");

        lblFechaMatch = new Label("-");
        lblFechaMatch.setStyle("-fx-font-size: 11px; -fx-text-fill: #8d99ae;");

        Button btnConfirmar = new Button("Confirmar y Guardar en Historial");
        btnConfirmar.setStyle(
                "-fx-background-color: #2a9d8f; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 10 20 10 20;"
        );
        btnConfirmar.setOnAction(e -> confirmarMatch());

        panel.getChildren().addAll(
                tituloResultado, lblPacienteGanador, lblOrganoAsignado,
                lblPuntuacionTotal, lblFechaMatch, btnConfirmar
        );

        return panel;
    }

    private TableView<Map.Entry<String, Double>> construirTablaDesglose() {
        TableView<Map.Entry<String, Double>> tv = new TableView<>();
        tv.setItems(datosDesglose);
        tv.setPrefHeight(220);

        TableColumn<Map.Entry<String, Double>, String> colCriterio = new TableColumn<>("Criterio de Puntuacion");
        colCriterio.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKey()));
        colCriterio.setPrefWidth(300);

        TableColumn<Map.Entry<String, Double>, String> colValor = new TableColumn<>("Valor");
        colValor.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getValue())));

        tv.getColumns().addAll(colCriterio, colValor);
        return tv;
    }

    private void cargarOrganosDisponibles() {
        cmbOrganoDisponible.getItems().clear();

        String sql = "SELECT id_organo, tipo_organo, tipo_sangre, donante_nombre, fecha_disponible, "
                + "tiempo_isquemia_max, hla_alelos_donante, estado FROM organos WHERE estado = 'DISPONIBLE'";

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

                    cmbOrganoDisponible.getItems().add(o);
                }
            }

            cmbOrganoDisponible.setConverter(new javafx.util.StringConverter<>() {
                @Override
                public String toString(Organo o) {
                    if (o == null) return "";
                    return "#" + o.getIdOrgano() + " - " + o.getTipoOrgano() + " (" + o.getTipoSangre() + ")";
                }

                @Override
                public Organo fromString(String s) {
                    return null;
                }
            });

        } catch (SQLException e) {
            mostrarError("Error al cargar organos disponibles: " + e.getMessage());
        }
    }

    private void ejecutarAutoMatch() {
        Organo organoSeleccionado = cmbOrganoDisponible.getValue();
        if (organoSeleccionado == null) {
            mostrarError("Seleccione un organo disponible antes de ejecutar el AutoMatch.");
            return;
        }

        try {
            MatchResult resultado = autoMatchService.ejecutarMatch(organoSeleccionado);
            this.ultimoResultado = resultado;

            if (resultado.getPacienteGanador() == null) {
                lblPacienteGanador.setText("Sin candidatos compatibles");
                lblOrganoAsignado.setText("Organo: " + organoSeleccionado.getTipoOrgano() + " (" + organoSeleccionado.getTipoSangre() + ")");
                lblPuntuacionTotal.setText("Puntuacion total: -");
                lblFechaMatch.setText("-");
                datosDesglose.clear();
                return;
            }

            Paciente ganador = resultado.getPacienteGanador();
            lblPacienteGanador.setText(ganador.getNombre() + " (" + ganador.getTipoSangre() + ")");
            lblOrganoAsignado.setText("Organo asignado: " + organoSeleccionado.getTipoOrgano()
                    + " #" + organoSeleccionado.getIdOrgano() + " (" + organoSeleccionado.getTipoSangre() + ")");
            lblPuntuacionTotal.setText(String.format("Puntuacion total: %.2f", resultado.getPuntuacionTotal()));
            lblFechaMatch.setText("Fecha del match: " + resultado.getFechaMatch().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            datosDesglose.clear();
            datosDesglose.addAll(resultado.getDesglosePuntuacion().entrySet());

        } catch (SQLException e) {
            mostrarError("Error al ejecutar AutoMatch: " + e.getMessage());
        }
    }

    /**
     * Confirma el match actual: guarda el registro en la tabla historial
     * y marca el organo como ASIGNADO.
     */
    private void confirmarMatch() {
        if (ultimoResultado == null || ultimoResultado.getPacienteGanador() == null) {
            mostrarError("No hay un match valido para confirmar. Ejecute el AutoMatch primero.");
            return;
        }

        String sqlHistorial = "INSERT INTO historial (id_paciente, id_organo, fecha_match, puntuacion_total, detalle_puntuacion) "
                + "VALUES (?, ?, ?, ?, ?)";
        String sqlActualizarOrgano = "UPDATE organos SET estado = 'ASIGNADO' WHERE id_organo = ?";

        try {
            Connection con = ConexionBD.getConexion();
            con.setAutoCommit(false);

            try (PreparedStatement stmtHist = con.prepareStatement(sqlHistorial);
                 PreparedStatement stmtOrg = con.prepareStatement(sqlActualizarOrgano)) {

                stmtHist.setInt(1, ultimoResultado.getPacienteGanador().getIdPaciente());
                stmtHist.setInt(2, ultimoResultado.getOrganoAsignado().getIdOrgano());
                stmtHist.setString(3, ultimoResultado.getFechaMatch().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                stmtHist.setDouble(4, ultimoResultado.getPuntuacionTotal());
                stmtHist.setString(5, ultimoResultado.desgloseComoJson());
                stmtHist.executeUpdate();

                stmtOrg.setInt(1, ultimoResultado.getOrganoAsignado().getIdOrgano());
                stmtOrg.executeUpdate();

                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }

            mostrarInfo("Match confirmado y guardado en el historial correctamente.");
            cargarOrganosDisponibles();
            datosDesglose.clear();
            lblPacienteGanador.setText("Sin resultado aun");
            lblOrganoAsignado.setText("-");
            lblPuntuacionTotal.setText("Puntuacion total: -");
            lblFechaMatch.setText("-");
            ultimoResultado = null;

        } catch (SQLException e) {
            mostrarError("Error al confirmar el match: " + e.getMessage());
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