package MeasurementMethods;

import Services.MediaWikiService;
import Services.PropertiesService;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

   /**
    * @author coca
    * 
    * @sectionViewTags: Recibe desde un archivo tipo propertie los tag validos de secciones para 
    *                   documentacion de una vista Arquitectonica.
    * @ sections: Es un conjunto instanciado con los tag validos que deberia 
    *             tener una vista.
    * Ejemplo, debria evaluar retornando true para la siguiente vista de ejemplo:
    *                     <api>
    *                       <parse title="OPC Module Decomposition View">
    *                           <categories>
    *                              <cl sortkey="" missing="" xml:space="preserve">View</cl>
    *                              <cl sortkey="" xml:space="preserve">Module</cl>
    *                              <cl sortkey="" missing="" xml:space="preserve">PrimaryPresentation</cl>
    *                              <cl sortkey="ViewSectionPrimaryPresentation" hidden="" xml:space="preserve">VSPP</cl>
    *                              <cl sortkey="" missing="" xml:space="preserve">ElementCatalog</cl>
    *                              <cl sortkey="" missing="" xml:space="preserve">ContextDiagram</cl>
    *                              <cl sortkey="" missing="" xml:space="preserve">VariabilityGuide</cl>
    *                              <cl sortkey="" missing="" xml:space="preserve">Rationale</cl>
    *                              <cl sortkey="" missing="" xml:space="preserve">RelatedView</cl>
    *                           </categories>
    *                      </parse>
    *                     </api>
    */
public class EvalCategoriesView extends MeasurementMethod{
    
    private static EvalCategoriesView INSTANCE = null;
    private  final Properties sectionViewTags = PropertiesService.Load("sectionView");
    private  Set sections = null;
    
    private  EvalCategoriesView(String name){
        super(name);
    }
    
    private synchronized static void createInstance(String name){
        if (INSTANCE == null){
            INSTANCE = new EvalCategoriesView(name);
        }
    }
    
    public static EvalCategoriesView getInstance(String name){
        if (INSTANCE == null) createInstance(name);
        return INSTANCE;
    }
    
    @Override
    public boolean check(HashMap param) {   
        /**
         * En param esta recibiendo estos parametros
         * param.put("format", "xml");
         * param.put("action", "parse");
         * param.put("page", "OPC_Module_Decomposition_View");
         * param.put("prop", "categories");
         */
        String XML = MediaWikiService.GetWikiContent(param);
        sections = new HashSet(sectionViewTags.values());
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document page;
        
        try {
            builder = factory.newDocumentBuilder();
            InputSource xmlElements = new InputSource();
            xmlElements.setCharacterStream(new StringReader(XML));
            page = builder.parse(xmlElements);
            
            NodeList tagCategory;
            tagCategory = page.getElementsByTagName((String) param.get("prop"));
            NodeList childCategory = tagCategory.item(0).getChildNodes();
            
            for (int i = 0; i < childCategory.getLength(); i++)
            {
               Element cat = (Element) childCategory.item(i);
               if (sections.contains(cat.getTextContent())){
                   sections.remove(cat.getTextContent());
               }               
            }
            
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(EvalCategoriesView.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return (sections.isEmpty())&&(sections!=null);
        
    }    
}
