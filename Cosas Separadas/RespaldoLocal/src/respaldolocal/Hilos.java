/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package respaldolocal;

import java.util.Calendar;

/**
 * Establece los horarios de respaldo y limpieza de respaldos.
 * El horario de respaldo es a las 3 de la mañana, todos los días.
 * El horario de limpieza son los días sábado, a las 4 de la mañana.
 * La cantidad de espera determinada son 30 minutos.
 * @author elyna
 */
public class Hilos extends Thread{
    static final int MIN_HORA_RESPALDO = 3;
    static final int MIN_MINUTO_RESPALDO = 1;
    static final int MAX_HORA_RESPALDO = 3;
    static final int MAX_MINUTO_RESPALDO = 59;
    
    static final int MIN_HORA_LIMPIEZA = 4;
    static final int MAX_HORA_LIMPIEZA = 4;
    static final int MIN_MINUTO_LIMPIEZA = 1;
    static final int MAX_MINUTO_LIMPIEZA = 59;
    
    static final int ESPERA = 30000;
    
    @Override
    public void run() {
        try {
            while(true) {
                Calendar calendario = Calendar.getInstance();
                int diaSemana = calendario.get(Calendar.DAY_OF_WEEK);
                int hora = calendario.get(Calendar.HOUR_OF_DAY);
                int minutos = calendario.get(Calendar.MINUTE);
                
                //1. consultar hora
                if(hora > MIN_HORA_RESPALDO && hora < MAX_HORA_RESPALDO) {
                    //2. si la hora esta entre los minutos de limpieza, hacer limpieza.
                    if(minutos > MIN_MINUTO_RESPALDO && minutos < MAX_MINUTO_RESPALDO){
                        RespaldoCliente.crearRespaldoCliente(calendario);
                    }
                }else {
                    System.out.println("aun no es hora del respaldo");
                }
                
                if(diaSemana == 6) {
                    //1. consultar hora
                    if(hora > MIN_HORA_LIMPIEZA && hora < MAX_HORA_LIMPIEZA) {
                        //2. si la hora esta entre los minutos de limpieza, hacer limpieza.
                        if(minutos > MIN_MINUTO_LIMPIEZA && minutos < MAX_MINUTO_LIMPIEZA){
                            Main.limpieza();
                        }
                    }
                }else {
                    System.out.println("aun no es hora de la limpieza");
                }
                sleep(ESPERA);
                
            }
        } catch (Exception e) {
        }
    }
}
