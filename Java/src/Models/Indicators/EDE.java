package Models.Indicators;

import MeasurementMethods.EvalCategoriesView;
import MeasurementMethods.EvalTODOinSectionView;
import Models.SimpleIndicator;
import Services.MediaWikiService;
import Services.PropertiesService;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Para obtener las paginas categorizadas como vistas, tener cuidado de no 
 * incluir las paginas tipo categorias.
 * http://localhost/mediawiki/api.php?action=query&list=categorymembers&cmtitle=Category:view
 */
public class EDE extends SimpleIndicator{

    /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args) {

        EDE m1 = new EDE("EDE",0.0);
        m1.checkList();
    }

    public EDE(String name, double p) {
        super(name, p);
        this.measurementMethod1 = EvalCategoriesView.getInstance(name);
        this.measurementMethod2 = EvalTODOinSectionView.getInstance(name);
    }
    
    @Override
    public void checkList() {
        String namePage = "";
        boolean ch1_viewSection = false;
        boolean ch2_viewSectionWithTODO = false;
        boolean ch3_viewSectionWithTODOinRational = false;
                
        this.pagesName = getNameViewPages(); 
        Iterator pagesNameIterator = this.pagesName.iterator();
        
        HashMap paramQueryPage = new HashMap();
        paramQueryPage.put("format", "xml");
        paramQueryPage.put("action", "parse");
        paramQueryPage.put("prop", "categories");
        
        while (pagesNameIterator.hasNext()){

            namePage = (String)pagesNameIterator.next();
            System.out.printf("Pagina a evaluar checklist EDE", namePage);
            
            paramQueryPage.put("page", namePage.replace(" ", "_"));  
            //Check Secciones en las vista 
            ch1_viewSection = measurementMethod1.check(paramQueryPage);
            //TODO en Secciones de las vistas
            paramQueryPage.put("prop", "sections");
            ch2_viewSectionWithTODO = measurementMethod2.check(paramQueryPage);
            //Rational sin TODO
            //measurementMethod2.setSection(architectureTags);
            ch3_viewSectionWithTODOinRational = !measurementMethod2.rationalWithoutTodo();
            
        }
        //Ponderacion, lalala determinamos el valor final a visualizar.
        //percentage = 0.5;
        if (ch1_viewSection) {
            percentage += 0.25;
        }
        if (!ch2_viewSectionWithTODO) {
            percentage += 0.75;
        } else if (!ch3_viewSectionWithTODOinRational) {
            percentage += 0.5;
        } 
    }
    
    /**
     * Obtener las paginas tipo Vista (con Tag view. [[Category:view]])
     * 
     */ 
    private String getPagesTypeView(){
        
        String viewTag = this.architectureTags.getProperty("view");
        HashMap hm = new HashMap();
        hm.put("format", "xml");
        hm.put("action", "query");
        hm.put("list", "categorymembers");
        hm.put("cmtitle", "Category:" + viewTag);
        String response = MediaWikiService.GetWikiContent(hm);

//        response
//                = "<api>"
//                + "  <query>   "
//                    + " <categorymembers>    "
//                    + "  <cm pageid=\"3\" ns=\"0\" title=\"OPC Module Decomposition View\" />  "
//                    + "  <cm pageid=\"7\" ns=\"0\" title=\"Top Level Module Uses View\" />  "
//                    + "  <cm pageid=\"101\" ns=\"0\" title=\"ViewModel1\" />   "
//                    + "  <cm pageid=\"102\" ns=\"14\" title=\"Category:Module\" />  "
//                    + "  </categorymembers>"
//                + "  </query>"
//                + "</api>";
        //response = ("<?xml version=\"1.0\"?><api><query><categorymembers><cm pageid=\"101\" ns=\"0\" title=\"ViewModel1\" /><cm pageid=\"102\" ns=\"14\" title=\"Category:Module\" /></categorymembers></query></api>");
        return response;
    }
    
   /**
    * Retorna la lista de paginas exceptuando aquellas que tienen en title=Category:
    * <?xml version="1.0"?>
    *   <api>
    *     <query>
    *       <categorymembers>
    *         <cm pageid="3" ns="0" title="OPC Module Decomposition View" />
    *         <cm pageid="102" ns="14" title="Category:Module" />
    *       </categorymembers>
    *     </query>
    *   </api>
    */
    private ArrayList<String> getNameViewPages(){
        
        ArrayList<String> listNamePages = new ArrayList<>();
        
        try {
            String xmlPages = getPagesTypeView();
            System.out.println(xmlPages);
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource xmlElements = new InputSource();
            xmlElements.setCharacterStream(new StringReader(xmlPages));
            Document pages = builder.parse(xmlElements);
            
            NodeList pagesCandidate = pages.getElementsByTagName("categorymembers").item(0).getChildNodes();
            
            for (int i = 0; i < pagesCandidate.getLength(); i++)
            {
               Element page = (Element) pagesCandidate.item(i);
               if (!(page.getAttribute("title").contains("Category:"))){
                    listNamePages.add(page.getAttribute("title"));                   
               }  
            }
            
        } catch (ParserConfigurationException | SAXException | IOException ex) {
                Logger.getLogger(EDE.class.getName()).log(Level.SEVERE, null, ex);
        }

        listNamePages.trimToSize();
        return listNamePages;
    }
    
    private final Properties architectureTags = PropertiesService.Load("architecturetags");
    //Nombre de paginas tipo vista.
    private ArrayList pagesName;
    private EvalCategoriesView  measurementMethod1 = null;
    private EvalTODOinSectionView measurementMethod2 = null;
    
}
