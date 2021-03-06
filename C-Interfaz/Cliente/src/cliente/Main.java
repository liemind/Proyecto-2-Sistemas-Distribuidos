/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author Liemind
 */
public class Main extends Application {
    //string de conección de la bd
    static public Connection conn;
    //estatus 1: hay transacciones, 0: no hay transacciones
    static public int status = 0;
    static public int surtidor;
    static final int HORA_RESPALDO = 23;
    static final int MINUTO_RESPALDO = 00;
    
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
    public static void main(String[] args) {
        conectar("estacion.db", "C:\\Users\\Liemind\\Documents\\Universidad de Talca\\2019-2\\Sistemas Distribuidos\\Unidad 2\\Proyecto 2\\Proyecto 2 Sistemas Distribuidos\\C-Interfaz\\Cliente\\");
        launch(args);
    }
    
    /**
     * Conecta el programa con la base de datos (SQLite)
     * @return la conección a la bd.
   */
    public static void conectar(String bd, String ruta) {
        Connection c = null;
        String url = ruta+""+bd;
        try {
           Class.forName("org.sqlite.JDBC");
           c = DriverManager.getConnection("jdbc:sqlite:"+url);
           System.out.println("Base de datos Conectada");
        } catch ( Exception e ) {
           System.err.println( e.getClass().getName() + ": " + e.getMessage() );
           System.exit(0);
        }
        Main.conn = c;
   }
}
