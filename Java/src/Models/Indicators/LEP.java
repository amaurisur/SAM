package Models.Indicators;

import Models.SimpleIndicator;
import Services.MediaWikiService;
import Services.PropertiesService;
import java.util.HashMap;
import java.util.Properties;

public class LEP extends SimpleIndicator{

    private Properties architectureTags = PropertiesService.Load("architecturetags");
    
    public LEP(String name, double p) {
        super(name, p);
    }

    @Override
    public void checkList() {

	throw new UnsupportedOperationException("Not supported yet.");
        
    }
    

    @Override
    public void eval() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double percentageValidated() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String FetchSystemOverviewPage(){
        String systemOverviewTag = architectureTags.getProperty("system_overview");
        HashMap hm = new HashMap();
        hm.put("format", "xml");
        hm.put("action", "query");
        hm.put("list", "categorymembers");
        hm.put("cmtitle", "Category:" + systemOverviewTag);
        String response = MediaWikiService.GetWikiContent(hm);
        
        return response;
    }
    
}
