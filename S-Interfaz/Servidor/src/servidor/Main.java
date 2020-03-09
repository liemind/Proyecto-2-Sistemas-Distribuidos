/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import java.sql.Connection;
import java.sql.DriverManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Liemind
 */
public class Main extends Application {
    //string de conección de la bd
    static public Connection conn;
    //estatus 1: en espera, 0: escuchando
    static public int status = 0;
    
    @Override
    public void start(Stage stage) throws Exception {
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
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException{
        conn = conectar();
        launch(args);
    }
    
    /**
     * Conecta el programa con la base de datos (SQLite)
     * @return la conección a la bd.
   */
   public static Connection conectar() {
        Connection c = null;
        String url = "C:\\Users\\Liemind\\Documents\\Universidad de Talca\\2019-2\\Sistemas Distribuidos\\Unidad 2\\Proyecto 2\\Proyecto 2 Sistemas Distribuidos\\S-Interfaz\\Servidor\\empresa.db";
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
    
}
