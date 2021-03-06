
package empresa;

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
    
    /**
     * Esteme metodo se encarga de ver de donde provienen los mensajes. Si provienen desde el servidor,
     * el mensaje se envia masivamente a todas las estaciones. Mientras que si el mensaje proviene de una
     * estacion es porque es una transaccion por lo tanto prepara un string con todos los datps proveniente
     * desde la estación y se procede a envia al metodoque crea una transaccion
     * @param mensaje
     * @param isServer
     * @param ip 
     */
    public void setMensaje(String mensaje, boolean isServer, String ip)
    {
        
        this.mensaje = mensaje;
        // Indica que el mensaje ha cambiado
        this.setChanged();

        if(isServer)
        {
            // Notifica a los observadores que el mensaje ha cambiado y se lo pasa
            // (Internamente notifyObservers llama al metodo update del observador)
            this.notifyObservers(this.getMensaje());
        }
        else
        {
            System.out.println("transaccion: " + mensaje);
            
            mensaje = mensaje.trim();
            String[] transaccion = mensaje.split(",");
            
            int id_surtidor = Integer.parseInt(transaccion[1]);
            int id_combustible = Integer.parseInt(transaccion[2]);
            int litros = Integer.parseInt(transaccion[3]);
            int costo = Integer.parseInt(transaccion[4]);
            String fecha_hora = transaccion[5];
            int id_transaccion = Procesos.CrearTransaccion(ip, id_surtidor, id_combustible, litros, costo, fecha_hora);
            System.out.println("El id de la transaccion realizada es: " + id_transaccion);
            
        }
    }
}