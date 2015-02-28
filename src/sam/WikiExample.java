package sam;


import org.basex.core.*;
import org.basex.core.cmd.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This example demonstrates how databases can be created from remote XML
 * documents, and how XQuery can be used to locally update the document and
 * perform full-text requests.
 *
 * @author BaseX Team 2005-14, BSD License
 */
public final class WikiExample {
  /**
   * Runs the example code.
   * @param args (ignored) command-line arguments
   * @throws BaseXException if a database command fails
   */
  public static void main(final String[] args) throws BaseXException {
      
    /********************* pruebas json ************************/
    Indicador i = new Metrica_Compuesta("RootNode");
    Indicador i_1 = new Metrica_Compuesta("Metrica Compuesta 1");
    Indicador i_2 = new Metrica_Compuesta("Metrica COmpuesta 2");
    Indicador i_1_1 = new Metrica_Simple("Metrica Simple 1.1",1);
    Indicador i_1_2 = new Metrica_Simple("Metrica Simple 1.2",1);

    i_1.agregar(i_1_1);
    i_1.agregar(i_1_2);
    i.agregar(i_1);
    i.agregar(i_2);
    
    //i.evaluar();  
    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
    String json = gson.toJson(i);
    
    try{
     
     FileWriter writer = new FileWriter("C:\\xampp\\htdocs\\js\\hierarchical_visualisation_2\\file.json");
     writer.write(json);
     writer.close();
     
    }catch(IOException e){
        e.printStackTrace();
    }
    
    System.out.println(i);
  }
}
