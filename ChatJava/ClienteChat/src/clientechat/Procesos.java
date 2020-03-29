/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientechat;

import Model.Combustible;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 *
 * @author yorch
 */
public class Procesos
{

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
    public static int CrearTransaccion(int idSurtidor, int idCombustible, int litros, int costo, String fecha_hora)
    {
        Statement stmt = null;
        int idTransaccion = -1;
        try
        {
            ClienteChat.conn.setAutoCommit(false);
            //bandera
            System.out.println("Transaccion: " + idSurtidor + "," + idCombustible + "," + litros + "," + costo + ","+fecha_hora);
            //end bandera

            stmt = ClienteChat.conn.createStatement();
            String sql = "INSERT INTO transaccion (id_surtidor, id_combustible, litros, costo, fecha_hora) VALUES (" + idSurtidor + ", " + idCombustible + ", " + litros + ", " + costo + ", '" + fecha_hora + "' );";
            stmt.execute(sql);
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
    public static String ObtenerTransaccion(int id)
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
                transaccion += rs.getString("id") + "," + rs.getString("id_surtidor") + "," + rs.getString("id_combustible") + "," + rs.getString("litros") + "," + rs.getString("costo")+ "," + rs.getString("fecha_hora");
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

    public static Combustible BuscarCombustible(String n)
    {
        ArrayList<Combustible> combustibles = new ArrayList<>();
        combustibles = ObtenerCombustibles();
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
     * Obtiene de la base de datos los combustibles del servidor.
     *
     * @return
     */
    public static synchronized ArrayList ObtenerCombustibles()
    {
        ArrayList<Combustible> combustibles = new ArrayList<>();
        Statement stmt = null;
        try
        {
            stmt = ClienteChat.conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM combustible;");

            while (rs.next())
            {
                Combustible cc = new Combustible(rs.getString("nombre"), rs.getInt("costo"), rs.getInt("id_comb_empresa"));
                cc.setId(rs.getInt("id"));
                combustibles.add(cc);
            }
            //end bandera
            rs.close();
            stmt.close();
            return combustibles;

        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Actualiza el numero de transacciones de un surtidor en la base de datos.
     *
     * @param id
     * @return
     */
    /*public static synchronized void GuardarSurtidor(int id)
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
    }*/

    

    public static String ObtenerFechaYHoraActual()
    {
        String formato = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern(formato);
        LocalDateTime ahora = LocalDateTime.now();
        return formateador.format(ahora);
    }

}
