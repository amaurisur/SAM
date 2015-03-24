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
package Models.Indicators;

import Models.SimpleIndicator;
import Services.MediaWikiService;
import Services.PropertiesService;
import java.util.HashMap;
import java.util.Properties;

import java.io.StringReader;
import java.net.URLEncoder;
 
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import org.basex.core.*;
import org.basex.core.cmd.*;

/**
 *
 * @author Daniel Altamirano
 */
public class LEP extends SimpleIndicator{

    private Properties architectureTags = PropertiesService.Load("architecturetags");
    
    public LEP(String name, double p) {
        super(name, p);
    }
    
    @Override
    public void eval() {
        try{
            FetchSystemOverviewPage();
        }
        catch(Exception e){
            
        }
    }
    
    private String FetchSystemOverviewPage() throws Exception{
        //Find page with System_overview category tag
        String systemOverviewTag = architectureTags.getProperty("system_overview");
        HashMap hm = new HashMap();
        hm.put("format", "xml");
        hm.put("action", "query");
        hm.put("list", "categorymembers");
        hm.put("cmtitle", "Category:" + systemOverviewTag);        
        String xquery = "data(/api/query/categorymembers/cm[1]/@title)";
        String systemOverviewPageTitle = MediaWikiService.ConsultWiki(hm, xquery);
        
        if(!systemOverviewPageTitle.equals("")){
            hm = new HashMap();
            hm.put("format", "xml");
            hm.put("action", "parse");
            hm.put("page", URLEncoder.encode(systemOverviewPageTitle, "UTF-8"));
            hm.put("prop", "sections");
            xquery = "data(/api/parse/sections/s/@line)";
            String pageSections = MediaWikiService.ConsultWiki(hm, xquery);
            
            return "";
        }
        else{
            throw new Exception("Page not found");
        }
    }
    
    private static Document convertStringToDocument(String xmlStr) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
        DocumentBuilder builder;  
        try 
        {  
            builder = factory.newDocumentBuilder();  
            Document doc = builder.parse( new InputSource( new StringReader( xmlStr ) ) ); 
            return doc;
        } catch (Exception e) {  
            e.printStackTrace();  
        } 
        return null;
    }
}
