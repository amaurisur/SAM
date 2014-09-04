/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sam;

import java.util.ArrayList;
import java.util.List;

/**
 *{
  "params": {
    "treeF1Score": 0.2, 
    "confusionmatrix": [
      [
        226687.0, 
        4852.0
      ], 
      [
        4778.0, 
        18815.0
      ]
    ]
  }, 
  "name": "RootNode", 
  "children": [
  ]
}
 * @author sarli
 */
public class indicador {
    public String params = "Soy parametro";
    public String name = " Mi nombre es: ";
    public List<String> children = new ArrayList<String>(){
        {
        add("Hijo derecho");
        add("Hija izquierdo");
        }
    };
    @Override
    public String toString(){
        return "indicador {params=" + params + ",name=" + name + ",children=" + children +"}";
    }
    
}
