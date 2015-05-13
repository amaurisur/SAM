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
package MeasurementMethods;

import java.util.HashMap;

/**
 *
 * @author coca
 * Este metodo sirve para evaluar la existencia de secciones en una pagina de 
 * la documentación sobre desarrolada sobre media wiki. Por ejemplo podemos 
 * evaluar la existencia de las 6 secciones para la documentacion de las vistas.
 * Primary Presentation, Element Catalog, Context Diagram, Variability Guide, 
 * Rationale, Related Views
 * 
 */
public class EvalSectionsView extends MeasurementMethod {

    public EvalSectionsView(String name) {
        super(name);
    }

    @Override
    /**
     * elements: recive en
     *                  pos1: nombre de la pagina
     *                  pos2: elemento XML que busca, por ejemplo sections, categories
     *                  pos3: atributos que quiera validar de ese elemento. (opcional) tipo hash
     *                  pos4: texto asociado al elemento XML.
     *  Ejemplo, debria evaluar:
     *      		<categories>
     *                       <cl sortkey="" missing="" xml:space="preserve">Model_View</cl>
     *                       <cl sortkey="" missing="" xml:space="preserve">Presentacion_Primaria</cl>
     *                       <cl sortkey="" missing="" xml:space="preserve">ElementCatalog</cl>
     *                   </categories>
     */
    public boolean check(HashMap param) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
