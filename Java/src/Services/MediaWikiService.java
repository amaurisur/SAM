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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;

import org.basex.core.*;
import org.basex.core.cmd.*;
/**
 *
 * @author Daniel altamirano
 */
public class MediaWikiService {
    
    private static final String ApiUrl = PropertiesService.Load("services").getProperty("mediawikiApi");
    
    public static HashMap BuildCategoryQueryHashMap(String category){
        HashMap hm = new HashMap();
        hm.put("format", "xml");
        hm.put("action", "query");
        hm.put("list", "categorymembers");
        hm.put("cmtitle", "Category:" + category);
        return hm;
    }
    
    public static HashMap BuildPageSectionsQueryHashMap(String pageTitle){
        try {
            HashMap hm = new HashMap();
            hm.put("format", "xml");
            hm.put("action", "parse");
            hm.put("page", URLEncoder.encode(pageTitle, "UTF-8"));
            hm.put("prop", "sections");
            return hm;
        } catch (Exception e) {
            return null;
        }
    }
    
    public static String ConsultWiki(HashMap hm){
        Iterator it = hm.keySet().iterator();
        String url = ApiUrl;
        if(it.hasNext()){
            url += "?";
        }
        while(it.hasNext()){
            String key = (String)it.next();
            String value = (String)hm.get(key);
            url += key + "=" + value;
            if (it.hasNext()) {
                url += "&";
            }
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
    
    public static String ConsultWiki(HashMap hm, String xquery){
        Iterator it = hm.keySet().iterator();
        String url = ApiUrl;
        if(it.hasNext()){
            url += "?";
        }
        while(it.hasNext()){
            String key = (String)it.next();
            String value = (String)hm.get(key);
            url += key + "=" + value;
            if (it.hasNext()) {
                url += "&";
            }
        }
        
        String result = "";
        
        try{
            String webResponse = sendGet(url);
            
            Context context = new Context();
            new CreateDB("MediawikiConsult", webResponse).execute(context);
            result = new XQuery(xquery).execute(context);
            new DropDB("MediawikiConsult").execute(context);
            context.close();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        
        return result;
    }
    
    private static String sendGet(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }
}
