package MeasurementMethods;

import Services.MediaWikiService;
import Services.PropertiesService;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
//import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
 * Evalua si la seccion de la vista, la cual esta etiquetadas segun las
 * correspondientes categorias que pueden delimitar secciones en una vista
 * tiene TODO o no.
 *                     <api>
 *                       <parse title="OPC Module Decomposition View">
 *                           <categories>
 *                              <cl sortkey="" missing="" xml:space="preserve">PrimaryPresentation</cl>
 *                              <cl sortkey="" missing="" xml:space="preserve">ElementCatalog</cl>
 *                              <cl sortkey="" missing="" xml:space="preserve">ContextDiagram</cl>
 *                              <cl sortkey="" missing="" xml:space="preserve">VariabilityGuide</cl>
 *                              <cl sortkey="" missing="" xml:space="preserve">Rationale</cl>
 *                              <cl sortkey="" missing="" xml:space="preserve">RelatedView</cl>
 *                           </categories>
 *                      </parse>
 *                     </api>
 */
public class EvalTODOinSectionView extends MeasurementMethod {

    private EvalTODOinSectionView(String name) {
        super(name);
        sectionNotVerify = new ArrayList<HashMap>();
    }

    private synchronized static void createInstance(String name){
        if (INSTANCE == null){
            INSTANCE = new EvalTODOinSectionView(name);
        }
    }

    public static void setSection(Properties section){
        EvalTODOinSectionView.sectionViewTags = section;
    }

    public boolean rationalWithoutTodo(){
        boolean rationalWithoutTodo = true;
        String XML="";

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document page;
        InputSource xmlElements = new InputSource();
        //builder = factory.newDocumentBuilder();
        //xmlElements.setCharacterStream(new StringReader(XML));
        if (sectionNotVerify != null && !sectionNotVerify.isEmpty()){
            Iterator<HashMap> it = sectionNotVerify.iterator();
            HashMap element;
            while(it.hasNext()){
                element = it.next();
                /**
                 * <api>
                 *  <parse title="ViewModel1">
                 *      <categories>
                 *          <cl sortkey="" missing="" xml:space="preserve">ElementCatalog</cl>
                 *      </categories>
                 *  </parse>
                 * </api>
                 */
                XML = MediaWikiService.GetWikiContent(element);
                try {
                    builder = factory.newDocumentBuilder();
                    xmlElements.setCharacterStream(new StringReader(XML));
                    page = builder.parse(xmlElements);

                    NodeList tagCategory;
                    tagCategory = page.getElementsByTagName("categories");
                    NodeList childCategory = tagCategory.item(0).getChildNodes();
                    for (int i = 0; i < childCategory.getLength(); i++) {
                        Element cat = (Element) childCategory.item(i);
                        if (cat.getTextContent().equals(sectionViewTags.getProperty("section5"))) {
                            rationalWithoutTodo = false;
                            System.out.printf("EvalTODOinSectionView - rationalWithoutTodo - False, La siguiente pagina y seccion no verifica",XML);
                        }
                    }
                } catch (ParserConfigurationException | SAXException | IOException ex) {
                    System.out.println("EvalTODOinSectionView - rationalWithoutTodo - Error en lectura xml");
                    Logger.getLogger(EvalTODOinSectionView.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return rationalWithoutTodo;
    }

    public static EvalTODOinSectionView getInstance(String name){
        if (INSTANCE == null) createInstance(name);
        return INSTANCE;
    }

    /**
     * http://localhost/mediawiki/api.php?format=xml&action=parse&page=ViewModel1&section=2&prop=categories
     *
     * Recorrer todas las secciones y una a una chequear que categoria tiene cada una.
     * Luego una vez identificada las secciones con sus categorias, recien ahi conociendo
     * la seccion correspondiente puedo saber el nombre de la misma o sea titulo,
     * de ahi puedo en text recorrer y obtener el texto asociado a la seccion en juego
     * y ver si tiene TODO o no.
     *
     * Pido las secciones de la pagina
     * http://localhost/mediawiki/api.php?format=xml&action=parse&page=ViewModel1&prop=sections
     * <api>
     *  <parse title="ViewModel1">
     *      <sections>
     *          <s toclevel="1" level="2" line="Primary Presentation" number="1" index="1" fromtitle="ViewModel1" byteoffset="51" anchor="Primary_Presentation"/>
     *          <s toclevel="1" level="2" line="Element Catalog" number="2" index="2" fromtitle="ViewModel1" byteoffset="173" anchor="Element_Catalog"/>
     *          <s toclevel="1" level="2" line="Context Diagram" number="3" index="3" fromtitle="ViewModel1" byteoffset="285" anchor="Context_Diagram"/>
     *          <s toclevel="1" level="2" line="Variability Guide" number="4" index="4" fromtitle="ViewModel1" byteoffset="398" anchor="Variability_Guide"/>
     *          <s toclevel="1" level="2" line="Rationale" number="5" index="5" fromtitle="ViewModel1" byteoffset="515" anchor="Rationale"/>
     *          <s toclevel="1" level="2" line="Related Views" number="6" index="6" fromtitle="ViewModel1" byteoffset="616" anchor="Related_Views"/>
     *      </sections>
     *  </parse>
     * </api>
     *
     * Obtener todas las secciones y por cada seccion con su numero ejecutar
     * la consulta de abajo y si machea con alguna categoria
     * tomar el nombre de la seccion y luego en text ver el tema TODO.
     *
     * http://localhost/mediawiki/api.php?format=xml&action=parse&page=ViewModel1&section=2&prop=categories
     * <api>
     *  <parse title="ViewModel1">
     *      <categories>
     *          <cl sortkey="" missing="" xml:space="preserve">ElementCatalog</cl>
     *      </categories>
     *  </parse>
     * </api>
     *
     * <api>
     *    <parse title="Top Level Module Uses View">
     *       <categories>
     *           <cl sortkey="" missing="" xml:space="preserve">View</cl>
     *           <cl sortkey="" missing="" xml:space="preserve">PrimaryPresentation</cl>
     *           <cl sortkey="" xml:space="preserve">Module</cl>
     *       </categories>
     *   </parse>
     * </api>
     */
    @Override
    public boolean check(HashMap param) {

        boolean todo = false;

        try {
            //Obtenemos las secciones de una pagina en particular.
            //http://localhost/mediawiki/api.php?format=xml&action=parse&prop=sections&page=ViewModel1
            String XML = MediaWikiService.GetWikiContent(param);
            //Buscamos las secciones validas de vista definidas para etiquetar en la wiki .
            /**
             * #Secciones para la documentación de una Vista
             * section1=PrimaryPresentation
             * section2=ElementCatalog
             * section3=ContextDiagram
             * section4=VariabilityGuide
             * section5=Rationale
             * section6=RelatedViews
             */
            sections = new HashSet(this.sectionViewTags.values());
            //Limpiamos la lista donde guardaremos las secciones que no verifican.
            sectionNotVerify.clear();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            Document page;

            builder = factory.newDocumentBuilder();
            InputSource xmlElements = new InputSource();
            xmlElements.setCharacterStream(new StringReader(XML));
               /**
                * <api>
                *   <parse title="Top Level Module Uses View">
                *     <sections>
                *       <s toclevel="1" level="2" line="Primary Presentation" number="1" index="1" fromtitle="Top_Level_Module_Uses_View" byteoffset="1" anchor="Primary_Presentation"/>
                *       <s toclevel="1" level="2" line="Element Catalog" number="2" index="2" fromtitle="Top_Level_Module_Uses_View" byteoffset="191" anchor="Element_Catalog"/>
                *       <s toclevel="1" level="2" line="Context Diagram" number="3" index="3" fromtitle="Top_Level_Module_Uses_View" byteoffset="2985" anchor="Context_Diagram"/>
                *       <s toclevel="1" level="2" line="Variability Guide" number="4" index="4" fromtitle="Top_Level_Module_Uses_View" byteoffset="3039" anchor="Variability_Guide"/>
                *       <s toclevel="1" level="2" line="Rationale" number="5" index="5" fromtitle="Top_Level_Module_Uses_View" byteoffset="3388" anchor="Rationale"/>
                *       <s toclevel="1" level="2" line="Related Views" number="6" index="6" fromtitle="Top_Level_Module_Uses_View" byteoffset="8241" anchor="Related_Views"/>
                *     </sections>
                *   </parse>
                * </api>
                */
            page = builder.parse(xmlElements);

            NodeList tagSection; //Guarda todas las secciones de la pagina.
            tagSection = page.getElementsByTagName((String) param.get("prop"));
            NodeList childSection = tagSection.item(0).getChildNodes();

            String numberSection = "";
            String paramCatSec = "";

            HashMap paramQueryPage = new HashMap();
            paramQueryPage.put("format", "xml");
            paramQueryPage.put("action", "parse");
            paramQueryPage.put("page", param.get("page"));
            paramQueryPage.put("prop", "categories");
            /**
             * <api>
             *  <parse title="Top Level Module Uses View">
             *      <categories>
             *          <cl sortkey="" missing="" xml:space="preserve">View</cl>
             *          <cl sortkey="" missing="" xml:space="preserve">PrimaryPresentation</cl>
             *          <cl sortkey="" xml:space="preserve">Module</cl>
             *          <cl sortkey="" missing="" xml:space="preserve">ElementCatalog</cl>
             *          <cl sortkey="" missing="" xml:space="preserve">ContextDiagram</cl>
             *          <cl sortkey="" missing="" xml:space="preserve">VariabilityGuide</cl>
             *          <cl sortkey="" missing="" xml:space="preserve">Rationale</cl>
             *          <cl sortkey="" missing="" xml:space="preserve">RelatedViews</cl>
             *      </categories>
             *  </parse>
             * </api>
             */

            InputSource categoriesSectionInputXML = new InputSource();
            Document pageSectionCategories;


            for (int i = 0; i < childSection.getLength(); i++)
            {
               Element section = (Element) childSection.item(i);
               numberSection = section.getAttribute("number");
               paramQueryPage.put("section", numberSection);
               //http://localhost/mediawiki/api.php?format=xml&action=parse&prop=categories&page=ViewModel1&section=2
               /**
                * <api>
                *  <parse title="ViewModel1">
                *   <categories>
                *    <cl sortkey="" missing="" xml:space="preserve">ElementCatalog</cl>
                *   </categories>
                *  </parse>
                * </api>
                */
               String categoriesSectionXML = MediaWikiService.GetWikiContent(paramQueryPage);
               categoriesSectionInputXML.setCharacterStream(new StringReader(categoriesSectionXML));
               pageSectionCategories = builder.parse(categoriesSectionInputXML);

               NodeList categories = pageSectionCategories.getElementsByTagName("categories").item(0).getChildNodes();
               String cat = "";

               InputSource sectionViewTextInputXML = new InputSource();
               Document pageSectionViewText;

               for(int j = 0; j < categories.getLength(); j++){
                   cat = categories.item(j).getTextContent();
                   //Si la seccion contiene una categoria que esta incluida en
                   //la categorias de secciones de documentacion de una vista,
                   //evaluar si tiene TODO.
                   if (sections.contains(cat)){
                        HashMap paramQueryPageTODO = new HashMap();
                        paramQueryPageTODO.put("format", "xml");
                        paramQueryPageTODO.put("action", "parse");
                        paramQueryPageTODO.put("page", param.get("page"));
                        paramQueryPageTODO.put("section", numberSection);
                        paramQueryPageTODO.put("prop", "text");
                        String sectionViewTextXML = MediaWikiService.GetWikiContent(paramQueryPageTODO);
                        sectionViewTextInputXML.setCharacterStream(new StringReader(sectionViewTextXML));
                        pageSectionViewText = builder.parse(sectionViewTextInputXML);

                        NodeList elementsByTagName = pageSectionViewText.getElementsByTagName("text");
                        for(int k=0; k<elementsByTagName.getLength()&&(!todo);k++){
                            todo = elementsByTagName.item(k).getTextContent().contains("TODO:")|| elementsByTagName.item(k).getTextContent().contains("TODO");
                        }
                        if (todo) {
                            //Guardo la pagina y seccion que no verifica
                            //Seguir aca, hacer un pequeño cambio en GetWikiContent
                            //agregando un conjunto de campos validos para la api.
                            paramQueryPageTODO.put("prop", "categories");
                            sectionNotVerify.add(paramQueryPageTODO);
                            todo = false;
                        }
                   }
               }

            }

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(EvalTODOinSectionView.class.getName()).log(Level.SEVERE, null, ex);
        }

        return !sectionNotVerify.isEmpty();
    }

    private static EvalTODOinSectionView INSTANCE = null;
    private  static Properties sectionViewTags = PropertiesService.Load("sectionView");
    private Set sections = null;
    private ArrayList<HashMap> sectionNotVerify = null;

}

  /**
    * http://localhost/mediawiki/api.php?format=xml&action=parse&page=ViewModel1&prop=text&section=2
    * <api>
    *     <parse title="ViewModel1">
    *        <text xml:space="preserve">
    *         <h2>
    *             <span class="mw-headline" id="Element_Catalog">Element Catalog</span>
    *             <span class="mw-editsection">
    *                 <span class="mw-editsection-bracket">[</span>
    *                     <a href="/mediawiki/index.php?title=ViewModel1&amp;action=edit&amp;section=1" title="Edit section: Element Catalog">edit</a><span class="mw-editsection-bracket">]</span>
    *             </span>
    *         </h2>
    *         <p>
    *             <i>TODO: This section can be organized as a dictionary where
    *                       each entry is an element of the Primary Presentation.
    *                       For each element, provide additional information and
    *                       properties that the readers would need that would not
    *                       fit in the Primary Presentation. Optionally, you can add
    *                       interface specifications and behavior diagrams
    *                       (e.g., UML sequence diagrams, statecharts).
    *             </i>
    *         </p>
    *        </text>
    *     </parse>
    * </api>
    */
