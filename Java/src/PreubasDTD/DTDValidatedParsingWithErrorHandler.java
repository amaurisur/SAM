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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class DTDValidatedParsingWithErrorHandler
{
    private static final StreamSource XML = new StreamSource("C:\\Users\\coca\\Desktop\\SAM-ConsultasPagAPI\\View1.xml");
    
    public static void main(String[] argv) throws Exception
    { 
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     factory.setValidating(true);
     //factory.setIgnoringElementContentWhitespace(true);

     DocumentBuilder builder = factory.newDocumentBuilder();
     MyErrorHandler e = new MyErrorHandler(null);
     builder.setErrorHandler(e);
     //Document doc = builder.parse(new File("C:\\Users\\coca\\Desktop\\SAM-ConsultasPagAPI\\View1.xml"));
     
       
            Document doc = builder.parse(new File("C:\\Users\\coca\\Desktop\\SAM-ConsultasPagAPI\\View1.xml"));
//             NodeList rows = doc.getDocumentElement().getChildNodes();
//             for (int i = 0; i < rows.getLength(); i++)
//                {
//                 Element row = (Element) rows.item(i);
//                 NodeList cells = row.getChildNodes();
//                 for (int j = 0; j < cells.getLength(); j++)
//                    {
//                        Element cell = (Element) cells.item(j);
//                    }
//                }            


//        XMLInputFactory factory = XMLInputFactory.newInstance();
//        //get Reader connected to XML input from somewhere..
//        try {
//            XMLStreamReader streamReader =
//            factory.createXMLStreamReader(XML);
//        } catch (XMLStreamException e) {
//            System.out.println("XXXXXXXXXXXXXXXXXXXX");
//            e.printStackTrace();
//            System.out.println("XXXXXXXXXXXXXXXXXXXX");
//        }

        // this resource is opened here to separate the mechanics of finding it from the process of using it
        // note that the parser is responsible for closing the stream
        
        //InputStream xml = Thread.currentThread().getContextClassLoader().getResourceAsStream("C:\\Users\\coca\\Desktop\\SAM-ConsultasPagAPI\\View1.xml");

//        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//        //dbf.setNamespaceAware(true);
//        dbf.setValidating(true);
//
//        DocumentBuilder db = dbf.newDocumentBuilder();
//        db.setErrorHandler(new ErrorHandler()
//        {
//            @Override
//            public void fatalError(SAXParseException exception) throws SAXException
//            {
//                System.err.println("fatalError: " + exception);
//            }
//
//            @Override
//            public void error(SAXParseException exception) throws SAXException
//            {
//                System.err.println("error: " + exception);
//                
//            }
//
//            @Override
//            public void warning(SAXParseException exception) throws SAXException
//            {
//                System.err.println("warning: " + exception);
//            }
//        });
//        String xml = "C:\\Users\\coca\\Desktop\\SAM-ConsultasPagAPI\\View1.xml";
//        Document dom = db.parse(new InputSource(xml));
//        
//
//        System.out.println("root element name = " + dom.getDocumentElement().getNodeName());
//    }
//}


//import javax.xml.stream.XMLInputFactory;
//import javax.xml.stream.XMLStreamException;
//import javax.xml.stream.XMLStreamReader;
//import javax.xml.transform.stream.StreamSource;
//import org.xml.sax.ErrorHandler;
//import org.xml.sax.SAXException;
//import org.xml.sax.SAXParseException;
//import org.xml.sax.XMLReader;
//
///**
// *
// * @author coca
// */
//public class PruebasDTDStSAX {
//
//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String[] args) {
//        // TODO code application logic here
//        
//        XMLInputFactory factory = XMLInputFactory.newInstance();
//        
//
//        //get Reader connected to XML input from somewhere..
//        
//
//        try {
//
//        XMLStreamReader streamReader =
//        factory.createXMLStreamReader(XML);
//
//        } catch (XMLStreamException e) {
//        e.printStackTrace();
//        
//        }
//
//    }
// 
//    private static final StreamSource XML = new StreamSource("C:\\Users\\coca\\Desktop\\SAM-ConsultasPagAPI\\View1.xml");
//  
    }
    private static final class MyErrorHandler implements ErrorHandler {
       
       private final XMLStreamReader reader1;
       //private final XMLReader reader2;
       
       public MyErrorHandler(XMLStreamReader r){
           //reader2 = null;
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
         //System.out.println("   Message NOMBRE!: "+ reader1.getName());

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
//    
}