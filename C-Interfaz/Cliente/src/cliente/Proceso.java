/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

/**
 *
 * @author Liemind
 */
public class Proceso implements Runnable{
    private int id;
    
    public Proceso(int id) {
        this.id = id;
    }
    
    
    
    @Override
    public void run() {
        
            if(id == 0) {
                System.out.println("Proceso 0");
            }
            else if(id == 1) {
                System.out.println("Proceso 1");
            }
        
    }
    
    
    
}
