/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidorchat;

import Model.Combustible;
import Model.Estacion;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author yorch
 */
public class PuenteConexiones  extends Thread
{
    private Logger log;
    private int contUsuarios, puerto, maximoConexiones;
    private ServerSocket servidor;
    private Socket socket;
    private MensajesChat mensajes;
    private boolean isServer;
    
    public PuenteConexiones()
    {
         // Carga el archivo de configuracion de log4J
        PropertyConfigurator.configure("log4j.properties");        
        this.log = Logger.getLogger(ServidorChat.class);
        this.contUsuarios=0;
        
        this.puerto = 1234;
        this.maximoConexiones = 10; // Maximo de conexiones simultaneas
        this.servidor = null; 
        this.socket = null;
        this.mensajes = new MensajesChat();
        this.isServer=true;
    }
    
    @Override
    public void run()
    {
         try 
        {
            // Se crea el serverSocket
            this.servidor = new ServerSocket(puerto, maximoConexiones);
            
            // Bucle infinito para esperar conexiones
            while (true) 
            {
                this.log.info("Servidor a la espera de conexiones.");
                this.socket = servidor.accept();
                this.log.info("Cliente con la IP " + socket.getInetAddress() + " conectado.");
                ConexionCliente cc;
                if(this.isServer)
                {
                    System.out.println("es servidor");
                    cc = new ConexionCliente(socket, mensajes, this.isServer);
                    cc.start();
                    this.isServer=false;
                }
                else
                {
                    System.out.println("es cliente");
                    cc = new ConexionCliente(socket, mensajes, false);
                    cc.start();
                    
                    String ip =  socket.getInetAddress().toString();
                    System.out.println("ip: " +ip);
                    Estacion estacion = Proceso.buscarEstacion(ip);
                    if(estacion==null)
                    {
                        Proceso.CrearEstacion(ip);
                    }
                    //Una vez establecida la conexion se debe enviar precio de los combustibles
                    enviarCombustibles(cc);
                    
                }
            }
        } 
        catch (IOException ex) 
        {
            this.log.error("Error: " + ex.getMessage());
        } 
        finally
        {
            try 
            {
                this.socket.close();
                this.servidor.close();
            } 
            catch (IOException ex) 
            {
                this.log.error("Error al cerrar el servidor: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Este metodo se encarga de reunir todos los datos de los combustibles y lo prepara como mensaje para que se pueda enviar masivamente a las estaciones
     * @param cc 
     */
    public void enviarCombustibles(ConexionCliente cc)
    {
        ArrayList<Combustible> combustibles = Proceso.ObtenerCombustibles();
        //envia al usuario los precios actuales del combustible EN ORDEN
        String temporal = null;
        int size = combustibles.size()-1;
        temporal = Integer.toString(combustibles.get(size-4).getCosto()) + "," + Integer.toString(combustibles.get(size-4).getId()) + ","
                + Integer.toString(combustibles.get(size-3).getCosto()) + "," + Integer.toString(combustibles.get(size-3).getId()) + ","
                + Integer.toString(combustibles.get(size-2).getCosto()) + "," + Integer.toString(combustibles.get(size-2).getId()) + ","
                + Integer.toString(combustibles.get(size-1).getCosto()) + "," + Integer.toString(combustibles.get(size-1).getId()) + ","
                + Integer.toString(combustibles.get(size).getCosto()) + "," + Integer.toString(combustibles.get(size).getId());
        cc.enviarMensajeParticular(temporal);
    }
    
}
