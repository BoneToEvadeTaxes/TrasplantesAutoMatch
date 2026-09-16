package com.trasplantes.trasplantes;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicacion JavaFX.
 * Organ Transplant AutoMatch System.
 *
 * Arquitectura modular: BorderPane raiz con un Sidebar de navegacion
 * a la izquierda y un area de contenido central que intercambia
 * dinamicamente entre las 5 vistas del sistema.
 */
public class HelloApplication extends Application {

    private BorderPane rootLayout;
    private VBox contenidoDashboard;
    private VBox contenidoPacientes;
    private VBox contenidoOrganos;
    private VBox contenidoAutoMatch;
    private VBox contenidoHistorial;

    @Override
    public void start(Stage stage) {
        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: #f4f6f9;");

        // Inicializar vistas (lazy real seria mejor, pero se instancian una vez)
        contenidoDashboard = new DashboardView().getVista();
        contenidoPacientes = new PacientesView().getVista();
        contenidoOrganos = new OrganosView().getVista();
        contenidoAutoMatch = new AutoMatchView().getVista();
        contenidoHistorial = new HistorialView().getVista();

        VBox sidebar = construirSidebar();
        rootLayout.setLeft(sidebar);
        rootLayout.setCenter(contenidoDashboard);

        Scene scene = new Scene(rootLayout, 1200, 750);
        stage.setTitle("Organ Transplant AutoMatch System");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Construye el menu lateral de navegacion (Sidebar) con 5 botones,
     * uno por cada modulo del sistema.
     */
    private VBox construirSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.setPadding(new Insets(20, 12, 20, 12));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #1b263b;");

        Label titulo = new Label("OrganMatch");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
        Label subtitulo = new Label("AutoMatch System");
        subtitulo.setStyle("-fx-text-fill: #8d99ae; -fx-font-size: 11px;");

        VBox encabezado = new VBox(2, titulo, subtitulo);
        encabezado.setPadding(new Insets(0, 0, 20, 4));

        Button btnDashboard = crearBotonMenu("Dashboard", () -> rootLayout.setCenter(contenidoDashboard));
        Button btnPacientes = crearBotonMenu("Pacientes", () -> rootLayout.setCenter(contenidoPacientes));
        Button btnOrganos = crearBotonMenu("Organos", () -> rootLayout.setCenter(contenidoOrganos));
        Button btnAutoMatch = crearBotonMenu("AutoMatch Engine", () -> rootLayout.setCenter(contenidoAutoMatch));
        Button btnHistorial = crearBotonMenu("Historial", () -> rootLayout.setCenter(contenidoHistorial));

        sidebar.getChildren().addAll(
                encabezado, btnDashboard, btnPacientes, btnOrganos, btnAutoMatch, btnHistorial
        );

        return sidebar;
    }

    private Button crearBotonMenu(String texto, Runnable accion) {
        Button boton = new Button(texto);
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setAlignment(Pos.CENTER_LEFT);
        boton.setPrefHeight(42);
        boton.setStyle(
                "-fx-background-color: #273a5c; -fx-text-fill: white; -fx-font-size: 13px; " +
                        "-fx-background-radius: 6; -fx-cursor: hand;"
        );
        boton.setOnMouseEntered(e -> boton.setStyle(
                "-fx-background-color: #3a5a9c; -fx-text-fill: white; -fx-font-size: 13px; " +
                        "-fx-background-radius: 6; -fx-cursor: hand;"
        ));
        boton.setOnMouseExited(e -> boton.setStyle(
                "-fx-background-color: #273a5c; -fx-text-fill: white; -fx-font-size: 13px; " +
                        "-fx-background-radius: 6; -fx-cursor: hand;"
        ));
        boton.setOnAction(e -> accion.run());
        VBox.setVgrow(boton, Priority.NEVER);
        return boton;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
