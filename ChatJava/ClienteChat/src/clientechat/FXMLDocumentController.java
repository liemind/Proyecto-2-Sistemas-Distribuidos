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
                cc = buscarCombustible(finalCombustible);
                costo = finalLitros * cc.getCosto();

                //bandera
                System.out.println("TRANSACCION: " + finalSurtidor + " ," + finalCombustible + "," + ltr);
                System.out.println("Combustible: " + cc.getNombre() + ", " + cc.getCosto() + "[" + cc.getId() + "]");
                //end bandera

                idTransaccion = crearTransaccion(Integer.parseInt(finalSurtidor), cc.getId(), finalLitros, costo);

                if (idTransaccion!=-1)
                {
                    total.setText(Integer.toString(costo));
                    guardarSurtidor(Integer.parseInt(finalSurtidor));
                    ClienteChat.surtidor = Integer.parseInt(finalSurtidor);
                    //bandera
                    System.out.println("Surtidor: " + finalSurtidor);
                    //end bandera
                    String transaccion = ObtenerTransaccion(idTransaccion);
                    
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
        llenarSurtidor();
        //obtener combustible
        obtenerCombustibles();
        //llenar combustible
        llenarCombustible();
    }

    /**
     * Obtiene de la base de datos los combustibles del servidor.
     *
     */
    public synchronized void obtenerCombustibles()
    {

        Statement stmt = null;
        try
        {
            stmt = ClienteChat.conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM combustible;");

            while (rs.next())
            {
                Combustible cc = new Combustible(rs.getString("nombre"), rs.getInt("costo"));
                cc.setId(rs.getInt("id"));
                this.combustibles.add(cc);
            }
            //end bandera
            rs.close();
            stmt.close();

        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    /**
     * Llena el surtidor en la ventana.
     */
    private void llenarSurtidor()
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
    private void llenarCombustible()
    {
        for (Combustible c : this.combustibles)
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

    /**
     * Guarda en la base de datos local una transacción
     *
     * @param conn
     * @param idSurtidor
     * @param idCombustible
     * @param litros
     * @param costo
     * @return
     */
    public int crearTransaccion(int idSurtidor, int idCombustible, int litros, int costo)
    {
        Statement stmt = null;
        int idTransaccion =-1;
        try
        {
            ClienteChat.conn.setAutoCommit(false);
            //bandera
            System.out.println("Transaccion: " + idSurtidor + "," + idCombustible + "," + litros + "," + costo);
            //end bandera

            stmt = ClienteChat.conn.createStatement();
            String sql = "INSERT INTO transaccion (id_surtidor, id_combustible, litros, costo) VALUES (" + idSurtidor + ", " + idCombustible + ", " + litros + ", " + costo + " );";
            stmt.executeUpdate(sql);
            ResultSet rs = stmt.executeQuery("SELECT id FROM transaccion ORDER BY id DESC LIMIT 1;");
            
            if (rs.next()) 
            {
                idTransaccion = Integer.parseInt(rs.getString("id"));
            }
            System.out.println("Id transaccion:  " + idTransaccion);
            stmt.close();
            ClienteChat.conn.commit();
            //bandera
            System.out.println("Crea la transaccion");
            //end bandera
        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            //bandera
            System.out.println("No crea la transaccion");
            //end bandera
        }
        return idTransaccion;
    }
    
    /*Obtiene:
        id transaccion
        id surtidor
        id combustible
        litros
        costo
    */
    public String ObtenerTransaccion(int id)
    {
        Statement stmt = null;
        String transaccion = "";
        try
        {
            ClienteChat.conn.setAutoCommit(false);

            stmt = ClienteChat.conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM transaccion WHERE id = " + id + ";");
            
            if (rs.next()) 
            {
                transaccion += rs.getString("id")+ "," + rs.getString("id_surtidor")+ "," + rs.getString("id_combustible")+ "," + rs.getString("litros")+ "," +  rs.getString("costo");
            }
            stmt.close();
            ClienteChat.conn.commit();
            //bandera
            System.out.println("Obtiene la transaccion");
            //end bandera
            
            return transaccion;
        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            //bandera
            System.out.println("No obtiene la transaccion");
            //end bandera
        }
        
        return null;
    }

    public Combustible buscarCombustible(String n)
    {
        for (Combustible cc : combustibles)
        {
            if (cc.getNombre().equalsIgnoreCase(n))
            {
                return cc;
            }
        }
        return null;
    }

    /**
     * Actualiza el numero de transacciones de un surtidor en la base de datos.
     *
     * @param id
     * @return
     */
    public synchronized void guardarSurtidor(int id)
    {
        int trans = 0;

        //seleccionar el surtidor
        Statement stmt = null;
        try
        {
            ClienteChat.conn.setAutoCommit(false);
            stmt = ClienteChat.conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT transacciones FROM surtidor WHERE id = " + id + ";");

            while (rs.next())
            {
                trans = rs.getInt("transacciones");
            }

            rs.close();
            //sumar la transaccion
            trans = trans + 1;

            //guardar la transaccion
            stmt.executeUpdate("UPDATE surtidor SET transacciones = " + trans + " WHERE id = " + id + ";");
            ClienteChat.conn.commit();

            stmt.close();

        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

    }

}
