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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

/**
 *
 * @author Daniel altamirano
 */
public class MediaWikiService {
    
    private static Properties properties = LoadProperties();
    private static String ApiUrl = properties.getProperty("mediawikiApi");
    
    public static String GetWikiContent(HashMap hm){
        Iterator it = hm.keySet().iterator();
        String url = ApiUrl;
        if(it.hasNext()){
            url += "?";
        }
        while(it.hasNext()){
            String key = (String)it.next();
            String value = (String)hm.get(key);
            url += key + "=" + value;
        }
        
        String result = "";
        
        try{
            result = sendGet(url);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        
        return result;
    }
    
    private static String sendGet(String url) throws Exception {
        
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);

        // add request header
        // request.addHeader("User-Agent", USER_AGENT);

        HttpResponse response = client.execute(request);

        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + 
            response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }
    
    private static Properties LoadProperties(){
        Properties prop = new Properties();
	InputStream input = null;
 
	try { 
            input = new FileInputStream("services.properties");

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
