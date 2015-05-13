/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Models;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Iterator;
/**
 *
 * @author Daniel Altamirano
 */
public class CompositeIndicator extends Indicator{
    @Expose @SerializedName("children") private ArrayList<Indicator> indicators = new ArrayList<Indicator>();
    
    public CompositeIndicator(String name) {
        super(name);
    }

    final public void add(Indicator i) {        
        indicators.add(i);
    }
    
    final public void remove(Indicator i) {
        indicators.remove(i);
    }
    
    final public double percentageValidated() {
        return percentage;
    }

    @Override
    public void eval() {
        Iterator<Indicator> iter = indicators.iterator();
        
        if(indicators.isEmpty()){
            percentage = 0;
            return;
        }
        
        double result = 0;
        
        while(iter.hasNext()){
            Indicator indicator = iter.next();
            indicator.eval();
            result += indicator.percentageValidated();
        }
        
        percentage = result/indicators.size();
    }
}
