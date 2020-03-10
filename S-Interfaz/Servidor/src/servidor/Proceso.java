/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import Model.Combustible;
import Model.Estacion;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


/**
 *
 * @author Liemind
 */
public class Proceso implements Runnable{
    private int id;
    private ArrayList<Combustible> cc;
    
    public Proceso(int id) {
        this.id = id;
        this.cc = new ArrayList<>();
    }
    
    @Override
    public void run() {
        //Estacion estacion = crearEstacion("/25.16.98.11");
        //System.out.println("la id de la estacion 25.16.98.10 es: " + buscarEstacion("25.16.98.11"));
        
        if(id == 0) {
            int cantTransacciones, id_surtidor, surtidor, id_combustible, litros, costo, status;
            String temporal;
            
            /**********************
             * CONEXION
             */
            try {
                InetAddress ip = InetAddress.getByName("25.95.144.139");
                DatagramSocket socket = new DatagramSocket(10500);
                DatagramPacket msjEntrada, msjSalida;
                int suma = 0;
                 
                    byte[] bufferEntrada, bufferSalida;
                    
                    //recibe la conexion usando un status de conexion
                    bufferEntrada = new byte[1000];
                    msjEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length); 
                    socket.receive(msjEntrada);
                    temporal = new String(bufferEntrada);
                    temporal = temporal.trim();
                    Main.status = Integer.parseInt(temporal);
                    
                    System.out.println("Status: "+Main.status);
                    
                    if (Main.status == 1) {
                        //hay conexion
                        
                        //envia al usuario los precios actuales del combustible EN ORDEN
                        temporal = new String();
                        temporal = cc.get(0)+","+cc.get(1)+","+cc.get(2)+","+cc.get(3)+","+cc.get(4);
                        bufferSalida = temporal.getBytes();
                        msjSalida = new DatagramPacket(bufferSalida, bufferSalida.length,ip, 10500);
                        socket.send(msjSalida);
                        
                        //crea la estacion para recibir la transacción
                        String nombreEmpresa = msjEntrada.getAddress().toString();
                        Estacion estacion = crearEstacion(nombreEmpresa);
                        
                        //pregunta al usuario si hay transacciones
                        bufferEntrada = new byte[1000];
                        msjEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length); 
                        socket.receive(msjEntrada);
                        temporal = new String(bufferEntrada);
                        temporal = temporal.trim();
                        int transacciones = Integer.parseInt(temporal);
                        
                        if(transacciones == 1) {
                            // cantidad de transacciones
                            bufferEntrada = new byte[1000];
                            socket.receive(msjEntrada);
                            temporal = new String(bufferEntrada);
                            temporal = temporal.trim();
                            
                            String[] arrtrans = temporal.split(",", 2);
                            surtidor = Integer.parseInt(arrtrans[0]);
                            cantTransacciones = Integer.parseInt(arrtrans[1]);
                            //guardar cantidad transacciones
                            actualizarTransacciones(estacion.getId(), surtidor, cantTransacciones);
                            
                            /***
                            * INICIO RECIBIR TRANSACCIONES
                            ***/
                            id_surtidor = buscarIdSurtidor(nombreEmpresa, surtidor);
                            
                            bufferEntrada = new byte[1000];
                            socket.receive(msjEntrada);
                            temporal = new String(bufferEntrada);
                            temporal = temporal.trim();
                            arrtrans = temporal.split(",");
                            id_combustible = Integer.parseInt(arrtrans[0]);
                            litros = Integer.parseInt(arrtrans[1]);
                            costo = Integer.parseInt(arrtrans[2]);
                            if(crearTransaccion(id_surtidor, id_combustible, litros, costo)) {
                                System.out.println("transacción guardada");
                            }
                        }
                        
                    }
            } catch (IOException | NumberFormatException e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            }
            /**********************
             * CONEXION
             */
            }
        else if(id == 1) {
            cc = obtenerCombustibles();
        }
    }

    /**
     * Obtiene de la base de datos los combustibles del servidor.
     * @return una lista de combustibles con sus precios respectivos.
     */
    public synchronized ArrayList<Combustible> obtenerCombustibles() {
        Statement stmt = null;
        try {
            ArrayList<Combustible> combustibles = new ArrayList<>();
            stmt = Main.conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM combustible;");

            while ( rs.next() ) {
               Combustible combustible = new Combustible( rs.getString("nombre") , rs.getInt("costo") );
               combustible.setId(rs.getInt("id"));
               combustibles.add(combustible);
            }
            //end bandera
            rs.close();
            stmt.close();
            return combustibles;

        }catch(Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            return null;
        }
    }
    
    public synchronized Estacion crearEstacion(String nombreEs) {
        String nombreE = nombreEs.split("/")[1];
        Statement stmt = null;
        int idEstacion = 0;
        Estacion es = null;
        try {
            Main.conn.setAutoCommit(false);
            stmt = Main.conn.createStatement();
            //consultar estacion
            if(existeEstacion(nombreE)) {
                System.out.println("entro a crear la ip");
                //crear Estacion
                String sql = "INSERT INTO estacion (nombre) VALUES ('"+nombreE+"');"; 
                stmt.executeUpdate(sql);
                System.out.println("Paso consulta -1");
                //por la id
                
                idEstacion = buscarEstacion(nombreE);
                System.out.println("la id de la estacion creada es: " + idEstacion);
                
                //ESTACIONES
                sql = "INSERT INTO surtidor (id_estacion, id_numero_surtidor, transacciones) " +
                           "VALUES ("+idEstacion+",1,0 );";
                stmt.executeUpdate(sql);
                
                
                sql = "INSERT INTO surtidor (id_estacion, id_numero_surtidor, transacciones) " +
                           "VALUES ("+idEstacion+",2,0 );";
                stmt.executeUpdate(sql);
                
                
                sql = "INSERT INTO surtidor (id_estacion, id_numero_surtidor, transacciones) " +
                           "VALUES ("+idEstacion+",3,0 );";
                stmt.executeUpdate(sql);
                
                sql = "INSERT INTO surtidor (id_estacion, id_numero_surtidor, transacciones) " +
                           "VALUES ("+idEstacion+",4,0 );";
                stmt.executeUpdate(sql);
                
                es = new Estacion(nombreE);
                es.setId(idEstacion);
                System.out.println("Fin if");
            }
            
            stmt.close();
            Main.conn.commit();
            System.out.println("salio de crear estacion");
            return es;
        } 
        catch ( SQLException e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return null;
    }
    
    public synchronized boolean existeEstacion(String nombreE) {
        Statement stmt = null;
        try {
            
            //consultar estacion
            String sql = "SELECT count(*) FROM estacion WHERE nombre = '"+nombreE+"';";
            Main.conn.setAutoCommit(false);
            stmt = Main.conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);
            
            int cantIP = 0; /*Representa a la cantidad de ip's que esta en la base de datos que son iguales a la nombreE*/
            while(rs.next()) {
                cantIP = rs.getInt("count(*)");
                System.out.println("Cantidad de ips: " + cantIP);
            }
            rs.close();
            stmt.close();
            Main.conn.commit();
            
            if (cantIP==0)
            {
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return false;
    }
    
    
    public synchronized int buscarEstacion(String nombreE) {
        Statement stmt = null;
        int idEstacion = 0;
        
        try {
            
            //consultar estacion
            String sql = "SELECT id, nombre FROM estacion WHERE nombre = '"+nombreE+"';";
            Main.conn.setAutoCommit(false);
            stmt = Main.conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);
            
            while (rs.next()) {
                idEstacion = rs.getInt("id");
                System.out.println("id: " + idEstacion);
            }
            rs.close();
            stmt.close();
            System.out.println("id2.0: " + idEstacion);
            Main.conn.commit();
            return idEstacion;

            // loop through the result set

            /*while (rs.next()) {
                n = rs.getString("nombre");
                System.out.println("n: " + n + " id: " + rs.getInt("id"));
            }*/
            //System.out.println("n: " + n + " id: " + rs.getInt("id"));

            
            /*ResultSet rs = stmt.executeQuery("SELECT * FROM estacion WHERE nombre = '"+nombreE+"';");
            System.out.println("ResultSet: " + rs.getInt("id"));
            System.out.println("paso la consulta de buscarEstacion");
            while ( rs.next() ) {
               n = rs.getString("nombre");
            }*/
            
            /*if(!n.isEmpty()) {
                number = rs.getInt("id");
            }*/
            
            
            
            
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        System.out.println("paso por fuera");
        return -1;
    }
    
    
    
    
    
    
    
    public synchronized boolean actualizarTransacciones(int id, int surtidor, int trans) {
        Statement stmt = null;
        try {
            Main.conn.setAutoCommit(false);
            stmt = Main.conn.createStatement();
            
            //guardar la transaccion
            stmt.executeUpdate("UPDATE surtidor SET transacciones = "+trans+" WHERE id_estacion = "+id+" AND id_numero_surtidor = "+surtidor+";");
            Main.conn.commit();
            stmt.close();
            return true;
        }
        catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return false;
    }
    
    public synchronized int buscarIdSurtidor(String nombreE, int surtidor) {
        Statement stmt = null;
        int number = 0;
        
        try {
            Main.conn.setAutoCommit(false);
            stmt = Main.conn.createStatement();
            //consultar estacion
            ResultSet rs = stmt.executeQuery("SELECT id FROM surtidor WHERE id_estacion = '"+nombreE+"' AND id_numero_surtidor "+surtidor+";");
            
            while ( rs.next() ) {
               number = rs.getInt("id");
            }
            rs.close();
            stmt.close();
            Main.conn.commit();
            return number;
            
        } catch (SQLException e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return 0;
    }
    
    public synchronized boolean crearTransaccion(int id_surtidor, int id_combustible, int litros, int costo) {
        Statement stmt = null;
         try {
            Main.conn.setAutoCommit(false);
   
            stmt = Main.conn.createStatement();
            String sql = "INSERT INTO transaccion (id_surtidor, id_combustible, litros, costo) " +
                           "VALUES ("+id_surtidor+", "+id_combustible+", "+litros+", "+costo+" );"; 
            stmt.executeUpdate(sql);
            stmt.close();
            Main.conn.commit();
            return true;
         } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
         }
         return false;
    }
}
