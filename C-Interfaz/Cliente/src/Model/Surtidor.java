/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

/**
 *
 * @author elyna
 */
public class Surtidor {
    private int id;
    private int Estacion;
    private int numeroSurtidor;
    private int numeroTransacciones;

    public Surtidor(int Estacion, int numeroSurtidor, int numeroTransacciones) {
        this.Estacion = Estacion;
        this.numeroSurtidor = numeroSurtidor;
        this.numeroTransacciones = numeroTransacciones;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEstacion() {
        return Estacion;
    }

    public void setEstacion(int Estacion) {
        this.Estacion = Estacion;
    }

    public int getNumeroSurtidor() {
        return numeroSurtidor;
    }

    public void setNumeroSurtidor(int numeroSurtidor) {
        this.numeroSurtidor = numeroSurtidor;
    }

    public int getNumeroTransacciones() {
        return numeroTransacciones;
    }

    public void setNumeroTransacciones(int numeroTransacciones) {
        this.numeroTransacciones = numeroTransacciones;
    }
    
    
    
}
