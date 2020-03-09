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
        if(id == 0) {
            int cantTransacciones, id_surtidor, surtidor, id_combustible, litros, costo;
            /**********************
             * CONEXION
             */
            try {
                InetAddress ip = InetAddress.getByName("25.6.57.186");
                DatagramSocket socket = new DatagramSocket(10500);
                int suma = 0;

                while(true)
                {                    
                    byte[] bufferEntrada, bufferSalida;
                    
                    bufferEntrada = new byte[1000];
                    DatagramPacket msjEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length); 
                    socket.receive(msjEntrada);
                    String temporal = new String(bufferEntrada);
                    temporal = temporal.trim();
                    //pregunta al usuario si hay transacciones
                    int transacciones = Integer.parseInt(temporal);
                    
                    //crea la estacion para recibir la transacción
                    String nombreEmpresa = msjEntrada.getAddress().toString();
                    Estacion estacion = crearEstacion(nombreEmpresa);
                    
                    if(transacciones == 1) {
                        // cantidad de transacciones
                        bufferEntrada = new byte[1000];
                        socket.receive(msjEntrada);
                        temporal = new String(bufferEntrada);
                        temporal = temporal.trim();
                        cantTransacciones = Integer.parseInt(temporal);
                        
                        //numero del surtidor ese
                        bufferEntrada = new byte[1000];
                        socket.receive(msjEntrada);
                        temporal = new String(bufferEntrada);
                        temporal = temporal.trim();
                        surtidor = Integer.parseInt(temporal);
                        
                        //guardar cantidad transacciones
                        actualizarTransacciones(estacion.getId(), surtidor, cantTransacciones);
                        
                        /***
                         * INICIO RECIBIR TRANSACCIONES
                        ***/
                        id_surtidor = buscarIdSurtidor(nombreEmpresa, surtidor);
                        
                        // id combustible
                        bufferEntrada = new byte[1000];
                        socket.receive(msjEntrada);
                        temporal = new String(bufferEntrada);
                        temporal = temporal.trim();
                        id_combustible = Integer.parseInt(temporal);
                        
                        // litros
                        bufferEntrada = new byte[1000];
                        socket.receive(msjEntrada);
                        temporal = new String(bufferEntrada);
                        temporal = temporal.trim();
                        litros = Integer.parseInt(temporal);
                        
                        // costo
                        bufferEntrada = new byte[1000];
                        socket.receive(msjEntrada);
                        temporal = new String(bufferEntrada);
                        temporal = temporal.trim();
                        costo = Integer.parseInt(temporal);
                        
                        if(crearTransaccion(id_surtidor, id_combustible, litros, costo)) {
                            System.out.println("transacción guardada");
                        }
                        
                    }
                    
                    
                    
                    //oremos
                    if(Main.status == 1) {
                        //envia al usuario los precios actuales del combustible EN ORDEN
                        for (int i = 0; i < cc.size(); i++) {
                            bufferSalida = Integer.toString( cc.get(i).getCosto() ).getBytes();
                            DatagramPacket msjSalida = new DatagramPacket(bufferSalida, bufferSalida.length, msjEntrada.getAddress(), msjEntrada.getPort());
                            socket.send(msjSalida);
                        }
                        Main.status = 0;
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
    
    public synchronized Estacion crearEstacion(String nombreE) {
        Statement stmt = null;
        int n = 0;
        Estacion es = null;
        try {
            Main.conn.setAutoCommit(false);
            stmt = Main.conn.createStatement();
            //consultar estacion
            
            n = buscarEstacion(nombreE);
            if(n == 0) {
                //crear Estacion
                String sql = "INSERT INTO estacion (nombre) " +
                               "VALUES ('"+nombreE+"' );"; 
                stmt.executeUpdate(sql);
                //por la id
                n = buscarEstacion(nombreE);
                
                
                //ESTACIONES
                sql = "INSERT INTO surtidor (id_estacion, id_numero_surtidor, transacciones) " +
                           "VALUES ("+n+",1,0 );";
                stmt.executeUpdate(sql);
                
                sql = "INSERT INTO surtidor (id_estacion, id_numero_surtidor, transacciones) " +
                           "VALUES ("+n+",2,0 );";
                stmt.executeUpdate(sql);
                
                sql = "INSERT INTO surtidor (id_estacion, id_numero_surtidor, transacciones) " +
                           "VALUES ("+n+",3,0 );";
                stmt.executeUpdate(sql);
                
                sql = "INSERT INTO surtidor (id_estacion, id_numero_surtidor, transacciones) " +
                           "VALUES ("+n+",4,0 );";
                stmt.executeUpdate(sql);
                
                es = new Estacion(nombreE);
                es.setId(n);
            }
            
            stmt.close();
            Main.conn.commit();
            return es;
        } 
        catch ( SQLException e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return null;
    }
    
    public synchronized int buscarEstacion(String nombreE) {
        Statement stmt = null;
        String n = new String();
        int number = 0;
        
        try {
            Main.conn.setAutoCommit(false);
            stmt = Main.conn.createStatement();
            //consultar estacion
            ResultSet rs = stmt.executeQuery("SELECT * FROM estacion WHERE nombre = '"+nombreE+"';");
            
            while ( rs.next() ) {
               n = rs.getString("nombre");
            }
            
            if(!n.isEmpty()) {
                number = rs.getInt("id");
            }
            rs.close();
            stmt.close();
            Main.conn.commit();
            return number;
            
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return 0;
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
            ResultSet rs = stmt.executeQuery("SELECT id FROM surtidor WHERE id_estacion = "+nombreE+" AND id_numero_surtidor "+surtidor+";");
            
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
