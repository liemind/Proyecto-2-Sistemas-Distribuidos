/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidorchat;

import Model.Combustible;
import Model.Estacion;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author Liemind
 */
public class Proceso
{

    //private ArrayList<Combustible> cc;

    public Proceso()
    {
        //this.cc = new ArrayList<>();
    }

    /**
     * Obtiene de la base de datos los combustibles del servidor.
     *
     * @return una lista de combustibles con sus precios respectivos.
     */
    public static synchronized ArrayList<Combustible> ObtenerCombustibles()
    {
        Statement stmt = null;
        try
        {
            ArrayList<Combustible> combustibles = new ArrayList<>();
            stmt = ServidorChat.conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM combustible;");

            while (rs.next())
            {
                Combustible combustible = new Combustible(rs.getString("nombre"), rs.getInt("costo"));
                combustible.setId(rs.getInt("id"));
                combustibles.add(combustible);
            }
            //end bandera
            rs.close();
            stmt.close();
            return combustibles;

        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return null;
        }
    }

    public static synchronized Estacion crearEstacion(String nombreEs)
    {
        //String nombreE = nombreEs.split("/")[1];
        String nombreE = nombreEs;
        Statement stmt = null;
        Estacion estacion = null;
        try
        {
            ServidorChat.conn.setAutoCommit(false);
            stmt = ServidorChat.conn.createStatement();
            //consultar estacion

            //crear Estacion
            String sql = "INSERT INTO estacion (nombre) VALUES ('" + nombreE + "');";
            stmt.executeUpdate(sql);
            //por la id

            estacion = buscarEstacion(nombreE);
            System.out.println("la id de la estacion creada es: " + estacion.getId());

            //ESTACIONES
            sql = "INSERT INTO surtidor (id_estacion, id_numero_surtidor, transacciones) "
                    + "VALUES (" + estacion.getId() + ",1,0 );";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO surtidor (id_estacion, id_numero_surtidor, transacciones) "
                    + "VALUES (" + estacion.getId() + ",2,0 );";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO surtidor (id_estacion, id_numero_surtidor, transacciones) "
                    + "VALUES (" + estacion.getId() + ",3,0 );";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO surtidor (id_estacion, id_numero_surtidor, transacciones) "
                    + "VALUES (" + estacion.getId() + ",4,0 );";
            stmt.executeUpdate(sql);

            stmt.close();
            ServidorChat.conn.commit();
        }
        catch (SQLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return estacion;
    }

    public static synchronized boolean existeEstacion(String nombreE)
    {
        Statement stmt = null;
        try
        {

            //consultar estacion
            String sql = "SELECT count(*) FROM estacion WHERE nombre = '" + nombreE + "';";
            ServidorChat.conn.setAutoCommit(false);
            stmt = ServidorChat.conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            int cantIP = 0;
            /*Representa a la cantidad de ip's que esta en la base de datos que son iguales a la nombreE*/
            while (rs.next())
            {
                cantIP = rs.getInt("count(*)");
                //System.out.println("Cantidad de ips: " + cantIP);
            }
            rs.close();
            stmt.close();
            ServidorChat.conn.commit();

            if (cantIP == 0)
            {
                return false;
            }
        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return true;
    }

    public static synchronized Estacion buscarEstacion(String nombreE)
    {
        Statement stmt = null;
        Estacion estacion = null;

        try
        {
            //consultar estacion
            String sql = "SELECT id, nombre FROM estacion WHERE nombre = '" + nombreE + "';";
            ServidorChat.conn.setAutoCommit(false);
            stmt = ServidorChat.conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next())
            {
                estacion = new Estacion(rs.getString("nombre"), rs.getInt("id"));
                //System.out.println("id: " + idEstacion);
            }
            rs.close();
            stmt.close();
            ServidorChat.conn.commit();
        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return estacion;
    }

    public static Estacion getEstacion(String nombreEs)
    {
        Estacion estacion = null;
        if (!existeEstacion(nombreEs))
        {
            estacion = crearEstacion(nombreEs);
        }
        else
        {
            estacion = buscarEstacion(nombreEs);
        }
        return estacion;
    }

    public static synchronized boolean actualizarTransacciones(int id, int surtidor, int trans)
    {
        Statement stmt = null;
        try
        {
            ServidorChat.conn.setAutoCommit(false);
            stmt = ServidorChat.conn.createStatement();

            //guardar la transaccion
            stmt.executeUpdate("UPDATE surtidor SET transacciones = " + trans + " WHERE id_estacion = " + id + " AND id_numero_surtidor = " + surtidor + ";");
            ServidorChat.conn.commit();
            stmt.close();
            return true;
        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return false;
    }

    public static synchronized int buscarIdSurtidor(String nombreE, int surtidor)
    {
        Statement stmt = null;
        int number = 0;

        try
        {
            ServidorChat.conn.setAutoCommit(false);
            stmt = ServidorChat.conn.createStatement();
            //consultar estacion
            ResultSet rs = stmt.executeQuery("SELECT id FROM surtidor WHERE id_estacion = '" + nombreE + "' AND id_numero_surtidor = '" + surtidor + "';");

            while (rs.next())
            {
                number = rs.getInt("id");
            }
            rs.close();
            stmt.close();
            ServidorChat.conn.commit();
            return number;

        }
        catch (SQLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return 0;
    }

    public static synchronized boolean crearTransaccion(int id_surtidor, int id_combustible, int litros, int costo)
    {
        Statement stmt = null;
        try
        {
            System.out.println("entro a crear transaccion");
            ServidorChat.conn.setAutoCommit(false);

            stmt = ServidorChat.conn.createStatement();
            String sql = "INSERT INTO transaccion (id_surtidor, id_combustible, litros, costo) "
                    + "VALUES (" + id_surtidor + ", " + id_combustible + ", " + litros + ", " + costo + " );";
            stmt.executeUpdate(sql);
            stmt.close();
            ServidorChat.conn.commit();
            System.out.println("salio de crear transaccion");
            return true;
        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return false;
    }
    
    

}
