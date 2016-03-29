package Models;
import com.google.gson.annotations.Expose;

public abstract class Indicator {
    @Expose protected String name = "Indicator: ";
    @Expose protected int position = 0;
    @Expose protected double percentage = 0.0;
    @Expose protected String description = "Descripcion: TODO";

    public Indicator (String name){
        this.name = name;
    }
    
    abstract public void eval();   
    abstract public double percentageValidated();
    
    abstract public void add(Indicator i);
    abstract public void remove(Indicator i);
    abstract public void checkList();
    
}
