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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
        
        /*Clase encargada de realizar las consultas a la base de datos*/
        this.proceso = new Proceso();
    }

    public void cambiarNumero()
    {
        String n3, n5, n7, dies, keros, fecha_hora;
        int finalNoventaytres, finalNoventaycinco, finalNoventaisiete, finalDiesel, finalKerosene;
        ArrayList<Combustible> finalc = new ArrayList<>();
        Combustible c;

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
            for(int i=0;i<finalc.size();i++)
            {
                finalc.get(i).save(ServidorChat.conn);
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
