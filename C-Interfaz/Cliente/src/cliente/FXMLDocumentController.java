/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cliente;

import Model.Combustible;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author Liemind
 */
public class FXMLDocumentController implements Initializable {
    
    @FXML private Label total;
    @FXML private ComboBox surtidor;
    @FXML private ComboBox combustible;
    @FXML private TextField litros;
    
    
    
    @FXML
    private void transaccion() {
        String ltr, finalSurtidor, finalCombustible;
        ltr = litros.getText();
        
        if(ltr.isEmpty()) {
            //aviso
            System.out.println("estaba vacío");
        }
        else {
            finalSurtidor = (String) surtidor.getValue();
            finalCombustible = (String) combustible.getValue();
            
            int finalLitros = Integer.parseInt(ltr);
            total.setText("aca deberia haber un precio");
            
        }
        
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        llenarSurtidor();
        //obtener combustible
        ArrayList<Combustible> c = obtenerCombustibles();
        //llenar combustible
        llenarCombustible(c);
        
        
        int surtidor = 2;
        Thread[]threads = new Thread[surtidor];
        
        for (int i = 0; i < surtidor; i++) {
            System.out.println("Proceso: "+i);
            threads[i] = new Thread(new Proceso(i));
            threads[i].start();
        }
        
    }

    /**
     * Obtiene de la base de datos los combustibles del servidor.
     * @return una lista de combustibles con sus precios respectivos.
     */
    public synchronized ArrayList<Combustible> obtenerCombustibles() {
        Statement stmt = null;
        try {
            ArrayList<Combustible> combustibles = new ArrayList<>();
            stmt = Main.conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM combustible;");

            while ( rs.next() ) {
               Combustible combustible = new Combustible( rs.getString("nombre") , rs.getInt("costo") );
               combustible.setId(rs.getInt("id"));
               combustibles.add(combustible);
            }
            //end bandera
            rs.close();
            stmt.close();
            return combustibles;

        }catch(Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            return null;
        }
     }
    
    /**
     * Llena el surtidor en la ventana.
     */
    private void llenarSurtidor() {
        //surtidor = new ComboBox();
        
        surtidor.getItems().add("1");
        surtidor.getItems().add("2");
        surtidor.getItems().add("3");
        surtidor.getItems().add("4");
    }
    
    /**
     * Llena el combustible en la ventana.
     * @param combustibles 
     */
    private void llenarCombustible(ArrayList<Combustible> combustibles) {
        for(Combustible c : combustibles) {
            combustible.getItems().add(c.getNombre());
        }
    }
        /**
     * Revisa si el caracter ingresado es numerico y solo lo acepta si es numerico
     * (Debe ir en keytyped)
     * @param t 
     */
    @FXML
    private void checkNumeric(KeyEvent t){
        char ar[] = t.getCharacter().toCharArray();
            char ch = ar[t.getCharacter().toCharArray().length - 1];
            if (!(ch >= '0' && ch <= '9')) {               
               t.consume();
        }
    }
    
    @FXML
    public void limpiar() {
        total.setText("");
        litros.clear();
        surtidor.getSelectionModel().clearSelection();
        combustible.getSelectionModel().clearSelection();
        
    }
}
