/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Models;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
/**
 *
 * @author Daniel Altamirano
 */
public class CompositeIndicator extends Indicator{
    @Expose @SerializedName("children") private ArrayList<Indicator> indicators = new ArrayList<Indicator>();
    
    public CompositeIndicator(String name) {
        super(name);
    }

    @Override
    final public void add(Indicator i) {        
        indicators.add(i);
    }
    
    @Override
    final public void remove(Indicator i) {
        indicators.remove(i);
    }

    @Override
    public void eval() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
