/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sam;

import com.google.gson.Gson;
import java.io.FileWriter;
import java.io.IOException;
/**
 *
 * @author sarli
 */
public class Metrica_Simple extends Indicador {
    
    private int valor;
    
    public Metrica_Simple(String nombre) {
        super(nombre);
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

//    @Override
//    public String draw(Gson g){
//        //Gson gson = new Gson();
//        String json = g.toJson(this);
//        return json;    
//    }
    
}
