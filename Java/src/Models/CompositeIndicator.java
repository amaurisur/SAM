package Models;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Iterator;

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
    final public double percentageValidated() {
        return percentage;
    }

    @Override
    public void checkList() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        Indicator indicator;
        
        for (int i=0; i < indicators.size(); i++){
            indicator = indicators.get(i);
            indicator.checkList();
            System.out.println("Ejecutando CheckList Compuesto: " + indicator.toString());
        }
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
