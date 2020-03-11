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
    ArrayList<Combustible> combustibles;
    
    
    @FXML
    private void transaccion() {
        String ltr, finalSurtidor, finalCombustible;
        ltr = litros.getText();
        int costo;
        int finalLitros;
        Combustible cc;
        
        if(ltr.isEmpty()) {
            //aviso
            System.out.println("estaba vacío");
        }
        else {
            finalSurtidor = (String) surtidor.getValue();
            finalCombustible = (String) combustible.getValue();
                if(!finalCombustible.isEmpty() && !finalSurtidor.isEmpty()) {
                    finalLitros = Integer.parseInt(ltr);
                    cc = buscarCombustible(finalCombustible);
                    costo = finalLitros * cc.getCosto();
                    
                    //bandera
                        System.out.println("TRANSACCION: "+finalSurtidor+" ,"+finalCombustible+","+ltr);
                        System.out.println("Combustible: "+cc.getNombre()+", "+cc.getCosto()+"["+cc.getId()+"]");
                    //end bandera
                    
                    boolean transaccionCreada = crearTransaccion(Integer.parseInt(finalSurtidor), cc.getId(), finalLitros, costo);
                    
                    if(transaccionCreada) {
                        total.setText(Integer.toString(costo));
                        guardarSurtidor(Integer.parseInt(finalSurtidor));
                        Main.surtidor = Integer.parseInt(finalSurtidor);
                        Main.status = 2;
                        //bandera
                        System.out.println("Surtidor: "+finalSurtidor);
                        //end bandera
                    }
                }
                else {
                    total.setText("0");
                }
        }
        
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.combustibles = new ArrayList<>();
        // TODO
        llenarSurtidor();
        //obtener combustible
        obtenerCombustibles();
        //llenar combustible
        llenarCombustible();
        
        
        int surtidor = 2;
        Thread[]threads = new Thread[surtidor];
        
        for (int i = 0; i < surtidor; i++) {
            threads[i] = new Thread(new Proceso(i));
            threads[i].start();
        }
        
    }

    /**
     * Obtiene de la base de datos los combustibles del servidor.
     * @return una lista de combustibles con sus precios respectivos.
     */
    public synchronized void obtenerCombustibles() {
        Statement stmt = null;
        try {
            stmt = Main.conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM combustible;");

            while ( rs.next() ) {
               Combustible cc = new Combustible( rs.getString("nombre") , rs.getInt("costo") );
               cc.setId(rs.getInt("id"));
               combustibles.add(cc);
            }
            //end bandera
            rs.close();
            stmt.close();

        }catch(Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
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
    private void llenarCombustible() {
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
    
    /**
     * Guarda en la base de datos local una transacción
     * @param conn
     * @param idSurtidor
     * @param idCombustible
     * @param litros
     * @param costo
     * @return
     */
    public boolean crearTransaccion(int idSurtidor, int idCombustible, int litros, int costo) {
         Statement stmt = null;
         try {
            Main.conn.setAutoCommit(false);  
            //bandera
                System.out.println("Transaccion: "+idSurtidor+","+idCombustible+","+litros+","+costo);
            //end bandera
   
            stmt = Main.conn.createStatement();
            String sql = "INSERT INTO transaccion (id_surtidor, id_combustible, litros, costo) VALUES ("+idSurtidor+", "+idCombustible+", "+litros+", "+costo+" );"; 
            stmt.executeUpdate(sql);
            stmt.close();
            Main.conn.commit();
            //bandera
                System.out.println("Crea la transaccion");
            //end bandera
            return true;
         } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            //bandera
                System.out.println("No crea la transaccion");
            //end bandera
            return false;
         }

    }
    
    public Combustible buscarCombustible(String n) {
        for (Combustible cc : combustibles) {
            if(cc.getNombre().equalsIgnoreCase(n)) {
                return cc;
            }
        }
        return null;
    }
    
    /**
      * Actualiza el numero de transacciones de un surtidor en la base de datos.
      * @param id
      * @return
      */
    public synchronized void guardarSurtidor(int id) {
         int trans = 0;
         
         //seleccionar el surtidor
         Statement stmt = null;
         try {
            Main.conn.setAutoCommit(false);
            stmt = Main.conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT transacciones FROM surtidor WHERE id = "+id+";");

            while ( rs.next() ) {
               trans = rs.getInt("transacciones");
            }
            
            rs.close();
            //sumar la transaccion
            trans = trans+1;

            //guardar la transaccion
            stmt.executeUpdate("UPDATE surtidor SET transacciones = "+trans+" WHERE id = "+id+";");
            Main.conn.commit();

            stmt.close();

         }catch(Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
         }

      }


    
}
