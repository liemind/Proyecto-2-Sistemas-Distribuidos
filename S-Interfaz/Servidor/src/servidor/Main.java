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
public class Main {
    //string de conección de la bd
     static public Connection conn;
    
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException{
        int surtidor = 1;
        conn = conectar();
        Thread[]threads = new Thread[surtidor];
        
        for (int i = 0; i < surtidor; i++) {
            threads[i] = new Thread(new Proceso(i,args));
            threads[i].start();
        }
        
    }
    
    /**
     * Conecta el programa con la base de datos (SQLite)
     * @return la conección a la bd.
   */
   public static Connection conectar() {
        Connection c = null;
        String url = "C:\\Users\\Liemind\\Documents\\Universidad de Talca\\2019-2\\Sistemas Distribuidos\\Unidad 2\\Proyecto 2\\S-Interfaz\\Servidor\\empresa.db";
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
