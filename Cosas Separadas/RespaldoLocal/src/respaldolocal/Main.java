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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
        
        
        //Carpeta del usuario
        String dir = System.getProperty("user.dir");
        dir = dir+"\\bdrespaldo";
        
        System.out.println("Carpeta del usuario = " + dir);
        
        
        
        /*
        int hora, minutos;
        String finalNBD;
        Calendar calendario = Calendar.getInstance();
        
        hora =calendario.get(Calendar.HOUR_OF_DAY);
        minutos = calendario.get(Calendar.MINUTE);
        
        
        //conectar("estacion.db", "C:\\Users\\elyna\\Documents\\Universidad de Talca\\2019-2\\Sistemas Distribuidos\\Proyecto-2-Sistemas-Distribuidos\\Cosas Separadas\\RespaldoLocal\\");
        //conectar("empresa.db", "C:\\Users\\elyna\\Documents\\Universidad de Talca\\2019-2\\Sistemas Distribuidos\\Proyecto-2-Sistemas-Distribuidos\\Cosas Separadas\\RespaldoLocal\\");
        
        if(hora >= HORA_RESPALDO && minutos >= MINUTO_RESPALDO) {
            crearRespaldo(calendario);
            //Thread.sleep(30000);
            
        }else {
            int finalMin = (60*HORA_RESPALDO)+MINUTO_RESPALDO;
            int currentMin = (60*hora)+minutos;
            int finalSleep = (finalMin-currentMin)*60000;
            //sleep 
            //sleep(finalSleep);
        }*/

        
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
    
    
    /**
     * 
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
    
    
    public static Connection conectar(String bd) {
        Connection c = null;
        String dir = System.getProperty("user.dir");
        String url = dir+"\\"+bd;
        try {
           Class.forName("org.sqlite.JDBC");
           c = DriverManager.getConnection("jdbc:sqlite:"+url);
           System.out.println("Base de datos Conectada");
           if (c == null) {
               //abre el respaldo
               bd = ultimoRespaldo();
               if(bd.isEmpty()) {
                   url = dir+"\\";
                   //significa que no existe un respaldo al que atenerse. En tal caso, mejor crear la base de datos.
                   bd = "empresa.db";
                   c = crearBasedeDatos(bd, url);
                   if(crearTablas(c)) {
                        System.out.println("Tablas creadas.");
                    }

                    if(llenarDatos(c)) {
                        System.out.println("Datos respaldados");
                    }
                    return c;
               }
               else {
                   //si existe, hay que acceder a este
                   url = dir+"\\bdrespaldo\\"+bd;
                   c = DriverManager.getConnection("jdbc:sqlite:"+url);
                   System.out.println("Base de datos de respaldo conectada");
               }
           }
           else {
               //se deben sincronizar los datos.
               
               //buscar ultimo data log.
               String baseDeDatos = ultimaBasedeDatos();
               if(!baseDeDatos.equals(bd)) {
                   //debe hacerse la sincronización de datos. En este momento, los datos de la base de datos de respaldo son mucho más actuales que la base de datos actual, por lo que debe ser actualizada.
               }
               
               
           }
           
        } catch ( Exception e ) {
           System.err.println( e.getClass().getName() + ": " + e.getMessage() );
           return null;
        }
        
        return c;
   }
    
    /**
     * Guarda en una base de datos oculta, las operaciones de una bd en específico.
     * @param nombre nombre de la base de datos involucrada
     * @param proceso codigo de proceso (explicado en el informe)
     * @param s si el proceso fue o no exitoso
     */
    public static void logBd(String nombre, int proceso, int s) {
        String dir = System.getProperty("user.dir");
        String bd = "files.db";
        String url = dir+"\\"+bd;
        Connection logconn = null;
        Statement stmt = null;
        Calendar calendario = Calendar.getInstance();
        DateFormat formato = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        String fecha = formato.format(calendario);
        
        try {
           Class.forName("org.sqlite.JDBC");
           logconn = DriverManager.getConnection("jdbc:sqlite:"+url);
           System.out.println("Base de datos de respaldo conectado");
           logconn.setAutoCommit(false);
           stmt = logconn.createStatement();
           
           String sql = "INSERT INTO log (nombre,proceso,fecha) VALUES ('"+nombre+"',"+proceso+",'"+fecha+"');"; 
           stmt.executeUpdate(sql);
           
           stmt.close();
           logconn.commit();
           logconn.close();
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
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
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
     * Busca en la base de datos de registro lo último hecho.
     * @return el nombre de la ultima base de datos que realizó alguna acción.
     */
    public static String ultimaBasedeDatos() {
        String dir = System.getProperty("user.dir");
        String bd = "files.db";
        String url = dir+"\\"+bd;
        Connection logconn = null;
        Statement stmt = null;
        String nombre = new String();
        
        try {
           Class.forName("org.sqlite.JDBC");
           logconn = DriverManager.getConnection("jdbc:sqlite:"+url);
           System.out.println("Base de datos de respaldo conectado");
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
    
}
