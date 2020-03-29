
package servidorchat;

import Model.Combustible;
import java.util.ArrayList;
import java.util.Observable;

/**
 * Objeto observable del patron observer.
 * 
 * @author yorch
 */
public class MensajesChat extends Observable{

    private String mensaje;
    
    public MensajesChat(){
    }
    
    public String getMensaje(){
        return mensaje;
    }
    
    public void setMensaje(String mensaje, boolean isServer, String ip)
    {
        
        this.mensaje = mensaje;
        // Indica que el mensaje ha cambiado
        this.setChanged();

        if(isServer)
        {
            System.out.println("envia el mensaje a los usuarios");
            // Notifica a los observadores que el mensaje ha cambiado y se lo pasa
            // (Internamente notifyObservers llama al metodo update del observador)
            this.notifyObservers(this.getMensaje());
        }
        else
        {
            System.out.println("guarda los registros en la bd");
            System.out.println("transaccion: " + mensaje);
            
            mensaje = mensaje.trim();
            String[] transaccion = mensaje.split(",");
            
            int id_surtidor = Integer.parseInt(transaccion[1]);
            int id_combustible = Integer.parseInt(transaccion[2]);
            int litros = Integer.parseInt(transaccion[3]);
            int costo = Integer.parseInt(transaccion[4]);
            String fecha_hora = transaccion[5];
            System.out.println("Id transaccion: " + Proceso.CrearTransaccion(ip, id_surtidor, id_combustible, litros, costo, fecha_hora));
            
        }
    }
}