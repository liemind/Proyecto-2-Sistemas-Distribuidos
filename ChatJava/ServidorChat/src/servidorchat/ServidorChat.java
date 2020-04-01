
package servidorchat;

import static javafx.application.Application.launch;


import java.sql.Connection;
import java.sql.DriverManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Servidor para el chat.
 * 
 * @author Yorch
 */
public class ServidorChat extends Application
{
    //estatus 1: algo para enviar, 0: no hay nada
    static public int status = 1;

    /**
     * @param stage
     * @throws java.lang.Exception
     */
    
    @Override
    public void start(Stage stage) throws Exception 
    {
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        
        Scene scene = new Scene(root);
        
        //Agrega un titulo a la ventana
        stage.setTitle("Empresa Popec");
        //Hace la ventana no redimensionable
        stage.setResizable(false);
        
        stage.setScene(scene);
        stage.show();
        
    }
    
    
    public static void main(String[] args)  
    {
        //hilos de ejecucion
        try {
            Hilos hilo = new Hilos();
            hilo.start();
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        
        for(int i=0; i<2;i++)
        {
            if(i==0)
            {
                PuenteConexiones puente = new PuenteConexiones();
                puente.start();
                
            }
            else
            {
                launch(args);
            }
        }
    }
}