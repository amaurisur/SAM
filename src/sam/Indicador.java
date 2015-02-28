/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sam;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sarli
 */
public abstract class Indicador {
    //protected String params = "Soy parametro";
    @Expose protected String name = " Mi nombre es: ";
    @Expose protected int posicion = 0;
    @Expose protected double porcentaje = 0;
    @Expose protected String descripcion = "Descripcion: TODO";

    public Indicador (String nombre){
        this.name = nombre;  
    }
    abstract public void agregar(Indicador i);
    abstract public void eliminar(Indicador i);
    abstract public void evaluar();
   
}
