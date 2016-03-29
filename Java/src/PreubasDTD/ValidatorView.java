/*
 * Copyright (C) 2015 coca
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
package PreubasDTD;

/**
 *
 * @author coca
 */


import PruebasSAX.SAXLocalNameCount;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.w3c.dom.Document;
import org.xml.sax.DTDHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.XMLReader;

class ValidatorView {
    
    private static final StreamSource XML = new StreamSource("C:\\Users\\coca\\Desktop\\SAM-ConsultasPagAPI\\View1.xml");
    
   public static void main(String[] args) throws XMLStreamException, ParserConfigurationException, SAXException, IOException {
       
       
       SAXParserFactory spf = SAXParserFactory.newInstance();
       spf.setNamespaceAware(true);
       spf.setValidating(true);
       SAXParser saxParser = spf.newSAXParser();
       
        // Get the encapsulated SAX XMLReader
        XMLReader xmlReader = saxParser.getXMLReader();
        

        // Set the ContentHandler of the XMLReader
        //xmlReader.setContentHandler(new SAXLocalNameCount());

        // Set an ErrorHandler before parsing
        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(XML);
        ErrorHandler h = new MyErrorHandler(reader);
        
        xmlReader.setErrorHandler(h);
       

        // Tell the XMLReader to parse the XML document
        xmlReader.parse(convertToFileURL("C:\\Users\\coca\\Desktop\\SAM-ConsultasPagAPI\\View1.xml"));
        
       
//       
//      try {
//         File x = new File("C:\\Users\\coca\\Desktop\\SAM-ConsultasPagAPI\\View1.xml");
//         DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
//         f.setValidating(true); // Default is false
//         
//         DocumentBuilder b = f.newDocumentBuilder(); 
//        // ErrorHandler h = new DefaultHandler();
//         
//
//         b.setErrorHandler(h);
//         
//         Document d = b.parse(x);
//         
//      } catch (ParserConfigurationException | SAXException | IOException e) {
//         System.out.println(e.toString());      
//      }
   }
   
       
   private static class MyErrorHandler implements ErrorHandler {
       
       private final XMLStreamReader reader1;
       private final XMLReader reader2;
       
       public MyErrorHandler(XMLStreamReader r){
           reader2 = null;
           reader1 = r;
           
       }
       
      @Override
      public void warning(SAXParseException e) throws SAXException {
         System.out.println("Warning: "); 
         printInfo(e);
      }
      @Override
      public void error(SAXParseException e) throws SAXException {
         System.out.println("Error: "); 
         printInfo(e);
         System.out.println("   Message NOMBRE!: "+ reader1.getName());

      }
      @Override
      public void fatalError(SAXParseException e) throws SAXException {
         System.out.println("Fattal error: "); 
         printInfo(e);
      }
      private void printInfo(SAXParseException e) {
         System.out.println("   Public ID: "+e.getPublicId());
         System.out.println("   System ID: "+e.getSystemId());
         System.out.println("   Line number: "+e.getLineNumber());
         System.out.println("   Column number: "+e.getColumnNumber());
         System.out.println("   Message: "+e.getMessage());
         System.out.println("   Message: "+e.getLocalizedMessage());
         System.out.println("   Message: "+e.getClass());
         
      }
   }
   
   private static String convertToFileURL(String filename) {
        return (new File(filename).toURI().toString());
        
    }
}