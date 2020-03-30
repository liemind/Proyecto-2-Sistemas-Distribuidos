package clientechat;

import java.sql.Connection;
import java.sql.DriverManager;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Clase principal del cliente del chat
 *
 * @author yorch
 */
public class ClienteChat extends Application//extends JFrame
{

    private Logger log = Logger.getLogger(ClienteChat.class);
    

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        
        Scene scene = new Scene(root);
        
        //Agrega un titulo a la ventana
        stage.setTitle("Estacion");
        //Hace la ventana no redimensionable
        stage.setResizable(false);
        
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        // Carga el archivo de configuracion de log4J
        PropertyConfigurator.configure("log4j.properties");
        launch(args);
        
        

        //ClienteChat c = new ClienteChat();
        //c.recibirMensajesServidor();
    }

}
