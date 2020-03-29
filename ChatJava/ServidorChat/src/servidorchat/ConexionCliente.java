
package servidorchat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;
import org.apache.log4j.Logger;


/**
 * Esta clase gestiona el envio de datos entre el servidor y el cliente al que atiende.
 * 
 * @author yorch
 */
public class ConexionCliente extends Thread implements Observer{
    
    private Logger log = Logger.getLogger(ConexionCliente.class);
    private Socket socket; 
    private MensajesChat mensajes;
    private DataInputStream entradaDatos;
    private DataOutputStream salidaDatos;
    private boolean isServer;
    
    public ConexionCliente (Socket socket, MensajesChat mensajes, boolean isServer)
    {
        this.socket = socket;
        this.mensajes = mensajes;
        this.isServer=isServer;
        
        try 
        {
            this.entradaDatos = new DataInputStream(socket.getInputStream());
            this.salidaDatos = new DataOutputStream(socket.getOutputStream());
        } 
        catch (IOException ex) 
        {
            this.log.error("Error al crear los stream de entrada y salida : " + ex.getMessage());
        }
    }
    
    @Override
    public void run()
    {
        String mensajeRecibido;
        boolean conectado = true;
        // Se apunta a la lista de observadores de mensajes
        this.mensajes.addObserver(this);
        
        while (conectado) 
        {
            try 
            {
                // Lee un mensaje enviado por el cliente
                mensajeRecibido = entradaDatos.readUTF();
                System.out.println(mensajeRecibido);
                
                
                // Pone el mensaje recibido en mensajes para que se notifique 
                // a sus observadores que hay un nuevo mensaje.
                mensajes.setMensaje(mensajeRecibido, isServer, socket.getInetAddress().toString());
            } 
            catch (IOException ex) 
            {
                log.info("Cliente con la IP " + socket.getInetAddress() + " desconectado.");
                conectado = false; 
                // Si se ha producido un error al recibir datos del cliente se cierra la conexion con el.
                try 
                {
                    entradaDatos.close();
                    salidaDatos.close();
                } 
                catch (IOException ex2) 
                {
                    log.error("Error al cerrar los stream de entrada y salida :" + ex2.getMessage());
                }
            }
        }   
    }
    
    /**
     * Este metodo se encarga de enviar masivamente los combustibles a todas las estaciones cuando se realizan nuevos cambios de precios. 
     * Esto se envia a través de un mensaje
     * @param o
     * @param arg 
     */
    @Override
    public void update(Observable o, Object arg) 
    {
        try 
        {
            // Envia el mensaje al cliente
            salidaDatos.writeUTF(arg.toString());
        } 
        catch (IOException ex) 
        {
            log.error("Error al enviar mensaje al cliente (" + ex.getMessage() + ").");
        }
    }
    
    /**
     * Estem método es utilizado para enviar mensajes personalizados al momento de realizar la conexion entre el servidor y el cliente
     * comunmente por aqui se envía los costos de los combustibles a la estación que recien se conecta al server
     * @param msj 
     */
    public void enviarMensajeParticular(String msj)
    {
        try 
        {
            // Envia el mensaje al cliente
            this.salidaDatos.writeUTF(msj);
        } 
        catch (IOException ex) 
        {
            this.log.error("Error al enviar mensaje al cliente (" + ex.getMessage() + ").");
        }
    }
}
