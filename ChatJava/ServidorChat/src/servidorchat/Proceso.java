/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidorchat;

import Model.Combustible;
import Model.Estacion;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Esta clase ayuda a realizar las gestiones de las estaciones tales como crear transacciones, nuevas estaciones, etc
 * 
 * @author Liemind
 */
public class Proceso
{


    public Proceso(){
    }
    
    /**
     * Conecta el programa con la base de datos (SQLite)
     * @param bd el nombre de la base de datos a conectar.
     * @return la conección a la bd.
     */
    public static Connection conectar(String bd)
    {
        Connection c = null;
        String dir = System.getProperty("user.dir");
        String url = dir+"\\"+bd;
        
        try
        {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + url);
            System.out.println("Base de datos Conectada");
            //registro de la bd
            logBd("empresa.db");
        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return c;
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
            conn = conectar("empresa.db");
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
    
    public static synchronized ArrayList ObtenerCombustibles(Connection conn) {
        ArrayList<Combustible> combustibles = new ArrayList<>();
        Statement stmt = null;
        try
        {
            conn = conectar("estacion.db");
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
     * Crea una estacion en caso de no existir con la ip en la que proviene la estacion
     * @param nombreEs
     * @return 
     */
    public static synchronized Estacion CrearEstacion(String nombreEs)
    {
        Connection conn = null;
        String nombreE = nombreEs;
        Statement stmt = null;
        Estacion estacion = null;
        try
        {
            conn = conectar("empresa.db");
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            //consultar estacion

            //crear Estacion
            String sql = "INSERT INTO estacion (nombre) VALUES ('" + nombreE + "');";
            stmt.executeUpdate(sql);
            //por la id

            estacion = buscarEstacion(nombreE);
            System.out.println("la id de la estacion creada es: " + estacion.getId());

            stmt.close();
            conn.commit();
        }
        catch (SQLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return estacion;
    }
    
    /**
     * Verifica la existencia de una estacion en particular
     * @param nombreE
     * @return 
     */
    public static synchronized boolean existeEstacion(String nombreE)
    {
        Connection conn = null;
        Statement stmt = null;
        try
        {
            conn = conectar("empresa.db");
            //consultar estacion
            String sql = "SELECT count(*) FROM estacion WHERE nombre = '" + nombreE + "';";
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            int cantIP = 0;
            //Representa a la cantidad de ip's que esta en la base de datos que son iguales a la nombreE
            while (rs.next())
            {
                cantIP = rs.getInt("count(*)");
                //System.out.println("Cantidad de ips: " + cantIP);
            }
            rs.close();
            stmt.close();
            conn.commit();
            if (cantIP == 0)
            {
                return false;
            }
        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return true;
    }
    
    /**
     * Busca una estación en particular. Si existe la estacion, devuelve una instancia con todos los datos, en caso contrario devuelve un null
     * @param nombreE
     * @return estacion
     */
    public static synchronized Estacion buscarEstacion(String nombreE)
    {
        Connection conn = null;
        Statement stmt = null;
        Estacion estacion = null;

        try
        {
            conn = conectar("empresa.db");
            //consultar estacion
            String sql = "SELECT id, nombre FROM estacion WHERE nombre = '" + nombreE + "';";
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next())
            {
                estacion = new Estacion(rs.getString("nombre"), rs.getInt("id"));
            }
            rs.close();
            stmt.close();
            conn.commit();
        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return estacion;
    }

    
    /**
     * Guarda en la base de datos local una transacción.
     * @param idSurtidor
     * @param idCombustible
     * @param litros
     * @param fecha_hora
     * @param costo
     * @return
     */
    public static int CrearTransaccion(String ip, int idSurtidor, int idCombustible, int litros, int costo, String fecha_hora)
    {
        Connection conn = null;
        Statement stmt = null;
        int idTransaccion = -1;
        try
        {
            conn = conectar("empresa.db");
            //Coneccion conn
            conn.setAutoCommit(false);
            //bandera
            System.out.println("Transaccion: " + ip + "," + idSurtidor + "," + idCombustible + "," + litros + "," + costo + ","+fecha_hora);
            //end bandera

            stmt = conn.createStatement();
            String sql = "INSERT INTO transaccion (id_estacion, id_surtidor, id_combustible, litros, costo, fecha_hora) VALUES ( '" + ip + "', " + idSurtidor + ", " + idCombustible + ", " + litros + ", " + costo + ", '" + fecha_hora + "' );";
            stmt.execute(sql);
            ResultSet rs = stmt.executeQuery("SELECT id FROM transaccion ORDER BY id DESC LIMIT 1;");

            if (rs.next())
            {
                idTransaccion = Integer.parseInt(rs.getString("id"));
            }
            System.out.println("Id transaccion:  " + idTransaccion);
            stmt.close();
            conn.commit();
            //bandera
            System.out.println("Crea la transaccion");
            //end bandera
        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            //bandera
            System.out.println("No crea la transaccion");
            //end bandera
        }
        return idTransaccion;
    }

    /**
     * Obtiene la fecha y hora actual del sistema.
     * @return fecha
     */
    public static String ObtenerFechaYHoraActual()
    {
        String formato = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern(formato);
        LocalDateTime ahora = LocalDateTime.now();
        return formateador.format(ahora);
    }
    
    /***
     * PROCESOS RELACIONADOS CON LA BD OCULTA
    */
    /**
     * Guarda en una base de datos oculta, la ultima conexion de la bd.
     * @param nombre nombre de la base de datos.
     */
    public static void logBd(String nombre) {
        String dir = System.getProperty("user.dir");
        String bd = "files.db";
        Connection logconn = null;
        Statement stmt = null;
        Calendar calendario = Calendar.getInstance();
        DateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String fecha = formato.format(calendario);
        
        try {
           logconn = conectar(bd);
           System.out.println("Base de datos de respaldo conectado");
           logconn.setAutoCommit(false);
           stmt = logconn.createStatement();
           
           String sql = "INSERT INTO log (nombre,fecha) VALUES ('"+nombre+"','"+fecha+"');"; 
           stmt.executeUpdate(sql);
           
           stmt.close();
           logconn.commit();
           logconn.close();
        } catch ( Exception e ) {
           System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }        
    }
        
    /**
     * Obtiene todas las transacciones que no pudieron ser enviadas al servidor.
     * @return un arreglo con enteros.
     */
    public static ArrayList<Integer> obtenerTransaccionesFallidas() {
        Connection conn = null;
        ArrayList<Integer> ids = new ArrayList<>();
        Statement stmt = null;
        try
        {
            conn = conectar("files.db");
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM transacciones_enviadas WHERE envio=-1;");

            while (rs.next())
            {
                int id = (rs.getInt("id_transaccion"));
                ids.add(id);
            }
            rs.close();
            stmt.close();
            conn.close();
            return ids;
        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Guarda en la base de datos de registro, la id de la ultima transaccion que realizo y su estado. 
     * @param id la id en el sistema de la ultima transaccion.
     * @param estado estado de la transaccion. 1, enviada. -1 no enviada
     */
    public static void guardarRegistroTransaccion(int id, int estado) {
        //DEBEMOS TERMINAR ESTO
    }
    /***
     * FIN PROCESOS RELACIONADOS CON LA BD OCULTA
    */
    
    
    /***
     * PROCESOS RELACIONADOS CON ARCHIVOS DE RESPALDO
    */
    
    /**
     * Limpia los archivos de respaldo, dejando sólo los cinco últimos.
     * @return si la operación fue exitosa o no.
     */
    public static boolean limpieza() {
        int cantidadAEliminar = 5;
        String dir = System.getProperty("user.dir");
        dir = dir+"\\bdrespaldo";
        
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        File carpeta = new File(dir);
        File[] archivos = carpeta.listFiles();
        File[] respaldos = null;
        
        if(archivos.length > 5) {
            System.out.println("Iniciando limpieza... ");
            
            //delete
            for (int i = archivos.length-cantidadAEliminar; i > 0; i--) {
                File del = archivos[i];
                if(del.delete()) {
                    System.out.println("Borrando: "+del.getName());
                }
            }
            
            System.out.println("Fin limpieza");
            return true;
            
        }
        else {
            return false;
        }
    }
    
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
    
    
}
