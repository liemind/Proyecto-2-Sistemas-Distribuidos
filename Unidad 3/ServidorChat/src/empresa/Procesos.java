/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package empresa;

import Model.Combustible;
import Model.Estacion;
import Model.Transaccion;
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
public class Procesos
{


    public Procesos(){
    }
    
    /**
     * Conecta el programa con la base de datos (SQLite)
     * @param bd el nombre de la base de datos a conectar.
     * @return la conección a la bd.
     */
    public static synchronized Connection conectar(String bd) {
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
                //System.out.println("deberia hacerse una sincronizacion aca");
                if(Sincronizacion(bd, baseDeDatos, c)) {
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
            Connection conn = DriverManager.getConnection("jdbc:sqlite:"+url);
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
                        combustible.save(actualConn);
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
                    Transaccion temporal = new Transaccion(rs.getInt("id_estacion"), rs.getInt("id_surtidor"),rs.getInt("id_combustible"),rs.getInt("litros"),rs.getInt("costo"));
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
               
               connAnterior.close();
               stmtAnterior.close();
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
    
    public static ArrayList<String> ObtenerTransaccionesAnualesPorSucursal() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = conectar("empresa.db");
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            String sql = "SELECT estacion.nombre as nombre, strftime('%Y', transaccion.fecha_hora) as año, count(strftime('%Y', transaccion.fecha_hora)) as cantTransacciones from transaccion, estacion WHERE transaccion.id_estacion = estacion.id GROUP by transaccion.id_estacion, strftime('%Y', fecha_hora);";
            ResultSet rs = stmt.executeQuery(sql);
             ArrayList<String> estacion = new ArrayList<>();
            
            while (rs.next())
            {
                String temporal = rs.getString("nombre")+","+rs.getInt("año")+","+rs.getInt("cantTransacciones");
                estacion.add(temporal);
                System.out.println(temporal);
            }
            
            stmt.close();
            conn.commit();
            conn.close();
            return estacion;
            
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return null;
    }
    
    public static ArrayList<String> filtrarPorMesYAño() {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            conn = conectar("empresa.db");
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            String sql = "SELECT estacion.nombre as nombre, strftime('%Y', transaccion.fecha_hora) as año, strftime('%m', transaccion.fecha_hora) as mes, count(strftime('%m', transaccion.fecha_hora)) as cantTransaciones FROM transaccion, estacion WHERE transaccion.id_estacion = estacion.id GROUP BY id_estacion, strftime('%m', fecha_hora);";
            ResultSet rs = stmt.executeQuery(sql);
            ArrayList<String> estacion = new ArrayList<>();
            
            while (rs.next())
            {
                String temporal = rs.getString("nombre")+","+rs.getInt("mes")+","+rs.getInt("año")+","+rs.getInt("cantTransaciones");
                estacion.add(temporal);
                System.out.println(temporal);
            }
            
            stmt.close();
            conn.commit();
            conn.close();
            return estacion;
        } catch (Exception e) {
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
            conn.close();
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
            stmt = conn.createStatement();
            //consultar estacion

            //crear Estacion
            String sql = "INSERT INTO estacion (nombre) VALUES ('" + nombreE + "');";
            stmt.executeUpdate(sql);
            //por la id

            estacion = buscarEstacion(nombreE);
            System.out.println("la id de la estacion creada es: " + estacion.getId());

            stmt.close();
            conn.close();
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
            conn.close();
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
            conn.close();
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
            
            String sql;
            sql = "select id from estacion where nombre = '" + ip +"';";
            int idSucursal = 0;
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next())
            {
                idSucursal = rs.getInt("id");
            }

            sql = "INSERT INTO transaccion (id_estacion, id_surtidor, id_combustible, litros, costo, fecha_hora) VALUES ( " + idSucursal + ", " + idSurtidor + ", " + idCombustible + ", " + litros + ", " + costo + ", '" + fecha_hora + "' );";
           
            stmt.execute(sql);
            rs = stmt.executeQuery("SELECT id FROM transaccion ORDER BY id DESC LIMIT 1;");

            if (rs.next())
            {
                idTransaccion = Integer.parseInt(rs.getString("id"));
            }
            System.out.println("Id transaccion:  " + idTransaccion);
            stmt.close();
            conn.commit();
            conn.close();
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
    public static synchronized void logBd(String nombre) {
        String dir = System.getProperty("user.dir");
        String bd = "procesos.db";
        Connection logconn = null;
        Statement stmt = null;
        String fecha = ObtenerFechaYHoraActual();
        
        try {
            String url = dir+"\\"+bd;
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
    public static synchronized boolean limpieza() {
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
        
        finalNBD = finalNBD+"_"+Integer.toString(dia)+"-"+Integer.toString(mes+1)+"-"+Integer.toString(year)+"_"+Integer.toString(hora)+"-"+Integer.toString(minutos)+".db";
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
            
            String fecha = ObtenerFechaYHoraActual();
            
            //valores base combustibles
            stmt.execute("INSERT INTO combustible (nombre,costo,fecha_hora) VALUES ('93', 10, '"+fecha+"');");
            stmt.execute("INSERT INTO combustible (nombre,costo,fecha_hora) VALUES ('95', 10, '"+fecha+"');");
            stmt.execute("INSERT INTO combustible (nombre,costo,fecha_hora) VALUES ('97', 10, '"+fecha+"');");
            stmt.execute("INSERT INTO combustible (nombre,costo,fecha_hora) VALUES ('Diesel', 10, '"+fecha+"');");
            stmt.execute("INSERT INTO combustible (nombre,costo,fecha_hora) VALUES ('Kerosene', 10, '"+fecha+"');");
            
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
            conn = conectar("empresa.db");
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
                    combustible.save(conn2);
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
                    tra.save(conn2);
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
