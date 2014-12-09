/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sam;

import com.google.gson.Gson;
import java.io.FileWriter;
import java.io.IOException;
/**
 *
 * @author sarli
 */
public interface View {
    Gson json = new Gson();
    public String draw(Gson g);
}
