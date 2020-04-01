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
    
    
    public static synchronized ArrayList ObtenerCombustibles(Connection conn) {
        ArrayList<Combustible> combustibles = new ArrayList<>();
        Statement stmt = null;
        try
        {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM combustible;");

            while (rs.next())
            {
                Combustible cc = new Combustible(rs.getString("nombre"), rs.getInt("costo"), rs.getString("fecha_hora"));
                cc.setId(rs.getInt("id"));
                combustibles.add(cc);
            }
            //end bandera
            rs.close();
            stmt.close();
            return combustibles;

        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Obtiene de la base de datos los combustibles del servidor.
     *
     * @return una lista de combustibles con sus precios respectivos.
     */
    public static synchronized ArrayList<Combustible> ObtenerCombustibles()
    {
        Connection conn = null;
        Statement stmt = null;
        try
        {
            conn = Main.conectar("empresa.db");
            ArrayList<Combustible> combustibles = new ArrayList<>();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM combustible;");

            while (rs.next())
            {
                Combustible combustible = new Combustible(rs.getString("nombre"), rs.getInt("costo"), rs.getString("fecha_hora"));
                combustible.setId(rs.getInt("id"));
                combustibles.add(combustible);
            }
            //end bandera
            rs.close();
            stmt.close();
            return combustibles;

        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return null;
        }
    }
    
    /***
     * PROCESOS RELACIONADOS CON ARCHIVOS DE RESPALDO
    */
    
    
    /**
     * Crea un respaldo de la base de datos actual.
     * @param calendario fecha y hora actual.
     */
    public static void crearRespaldoServidor(Calendar calendario) {
        System.out.println("Inicio del respaldo");
        //Carpeta del usuario
        String dir = System.getProperty("user.dir");
        dir = dir+"\\bdrespaldo\\";
        
        Connection conn2 = null; //coneccion a la nueva bd
        String finalNBD = "empresa"; //nombre de la base de datos
        int hora, minutos, dia, mes, year;
        String ruta = "jdbc:sqlite:"+dir;
        
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
    
    /**
     * Crea una base de datos dado el nombre y la ruta.
     * @param bd
     * @param ruta
     * @return 
    */
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
    
    /**
     * Dada una conexión, crea tablas para una base de datos vacía.
     * @param conn2
     * @return 
     */
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
            "   id_estacion INTEGER NOT NULL,"+
            "   id_surtidor INTEGER NOT NULL,"+
            "   id_combustible INTEGER NOT NULL,"+
            "   litros INTEGER NOT NULL,"+
            "   costo INTEGER NOT NULL,"+
            "   fecha_hora TEXT NOT NULL,"+
            "   FOREIGN KEY(id_estacion) REFERENCES estacion(id),"+
            "   FOREIGN KEY(id_combustible) REFERENCES combustible(id)"+
            ");");
            
            String fecha = Main.ObtenerFechaYHoraActual();
            
            //valores base combustibles
            stmt.execute("INSERT INTO combustible (nombre,costo,fecha_hora) VALUES ('93', 10, '"+fecha+"');");
            stmt.execute("INSERT INTO combustible (nombre,costo,fecha_hora) VALUES ('95', 10, '"+fecha+"');");
            stmt.execute("INSERT INTO combustible (nombre,costo,fecha_hora) VALUES ('97', 10, '"+fecha+"');");
            stmt.execute("INSERT INTO combustible (nombre,costo,fecha_hora) VALUES ('Diesel', 10, '"+fecha+"');");
            stmt.execute("INSERT INTO combustible (nombre,costo,fecha_hora) VALUES ('Kerosene', 10, '"+fecha+"');");
            
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
            
            combustibles = ObtenerCombustibles();
            if(combustibles != null) {
                for (Combustible combustible : combustibles) {
                    //combustible.save(conn2);
                    combustible.saveS(conn2);
                }
            }
           
            
            rs = stmt.executeQuery("SELECT * FROM transaccion;");
            while ( rs.next() ) {
               trans = new Transaccion(rs.getInt("id_estacion"), rs.getInt("id_surtidor"), rs.getInt("id_combustible"), rs.getInt("litros"), rs.getInt("costo"));
               trans.setId(rs.getInt("id"));
               trans.setFechaHora(rs.getString("fecha_hora"));
               transacciones.add(trans);
                System.out.println("Trans id:"+ trans.getId());
            }
            
            if(transacciones != null) {
                //guardar
                for (Transaccion tra : transacciones) {
                    tra.saveS(conn2);
                }
            }
            
            
            conn.close();
            conn2.close();
            stmt.close();
            stmt2.close();
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
    
    /***
     * FIN PROCESOS RELACIONADOS CON ARCHIVOS DE RESPALDO
    */
}
