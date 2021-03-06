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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


/**
 *
 * @author Liemind
 */
public class Proceso implements Runnable
{
    private int id;
    private ArrayList<Combustible> cc;
    
    public Proceso(int id) 
    {
        this.id = id;
        this.cc = new ArrayList<>();
    }
    
    @Override
    public void run() 
    {
        //Estacion estacion = crearEstacion("/25.16.98.11");
        //System.out.println("la id de la estacion 25.16.98.10 es: " + buscarEstacion("25.16.98.11"));
        
        if(id == 0) 
        {
            sucursal();
        }
        else if(id == 1) 
        {
            //sucursal();
            System.out.println("hola existo");
        }
    }

    /**
     * Obtiene de la base de datos los combustibles del servidor.
     * @return una lista de combustibles con sus precios respectivos.
     */
    public synchronized ArrayList<Combustible> obtenerCombustibles() {
        Statement stmt = null;
        try 
        {
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

        }
        catch(Exception e) 
        {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            return null;
        }
    }
    
    public synchronized Estacion crearEstacion(String nombreEs) {
        //String nombreE = nombreEs.split("/")[1];
        String nombreE = nombreEs;
        Statement stmt = null;
        Estacion estacion = null;
        try {
            Main.conn.setAutoCommit(false);
            stmt = Main.conn.createStatement();
            //consultar estacion

            //crear Estacion
            String sql = "INSERT INTO estacion (nombre) VALUES ('"+nombreE+"');"; 
            stmt.executeUpdate(sql);
            //por la id
                
            estacion = buscarEstacion(nombreE);
            System.out.println("la id de la estacion creada es: " + estacion.getId());

            //ESTACIONES
            sql = "INSERT INTO surtidor (id_estacion, id_numero_surtidor, transacciones) " +
                       "VALUES ("+estacion.getId()+",1,0 );";
            stmt.executeUpdate(sql);


            sql = "INSERT INTO surtidor (id_estacion, id_numero_surtidor, transacciones) " +
                       "VALUES ("+estacion.getId()+",2,0 );";
            stmt.executeUpdate(sql);


            sql = "INSERT INTO surtidor (id_estacion, id_numero_surtidor, transacciones) " +
                       "VALUES ("+estacion.getId()+",3,0 );";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO surtidor (id_estacion, id_numero_surtidor, transacciones) " +
                       "VALUES ("+estacion.getId()+",4,0 );";
            stmt.executeUpdate(sql);

            stmt.close();
            Main.conn.commit();
        } 
        catch ( SQLException e ) 
        {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return estacion;
    }
    
    public synchronized boolean existeEstacion(String nombreE) 
    {
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
                //System.out.println("Cantidad de ips: " + cantIP);
            }
            rs.close();
            stmt.close();
            Main.conn.commit();
            
            if (cantIP==0)
            {
                return false;
            }
        } 
        catch (Exception e) 
        {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return true;
    }
    
    
    public synchronized Estacion buscarEstacion(String nombreE) 
    {
        Statement stmt = null;
        Estacion estacion = null;
        
        try 
        {
            //consultar estacion
            String sql = "SELECT id, nombre FROM estacion WHERE nombre = '"+nombreE+"';";
            Main.conn.setAutoCommit(false);
            stmt = Main.conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);
            
            while (rs.next()) 
            {
                 estacion = new Estacion(rs.getString("nombre"),rs.getInt("id"));
                //System.out.println("id: " + idEstacion);
            }
            rs.close();
            stmt.close();
            Main.conn.commit();
        } 
        catch (Exception e) 
        {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return estacion;
    }
    
    public Estacion getEstacion(String nombreEs)
    {
        Estacion estacion = null;
        if(!existeEstacion(nombreEs))
        {
            estacion = crearEstacion(nombreEs);
        }
        else
        {
            estacion = buscarEstacion(nombreEs);
        }
        return estacion;
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
        
        try 
        {
            Main.conn.setAutoCommit(false);
            stmt = Main.conn.createStatement();
            //consultar estacion
            ResultSet rs = stmt.executeQuery("SELECT id FROM surtidor WHERE id_estacion = '"+nombreE+"' AND id_numero_surtidor = '"+surtidor+"';");
            
            while ( rs.next() ) {
               number = rs.getInt("id");
            }
            rs.close();
            stmt.close();
            Main.conn.commit();
            return number;
            
        } 
        catch (SQLException e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return 0;
    }
    
    public synchronized boolean crearTransaccion(int id_surtidor, int id_combustible, int litros, int costo) {
        Statement stmt = null;
         try {
            System.out.println("entro a crear transaccion");
            Main.conn.setAutoCommit(false);
   
            stmt = Main.conn.createStatement();
            String sql = "INSERT INTO transaccion (id_surtidor, id_combustible, litros, costo) " +
                           "VALUES ("+id_surtidor+", "+id_combustible+", "+litros+", "+costo+" );"; 
            stmt.executeUpdate(sql);
            stmt.close();
            Main.conn.commit();
            System.out.println("salio de crear transaccion");
            return true;
         } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
         }
         return false;
    }
    
    
    public void sucursal()
    {
        int cantTransacciones, id_surtidor, surtidor, id_combustible, litros, costo;
        String opcion, temporal;

        /**********************
         * CONEXION
         */
        try 
        {
            //InetAddress ip = InetAddress.getByName("25.6.57.186");
            DatagramSocket socket = new DatagramSocket(10500);
            DatagramPacket msjEntrada, msjSalida;
            Estacion estacion;
            while(true)
            {
                byte[] bufferEntrada, bufferSalida;

                //recibe la conexion usando un status de conexion
                bufferEntrada = new byte[1000];
                msjEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length);
                socket.receive(msjEntrada);
                
                opcion = new String(bufferEntrada);
                opcion = opcion.trim();
                System.out.println("recibe opcion: " + opcion);
                
                switch(opcion)
                {
                    case "1":
                        //obtener combustible
                        cc = obtenerCombustibles();

                        //envia al usuario los precios actuales del combustible EN ORDEN
                        temporal = null;
                        temporal = Integer.toString(cc.get(0).getCosto())+","+Integer.toString(cc.get(1).getCosto())+","+Integer.toString(cc.get(2).getCosto())+","+Integer.toString(cc.get(3).getCosto())+","+Integer.toString(cc.get(4).getCosto());
                        System.out.println("t: "+temporal);
                        
                        bufferSalida = temporal.getBytes();
                        msjSalida = new DatagramPacket(bufferSalida, bufferSalida.length, msjEntrada.getAddress(), msjEntrada.getPort());
                        socket.send(msjSalida);

                        System.out.println("envio combustibles: " + msjEntrada.getAddress());
                        break;
                    case "2":
                        
                        /***
                        * INICIO RECIBIR TRANSACCIONES
                        ***/
                        System.out.println("entro a guardar transaccion");
                        //confirma recepcion de opcion
                        temporal = "ok";
                        bufferSalida = temporal.getBytes();
                        msjSalida = new DatagramPacket(bufferSalida, bufferSalida.length, msjEntrada.getAddress(), msjEntrada.getPort());
                        socket.send(msjSalida);
                        
                        bufferEntrada = new byte[1000];
                        msjEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length); 
                        socket.receive(msjEntrada);
                        
                        //obtiene la estacion para recibir la transacción
                        String nombreEmpresa = msjEntrada.getAddress().toString();
                        estacion = getEstacion(nombreEmpresa);
                        System.out.println("obtiene la estacion: " + estacion.getNombre());
                        
                        //obtiene el surtidor y la cantidad de transacciones
                        temporal = new String(bufferEntrada);
                        temporal = temporal.trim();
                        System.out.println("surtidor y cant. transacciones: " + temporal);
                        
                        String[] arrtrans = temporal.split(",", 2);
                        surtidor = Integer.parseInt(arrtrans[0]);
                        cantTransacciones = Integer.parseInt(arrtrans[1]);
                        
                        //guardar cantidad transacciones
                        actualizarTransacciones(estacion.getId(), surtidor, cantTransacciones);
                        System.out.println("actuliza transacciones");
                        id_surtidor = buscarIdSurtidor(nombreEmpresa, surtidor);
                        
                        //confirma recepcion de opcion
                        temporal = "ok";
                        bufferSalida = temporal.getBytes();
                        msjSalida = new DatagramPacket(bufferSalida, bufferSalida.length, msjEntrada.getAddress(), msjEntrada.getPort());
                        socket.send(msjSalida);
                        
                        /*Obtiene la transaccion*/
                        bufferEntrada = new byte[1000];
                        msjEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length); 
                        socket.receive(msjEntrada);

                        temporal = new String(bufferEntrada);
                        temporal = temporal.trim();
                        System.out.println("datos transaccion: " + temporal);
                        arrtrans = temporal.split(",");
                        id_combustible = Integer.parseInt(arrtrans[0]);
                        litros = Integer.parseInt(arrtrans[1]);
                        costo = Integer.parseInt(arrtrans[2]);
                        
                        if(crearTransaccion(id_surtidor, id_combustible, litros, costo)) {
                            System.out.println("transacción guardada");
                        }
                        //confirma recepcion de opcion
                        temporal = "ok";
                        bufferSalida = temporal.getBytes();
                        msjSalida = new DatagramPacket(bufferSalida, bufferSalida.length, msjEntrada.getAddress(), msjEntrada.getPort());
                        socket.send(msjSalida);
                        break;
                }
            }
        } 
        catch (IOException | NumberFormatException e) 
        {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        /**********************
         * CONEXION
         */
    }
}
