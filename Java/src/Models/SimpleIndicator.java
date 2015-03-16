/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Models;
/**
 *
 * @author Daniel Altamirano
 */
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
    public void eval() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
