/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package respaldolocal;

import Model.Combustible;
import Model.Surtidor;
import Model.Transaccion;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author elyna
 */
public class RespaldoServidor {
    public static void crearRespaldoServidor(Calendar calendario) {
        System.out.println("Inicio del respaldo");
        //Carpeta del usuario
        String dir = System.getProperty("user.dir");
        dir = dir+"\\bdrespaldo\\";
        
        Connection conn2 = null; //coneccion a la nueva bd
        String finalNBD = "empresa"; //nombre de la base de datos
        int hora, minutos, dia, mes, year;
        String ruta = "jdbc:sqlite:C:"+dir;
        
        hora =calendario.get(Calendar.HOUR_OF_DAY);
        minutos = calendario.get(Calendar.MINUTE);
        dia = calendario.get(Calendar.DAY_OF_MONTH);
        mes = calendario.get(Calendar.MONTH);
        year = calendario.get(Calendar.YEAR);
        
        finalNBD = finalNBD+"_"+Integer.toString(dia)+"-"+Integer.toString(mes)+"-"+Integer.toString(year)+"_"+Integer.toString(hora)+"-"+Integer.toString(minutos)+".db";
        System.out.println("nombre BD: "+finalNBD);
        conn2= crearBasedeDatos(finalNBD, ruta);
        
        if(crearTablas(conn2)) {
            System.out.println("Tablas creadas.");
        }
        
        if(llenarDatos(conn2)) {
            System.out.println("Datos respaldados");
        }
    }
    
    public static Connection crearBasedeDatos(String bd, String ruta) {
        Connection conn2 = null;
        String url = ruta+""+bd;
        try {
            conn2 = DriverManager.getConnection(url);
            if (conn2 != null) {
                //DatabaseMetaData meta = conn.getMetaData();
                System.out.println("Base de datos de respaldo creada.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return conn2;
    }
    
    public static boolean crearTablas(Connection conn2) {
        Statement stmt = null;
        try {
            conn2.setAutoCommit(false);
            stmt = conn2.createStatement();
            stmt.execute("CREATE TABLE combustible("+
            "   id INTEGER PRIMARY KEY AUTOINCREMENT,"+
            "   nombre TEXT NOT NULL,"+
            "   costo INTEGER NOT NULL,"+
            "   fecha_hora TEXT NOT NULL"+
            ");");
            
            stmt.execute("CREATE TABLE estacion("+
            "   id INTEGER PRIMARY KEY AUTOINCREMENT,"+
            "   nombre TEXT NOT NULL"+
            ");");
            
            stmt.execute("CREATE TABLE transaccion("+
            "   id INTEGER PRIMARY KEY AUTOINCREMENT,"+
            "   id_surtidor INTEGER NOT NULL,"+
            "   id_combustible INTEGER NOT NULL,"+
            "   litros INTEGER NOT NULL,"+
            "   costo INTEGER NOT NULL,"+
            "   FOREIGN KEY(id_surtidor) REFERENCES surtidor(id),"+
            "   FOREIGN KEY(id_combustible) REFERENCES combustible(id)"+
            ");");
            
            //valores base combustibles
            stmt.execute("INSERT INTO combustible (nombre,costo) VALUES ('93', 10);");
            stmt.execute("INSERT INTO combustible (nombre,costo) VALUES ('95', 10);");
            stmt.execute("INSERT INTO combustible (nombre,costo) VALUES ('97', 10);");
            stmt.execute("INSERT INTO combustible (nombre,costo) VALUES ('Diesel', 10);");
            stmt.execute("INSERT INTO combustible (nombre,costo) VALUES ('Kerosene', 10);");
            
            conn2.commit();
            stmt.close();
            return true;
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
   
    public static boolean llenarDatos(Connection conn2) {
        Statement stmt = null;
        Statement stmt2 = null;
        ResultSet rs;
        String est;
        Transaccion trans;
        Surtidor sur;
        Connection conn = null;
        ArrayList<String> estaciones = new ArrayList<>();
        ArrayList<Transaccion> transacciones = new ArrayList<>();
        ArrayList<Combustible> combustibles = new ArrayList<>();
        
        try {
            conn = Main.conectar("empresa.db");
            conn2.setAutoCommit(false);
            conn.setAutoCommit(false);
            //bd hecha
            stmt = conn.createStatement();
            //bd nueva
            stmt2 = conn2.createStatement();
            
            rs = stmt.executeQuery("SELECT * FROM estacion;");
            while ( rs.next() ) {
               est = rs.getString("nombre");
               estaciones.add(est);
            }
            //guardar
            for (String estacion : estaciones) {
                stmt2.executeUpdate("INSERT INTO estacion (nombre) VALUES ('"+estacion+"' );");
            }
            
            combustibles = Main.ObtenerCombustibles();
            if(combustibles != null) {
                for (Combustible combustible : combustibles) {
                    combustible.save(conn2);
                }
            }
           
            
            rs = stmt.executeQuery("SELECT * FROM transaccion;");
            while ( rs.next() ) {
               trans = new Transaccion(rs.getInt("id_surtidor"), rs.getInt("id_combustible"), rs.getInt("litros"), rs.getInt("costo"));
               trans.setId(rs.getInt("id"));
               trans.setFechaHora(rs.getString("fecha_hora"));
               transacciones.add(trans);
            }
            
            if(transacciones != null) {
                //guardar
                for (Transaccion tra : transacciones) {
                    tra.save(conn2);
                }
            }
            
            
            
            conn2.close();
            stmt.close();
            stmt2.close();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
    
}
