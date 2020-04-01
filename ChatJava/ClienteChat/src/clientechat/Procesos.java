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
                System.out.println("deberia hacerse una sincronizacion aca");
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

    /**
     * Abre la base de datos de respaldo.
     * @param bd
     * @param dir
     * @return
     */
    public static synchronized Connection abrirRespaldo(String bd, String dir) {
        try {
            //abre el respaldo
            bd = ultimoRespaldo();
            System.out.println("Abriendo respaldo");
            String url = dir+"\\bdrespaldo\\"+bd;
            Connection conn = DriverManager.getConnection("jdbc:sqlite:"+url);
            System.out.println("Base de datos de respaldo conectada");
            //registro de la bd
            logBd(bd); 
            return conn;
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            return null;
        }
    }

    /**
     * Sincroniza la base de datos actual con la anterior.
     * @param actualBD
     * @param anteriorBD
     * @param actualConn
     */
    public static synchronized boolean Sincronizacion(String actualBD, String anteriorBD, Connection actualConn) {
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
                Transaccion t = obtenerUltimaTransaccion(connAnterior);
                
                //3. consultar cantidad de transacciones con fecha superior a la de la ultima transaccion
                stmtAnterior = connAnterior.createStatement();
                String sql = "SELECT * FROM transaccion WHERE  date(fecha_hora) >= date('"+t.getFechaHora()+"');";
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
    
    /**
     * Guarda en la base de datos local una transacción
     * @param idSurtidor
     * @param idCombustible
     * @param litros
     * @param fecha_hora
     * @param costo
     * @return
     */
    public static int CrearTransaccion(int idSurtidor, int idCombustible, int litros, int costo, String fecha_hora) {
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

    /**
     * Obtiene los datos de una tranacción segun su ID.
     * @param id id de la transacción.
     * @return un string comprimido con los datos de la transacción.
     */
    public static String ObtenerTransaccion(int id) {
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
     * Según el nombre de un combustible, busca este en un arreglo global.
     * @param n el nombre del combustible a enviar.
     * @return un objeto de tipo combustible.
     */
    public static synchronized Combustible BuscarCombustible(String n)
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
     * @return El arraylist de combustible.
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
    
    
    /**
     * Obtiene la fecha y hora actual en el servidor.
     * @return un string con la fecha y hora según el formato establecido.
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
     * Busca entre los respaldos y retorna el mas reciente.
     * @return Nombre de la base de datos del ultimo respaldo.
     */
    public static synchronized String ultimoRespaldo() {
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
    public static synchronized void crearRespaldoCliente(Calendar calendario) {
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
        logBd(finalNBD);
        //fin crear log
        
    }
    
    /**
     * Crea una base de datos dado el nombre y la ruta.
     * @param bd
     * @param ruta
     * @return 
     */
    public static synchronized Connection crearBasedeDatos(String bd, String ruta) {
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
    public static synchronized boolean crearTablas(Connection conn2) {
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
    public static synchronized boolean llenarDatos(Connection conn2) {
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
                    //tra.save(conn2);
                    System.out.println("T: "+tra.getId());
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
