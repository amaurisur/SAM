/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Models;
import com.google.gson.annotations.Expose;
/**
 *
 * @author Daniel Altamirano
 */
public abstract class Indicador {
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
