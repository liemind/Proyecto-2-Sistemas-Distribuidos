
package servidorchat;

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
    
    public void setMensaje(String mensaje, boolean isServer)
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
            
        }
    }
}