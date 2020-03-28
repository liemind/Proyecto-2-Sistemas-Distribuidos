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
public class Combustible { 
    private String nombre;
    private int costo;
    private int id;

    public Combustible(String nombre, int costo) {
        this.nombre = nombre;
        this.costo = costo;
    }
    
    public synchronized String getNombre() {
        return nombre;
    }

    public synchronized void setId(int i) {
        this.id = i;
    }

    public synchronized int getId() {
        return id;
    }

    public synchronized int getCosto() {
        return costo;
    }

    public synchronized void setNombre(String s) {
        this.nombre = s;
    }

    public synchronized void setCosto(int s) {
        this.costo = s;
    }

    /**
     * Actualiza en la base de datos el combustible del objeto. Las base de datos ya contienen un combustible previamente creado.
     * @param conn
     * @return
     */
    public synchronized boolean save(Connection conn) {
        Statement stmt = null;
        try {
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            //guardar la transaccion
            stmt.executeUpdate("UPDATE combustible SET costo = "+costo+" WHERE id = "+id+";");
            conn.commit();
            stmt.close();
            return true;
        }
        catch(Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            return false;
        }
    }

}
