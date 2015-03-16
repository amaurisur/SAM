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
public abstract class Indicator {
    @Expose protected String name = "Mi nombre es: ";
    @Expose protected int position = 0;
    @Expose protected double percentage = 0;
    @Expose protected String description = "Descripcion: TODO";

    public Indicator (String name){
        this.name = name;  
    }
    
    abstract public void add(Indicator i);
    abstract public void remove(Indicator i);
    abstract public void eval();
}
