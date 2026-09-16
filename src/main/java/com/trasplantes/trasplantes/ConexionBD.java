package com.trasplantes.trasplantes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Clase de utilidad para gestionar la conexion JDBC a la base de datos
 * SQLite del sistema Organ Transplant AutoMatch System.
 */
public class ConexionBD {

    private static final String URL_BASE = "jdbc:sqlite:trasplantes.db";
    private static Connection conexion = null;

    // Bloque estatico para cargar el driver JDBC de SQLite
    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No se encontro el driver JDBC de SQLite. " +
                    "Verifique que la dependencia sqlite-jdbc este en el classpath.", e);
        }
    }

    private ConexionBD() {
        // Constructor privado: clase de utilidad, no instanciable
    }

    /**
     * Obtiene una conexion unica (singleton) a la base de datos SQLite.
     * Si la conexion no existe o esta cerrada, crea una nueva.
     *
     * @return Connection activa hacia trasplantes.db
     * @throws SQLException si ocurre un error al conectar
     */
    public static Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            conexion = DriverManager.getConnection(URL_BASE);
            // Habilitar el soporte de claves foraneas en SQLite
            try (Statement stmt = conexion.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
        }
        return conexion;
    }

    /**
     * Cierra la conexion activa a la base de datos, si existe.
     */
    public static void cerrarConexion() {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexion: " + e.getMessage());
            } finally {
                conexion = null;
            }
        }
    }

    /**
     * Metodo de prueba rapido para verificar que la conexion funciona.
     */
    public static void main(String[] args) {
        try {
            Connection con = ConexionBD.getConexion();
            if (con != null) {
                System.out.println("Conexion exitosa a trasplantes.db");
            }
        } catch (SQLException e) {
            System.err.println("Fallo la conexion: " + e.getMessage());
        } finally {
            ConexionBD.cerrarConexion();
        }
    }
}