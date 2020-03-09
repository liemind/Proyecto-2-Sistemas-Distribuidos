/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


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
                InetAddress ip = InetAddress.getByName("25.6.57.186");
                DatagramSocket socket = new DatagramSocket(10500);
                int suma = 0;

                while(true)
                {
                    byte[] bufferEntrada = new byte[1000];
                    DatagramPacket msjEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length); 
                    socket.receive(msjEntrada);

                    String numeroString = new String(bufferEntrada);
                    numeroString = numeroString.trim();
                    int numero = Integer.parseInt(numeroString);
                    suma +=numero;

                    System.out.println("El numero acumulado es: " + suma);

                    numeroString = Integer.toString(suma);


                    byte[] bufferSalida = numeroString.getBytes();
                    DatagramPacket msjSalida = new DatagramPacket(bufferSalida, bufferSalida.length, msjEntrada.getAddress(), msjEntrada.getPort());
                    socket.send(msjSalida);
                }
            } catch (IOException | NumberFormatException e) {
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
