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
        
    }
    
    private String FetchSystemOverviewPage(){
        String systemOverviewTag = architectureTags.getProperty("system_overview");
        HashMap hm = new HashMap();
        hm.put("format", "xml");
        hm.put("action", "query");
        hm.put("list", "categorymembers");
        hm.put("cmtitle", "Category:" + systemOverviewTag);
        String response = MediaWikiService.GetWikiContent(hm);
        
        
    }
}
