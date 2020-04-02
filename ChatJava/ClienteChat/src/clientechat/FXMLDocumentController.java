/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientechat;

import Model.Combustible;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
    private Label total;
    @FXML
    private ComboBox surtidor;
    @FXML
    private ComboBox combustible;
    @FXML
    private TextField litros;
    private ArrayList<Combustible> combustibles;

    /*Datos para la conexion co el servidor*/
    private Socket socket;
    private int puerto;
    private String host;
    private String usuario;
    private Logger log;
    private ConexionServidor conexion;

    @FXML
    private void transaccion()
    {
        String ltr, finalSurtidor, finalCombustible;
        ltr = litros.getText();
        int costo;
        int finalLitros;
        Combustible cc;
        String fechaHora;
        int idTransaccion = -1;

        if (ltr.isEmpty())
        {
            //aviso
            System.out.println("estaba vacío");
        }
        else
        {
            finalSurtidor = (String) surtidor.getValue();
            finalCombustible = (String) combustible.getValue();
            if (!finalCombustible.isEmpty() && !finalSurtidor.isEmpty())
            {
                finalLitros = Integer.parseInt(ltr);
                cc = Procesos.BuscarCombustible(finalCombustible);
                costo = finalLitros * cc.getCosto();
                fechaHora = Procesos.ObtenerFechaYHoraActual();

                idTransaccion = Procesos.CrearTransaccion(Integer.parseInt(finalSurtidor), cc.getId_comb_empresa(), finalLitros, costo, fechaHora);

                if (idTransaccion!=-1)
                {
                    total.setText(Integer.toString(costo));
                    String transaccion = Procesos.ObtenerTransaccion(idTransaccion);
                    
                    if(transaccion != null)
                    {
                        this.conexion.enviarMensaje(transaccion);
                    }
                }
            }
            else
            {
                total.setText("0");
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {

        // TODO
        this.combustibles = new ArrayList<>();
        this.log = Logger.getLogger(ClienteChat.class);
        this.host = "localhost";
        this.puerto = 1234;
        this.usuario = "Usuario";

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

        RecibirMensaje cliente = new RecibirMensaje(socket, puerto, host, usuario);
        cliente.start();

        // TODO
        SurtidorComboBox();
        //llenar combustible
        CombustibleComboBox();
    }

    /**
     * Llena el surtidor en la ventana.
     */
    private void SurtidorComboBox()
    {
        //surtidor = new ComboBox();
        this.surtidor.getItems().add("1");
        this.surtidor.getItems().add("2");
        this.surtidor.getItems().add("3");
        this.surtidor.getItems().add("4");
    }

    /**
     * Llena el combustible en la ventana.
     *
     * @param combustibles
     */
    private void CombustibleComboBox()
    {
        //obtener combustible
        ArrayList<Combustible> combustibles = Procesos.ObtenerCombustibles();
        for (Combustible c : combustibles)
        {
            this.combustible.getItems().add(c.getNombre());
        }
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

    @FXML
    public void limpiar()
    {
        total.setText("");
        litros.clear();
        surtidor.getSelectionModel().clearSelection();
        combustible.getSelectionModel().clearSelection();
    }

    

}
