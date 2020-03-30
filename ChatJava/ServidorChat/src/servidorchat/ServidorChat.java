
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
    //string de conección de la bd
    static public Connection conn;
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
    
    /**
     * Conecta el programa con la base de datos (SQLite)
     * @return la conección a la bd.
   */
   public static Connection conectar() {
        Connection c = null;
        String bd = "empresa.db";
        String dir = System.getProperty("user.dir");
        String url = dir+"\\"+bd;
        
        try {
           Class.forName("org.sqlite.JDBC");
           c = DriverManager.getConnection("jdbc:sqlite:"+url);
           System.out.println("Base de datos Conectada");
        } catch ( Exception e ) {
           System.err.println( e.getClass().getName() + ": " + e.getMessage() );
           System.exit(0);
        }
        return c;
   }
    
    public static void main(String[] args)  
    {
        conn = conectar();
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