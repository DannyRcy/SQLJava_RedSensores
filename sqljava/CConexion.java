package com.mycompany.sqljava;

import java.sql.*;

public class CConexion {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=dbSimade;encrypt=true;trustServerCertificate=true";
    private static final String USUARIO = "usersql";
    private static final String CONTRASENIA = "1234";

    private static CConexion instancia;
    private Connection conexion;

    // Constructor privado para evitar instanciación directa
    private CConexion() {
        try {
            // Cargar el driver JDBC (puede no ser necesario en versiones recientes)
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el driver JDBC de SQL Server.");
            e.printStackTrace();
        }
    }

    // Método para obtener la única instancia de CConexion (patrón Singleton)
    public static synchronized CConexion getInstancia() {
        if (instancia == null) {
            instancia = new CConexion();
        }
        return instancia;
    }

    // Método para conectar a la base de datos
    public Connection conectar() {
        try {
            if (conexion == null || conexion.isClosed()) {
                conexion = DriverManager.getConnection(URL, USUARIO, CONTRASENIA);
                System.out.println("Conexion establecida con exito.");
            }
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
        return conexion;
    }

    // Método para cerrar la conexión
    public void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                conexion = null;
                System.out.println("Conexion cerrada correctamente.");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para ejecutar procedimientos almacenados
    public void ejecutarProcedimiento(String nombreProcedimiento, Object... parametros) {
        if (conexion == null) {
            System.err.println("Error: No hay conexion a la base de datos.");
            return;
        }

        String llamadaSP = "{call " + nombreProcedimiento + "(" + "?,".repeat(parametros.length).replaceAll(",$", "") + ")}";

        try (CallableStatement stmt = conexion.prepareCall(llamadaSP)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            stmt.execute();
            System.out.println("Procedimiento almacenado '" + nombreProcedimiento + "' ejecutado correctamente.");
        } catch (SQLException e) {
            System.err.println("Error al ejecutar el procedimiento almacenado '" + nombreProcedimiento + "': " + e.getMessage());
            e.printStackTrace();
        }
    }
}
