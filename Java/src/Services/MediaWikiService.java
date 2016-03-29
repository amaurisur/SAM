package Services;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import org.basex.core.Context;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.XQuery;

public class MediaWikiService {
    
    private static final String ApiUrl = PropertiesService.Load("services").getProperty("mediawikiApi");
    
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
