package Models;

public class SimpleIndicator extends Indicator {
    
    public SimpleIndicator(String name, double p) {
        super(name);
        percentage = p;
    }
    
    @Override
    final public void add(Indicator i) {}

    @Override
    final public void remove(Indicator i) {}

    @Override
    public void checkList() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        percentage = 0.0;
    }
    
    @Override
    public void eval() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double percentageValidated() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
