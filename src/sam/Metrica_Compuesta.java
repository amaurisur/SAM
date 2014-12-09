/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sam;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

/**
 *
 * @author sarli
 */
public class Metrica_Compuesta extends Indicador{

    @Expose private ArrayList<Indicador> indicadores;
    
    public Metrica_Compuesta(String nombre) {
        super(nombre);
        indicadores = new ArrayList<Indicador>();
    }

    @Override
    public void agregar(Indicador i) {
        
        indicadores.add(i);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void eliminar(Indicador i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void evaluar() {
//        this.posicion += 1;
//        for (int i = 0; i < this.indicadores.size(); i++){
//            this.indicadores.get(i).posicion += 1;
//        }
//        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
//    @Override
//    public String draw(Gson g){
//        String s;
//        for (int i = 0; i < this.indicadores.size(); i++){
//            s =  this.indicadores.get(i).draw(g);
//        }
//        return "";
//    }
    
}
