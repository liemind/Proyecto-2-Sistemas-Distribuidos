/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import empresa.Procesos;

/**
 *
 * @author Liemind
 */
public class Combustible
{

    private String nombre;
    private int costo;
    private int id;
    private int id_comb;
    private String fecha_hora;

    public Combustible(String nombre, int costo, String fecha_hora)
    {
        this.nombre = nombre;
        this.costo = costo;
        this.fecha_hora = fecha_hora;
    }

    public synchronized String getNombre()
    {
        return nombre;
    }

    public synchronized void setId(int i)
    {
        this.id = i;
    }

    public synchronized int getId()
    {
        return id;
    }

    public synchronized int getCosto()
    {
        return costo;
    }

    public synchronized void setNombre(String s)
    {
        this.nombre = s;
    }

    public synchronized void setCosto(int s)
    {
        this.costo = s;
    }

    public synchronized String getFecha_hora()
    {
        return fecha_hora;
    }

    public synchronized void setFecha_hora(String fecha_hora)
    {
        this.fecha_hora = fecha_hora;
    }

    public int getId_comb()
    {
        return id_comb;
    }

    public void setId_comb(int id_comb)
    {
        this.id_comb = id_comb;
    }
    
    
    

    /**
     * Actualiza en la base de datos el combustible del objeto. Las base de
     * datos ya contienen un combustible previamente creado.
     *
     * @param conn
     * @return
     */
    public synchronized boolean save(Connection conn)
    {
        Statement stmt = null;
        try
        {
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            //guarda el nuevo combustible
            String sql = "INSERT INTO combustible (nombre, costo, fecha_hora) "
                    + "VALUES ('" + nombre + "', " + costo + ", '" + fecha_hora+ "' );";
            //stmt.executeUpdate("UPDATE combustible SET costo = " + costo + " WHERE id = " + id + ";");
            stmt.execute(sql);
            
            sql = "select id from combustible where nombre = '" + nombre + "' and fecha_hora = '" + fecha_hora +"';";
            System.out.println("sql: " + sql);
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next())
            {
                this.id_comb += rs.getInt("id");
                System.out.println("id_comb: " + id_comb);
            }

            conn.commit();
            stmt.close();
            return true;
        }
        catch (Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
    }

}
