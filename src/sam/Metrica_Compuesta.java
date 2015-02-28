/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sam;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 *
 * @author sarli
 */
public class Metrica_Compuesta extends Indicador{

    @Expose @SerializedName("children")  private ArrayList<Indicador> indicadores;
    
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
   
}
