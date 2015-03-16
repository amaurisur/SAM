/*
 * Copyright (C) 2015 Mr. Leinad
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package Services;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

/**
 *
 * @author Mr. Leinad
 */
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
