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
public class Estacion {
    private String nombre;
    private int id;

    public Estacion(String nombre) {
        this.nombre = nombre;
    }
    
    public Estacion(String nombre, int id) {
        this.nombre = nombre;
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public synchronized boolean save(Connection conn) {
        Statement stmt = null;
        try
        {
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            //guarda el nuevo combustible
            stmt.executeUpdate("INSERT INTO estacion (nombre) VALUES ('"+nombre+"' );");

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
