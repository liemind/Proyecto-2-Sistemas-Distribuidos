/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;


/**
 *
 * @author Liemind
 */
public class Proceso implements Runnable{
    private int id;
    
    public Proceso(int id) {
        this.id = id;
    }
    
    @Override
    public void run() {
        if(id == 0) {
            /**********************
             * CONEXION
             */
            try {
                DatagramSocket socket = new DatagramSocket();
                InetAddress ip = InetAddress.getByName("localhost");
                Random random = new Random();
                int numero = random.nextInt(100) + 1;
                System.out.println("numero es: " + numero);
                String numeroString = Integer.toString(numero);

                byte[] bufferSalida = numeroString.getBytes();


                DatagramPacket msjSalida = new DatagramPacket(bufferSalida, bufferSalida.length,ip, 10500);

                socket.send(msjSalida);

                byte[] bufferEntrada = new byte[1000];
                DatagramPacket msjEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length);
                socket.receive(msjEntrada);

            System.out.println("El numero acumulado es: " + new String(bufferEntrada));
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
    
    
    
}
