/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package respaldolocal;

import Model.Combustible;
import Model.Transaccion;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author elyna
 */
public class RespaldoCliente {
    /***
     * PROCESOS RELACIONADOS CON ARCHIVOS DE RESPALDO
    */
    
    /**
     * Busca entre los respaldos y retorna el mas reciente.
     * @return Nombre de la base de datos del ultimo respaldo.
     */
    public static String ultimoRespaldo() {
        String temporalFile = new String();
        String dir = System.getProperty("user.dir");
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dir = dir+"\\bdrespaldo";
        File carpeta = new File(dir);
        File[] archivos = carpeta.listFiles();
        File[] respaldos = null;
        
        if (archivos != null){
            //buscar respaldos
            for (int i = 0; i < archivos.length; i++) {
                File archivo = archivos[i];
                int j=0;
                if(!archivo.isDirectory()){
                    respaldos[j] = archivo;
                    j++;
                }
            }
            
            if(respaldos != null) {
                File temporal = archivos[0];
                if(respaldos.length > 1) {
                    for (int i = 1; i < respaldos.length; i++) {
                        File archivo = archivos[i];
                        if(formato.format(archivo.lastModified()).compareTo(formato.format(temporal.lastModified())) > 0) {
                            temporal = archivo;
                        }
                    }
                }
                else {
                    temporalFile = temporal.getName();
                }
            }
        }
        return temporalFile;
    }
    
    /**
     * Crea un respaldo de la base de datos actual.
     * @param calendario fecha y hora actual.
     */
    public static void crearRespaldoCliente(Calendar calendario) {
        System.out.println("Inicio del respaldo");
        //Carpeta del usuario
        String dir = System.getProperty("user.dir");
        dir = dir+"\\bdrespaldo\\";
        //bandera
        System.out.println(dir);
        //end bandera
        
        Connection conn2 = null; //coneccion a la nueva bd
        String finalNBD = "estacion"; //nombre de la base de datos
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
        //crear log
        Main.logBd(finalNBD);
        //fin crear log
        
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
            "   id_comb_empresa INTEGER NOT NULL,"+
            "   nombre TEXT NOT NULL,"+
            "   costo INTEGER NOT NULL"+
            ");");
            stmt.execute("CREATE TABLE transaccion("+
            "   id INTEGER PRIMARY KEY AUTOINCREMENT,"+
            "   id_surtidor INTEGER NOT NULL,"+
            "   id_combustible INTEGER NOT NULL,"+
            "   litros INTEGER NOT NULL,"+
            "   costo INTEGER NOT NULL,"+
            "   fecha_hora TEXT NOT NULL,"+
            "   FOREIGN KEY(id_combustible) REFERENCES combustible(id)"+
            ");");
            //valores base combustibles
            stmt.execute("INSERT INTO combustible (id_comb_empresa,nombre,costo) VALUES (1,'93', 10);");
            stmt.execute("INSERT INTO combustible (id_comb_empresa,nombre,costo) VALUES (2,'95', 10);");
            stmt.execute("INSERT INTO combustible (id_comb_empresa,nombre,costo) VALUES (3,'97', 10);");
            stmt.execute("INSERT INTO combustible (id_comb_empresa,nombre,costo) VALUES (4,'Diesel', 10);");
            stmt.execute("INSERT INTO combustible (id_comb_empresa,nombre,costo) VALUES (5,'Kerosene', 10);");
            
            conn2.commit();
            stmt.close();
            return true;
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
    
    /**
     * Llena la base de datos con datos de la base de datos original.
     * @param conn2
     * @return 
     */
    public static boolean llenarDatos(Connection conn2) {
        Statement stmt = null;
        Statement stmt2 = null;
        Connection conn = null;
        ResultSet rs;
        int sur = 0;
        Transaccion trans;
        ArrayList<Integer> surtidores = new ArrayList<>();
        ArrayList<Transaccion> transacciones = new ArrayList<>();
        ArrayList<Combustible> combustibles = new ArrayList<>();
        
        try {
            conn = Main.conectar("estacion.db");
            
            conn2.setAutoCommit(false);
            conn.setAutoCommit(false);
            //bd hecha
            stmt = conn.createStatement();
            //bd nueva
            stmt2 = conn2.createStatement();
            
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
                    //tra.save(conn2);
                    System.out.println("T: "+tra.getId());
                }
            }
            
            combustibles = Main.ObtenerCombustibles();
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
    /***
     * FIN PROCESOS RELACIONADOS CON ARCHIVOS DE RESPALDO
    */
    
    /**
     * PROCESOS RELACIONADOS CON ARCHIVOS DE SINCRONIZACION
     */
    
    
    /**
     * FIN PROCESOS RELACIONADOS CON ARCHIVOS DE SINCRONIZACION
     */
}
