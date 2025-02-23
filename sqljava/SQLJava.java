
package com.mycompany.sqljava;

public class SQLJava {
    public static void main(String[] args) {
        CConexion conexion = CConexion.getInstancia();
        if(conexion.conectar() !=null){
            System.out.println("Conexion establecida correctamente.");
        }else{
            System.err.println("Error al conectar con la base de datos..");
        }
        conexion.cerrarConexion();
    }
}

