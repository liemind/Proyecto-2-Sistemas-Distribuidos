/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package respaldolocal;

import Model.Combustible;
import Model.Surtidor;
import Model.Transaccion;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author elyna
 */
public class Main {
    static final int HORA_RESPALDO = 0;
    static final int MINUTO_RESPALDO = 0;
    static public Connection conn;
    //string de conección de la bd
    //static public Connection conn;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        int hora, minutos;
        String finalNBD;
        Calendar calendario = Calendar.getInstance();
        
        hora =calendario.get(Calendar.HOUR_OF_DAY);
        minutos = calendario.get(Calendar.MINUTE);
        
        //conectar("estacion.db", "C:\\Users\\elyna\\Documents\\Universidad de Talca\\2019-2\\Sistemas Distribuidos\\Proyecto-2-Sistemas-Distribuidos\\Cosas Separadas\\RespaldoLocal\\");
        conectar("empresa.db", "C:\\Users\\elyna\\Documents\\Universidad de Talca\\2019-2\\Sistemas Distribuidos\\Proyecto-2-Sistemas-Distribuidos\\Cosas Separadas\\RespaldoLocal\\");
        
        if(hora >= HORA_RESPALDO && minutos >= MINUTO_RESPALDO) {
            crearRespaldo(calendario);
            //Thread.sleep(30000);
            
        }else {
            int finalMin = (60*HORA_RESPALDO)+MINUTO_RESPALDO;
            int currentMin = (60*hora)+minutos;
            int finalSleep = (finalMin-currentMin)*60000;
            //sleep 
            //sleep(finalSleep);
        }

        
    }
    
    /*****************
    public static void crearRespaldo(Calendar calendario) {
        System.out.println("Inicio del respaldo");
        
        Connection conn2 = null; //coneccion a la nueva bd
        String finalNBD = "estacion"; //nombre de la base de datos
        int hora, minutos, dia, mes, year;
        String ruta = "jdbc:sqlite:C:\\Users\\elyna\\Documents\\Universidad de Talca\\2019-2\\Sistemas Distribuidos\\Proyecto-2-Sistemas-Distribuidos\\Cosas Separadas\\RespaldoLocal\\bdrespaldo\\";
        
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
            "   costo INTEGER NOT NULL"+
            ");");
            stmt.execute("CREATE TABLE surtidor("+
            "   id INTEGER PRIMARY KEY AUTOINCREMENT,"+
            "   transacciones INTEGER"+
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
        int sur = 0;
        Transaccion trans;
        ArrayList<Integer> surtidores = new ArrayList<>();
        ArrayList<Transaccion> transacciones = new ArrayList<>();
        ArrayList<Combustible> combustibles = new ArrayList<>();
        
        try {
            conn2.setAutoCommit(false);
            conn.setAutoCommit(false);
            //bd hecha
            stmt = conn.createStatement();
            //bd nueva
            stmt2 = conn2.createStatement();
            
            rs = stmt.executeQuery("SELECT * FROM surtidor;");

            while ( rs.next() ) {
               sur = rs.getInt("transacciones");
               surtidores.add(sur);
            }
            
            //guardar
            for (Integer su : surtidores) {
                stmt2.executeUpdate("INSERT INTO surtidor (transacciones) VALUES ("+su+" );");
            }
            
            rs = stmt.executeQuery("SELECT * FROM transaccion;");
            while ( rs.next() ) {
               trans = new Transaccion(rs.getInt("id_surtidor"), rs.getInt("id_combustible"), rs.getInt("litros"), rs.getInt("costo"));
               trans.setId(rs.getInt("id"));
               transacciones.add(trans);
            }
            
            if(transacciones != null) {
                //guardar
                for (Transaccion tra : transacciones) {
                    tra.save(conn2);
                }
            }
            
            combustibles = obtenerCombustibles(conn);
            if(combustibles != null) {
                for (Combustible combustible : combustibles) {
                    combustible.save(conn2);
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
    * 
    ***********************/
    
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
    
    public static void crearRespaldo(Calendar calendario) {
        System.out.println("Inicio del respaldo");
        
        Connection conn2 = null; //coneccion a la nueva bd
        String finalNBD = "empresa"; //nombre de la base de datos
        int hora, minutos, dia, mes, year;
        String ruta = "jdbc:sqlite:C:\\Users\\elyna\\Documents\\Universidad de Talca\\2019-2\\Sistemas Distribuidos\\Proyecto-2-Sistemas-Distribuidos\\Cosas Separadas\\RespaldoLocal\\bdrespaldo\\";
        
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
    
    public static boolean crearTablas(Connection conn2) {
        Statement stmt = null;
        try {
            conn2.setAutoCommit(false);
            stmt = conn2.createStatement();
            stmt.execute("CREATE TABLE combustible("+
            "   id INTEGER PRIMARY KEY AUTOINCREMENT,"+
            "   nombre TEXT NOT NULL,"+
            "   costo INTEGER NOT NULL"+
            ");");
            
            stmt.execute("CREATE TABLE estacion("+
            "   id INTEGER PRIMARY KEY AUTOINCREMENT,"+
            "   nombre TEXT NOT NULL"+
            ");");
            
            stmt.execute("CREATE TABLE surtidor("+
            "   id INTEGER PRIMARY KEY AUTOINCREMENT,"+
            "   id_estacion INTEGER NOT NULL,"+
            "   id_numero_surtidor INTEGER NOT NULL,"+
            "   transacciones INTEGER,"+
            "   FOREIGN KEY(id_estacion) REFERENCES estacion(id)"+
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
        ArrayList<Surtidor> surtidores = new ArrayList<>();
        ArrayList<String> estaciones = new ArrayList<>();
        ArrayList<Transaccion> transacciones = new ArrayList<>();
        ArrayList<Combustible> combustibles = new ArrayList<>();
        
        try {
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
            
            rs = stmt.executeQuery("SELECT * FROM surtidor;");
            while ( rs.next() ) {
               sur = new Surtidor(rs.getInt("id_estacion"), rs.getInt("id_numero_surtidor"), rs.getInt("transacciones"));
               sur.setId(rs.getInt("id"));
               surtidores.add(sur);
            }
            
            combustibles = obtenerCombustibles(conn);
            if(combustibles != null) {
                for (Combustible combustible : combustibles) {
                    combustible.save(conn2);
                }
            }
            
            //guardar
            for (Surtidor su : surtidores) {
                stmt2.executeUpdate("INSERT INTO surtidor (id_estacion,id_numero_surtidor,transacciones) VALUES ("+su.getEstacion()+","+su.getNumeroSurtidor()+","+su.getNumeroTransacciones()+" );");
            }
            
            rs = stmt.executeQuery("SELECT * FROM transaccion;");
            while ( rs.next() ) {
               trans = new Transaccion(rs.getInt("id_surtidor"), rs.getInt("id_combustible"), rs.getInt("litros"), rs.getInt("costo"));
               trans.setId(rs.getInt("id"));
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
    
    /**
     * Obtiene de la base de datos los combustibles del servidor.
     * @return una lista de combustibles con sus precios respectivos.
     */
    public static ArrayList<Combustible> obtenerCombustibles(Connection conn) {
        Statement stmt = null;
        ArrayList<Combustible> combustibles = new ArrayList<>();
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM combustible;");

            while ( rs.next() ) {
               Combustible cc = new Combustible( rs.getString("nombre") , rs.getInt("costo") );
               cc.setId(rs.getInt("id"));
               combustibles.add(cc);
            }
            //end bandera
            rs.close();
            stmt.close();
            return combustibles;

        }catch(Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return null;
     }
    
    
    public static void conectar(String bd, String ruta) {
        Connection c = null;
        String url = ruta+""+bd;
        try {
           Class.forName("org.sqlite.JDBC");
           c = DriverManager.getConnection("jdbc:sqlite:"+url);
           System.out.println("Base de datos Conectada");
        } catch ( Exception e ) {
           System.err.println( e.getClass().getName() + ": " + e.getMessage() );
           System.exit(0);
        }
        
        Main.conn = c;
   }
    
}
