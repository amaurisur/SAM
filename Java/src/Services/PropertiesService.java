package Services;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class PropertiesService {
    
    private static HashMap propertiesHm = new HashMap();
    
    public static Properties Load(String propertiesFile){
        
        if(propertiesHm.containsKey((String)propertiesFile)){
            return (Properties)propertiesHm.get(propertiesFile);
        }
        
        Properties prop = new Properties();
	InputStream input = null;

	try { 
            input = new FileInputStream("src\\Resources\\" + propertiesFile + ".properties");
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
        
        propertiesHm.put(propertiesFile, prop);
        return prop;
    }
}
