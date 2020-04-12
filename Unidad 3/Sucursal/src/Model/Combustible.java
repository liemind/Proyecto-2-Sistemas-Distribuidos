/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.sql.Connection;
import java.sql.Statement;

/**
 *
 * @author Liemind
 */
public class Combustible
{

    private String nombre;
    private int costo;
    private int id;
    private int id_comb_empresa;

    public Combustible(String nombre, int costo, int id_comb_empresa)
    {
        this.nombre = nombre;
        this.costo = costo;
        this.id_comb_empresa = id_comb_empresa;
    }
    
    public synchronized void setNombre(String s)
    {
        this.nombre = s;
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

    public synchronized void setCosto(int s)
    {
        this.costo = s;
    }
    
    public synchronized int getCosto()
    {
        return costo;
    }

    public synchronized int getId_comb_empresa()
    {
        return id_comb_empresa;
    }

    public synchronized void setId_comb_empresa(int id_comb_empresa)
    {
        this.id_comb_empresa = id_comb_empresa;
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
            //guardar la transaccion
            stmt.executeUpdate("UPDATE combustible SET costo = " + costo + ", id_comb_empresa = " + id_comb_empresa + " WHERE nombre = '" + nombre + "';");
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
