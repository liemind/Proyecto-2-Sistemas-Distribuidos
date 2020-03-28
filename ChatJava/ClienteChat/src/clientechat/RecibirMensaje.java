/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientechat;

import Model.Combustible;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 *
 * @author yorch
 */
public class RecibirMensaje extends Thread
{
    private Logger log = Logger.getLogger(ClienteChat.class);
    private Socket socket;
    private int puerto;
    private String host;
    private String usuario;
    
    public RecibirMensaje(Socket socket, int puerto, String host, String usuario)
    {
        this.socket = socket;
        this.puerto = puerto;
        this.host = host;
        this.usuario = usuario;
    }

    @Override
    public void run()
    {
        RecibirMensajesServidor();
    }

    /**
     * Recibe los mensajes del chat reenviados por el servidor
     */
    public void RecibirMensajesServidor()
    {
        // Obtiene el flujo de entrada del socket
        DataInputStream entradaDatos = null;
        String mensaje;
        try
        {
            entradaDatos = new DataInputStream(socket.getInputStream());
        }
        catch (IOException ex)
        {
            log.error("Error al crear el stream de entrada: " + ex.getMessage());
        }
        catch (NullPointerException ex)
        {
            log.error("El socket no se creo correctamente. ");
        }

        // Bucle infinito que recibe mensajes del servidor
        boolean conectado = true;
        while (conectado)
        {
            try
            {
                mensaje = entradaDatos.readUTF();
                guardarCombustible(mensaje);
            }
            catch (IOException ex)
            {
                log.error("Error al leer del stream de entrada: " + ex.getMessage());
                conectado = false;
            }
            catch (NullPointerException ex)
            {
                log.error("El socket no se creo correctamente. ");
                conectado = false;
            }
        }
    }
    
    public void guardarCombustible(String precios)//public synchronized void guardarCombustible(String precios)
    {
        System.out.println("recibio las bencinas");
        precios = precios.trim();
        String[] preciosString = precios.split(",");
        ArrayList<Combustible> arr = new ArrayList<Combustible>();
        Combustible c;
        int i, tipoCombustible = 93;
        
        for (i = 0; i < preciosString.length; i++)
        {
            System.out.println("bencina: " + preciosString[i]);

            if (i < 3)
            {
                c = new Combustible(Integer.toString(tipoCombustible), Integer.parseInt(preciosString[i]));
                tipoCombustible += 2;
            }
            else if (i == 3)
            {
                c = new Combustible("Diesel", Integer.parseInt(preciosString[i]));
            }
            else
            {
                c = new Combustible("Kerosene", Integer.parseInt(preciosString[i]));
            }

            c.setId(i + 1);
            arr.add(c);
        }
        
        //save all
        for (Combustible comb : arr)
        {
            if (comb.save(ClienteChat.conn))
            {
                System.out.println("La actualizacion de " + comb.getNombre() + " fue hecha con éxito");
            }
            else
            {
                System.out.println("La actualizacion de " + comb.getNombre() + " no pudo realizarse");
            }
        }
       
    }
    
}
