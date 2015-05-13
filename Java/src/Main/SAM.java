package Main;

import Models.SimpleIndicator;
import Models.Indicator;
import Models.CompositeIndicator;
import Models.Indicators.EDE;
import Services.MediaWikiService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

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
        
        Indicator i = new CompositeIndicator("Indicador QA-Document");
        Indicator i_1 = new CompositeIndicator("Metrica Compuesta 1");
        Indicator i_2 = new CompositeIndicator("Metrica Compuesta 2");
        Indicator i_1_1 = new SimpleIndicator("Metrica Simple 1.1",1.0);
        Indicator i_1_2 = new SimpleIndicator("Metrica Simple 1.2",1.0);
        
        /* Prueba integracion EDE */
        Indicator ede = new EDE("Ede",0.0);

        i_1.add(i_1_1);
        //i_1.add(i_1_2);
        i_1.add(ede); //Prueba integracion EDE
        i.add(i_1);
        i.add(i_2);
        
        //Corro los check's
        i.checkList();
        
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
