/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientechat;

import Model.Combustible;
import Model.Transaccion;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author yorch
 */
public class Procesos
{
    /**
     * Conecta el programa con la base de datos (SQLite)
     *
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
        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return c;
    }
    
    /**
     * Guarda en la base de datos local una transacción
     *
     * @param conn
     * @param idSurtidor
     * @param idCombustible
     * @param litros
     * @param fecha_hora
     * @param costo
     * @return
     */
    public static int CrearTransaccion(int idSurtidor, int idCombustible, int litros, int costo, String fecha_hora)
    {
        Connection conn = null;
        Statement stmt = null;
        int idTransaccion = -1;
        try
        {
            conn = conectar("estacion.db");
            conn.setAutoCommit(false);
            //bandera
            System.out.println("Transaccion: " + idSurtidor + "," + idCombustible + "," + litros + "," + costo + ","+fecha_hora);
            //end bandera

            stmt = conn.createStatement();
            String sql = "INSERT INTO transaccion (id_surtidor, id_combustible, litros, costo, fecha_hora) VALUES (" + idSurtidor + ", " + idCombustible + ", " + litros + ", " + costo + ", '" + fecha_hora + "' );";
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
            conn.close();
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

    /*Obtiene:
        id transaccion
        id surtidor
        id combustible
        litros
        costo
     */
    public static String ObtenerTransaccion(int id)
    {
        Connection conn = null;
        Statement stmt = null;
        String transaccion = "";
        try
        {
            conn = conectar("estacion.db");
            conn.setAutoCommit(false);

            stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM transaccion WHERE id = " + id + ";");

            if (rs.next())
            {
                transaccion += rs.getString("id") + "," + rs.getString("id_surtidor") + "," + rs.getString("id_combustible") + "," + rs.getString("litros") + "," + rs.getString("costo")+ "," + rs.getString("fecha_hora");
            }
            stmt.close();
            conn.commit();
            //bandera
            System.out.println("Obtiene la transaccion");
            //end bandera
            conn.close();
            return transaccion;
        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            //bandera
            System.out.println("No obtiene la transaccion");
            //end bandera
        }

        return null;
    }

    public static Combustible BuscarCombustible(String n)
    {
        ArrayList<Combustible> combustibles = new ArrayList<>();
        combustibles = ObtenerCombustibles();
        for (Combustible cc : combustibles)
        {
            if (cc.getNombre().equalsIgnoreCase(n))
            {
                return cc;
            }
        }
        return null;
    }
    
    /**
     * Obtiene de la base de datos los combustibles del servidor.
     *
     * @return
     */
    public static synchronized ArrayList ObtenerCombustibles()
    {
        Connection conn = null;
        ArrayList<Combustible> combustibles = new ArrayList<>();
        Statement stmt = null;
        try
        {
            conn = conectar("estacion.db");
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM combustible;");

            while (rs.next())
            {
                Combustible cc = new Combustible(rs.getString("nombre"), rs.getInt("costo"), rs.getInt("id_comb_empresa"));
                cc.setId(rs.getInt("id"));
                combustibles.add(cc);
            }
            //end bandera
            rs.close();
            stmt.close();
            conn.close();
            return combustibles;

        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        return null;
    }

    
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
            //inicio insertsort
            int n = archivos.length; 
            for (int i = 1; i < n; ++i) {
                
                File key = archivos[i];
                int j = i - 1; 

                while (j >= 0 && formato.format(archivos[j].lastModified()).compareTo(formato.format(key.lastModified())) > 0) {
                    archivos[j + 1] = archivos[j]; 
                    j = j - 1; 
                } 
                archivos[j + 1] = key; 
            } 
            //fin insersort
            
            //alrevez
            int j = 0;
            for (int i = archivos.length-1; i >= 0; i--) {
                respaldos[j] = archivos[i];
                j++;
            }
            
            //delete
            for (int i = cantidadAEliminar-1; i < respaldos.length; i++) {
                File del = respaldos[i];
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
    
    /**
     * Crea un respaldo de la base de datos actual.
     * @param calendario fecha y hora actual.
     */
    public static void crearRespaldoCliente(Calendar calendario) {
        System.out.println("Inicio del respaldo");
        //Carpeta del usuario
        String dir = System.getProperty("user.dir");
        dir = dir+"\\bdrespaldo";
        
        Connection conn2 = null; //coneccion a la nueva bd
        String finalNBD = "estacion"; //nombre de la base de datos
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
        //crear log
        logBd(finalNBD);
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
            conn = conectar("estacion.db");
            
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
                    tra.save(conn2);
                }
            }
            
            combustibles = ObtenerCombustibles();
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
    
    
}
