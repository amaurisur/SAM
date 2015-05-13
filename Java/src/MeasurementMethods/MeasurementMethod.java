package MeasurementMethods;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class MeasurementMethod {
    String name;
    
    public MeasurementMethod(String name){
        this.name = name;
    }
    abstract public boolean check(HashMap param);
    
}
