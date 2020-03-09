/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

import Model.Combustible;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;


/**
 *
 * @author Liemind
 */
public class Proceso implements Runnable{
    private int id;
    private ArrayList<Combustible> cc;
    private int combustibleId;
    private int litros;
    private int costo;
    
    public Proceso(int id) {
        this.id = id;
    }
    
    @Override
    public void run() {
        if(id == 0) {
            int surtidor, status = 0, transacciones;
            int n3,n5,n7,die,kero;
            /**********************
             * CONEXION
             */
            try {
                DatagramSocket socket = new DatagramSocket();
                InetAddress ip = InetAddress.getByName("25.6.57.186");
                //enviar si hay o no transacción
                int numero = Main.status;
                String numeroString = Integer.toString(numero);
                byte[] bufferSalida = numeroString.getBytes();
                DatagramPacket msjSalida = new DatagramPacket(bufferSalida, bufferSalida.length,ip, 10500);
                socket.send(msjSalida);
                
                if(numero == 1) {
                    //cantidad de transacciones
                    transacciones = cantidadTransacciones(Main.surtidor);
                    numeroString = Integer.toString(transacciones);
                    bufferSalida = numeroString.getBytes();
                    msjSalida = new DatagramPacket(bufferSalida, bufferSalida.length,ip, 10500);
                    socket.send(msjSalida);
                    
                    
                    //numero del surtidor
                    surtidor = Main.surtidor;
                    numeroString = Integer.toString(surtidor);
                    bufferSalida = numeroString.getBytes();
                    msjSalida = new DatagramPacket(bufferSalida, bufferSalida.length,ip, 10500);
                    socket.send(msjSalida);
                    
                    buscarUltimaTransaccion();
                    //id combustible
                    numeroString = Integer.toString(combustibleId);
                    bufferSalida = numeroString.getBytes();
                    msjSalida = new DatagramPacket(bufferSalida, bufferSalida.length,ip, 10500);
                    socket.send(msjSalida);
                    
                    //litros
                    numeroString = Integer.toString(litros);
                    bufferSalida = numeroString.getBytes();
                    msjSalida = new DatagramPacket(bufferSalida, bufferSalida.length,ip, 10500);
                    socket.send(msjSalida);
                    
                    //costo
                    numeroString = Integer.toString(costo);
                    bufferSalida = numeroString.getBytes();
                    msjSalida = new DatagramPacket(bufferSalida, bufferSalida.length,ip, 10500);
                    socket.send(msjSalida);
                }
                
                //consultar si hay cambios actuales en el combustible
                byte[] bufferEntrada = new byte[1000];
                DatagramPacket msjEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length);
                socket.receive(msjEntrada);
                numeroString = new String(bufferEntrada);
                numeroString = numeroString.trim();
                status = Integer.parseInt(numeroString);
                
                if(status == 1) {
                    //recibir cambios en el combustible
                    
                    //93
                    bufferEntrada = new byte[1000];
                    msjEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length);
                    socket.receive(msjEntrada);
                    numeroString = new String(bufferEntrada);
                    numeroString = numeroString.trim();
                    n3 = Integer.parseInt(numeroString);
                    
                    //95
                    bufferEntrada = new byte[1000];
                    msjEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length);
                    socket.receive(msjEntrada);
                    numeroString = new String(bufferEntrada);
                    numeroString = numeroString.trim();
                    n5 = Integer.parseInt(numeroString);
                    
                    //97
                    bufferEntrada = new byte[1000];
                    msjEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length);
                    socket.receive(msjEntrada);
                    numeroString = new String(bufferEntrada);
                    numeroString = numeroString.trim();
                    n7 = Integer.parseInt(numeroString);
                    
                    //diesel
                    bufferEntrada = new byte[1000];
                    msjEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length);
                    socket.receive(msjEntrada);
                    numeroString = new String(bufferEntrada);
                    numeroString = numeroString.trim();
                    die = Integer.parseInt(numeroString);
                    
                    //kerosene
                    bufferEntrada = new byte[1000];
                    msjEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length);
                    socket.receive(msjEntrada);
                    numeroString = new String(bufferEntrada);
                    numeroString = numeroString.trim();
                    kero = Integer.parseInt(numeroString);
                    
                    //guardar cambios
                    guardarCombustible(n3, n5, n7, die, kero);
                }

                
            } catch (IOException e) {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            }
            
            /**********************
             * CONEXION
             */
        }
        else if(id == 1) {
            System.out.println("Proceso 1");
        }
    }
    
    public synchronized int cantidadTransacciones(int id) {
        int trans = 0;
         
        //seleccionar el surtidor
        Statement stmt = null;
        try {
            Main.conn.setAutoCommit(false);
            stmt = Main.conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT transacciones FROM surtidor WHERE id = "+id+";");

            while ( rs.next() ) {
               trans = rs.getInt("transacciones");
            }
            
            rs.close();
            Main.conn.commit();
            stmt.close();
            return trans;

        }catch(Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        return 0;
    }
    
    public synchronized void buscarUltimaTransaccion() {
        //seleccionar el surtidor
        Statement stmt = null;
        try {
            Main.conn.setAutoCommit(false);
            stmt = Main.conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM transaccion ORDER BY id DESC LIMIT 1;");

            while ( rs.next() ) {
               combustibleId = rs.getInt("id_combustible");
               litros = rs.getInt("litros");
               costo = rs.getInt("costo");
            }
            
            rs.close();
            Main.conn.commit();
            stmt.close();
        }catch(Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        } 
    }
    
    public synchronized void guardarCombustible(int n3, int n5, int n7, int die, int kero) {
        ArrayList<Combustible> arr = new ArrayList<Combustible>();
        Combustible c;
        c = new Combustible("93", n3);
        c.setId(1);
        arr.add(c);
        
        c = new Combustible("95", n5);
        c.setId(2);
        arr.add(c);
        
        c = new Combustible("97", n7);
        c.setId(3);
        arr.add(c);
        
        c = new Combustible("Diesel", die);
        c.setId(4);
        arr.add(c);
        
        c = new Combustible("Kerosene", kero);
        c.setId(5);
        arr.add(c);
        
        //save all
        for(Combustible comb : arr) {
            if(comb.save(Main.conn)) {
                System.out.println("La actualizacion de "+comb.getNombre()+" fue hecha con éxito");
            }
            else {
                System.out.println("La actualizacion de "+comb.getNombre()+" no pudo realizarse");
            }
        }
    }
}
