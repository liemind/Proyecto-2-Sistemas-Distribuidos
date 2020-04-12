/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package respaldolocal;

import Model.Combustible;
import Model.Estacion;
import Model.Surtidor;
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
        Calendar calendario = Calendar.getInstance();
        
        /*try {
            Hilos hilo = new Hilos();
            hilo.start();
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }*/
        
        conectar("empresa.db");
        //limpieza();

        
    }
    
    public static synchronized ArrayList ObtenerCombustibles() {
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

    public static synchronized ArrayList ObtenerCombustibles(Connection conn) {
        ArrayList<Combustible> combustibles = new ArrayList<>();
        Statement stmt = null;
        try
        {
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
            return combustibles;

        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        return null;
    }
    
    
    
    public static Connection conectar(String bd) {
        Connection c = null;
        String dir = System.getProperty("user.dir");
        String url = dir+"\\"+bd;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"+url);
            System.out.println("Base de datos Conectada");
           
            //se deben sincronizar los datos.
            //buscar ultimo data log.
            String baseDeDatos = ultimaBasedeDatos();
            if(!baseDeDatos.equals(bd)) {
                //debe hacerse la sincronización de datos. En este momento, los datos de la base de datos de respaldo son mucho más actuales que la base de datos actual, por lo que debe ser actualizada.
                System.out.println("deberia hacerse una sincronizacion aca");
                if(SincronizacionS(bd, baseDeDatos, c)) {
                    System.out.println("Sincronizacion completada.");
                }
            }
        //registro de la bd
        logBd(bd);   
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            c = abrirRespaldo(bd, dir);
            return c;
        }
        
        return c;
   }
    
    public static Connection abrirRespaldo(String bd, String dir) {
        try {
            //abre el respaldo
            bd = ultimoRespaldo();
            System.out.println("Abriendo respaldo");
            String url = dir+"\\bdrespaldo\\"+bd;
            conn = DriverManager.getConnection("jdbc:sqlite:"+url);
            System.out.println("Base de datos de respaldo conectada");
            return conn;
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            return null;
        }
    }
    
    public static boolean Sincronizacion(String actualBD, String anteriorBD, Connection actualConn) {
        Connection connAnterior = null;
        Statement stmtAnterior = null;
        String dir = System.getProperty("user.dir");
        String url = dir+"\\bdrespaldo\\"+anteriorBD;
        try {
           Class.forName("org.sqlite.JDBC");
           connAnterior = DriverManager.getConnection("jdbc:sqlite:"+url);
           if(connAnterior != null) {
                //1. traspasar valores del combustible
                ArrayList<Combustible> combustibles = ObtenerCombustibles(connAnterior);
                if(combustibles != null) {
                    for (Combustible combustible : combustibles) {
                        combustible.save(actualConn);
                    }
                }
                
                //2. consultar la fecha de la ultima transaccion en la bd anterior.
                //Transaccion t = obtenerUltimaTransaccion(connAnterior);
                
                //ó. consultar la fecha de la última conexion de la base de datos anterior.
                String fechaConexionAnterior = obtenerUltimaConexion(actualBD);
                
                //3. consultar cantidad de transacciones con fecha superior a la de la ultima transaccion
                stmtAnterior = connAnterior.createStatement();
                String sql = "SELECT * FROM transaccion WHERE  date(fecha_hora) >= date('"+fechaConexionAnterior+"');";
                ResultSet rs = stmtAnterior.executeQuery(sql);
                ArrayList<Transaccion> transacciones = new ArrayList<>();
                
                if (rs.next())
                {
                    Transaccion temporal = new Transaccion(rs.getInt("id_surtidor"),rs.getInt("id_combustible"),rs.getInt("litros"),rs.getInt("costo"));
                    temporal.setId(rs.getInt("id"));
                    temporal.setFechaHora(rs.getString("fecha_hora"));
                    
                    transacciones.add(temporal);
                }
                
                //4. guardar 
               for (Transaccion transaccion : transacciones) {
                   transaccion.save(actualConn);
               }
           }
           
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return false;
    }
    
    public static boolean SincronizacionS(String actualBD, String anteriorBD, Connection actualConn) {
        Connection connAnterior = null;
        Statement stmtAnterior = null;
        String dir = System.getProperty("user.dir");
        String url = dir+"\\bdrespaldo\\"+anteriorBD;
        try {
           Class.forName("org.sqlite.JDBC");
           connAnterior = DriverManager.getConnection("jdbc:sqlite:"+url);
           if(connAnterior != null) {
                stmtAnterior = connAnterior.createStatement();
                //1. consultar la fecha de la última conexion de la base de datos anterior.
                String fechaConexionAnterior = obtenerUltimaConexion(actualBD);

                //2. Obtener valores del combustible con fecha superior a la de la última conexion
                String sql = "SELECT * FROM combustible WHERE  date(fecha_hora) >= date('"+fechaConexionAnterior+"');";
                ResultSet rs = stmtAnterior.executeQuery(sql);
                ArrayList<Combustible> combustibles = new ArrayList<>();
                
                if(rs.next()) {
                    Combustible temporal = new Combustible(rs.getString("nombre"), rs.getInt("costo"), rs.getString("fecha_hora"));
                    temporal.setId(rs.getInt("id"));
                    combustibles.add(temporal);
                }
                
                if(combustibles != null) {
                    for (Combustible combustible : combustibles) {
                        combustible.saveS(actualConn);
                    }
                }
                
                //3. Obtener valores del combustible (otra vez) con fecha superior al primer combustible de la lista.
                sql = "SELECT * FROM combustible WHERE  date(fecha_hora) >= date('"+combustibles.get(0).getFecha_hora()+"');";
                rs = stmtAnterior.executeQuery(sql);
                ArrayList<Combustible> nCombustibles = new ArrayList<>();
                
                if(rs.next()) {
                    Combustible temporal = new Combustible(rs.getString("nombre"), rs.getInt("costo"), rs.getString("fecha_hora"));
                    temporal.setId(rs.getInt("id"));
                    nCombustibles.add(temporal);
                }
                
                //4.Obtener las estaciones de la bd anterior.
                ArrayList<Estacion> estacionesAnteriores = new ArrayList<>();
                rs = stmtAnterior.executeQuery("SELECT * FROM estacion;");
                while ( rs.next() ) {
                   Estacion est = new Estacion(rs.getString("nombre"));
                   est.setId(rs.getInt("id"));
                   estacionesAnteriores.add(est);
                }
            
                //5.Obtener las estaciones de la bd actual.
                ArrayList<Estacion> estacionesActual = new ArrayList<>();
                rs = stmtAnterior.executeQuery("SELECT * FROM estacion;");
                while ( rs.next() ) {
                   Estacion est = new Estacion(rs.getString("nombre"));
                   est.setId(rs.getInt("id"));
                   estacionesActual.add(est);
                }
                
                //6.Guardar las estaciones que no estén en la actual.
                ArrayList<Estacion> estacionAGuardar = new ArrayList<>();
                for (Estacion estacion : estacionesActual) {
                    if(!buscarEstacionEnEstaciones(estacionesAnteriores, estacion)) {
                        estacionAGuardar.add(estacion);
                    }
                }
                
                //guardar
                for (Estacion estacion : estacionAGuardar) {
                    estacion.save(actualConn);
                }
                
                
                //7.Obtener las estaciones nuevamente a partir de la id de la primera estacion.
                estacionesActual.clear();
                rs = stmtAnterior.executeQuery("SELECT * FROM estacion;");
                while ( rs.next() ) {
                   Estacion est = new Estacion(rs.getString("nombre"));
                   est.setId(rs.getInt("id"));
                   estacionesActual.add(est);
                }
                
                
                //6. consultar cantidad de transacciones con fecha superior a la de la ultima conexion
                sql = "SELECT * FROM transaccion WHERE  date(fecha_hora) >= date('"+fechaConexionAnterior+"');";
                rs = stmtAnterior.executeQuery(sql);
                ArrayList<Transaccion> transacciones = new ArrayList<>();
                
                if (rs.next())
                {
                    Transaccion temporal = new Transaccion(rs.getInt("id_surtidor"),rs.getInt("id_combustible"),rs.getInt("litros"),rs.getInt("costo"));
                    temporal.setId(rs.getInt("id"));
                    temporal.setFechaHora(rs.getString("fecha_hora"));
                    
                    transacciones.add(temporal);
                }
                
                //7. guardar 
               for (Transaccion transaccion : transacciones) {
                   //7.1 buscar el id combustible actual dada la id en transaccion y setearla.
                   Combustible temporal = buscarCombustibleActual(combustibles, nCombustibles, transaccion.getIdCombustible());
                   if(temporal != null) {
                       transaccion.setIdCombustible(temporal.getId());
                   }
                   //7.2 buscar la estacion dada la id de transacciones guardada y setearla.
                   Estacion estacion = buscarEstacionActual(estacionAGuardar, estacionesActual, transaccion.getIdEstacion());
                   if(estacion != null) {
                       transaccion.setIdEstacion(estacion.getId());
                   }
                   transaccion.save(actualConn);
               }
           }
           
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return false;
    }
    
    /**parte de sincronizacion**/
    
    public static Combustible buscarCombustibleActual(ArrayList<Combustible> combustibleAnterior, ArrayList<Combustible> combustibleActual, int idABuscar) {
        for (int i = 0; i < combustibleAnterior.size(); i++) {
            if(combustibleAnterior.get(i).getId() == idABuscar ) {
                return combustibleActual.get(i);
            }
        }
        
        return null;
    }
    
    public static boolean buscarEstacionEnEstaciones(ArrayList<Estacion> estaciones, Estacion e){
        for (Estacion estacion : estaciones) {
            if(!estacion.getNombre().equalsIgnoreCase(e.getNombre())){
                return true;
            }
        }
        return false;
    }
    
    
    public static Estacion buscarEstacionActual(ArrayList<Estacion> estacionAnterior, ArrayList<Estacion> estacionActual, int idABuscar) {
        for (Estacion estacion : estacionAnterior) {
            if(estacion.getId() == idABuscar ) {
                for (Estacion estacionAc : estacionActual) {
                    if(estacionAc.getNombre().equalsIgnoreCase(estacion.getNombre())){
                        return estacionAc;
                    }
                }
            }
        }
        return null;
    }
    /**parte de sincronizacion off**/
    
    /**
     * Obtiene la última transacción de una base de datos.
     * @param conn string de conexión.
     * @return un objeto de tipo Transacción.
     */
    public static Transaccion obtenerUltimaTransaccion(Connection conn) {
        Statement stmt = null;
        try {
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            String sql = "SELECT * FROM transaccion ORDER BY id DESC LIMIT 1";
            ResultSet rs = stmt.executeQuery(sql);
            Transaccion t = null;
            
            if (rs.next())
            {
                t = new Transaccion(rs.getInt("id_surtidor"),rs.getInt("id_combustible"),rs.getInt("litros"),rs.getInt("costo"));
                t.setId(rs.getInt("id"));
                t.setFechaHora(rs.getString("fecha_hora"));
            }
            return t;
            
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Obtiene la fecha de la última conexion de la base de datos antes de que esta muriera.
     * @param bdAConsultar 
     * @return 
     */
    public static synchronized String obtenerUltimaConexion(String bdAConsultar) {
        String dir = System.getProperty("user.dir");
        String bd = "procesos.db";
        Connection logconn = null;
        Statement stmt = null;
        try {
            String url = dir+"\\"+bd;
            System.out.println("url respaldo: "+url);
            Class.forName("org.sqlite.JDBC");
            logconn = DriverManager.getConnection("jdbc:sqlite:"+url);
            
            System.out.println("Base de datos de registro conectado");
            logconn.setAutoCommit(false);
            stmt = logconn.createStatement();
            String sql = "SELECT * FROM log WHERE nombre='"+bdAConsultar+"' ORDER BY id DESC LIMIT 1";
            ResultSet rs = stmt.executeQuery(sql);
            String fecha = null;
            
            if (rs.next())
            {
                fecha = rs.getString("fecha");
            }
            
            stmt.close();
            logconn.commit();
            logconn.close();
            return fecha;
            
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return null;
    }
    
    
    /**
     * Guarda en una base de datos oculta, la ultima conexion de la bd.
     * @param nombre nombre de la base de datos.
     */
    public static void logBd(String nombre) {
        String dir = System.getProperty("user.dir");
        String bd = "procesos.db";
        Connection logconn = null;
        Statement stmt = null;
        String fecha = ObtenerFechaYHoraActual();
        
        try {
           String url = dir+"\\"+bd;
            System.out.println("url respaldo: "+url);
           Class.forName("org.sqlite.JDBC");
           logconn = DriverManager.getConnection("jdbc:sqlite:"+url);
           
           System.out.println("Base de datos de registro conectado");
           logconn.setAutoCommit(false);
           stmt = logconn.createStatement();
           
           String sql = "INSERT INTO log (nombre,fecha) VALUES ('"+nombre+"','"+fecha+"');"; 
           stmt.executeUpdate(sql);
           
           stmt.close();
           logconn.commit();
           logconn.close();
            System.out.println("Registro completado");
        } catch ( Exception e ) {
           System.err.println( e.getClass().getName() + ": " + e.getMessage() );
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
     * Busca en la base de datos de registro lo último hecho.
     * @return el nombre de la ultima base de datos que realizó alguna acción.
     */
    public static String ultimaBasedeDatos() {
        String dir = System.getProperty("user.dir");
        String bd = "procesos.db";
        String url = dir+"\\"+bd;
        Connection logconn = null;
        Statement stmt = null;
        String nombre = new String();
        
        try {
           Class.forName("org.sqlite.JDBC");
           logconn = DriverManager.getConnection("jdbc:sqlite:"+url);
           logconn.setAutoCommit(false);
           stmt = logconn.createStatement();
           
           ResultSet rs = stmt.executeQuery("SELECT nombre FROM log ORDER BY id DESC LIMIT 1;");

            while ( rs.next() ) {
               nombre = rs.getString("nombre");
                System.out.println("N: "+nombre);
            }
           
           stmt.close();
           logconn.commit();
           logconn.close();
           return nombre;
        } catch ( Exception e ) {
           System.err.println( e.getClass().getName() + ": " + e.getMessage() );
           return nombre;
        }
        
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
    
}
