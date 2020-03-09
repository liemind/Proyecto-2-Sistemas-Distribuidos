/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import Model.Combustible;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author Liemind
 */
public class FXMLDocumentController implements Initializable {
    
    @FXML private TextField noventaytres;
    @FXML private TextField noventaycinco;
    @FXML private TextField noventaysiete;
    @FXML private TextField diesel;
    @FXML private TextField kerosene;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        
        //Obtener combustibles
        ArrayList<Combustible> cc = obtenerCombustibles();
        //Setear combustibles.
        noventaytres.setText(Integer.toString(cc.get(0).getCosto()));
        noventaycinco.setText(Integer.toString(cc.get(1).getCosto()));
        noventaysiete.setText(Integer.toString(cc.get(2).getCosto()));
        diesel.setText(Integer.toString(cc.get(3).getCosto()));
        kerosene.setText(Integer.toString(cc.get(4).getCosto()));
        
        
        int surtidor = 2;
        Thread[]threads = new Thread[surtidor];
        
        for (int i = 0; i < surtidor; i++) {
            threads[i] = new Thread(new Proceso(i));
            threads[i].start();
        }
    }
    
    public void cambiarNumero() {
        String n3,n5,n7,dies,keros;
        int finalNoventaytres, finalNoventaycinco, finalNoventaisiete, finalDiesel, finalKerosene;
        ArrayList<Combustible> finalc = new ArrayList<>();
        Combustible c;
        
        n3 = noventaytres.getText();
        n5 = noventaycinco.getText();
        n7 = noventaysiete.getText();
        dies = diesel.getText();
        keros = kerosene.getText();
        
        if(!n3.isEmpty()) {
            finalNoventaytres = Integer.parseInt(n3);
            c = new Combustible("93", finalNoventaytres);
            c.setId(1);
            finalc.add(c);
        }
        
        if(!n5.isEmpty()) {
            finalNoventaycinco = Integer.parseInt(n5);
            c = new Combustible("95", finalNoventaycinco);
            c.setId(2);
            finalc.add(c);
        }
        
        if(!n7.isEmpty()) {
            finalNoventaisiete = Integer.parseInt(n7);
            c = new Combustible("97", finalNoventaisiete);
            c.setId(3);
            finalc.add(c);
        }
        
        if(!dies.isEmpty()) {
            finalDiesel = Integer.parseInt(dies);
            c = new Combustible("97", finalDiesel);
            c.setId(4);
            finalc.add(c);
        }
        if(!keros.isEmpty()) {
            finalKerosene = Integer.parseInt(keros);
            c = new Combustible("97", finalKerosene);
            c.setId(5);
            finalc.add(c);
        }
        
        for (Combustible combustible : finalc) {
            combustible.save(Main.conn);
        }
        
        
        
    }
    
    @FXML
    public void limpiar() {
        noventaytres.clear();
        noventaycinco.clear();
        noventaysiete.clear();
        diesel.clear();
        kerosene.clear();
        
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
}
