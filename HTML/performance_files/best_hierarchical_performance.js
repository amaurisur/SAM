console.log("Starting main script...")

// Setting up json data
var jsonRoot = "/js/hierarchical_visualisation_2"
//var jsonRoot = "./"
var normFac = 90
var fontSize = 14
var lineSpace = 2
var boxHeight = 90 // 60
var boxWidth = 195 // 130
var infoBoxHeight = boxHeight*4.5
var infoBoxWidth = boxWidth*4.5
var width = 1366
var height = 760

var yscale_performancebar = d3.scale.linear()
                                .domain([0,1])
                                .rangeRound([boxHeight/2,-boxHeight/2])
//metodo de ordenamiento
// Deveria usarlo para ordenar los nodos al mismo nivel
function comparator(a,b){
	// Order nodes alphabetically
	return a.name < b.name ? -1 : a.name > b.name ? 1 : 0;
}
//metodo espaciado en el arbol
function separation(a, b) {
	  return (a.parent == b.parent ? 1 : 2);
	}
//define el arbol                                
var tree = d3.layout.tree()
    .size([height,height])
	.sort(comparator)
	.separation(separation)

//diagonal de la grafica
var diagonal = d3.svg.diagonal()
    .projection(function(d) { return [d.y, d.x]; });

//crea la grafica que se va a caracterizar segun update
var svg = d3.select("body").append("svg")
    .attr("width", width)
    .attr("height", height)
  .append("g")// para agrupar los elementos dentro del svg
    .attr("transform", "translate(110,0)"); //Mueve los elementos sobre x e y

//update('datos.json')
update('file.json')

function update(fileName) {
    //busca el archivo con los datos o sea el json
    var jsonPath = [jsonRoot, fileName].join("/")

    // Load a json file, and perform the following function when it loads
    console.log('Loading json file: '.concat(jsonPath))
    d3.json(jsonPath, function(error, root) {

        // Retorna un array de nodos asociados a root
        var nodes = tree.nodes(root),
            links = tree.links(nodes);

	    //DATA JOIN: Bind existing objects to new data
        // Agrega clases tipo nodo y enlaces al SVG
        var existingLinks = svg.selectAll(".link")
    		                     .data(links)
        var existingNodes = svg.selectAll(".node")
                                .data(nodes)
        //children se cambio por children
        //No estaría funcionando ver
        var totalInst = d3.sum(root.children.map(function(d){return d.porcentaje}))
        normFac = 1*totalInst/boxHeight

        //UPDATE: Update old elements (before making new ones)

        //ENTER: Create new objects where necessary
        //Agrega el elmento svg path para digamos definir el camino del arco
        //Le agrega el estilo link

        existingLinks.enter().append("path")
            .attr("class", "link")
            .attr("d", diagonal)

        // Create a box for each classification node, and assign properties
        newNodes = existingNodes.enter().append("g")
        newNodes
            //Agrego el atributo class de tipo nodo
            .attr("class", "node")
            .attr("id", function(d){console.log(d.name);console.log("  Coordenadas antes del translate: (" + d.x +" , "+ d.y + ")\n");return d.name})
            .attr("transform", function(d) { return ("translate(" + d.y + "," + d.x + ")") })
            .append("rect")
                .attr('class', 'nodebox')
                .attr("x", -boxWidth/2)
                .attr("y", -boxHeight/2)
                .attr("width", boxWidth)
                .attr("height", boxHeight)
        newNodes.append("rect")
            .attr('id', 'performancebar')
            .attr("x", boxWidth/2*1.05)
            .attr("width", boxWidth/10)
            .style("fill", "red")
            .style("stroke", "red")
            .attr("y", boxHeight/2)
            .attr("height", 0)

        //Informacion en los nodos
        newNodes.append("text")
            .attr("id", "tituloMetrica")
            .attr("class", "tituloMetrica")
            .attr("y", -boxHeight/2 + fontSize + 2*lineSpace)
            .attr("text-anchor", "middle")

        newNodes.append("text")
            .attr("text-anchor", "middle")
            .attr("class", "nodeText")
            .attr("id", "porcentaje")
            .attr("y", -boxHeight/2 + 2*fontSize + 4*lineSpace)

        newNodes.append("text")
            .attr("text-anchor", "middle")
            .attr("class", "nodeText")
            .attr("id", "descripcionMetrica")


/*        newNodes.append("g")
            .attr("class", "confusionmatrix")
            .attr("id", "confusionmatrix")
            .selectAll("g").data(function(d){return d.params.confusionmatrix})
            .enter().append("g")
            .attr("class", "rows")
            .attr("transform", function(d,i) { return "translate(0," + (-boxHeight/2 + (i+4)*fontSize+(i+2)*lineSpace) + ")"; })
            .selectAll("g").data(function(d){return d})
            .enter().append("g")
            .attr("class", "columns")
            .attr("transform", function(d,i) { return "translate(" + i*3*fontSize + ",0)"; })
            .append("text")
            .attr("text-anchor", "middle")
            .attr("class", "nodeText")*/

        //ENTER + UPDATE: Update all nodes with new attributes (text, edge width)
        existingNodes.select('#performancebar')
            .transition()
            .duration(1050)
            .attr("y", function(d){
                            //Define el valor de la posicion de y segun la escala
                            return yscale_performancebar(d.porcentaje)
                            })
            .attr("height", function(d){
                            //Rellena segun el valor recibido
                            return boxHeight/2 - yscale_performancebar(d.porcentaje)
                            })

        existingLinks
            .transition()
            .duration(1000)
            .style("stroke-width", function(d){return ((d.porcentaje*100)*60)/100})
        existingNodes.select("#tituloMetrica")
            .text(function(d){return d.name/*.split("_").slice(-1)*/})

        existingNodes.select("#porcentaje")
            .text(function(d){return node1Text(d)})

        existingNodes.select("#descripcionMetrica")
            .text(function(d){return d.descripcion})


/*        // Update confusion matrix
        existingNodes.select("#confusionmatrix")
            .selectAll(".rows")
            .data(function(d){return d.params.confusionmatrix})
                .selectAll(".columns") //rows
                .data(function(d){return d})
                    .select("text")
                    .text(function(d){return d})*/


        //Overwrite data in root node, to give the "Tree" f1-score
        var rootNode = svg.select("#RootNode")

        var rootParams = rootNode.data()[0]
        //Update root node performance bar
          rootNode.select('#performancebar')
          .transition()
          .duration(1000)
          .attr("y", function(d){
              return yscale_performancebar(rootParams["porcentaje"])
          })
          .attr("height", function(d){
              return boxHeight/2 - yscale_performancebar(rootParams["porcentaje"])
          })
        rootNode.select("#porcentaje")
            //.text("Total Calidad Documentacion Cubierta:")
            //.append("text")
            .text(rootText)

        // Highlight a node if we mouse-over it, and display the information box
        newNodes.on("mouseenter", function() {
            thisNode = d3.select(this)
            displayInfoBox(thisNode)
            thisNodeCol = thisNode.select(".nodebox").style("stroke")
            thisNode.selectAll(".nodebox")
                .transition()
                .duration(1000)
                .style("fill", thisNodeCol)
            // 	            .style("opacity", 0.6)
            svg.selectAll(".link")
                .transition().duration(1000)
                .style("stroke", thisNodeCol)
                .style("stroke-width",  getLinkWidthClass)
        })

        newNodes.on("mouseleave", function(){
            destroyInfoBox()
            d3.select(this).selectAll(".nodebox")
                .transition()
                .duration(250)
                .style("fill", null)
                .style("opacity", null)
            svg.selectAll(".link")
                .transition().duration(250)
                .style("stroke", null)
                .style("stroke-width", function(d){return (d.porcentaje)})
        })

    });

    // Display up the info box (for mouse overs)
	function displayInfoBox(node) {
		//var nodeName = node.attr("#descripcionMetrica")
        var nodeName = thisNode.data()[0]["descripcion"]
        var infoX = infoBoxWidth/2*0.6
        var infoY = infoBoxHeight/2*1.05
		var infoBox = svg.append("g")
		infoBox
            .attr("class", "popup")
            .attr("transform", function(d) {return "translate(" + infoX + "," + infoY + ")";})

		infoBox
            .append("text")
            .attr("y", -infoBoxHeight/2 + fontSize + 2*lineSpace)
            .attr("text-anchor", "middle")
            .text(nodeName)
            .attr("font-size", fontSize + 8 + "px")

/*        var imgOffsetX = -infoBoxWidth/2 * 0.95
        var imgOffsetY = -infoBoxHeight/2 + fontSize+8 + 2*lineSpace
		infoBox
            .append("svg:image")
        	.attr("xlink:href", "sample_patches/"+nodeName+".png")
            .attr("width", infoBoxWidth*0.99)
            .attr("height", infoBoxHeight*0.99)
            .attr("transform", function(d) {return "translate(" + imgOffsetX + "," + imgOffsetY + ")";})*/
	}


    // Destroy the imfo box (when the mouseover ends)
    function destroyInfoBox() {
		svg.selectAll(".popup")
			.remove()
	}

    // Format f1score message
    //valor de la metrica, o sea porcentaje a mostrar
    function node1Text(d) {
        var f1Score = d.porcentaje
        return "porcentaje: "+ d3.format("0.1f")(f1Score*100) + "%"
    }
    
    // Format root node message
    //valor de la metrica para el indicador final, o sea nodo raiz
    function rootText(d) {
        var f1Score = d.porcentaje
        return d3.format("0.1f")(f1Score*100) + "%"
    }

    // Calculate the width of the edge between nodes
    function getLinkWidthClass(node) {
    	var className = thisNode.attr("id")
    	var rootNode = d3.select('#RootNode')[0][0].__data__
    	//var allInstOfThisClass = d3.sum(rootNode.children.map(function(d){return d.params.numInstToThisNode[className]}))
        //var thisNodeTPs = node.target.params.numInstToThisNode[className]
        //var myWidth = thisNodeTPs * boxHeight / allInstOfThisClass
        var ancho = d3.select('#RootNode')[0][0].__data__.porcentaje
        return d3.format("0.1f")((ancho*100)*90/100)
        // var ancho = d3.select('#RootNode')[0][0].__data__.porcentaje.format("0.1f")
        // return ancho*90/100
    }
}

d3.select(self.frameElement).style("height", height + "px");
