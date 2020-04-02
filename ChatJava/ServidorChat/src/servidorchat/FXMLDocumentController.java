/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidorchat;

import Model.Combustible;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import org.apache.log4j.Logger;

/**
 *
 * @author Liemind
 */
public class FXMLDocumentController implements Initializable
{

    @FXML
    private TextField noventaytres;
    @FXML
    private TextField noventaycinco;
    @FXML
    private TextField noventaysiete;
    @FXML
    private TextField diesel;
    @FXML
    private TextField kerosene;
    @FXML
    private ComboBox informe;
    @FXML
    private ComboBox list;
    @FXML
    private Label fechaH1;
    @FXML
    private Label nombre;
    @FXML
    private Label fecha;
    @FXML
    private Label trans;
    

    private Socket socket;
    private int puerto;
    private String host;
    private String usuario;
    private Logger log;
    private ConexionServidor conexion;
    private Proceso proceso;
   


    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        
        // TODO
        this.log = Logger.getLogger(FXMLDocumentController.class);
        this.host = "localhost";
        this.puerto = 1234;
        this.usuario = "Servidor";

        log.info("Quieres conectarte a " + host + " en el puerto " + puerto + " con el nombre de ususario: " + usuario + ".");

        // Se crea el socket para conectar con el Sevidor del Chat
        try
        {
            this.socket = new Socket(host, puerto);
        }
        catch (UnknownHostException ex)
        {
            log.error("No se ha podido conectar con el servidor (" + ex.getMessage() + ").");
        }
        catch (IOException ex)
        {
            log.error("No se ha podido conectar con el servidor (" + ex.getMessage() + ").");
        }
        this.conexion = new ConexionServidor(socket, usuario);

        //Obtener combustibles
        ArrayList<Combustible> cc = Proceso.ObtenerCombustibles();
        int size = cc.size()-1;
        System.out.println("tam combustibles: " + cc.size());
        //Setear combustibles.
        noventaytres.setText(Integer.toString(cc.get(size-4).getCosto()));
        noventaycinco.setText(Integer.toString(cc.get(size-3).getCosto()));
        noventaysiete.setText(Integer.toString(cc.get(size-2).getCosto()));
        diesel.setText(Integer.toString(cc.get(size-1).getCosto()));
        kerosene.setText(Integer.toString(cc.get(size).getCosto()));
        InformeCombobox();
        
        nombre.setText("");
        fecha.setText("");
        trans.setText("");
        /*Clase encargada de realizar las consultas a la base de datos*/
        this.proceso = new Proceso();
    }
    
    @FXML
    public void obtenerInforme() {
        nombre.setText("");
        fecha.setText("");
        trans.setText("");
        list.getItems().clear();
        String opcion = (String) informe.getValue();
        ArrayList<String> arr = new ArrayList<>();
        if(!opcion.isEmpty()) {
            if(opcion.equals("Transacciones anuales por sucursal")) {
                arr = Proceso.ObtenerTransaccionesAnualesPorSucursal();
                fechaH1.setText("Año");
                
                //lista.getItems().add("Nombre Sucursal (año):  <Cantidad de Transacciones>");
                if(arr != null) {
                    for (String string : arr) {
                        list.getItems().add(string);
                    }
                }
                
            }else {
                arr = Proceso.filtrarPorMesYAño();
                fechaH1.setText("Mes/Año");
                
                if(arr != null) {
                    for (String string : arr) {
                        list.getItems().add(string);
                    }
                }
            }
        }
    }
    
    public void mostrarInforme() {
        String opcion = (String) list.getValue();
        nombre.setText("");
        fecha.setText("");
        trans.setText("");
        
        if(!opcion.isEmpty()) {
            String[] parts = opcion.split(",");
            System.out.println("parts: "+parts.length);
            if(parts.length == 4) {
                nombre.setText(parts[0]);
                fecha.setText(parts[1]+"/"+parts[2]);
                trans.setText(parts[3]);
            } else {
                nombre.setText(parts[0]);
                fecha.setText(parts[1]);
                trans.setText(parts[2]);
            }
        }
        
        
        
    }
    
    /**
     * Llena el surtidor en la ventana.
     */
    private void InformeCombobox()
    {
        //surtidor = new ComboBox();
        this.informe.getItems().add("Transacciones anuales por sucursal");
        this.informe.getItems().add("Transacciones por mes y año");

    }
    
    
    /**
     * Este metodo se encarga de actualizar los costos de los combustibles  y de enviarlo a la clase ConexionServidor para que este se encargue de enviar el mensaje 
     * masivamente a todas las estaciones que se encuentren conectadas
     */
    public void cambiarNumero()
    {
        String n3, n5, n7, dies, keros, fecha_hora;
        int finalNoventaytres, finalNoventaycinco, finalNoventaisiete, finalDiesel, finalKerosene;
        ArrayList<Combustible> finalc = new ArrayList<>();
        Combustible c;
        Connection conn = null;

        n3 = noventaytres.getText();
        n5 = noventaycinco.getText();
        n7 = noventaysiete.getText();
        dies = diesel.getText();
        keros = kerosene.getText();
        fecha_hora=Proceso.ObtenerFechaYHoraActual();
        if (!n3.isEmpty())
        {
            finalNoventaytres = Integer.parseInt(n3);
            c = new Combustible("93", finalNoventaytres, fecha_hora);
            c.setId(1);
            finalc.add(c);
        }
        if (!n5.isEmpty())
        {
            finalNoventaycinco = Integer.parseInt(n5);
            c = new Combustible("95", finalNoventaycinco, fecha_hora);
            c.setId(2);
            finalc.add(c);
        }
        if (!n7.isEmpty())
        {
            finalNoventaisiete = Integer.parseInt(n7);
            c = new Combustible("97", finalNoventaisiete, fecha_hora);
            c.setId(3);
            finalc.add(c);
        }
        if (!dies.isEmpty())
        {
            finalDiesel = Integer.parseInt(dies);
            c = new Combustible("Diesel", finalDiesel, fecha_hora);
            c.setId(4);
            finalc.add(c);
        }
        if (!keros.isEmpty())
        {
            finalKerosene = Integer.parseInt(keros);
            c = new Combustible("Kerosene", finalKerosene, fecha_hora);
            c.setId(5);
            finalc.add(c);
        }
        if (!finalc.isEmpty())
        {
            String mensaje = "";
            conn = Proceso.conectar("empresa.db");
            for(int i=0;i<finalc.size();i++)
            {
                finalc.get(i).save(conn);
                if(i<finalc.size()-1)
                {
                    mensaje += finalc.get(i).getCosto() + "," + finalc.get(i).getId_comb() + ","  ;
                }
                else
                {
                    mensaje += finalc.get(i).getCosto() + "," + finalc.get(i).getId_comb();
                }
            }
            this.conexion.enviarMensaje(mensaje);
            //ServidorChat.status = 1;
        }
    }
    
    /**
     * Este metodo se encarga de limpiar los campos de los costos de combustibles
     */
    @FXML
    public void limpiar()
    {
        noventaytres.clear();
        noventaycinco.clear();
        noventaysiete.clear();
        diesel.clear();
        kerosene.clear();
    }

    /**
     * Revisa si el caracter ingresado es numerico y solo lo acepta si es
     * numerico (Debe ir en keytyped)
     *
     * @param t
     */
    @FXML
    private void checkNumeric(KeyEvent t)
    {
        char ar[] = t.getCharacter().toCharArray();
        char ch = ar[t.getCharacter().toCharArray().length - 1];
        if (!(ch >= '0' && ch <= '9'))
        {
            t.consume();
        }
    }

    
}
