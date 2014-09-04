/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sam;

import java.util.ArrayList;

/**
 *
 * @author sarli
 */
public class Metrica_Compuesta extends Indicador{

    private ArrayList<Indicador> indicadores;
    public Metrica_Compuesta(String nombre) {
        super(nombre);
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
        this.posicion += 1;
        for (int i = 0; i < this.indicadores.size(); i++){
            this.indicadores.get(i).posicion += 1;
        }
        
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
