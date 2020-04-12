/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Calendar;

/**
 *
 * @author Liemind
 */
public class Transaccion { 
    private int idSurtidor;
    private int idCombustible;
    private int litros;
    private int costo;
    private String fechaHora;
    private int id;

    public Transaccion(int idSurtidor, int idCombustible, int litros, int costo) {
        this.idSurtidor = idSurtidor;
        this.idCombustible = idCombustible;
        this.litros = litros;
        this.costo = costo;
    }

    public int getIdSurtidor() {
        return idSurtidor;
    }

    public void setIdSurtidor(int idSurtidor) {
        this.idSurtidor = idSurtidor;
    }

    public int getIdCombustible() {
        return idCombustible;
    }

    public void setIdCombustible(int idCombustible) {
        this.idCombustible = idCombustible;
    }

    public int getLitros() {
        return litros;
    }

    public void setLitros(int litros) {
        this.litros = litros;
    }

    public int getCosto() {
        return costo;
    }

    public void setCosto(int costo) {
        this.costo = costo;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }
    
    public synchronized void setId(int i) {
        this.id = i;
    }

    public synchronized int getId() {
        return id;
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
            stmt.executeUpdate("INSERT INTO transaccion (id_surtidor, id_combustible, litros, costo, fecha_hora) VALUES ("+idSurtidor+", "+idCombustible+", "+litros+", "+costo+", '"+fechaHora+"' );" );
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
