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
import java.util.List;


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
            int surtidor, status = 0;
            int n3,n5,n7,die,kero;
            /**********************
             * CONEXION
             */
            try {
                DatagramSocket socket = new DatagramSocket();
                InetAddress ip = InetAddress.getByName("25.6.57.186");
                
                /*Para iniciar la conexion con el servidor se envia un 1*/
                String conectar = "1";
                byte[] bufferSalida = conectar.getBytes();
                DatagramPacket msjSalida = new DatagramPacket(bufferSalida, bufferSalida.length, ip, 10500);
                socket.send(msjSalida);
                System.out.println("envio con exito conexion");
                
                //Recibe combustible
                byte[] bufferEntrada = new byte[1000];
                DatagramPacket msjEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length);
                socket.receive(msjEntrada);
                System.out.println("recibio las bencinas");
                String datosBrutos = new String(bufferEntrada);
                datosBrutos=datosBrutos.trim();
                String[] datosString = datosBrutos.split(",");
                ArrayList<Integer> datos = new ArrayList<>();
                
                for (int i = 0; i < datosString.length; i++) 
                {
                    datos.add(Integer.parseInt(datosString[i]));
                    System.out.println("bencina: " + datosString[i]);
                }
                boolean validar = true;
                while(validar)
                {
                    //envia "1" si hay transaccion o "0" si no lo hay
                    String estadoTransaccion = Integer.toString(Main.status);
                    System.out.println("estatus: " + estadoTransaccion);
                    if(estadoTransaccion.equals("2")) 
                    {
                        bufferSalida = estadoTransaccion.getBytes();
                        msjSalida = new DatagramPacket(bufferSalida, bufferSalida.length,ip, 10500);
                        socket.send(msjSalida);
                        System.out.println("Envio la opcion de ingreso ingresar transaccion");
                        
                        bufferEntrada = new byte[1000];
                        msjEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length);
                        socket.receive(msjEntrada);
                        System.out.println("Confirma recpcion de opcion");
                        
                        //envia numero del surtidor y la cantidad de transacciones
                        datosBrutos = Integer.toString(Main.surtidor)+","+Integer.toString(cantidadTransacciones(Main.surtidor));
                        bufferSalida = datosBrutos.getBytes();
                        msjSalida = new DatagramPacket(bufferSalida, bufferSalida.length,ip, 10500);
                        socket.send(msjSalida);

                        bufferEntrada = new byte[1000];
                        msjEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length);
                        socket.receive(msjEntrada);
                        System.out.println("Confirma recpcion de numero del surtidor y cantidad de transacciones");
                        
                        
                        buscarUltimaTransaccion();
                        System.out.println("busca transaccion");
                        /*envia el id del combustible, litros y costo*/
                        datosBrutos = Integer.toString(combustibleId)+","+Integer.toString(litros)+","+Integer.toString(costo);
                        System.out.println("datosBrutos: " + datosBrutos);
                        bufferSalida = datosBrutos.getBytes();
                        msjSalida = new DatagramPacket(bufferSalida, bufferSalida.length,ip, 10500);
                        socket.send(msjSalida);
                        
                        System.out.println("envia transaccion");
                        validar = false;
                        
                        bufferEntrada = new byte[1000];
                        msjEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length);
                        socket.receive(msjEntrada);
                        System.out.println("Confirma recpcion del guardado de los datos de la bd principal");
                    }
                }
                
                
                
            } 
            catch (IOException e) 
            {
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            }
            
            /**********************
             * CONEXION
             */
        }
        else if(id == 1) 
        {
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
    
    public synchronized void guardarCombustible(ArrayList<Integer> datos) {
        ArrayList<Combustible> arr = new ArrayList<Combustible>();
        Combustible c;
        int i, tipoCombustible = 93;
        for(i=0; i<5;i++)
        {
            if(i<3)
            {
                c = new Combustible(Integer.toString(tipoCombustible), datos.get(i));
                tipoCombustible+=2;
            }
            else if(i==3)
            {
                c = new Combustible("Diesel", datos.get(i));
            }
            else
            {
                c = new Combustible("Kerosene", datos.get(i));
            }
            
            c.setId(i+1);
            arr.add(c);
        }
        
        //save all
        for(Combustible comb : arr) 
        {
            if(comb.save(Main.conn)) {
                System.out.println("La actualizacion de "+comb.getNombre()+" fue hecha con éxito");
            }
            else {
                System.out.println("La actualizacion de "+comb.getNombre()+" no pudo realizarse");
            }
        }
    }
}
