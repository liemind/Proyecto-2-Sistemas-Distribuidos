/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Liemind
 */
public class Proceso extends Application implements Runnable{
    private int id;
    private String[] args;
    
    public Proceso(int id, String[] args) {
        this.id = id;
        this.args = args;
    }
    
    @Override
    public void run() {
        while(true) {
            if(id == 0) {
                System.out.println("Proceso 0");
                
            }
            else if(id == 1) {
                System.out.println("Proceso 1");
            }
        }
    }
}
