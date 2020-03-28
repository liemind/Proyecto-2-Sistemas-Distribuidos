package servidorchat;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.apache.log4j.Logger;

/**
 * Esta clase gestiona el envio de datos entre el cliente y el servidor.
 *
 * @author yorch
 */
public class ConexionServidor
{

    private Logger log = Logger.getLogger(ConexionServidor.class);
    private Socket socket;
    private String usuario;
    private DataOutputStream salidaDatos;

    public ConexionServidor(Socket socket, String usuario)
    {
        this.socket = socket;
        this.usuario = usuario;
        try
        {
            this.salidaDatos = new DataOutputStream(socket.getOutputStream());
        }
        catch (IOException ex)
        {
            log.error("Error al crear el stream de salida : " + ex.getMessage());
        }
        catch (NullPointerException ex)
        {
            log.error("El socket no se creo correctamente. ");
        }
    }

    public void enviarMensaje(String msj)
    {
        System.out.println("Entro a enviar el msj");
        try
        {
            salidaDatos.writeUTF(msj);
            //tfMensaje.setText("");
        }
        catch (IOException ex)
        {
            log.error("Error al intentar enviar un mensaje: " + ex.getMessage());
        }
    }

}
