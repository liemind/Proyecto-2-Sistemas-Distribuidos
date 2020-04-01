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
 * @author elyna
 */
public class Transaccion {
    private int idEstacion;
    private int idSurtidor;
    private int idCombustible;
    private int litros;
    private int costo;
    private String fechaHora;
    private int id;
    
    public Transaccion(int idEstacion, int idSurtidor, int idCombustible, int litros, int costo) {
        this.idEstacion = idEstacion;
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

    public int getIdEstacion() {
        return idEstacion;
    }

    public void setIdEstacion(int idEstacion) {
        this.idEstacion = idEstacion;
    }
    
    public synchronized boolean save(Connection conn) {
        Statement stmt = null;
        try {
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            //guardar la transaccion
            stmt.executeUpdate("INSERT INTO transaccion (id_estacion, id_surtidor, id_combustible, litros, costo, fecha_hora) VALUES ("+idEstacion+","+idSurtidor+", "+idCombustible+", "+litros+", "+costo+", '"+fechaHora+"' );" );
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
