/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Main;

import Models.Metrica_Simple;
import Models.Indicador;
import Models.Metrica_Compuesta;
import Services.MediaWikiService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
/**
 *
 * @author Daniel Altamirano
 */
public class SAM {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Properties prop = LoadProperties();
        String path = prop.getProperty("jsonPath");
        
        MediaWikiService wikiService = new MediaWikiService();
        
        HashMap wikiParms = new HashMap();
        wikiParms.put("format", "xml");
        wikiParms.put("action", "parse");
        wikiParms.put("page", "Top_Level_Module_Uses_View");
        wikiParms.put("prop", "sections|categories");
        wikiService.GetWikiContent(wikiParms);
        
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
            try (FileWriter writer = new FileWriter(path)) {
                writer.write(json);
            }

        }catch(IOException e){
        }

        System.out.println(i);
    }    
    
    private static Properties LoadProperties(){
        Properties prop = new Properties();
	InputStream input = null;
 
	try { 
            input = new FileInputStream("src\\Resources\\sam.properties");

            // load a properties file
            prop.load(input);
	} catch (IOException ex) {
            ex.printStackTrace();
	} finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
	}
        
        return prop;
    }
}
