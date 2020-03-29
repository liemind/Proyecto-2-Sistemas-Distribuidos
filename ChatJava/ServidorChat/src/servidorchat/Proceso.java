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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Esta clase ayuda a realizar las gestiones de las estaciones tales como crear transacciones, nuevas estaciones, etc
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
                Combustible combustible = new Combustible(rs.getString("nombre"), rs.getInt("costo"), rs.getString("fecha_hora"));
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
    
    /**
     * Crea una estacion en caso de no existir con la ip en la que proviene la estacion
     * @param nombreEs
     * @return 
     */
    public static synchronized Estacion CrearEstacion(String nombreEs)
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

            stmt.close();
            ServidorChat.conn.commit();
        }
        catch (SQLException e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return estacion;
    }
    
    /**
     * Verifica la existencia de una estacion en particular
     * @param nombreE
     * @return 
     */
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
            //Representa a la cantidad de ip's que esta en la base de datos que son iguales a la nombreE
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
    
    /**
     * Busca una estación en particular. Si existe la estacion, devuelve una instancia con todos los datos, en caso contrario devuelve un null
     * @param nombreE
     * @return estacion
     */
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

    /**
     * Obtiene los datos de una estacion en particula en caso de que exista
     * @param nombreEs
     * @return estacion
     */
    /*public static Estacion getEstacion(String nombreEs)
    {
        Estacion estacion = null;
        if (!existeEstacion(nombreEs))
        {
            estacion = CrearEstacion(nombreEs);
        }
        else
        {
            estacion = buscarEstacion(nombreEs);
        }
        return estacion;
    }*/

    /*public static synchronized boolean actualizarTransacciones(int id, int surtidor, int trans)
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
    }*/

    /*public static synchronized int buscarIdSurtidor(String nombreE, int surtidor)
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
    } */
    
    /**
     * Guarda en la base de datos local una transacción
     *
     * @param conn
     * @param idSurtidor
     * @param idCombustible
     * @param litros
     * @param fecha_hora
     * @param costo
     * @return
     */
    public static int CrearTransaccion(String ip, int idSurtidor, int idCombustible, int litros, int costo, String fecha_hora)
    {
        Statement stmt = null;
        int idTransaccion = -1;
        try
        {
            ServidorChat.conn.setAutoCommit(false);
            //bandera
            System.out.println("Transaccion: " + ip + "," + idSurtidor + "," + idCombustible + "," + litros + "," + costo + ","+fecha_hora);
            //end bandera

            stmt = ServidorChat.conn.createStatement();
            String sql = "INSERT INTO transaccion (id_estacion, id_surtidor, id_combustible, litros, costo, fecha_hora) VALUES ( '" + ip + "', " + idSurtidor + ", " + idCombustible + ", " + litros + ", " + costo + ", '" + fecha_hora + "' );";
            stmt.execute(sql);
            ResultSet rs = stmt.executeQuery("SELECT id FROM transaccion ORDER BY id DESC LIMIT 1;");

            if (rs.next())
            {
                idTransaccion = Integer.parseInt(rs.getString("id"));
            }
            System.out.println("Id transaccion:  " + idTransaccion);
            stmt.close();
            ServidorChat.conn.commit();
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

    /**
     * Obtiene la fecha y hora actual del sistema
     * @return fecha
     */
    public static String ObtenerFechaYHoraActual()
    {
        String formato = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern(formato);
        LocalDateTime ahora = LocalDateTime.now();
        return formateador.format(ahora);
    }
}
