/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sam;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import java.io.FileWriter;
import java.io.IOException;
/**
 *
 * @author sarli
 */
public class Metrica_Simple extends Indicador {

    public Metrica_Simple(String nombre, double p) {
        super(nombre);
        porcentaje = p;
    }

    @Override
    public void agregar(Indicador i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
