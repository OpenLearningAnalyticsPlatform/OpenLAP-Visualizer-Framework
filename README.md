## INTRODUCTION
The Open Learning Analytics Platform (OpenLAP) supports extensibility and modularity by allowing developers and researches to add new Analytics Methods, Visualizations Techniques and Analytics Modules. The OpenLAP follows a simple principle of Inversion of Control: Once the new component is implemented and the binaries are uploaded, it is possible for the OpenLAP to use it at runtime, allowing developers and researches to extend the functionalities of the OpenLAP. 

The Visualizer component is responsible for managing the repository of all available visualization techniques in the OpenLAP. The OpenLAP-Visualizer-Framework project contains the necessary classes that the developers can extend, implement, pack in JAR and upload to add new visualization technique to the OpenLAP. The following video gives the introduction to the OpenLAP followed by the tutorial to add new visualization technique to the OpenLAP.

<p align="center">
	<a href="http://www.youtube.com/watch?feature=player_embedded&v=e6dwtO0mneo" target="_blank">
		<span><strong>Video Tutorial to add new Visualization techniques to the OpenLAP</strong></span>
		<br>
		<img src="http://lanzarote.informatik.rwth-aachen.de/openlap/img/others/visualizer.png" width=480 alt="OpenLAP Introduction and New Visualizer"/>
	</a>
</p>

## Fundamental Concepts
The main idea behind the Visualizer is to receive the incoming analyzed data in the OpenLAP-DataSet format and convert it to the format consisting of HTML and JavaScript code which can be visualized on the client side. To clarify the concept, take a look at the example below which uses Google Charts API to generate a bar chart. 

```javascript
<!-- Visualization Library Script -->
<script type='text/javascript' src='https://www.google.com/jsapi?autoload=%7B%22modules%22%3A%5B%7B%22name%22%3A%22visualization%22%2C%22version%22%3A%221%22%2C%22packages%22%3A%5B%22corechart%22%5D%7D%5D%7D'></script>

<!-- Visualization Generation Script -->
<div id='chartdiv'></div>
<script type='text/javascript'>
	// Visualization data
	var data = google.visualization.arrayToDataTable([
		['Title', 'Count'], ['Topic Assignment.pdf', 124], ['Orga.pdf', 119],
		['Overview.pdf', 105], ['Assignment 4.pdf', 59], ['Assignment 3.pdf', 53],
		['Presentation.pdf', 52], ['Assignment Template.docx', 45],
		['Assignment Template.pdf', 39], ['Node_js_final.pdf', 37], ['Handout.pdf', 36] ]);
		
	//Visualization options
	var options = {
		is3D: true, vAxis: { title: 'Count' }, hAxis: { title: 'Documents' },
		chartArea: { width: '95%', height: '135', left: '50', top: '10' }, legend: { position: 'none' },
		width: 440, height: 200, backgroundColor: { fill: 'transparent' }};
	
	//Visualization generation
	var chart = new google.visualization.ColumnChart(document.getElementById('chartdiv'));
	chart.draw(data, options);
</script>
```
The code consists of two main section, the `Visualization Library Script` section responsible for including the visualization library scripts on the webpage and the `Visualization Generation Script` section containing the code to generate the chart. The `Visualization Generation Script` section is further divided into three sub-sections: the `Visualization data` sub-section consists of the data to generate the graph. This section should always be generated dynamically based on the input data coming in the input `OLAPDataSet`. The `Visualization options` sub-section contains the options to define the chart. Which parameters can be customized is explained later in the step by step guide to implement new visualization technique. The `Visualization generation` section contains the scripts which uses the data and the options to generate the chart. This example is specifically using Google Charts to explain the concept, but any visualization technique can be categorized into these sections and implemented. 

Before going further into the details, here is a list of terminologies which will be helpful to understand this guide and how they are interacting with each other:

* <strong>VisualizationFramework</strong>: A web visualization library or framework which can be utilized to create interactive visualizations. E.g. D3.js, Google Charts, dygraphs etc.

* <strong>DataTransformer</strong>: A concrete implementation which transforms data received from the client in the form of the `OLAPDataSet` into a data structure understandable by the VisualizationMethod that uses it.

* <strong>VisualizationMethod</strong>: A concrete interactive visualization type. E.g. bar chart, pie chart etc.



To implement a new visualization technique, the developer must extend the abstract class `VisualizationCodeGenerator` and an interface `DataTransformer` available in the OpenLAP-Visualizer-Framework project. In the following sub-sections the OpenLAP-DataSet and the methods of these classes are explained in detail.

### OpenLAP-DataSet
The OpenLAP-DataSet is the internal data exchange format used in the OpenLAP. It is a modular JSON based serializable dataset to validate and exchange data between different components of the OpenLAP. Since the modular approach is used to develop the OpenLAP, different components act with relative independence from each other. Thus, a data exchange model is needed which can easily be serialized to and from JSON.

The OpenLAP-DataSet is implemented under the class name `OLAPDataSet`. It is a collection of columns represented using the class `OLAPDataColumns`. Each column consists of two distinctive sections. A metadata section contains id, type, required flag, title and description of the column encapsulated in a class `OLAPColumnConfigurationData`. The second section is the data itself, represented as an array of the specified type. More details are available on the [OpenLAP-DataSet project](https://github.com/OpenLearningAnalyticsPlatform/OpenLAP-DataSet) page. Concrete examples to initialize and read data from OpenLAP-DataSet is given below in step by step guide to implement a new Visualization Technique.

### Methods of the `VisualizationCodeGenerator` abstract class
The `VisualizationCodeGenerator` abstract class has a series of methods that allows new classes that extend it to be used by the OpenLAP.

#### Implemented Methods

* The ` isDataProcessable()` method takes an `OLAPPortConfiguration` as parameter and validate if the `OLAPDataSet` with the specified configuration can be processed or not.

* The `generateVisualizationCode()` method takes an `OLAPDataSet`, an `OLAPPortConfiguration` and a `DataTransformer` as parameters. The incoming `OLAPDataSet` is used as an input data which is transformed using the `DataTransformer` if the `OLAPPortConfiguration` is valid. The transformed data is then processed using the `visualizationCode()` method to produce the “Visualization Generation Script” (as discussed in the Fundamental Concepts section).

* The `getVisualizationLibraryScript()` method returns the “Visualization Library Script” (as discussed in the Fundamental Concepts section) by calling the `visualizationLibraryScript()` method. 

* The `getInput()` method allow other classes to obtain the input `OLAPDataSet` which can be used to get the columns metadata as `OLAPColumnConfigurationData` class.

#### Abstract Methods

* The `initializeDataSetConfiguration()` method is where the developer will define the column configuration of the input  `OLAPDataSet`.

* The `visualizationCode()` is the core method where the developer will implement the logic to convert the data transformed using the `DataTransformer` in the “Visualization Generation Script” which is returned as a string. This method is called by the `generateVisualizationCode()` method described above.

* The `visualizationLibraryScript()` method is where the developer will provide the “Visualization Library Script” section of the implementing visualization technique.

### Methods of the `DataTransformer` interface class
The `DataTransformer` interface class provides a single method `transformData()` to be implemented which accept the `OLAPDataSet` as input parameter. The developers will implement the logic here to transform the incoming `OLAPDataSet` to any data structure which will be used by the `visualizationCode()` method of the ‘VisualizationCodeGenerator’ abstract class to generate the “Visualization Generation Script”. The return of this method is a class `TransformedData<T>` which contains a single object `dataContent` of type `<T>` to store the transformed data.


#### Important Note

The concept of the abstract `VisualizationCodeGenerator` class and the `DataTransformer` interface should be clear from the above description. In the `VisualizationCodeGenerator` class the developer defines the input `OLAPDataSet` column configuration, the core logic to generate the “Visualization Generation Script”, and provide “Visualization Library Script”. Whereas, in the `DataTransformer` the developer implement the conversion of input `OLAPDataSet` to suitable data structure which can be used to generate “Visualization Generation Script” in the `VisualizationCodeGenerator` class. So the important point here is that the input `OLAPDataSet` is defined in the `VisualizationCodeGenerator` class but it is not used there. It is used in the `DataTransformer` and the transformed data from this interface class is then used in the `VisualizationCodeGenerator`.

## Step by step guide to implement a new Analytics Method

The following steps must be followed by the developer to implement a new Analytics Method for the OpenLAP:

1. Setting up the development environment.

2. Creating project and importing the dependencies into it.

3. Create a class that extends the `VisualizationCodeGenerator` class.

4. Define the input `OLAPDataSet`.

5. Create a class that implements the `DataTransformer` interface.

6. Implement the method of the `DataTransformer` interface.

7. Implement the remaining abstract methods of the `VisualizationCodeGenerator` class.

8. Pack the binaries into a JAR bundle.

9. Upload the JAR bundle using the OpenLAP administration panel along with the configuration.

These steps are explained in more details with concrete examples in the following sections.

### Step 1. Setting up the development environment
To develop a new analytics method, you need to install the following softwares.
* [Java Development Kit (JDK) 7+](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* Any Integrated Development Environment (IDE) for Java development, such as, [Intellij IDEA](https://www.jetbrains.com/idea/download), [NetBeans](https://netbeans.org/downloads/), [Eclipse](https://eclipse.org/downloads/), etc. 

In the following steps we are going to use the Intellij IDEA for developing the sample analytics method using maven.

### Step 2. Creating project and importing the dependencies into it.
* Create a new project. `File -> New -> Project`
* Select `Maven` from the left and click `Next`.
* Enter the `GroupId`, `ArtifactId` and `Version`, e.g.

	`GroupId`: de.rwthaachen.openlap.visualizers.googlecharts
	
	`ArtifactId`: GoogleCharts
	
	`Version`: 1.0-SNAPSHOT
	
* Specify project name and location, e.g.

	`Project Name`: Google-Charts
	
	`Project Location`: C:\Users\xxxxx\Documents\IdeaProjects\Google-Charts
	
* Add JitPack repository to the `pom.xml` file.

Maven:
```xml
<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
</repositories>
```

* Add dependency of the OpenLAP-Visualizer-Framework project to the ‘pom.xml’ file. The latest version of the dependency xml can be retrieved from the [![](https://jitpack.io/v/OpenLearningAnalyticsPlatform/OpenLAP-Visualizer-Framework.svg)](https://jitpack.io/#OpenLearningAnalyticsPlatform/OpenLAP-Visualizer-Framework). 

Maven:
```xml
	<dependency>
	    <groupId>com.github.OpenLearningAnalyticsPlatform</groupId>
	    <artifactId>OpenLAP-Visualizer-Framework</artifactId>
	    <version>v2.2.1</version>
	</dependency>
```

### Step 3. Create a class that extends the `VisualizationCodeGenerator` class.
In the project create a class that extends the `VisualizationCodeGenerator` abstract class as shown in the example below. The class should be contained in a package within the `src` folder to avoid naming conflicts.

```java
package de.rwthaachen.openlap.visualizers.googlecharts;

import de.rwthaachen.openlap.visualizer.framework.VisualizationCodeGenerator;
import de.rwthaachen.openlap.visualizer.framework.exceptions.VisualizationCodeGenerationException;
import de.rwthaachen.openlap.visualizer.framework.model.TransformedData;

import java.util.Map;

public class BarChart extends VisualizationCodeGenerator {
    protected void initializeDataSetConfiguration() {
		...
    }

    protected String visualizationCode(TransformedData<?> transformedData, Map<String, Object> map) throws VisualizationCodeGenerationException {
		...
    }

    protected String visualizationLibraryScript() {
		...
    }
}
```
### Step 4. Define the input and output `OLAPDataSet`.
The input `OLAPDataSet` should be defined in the `initializeDataSetConfiguration()` method of the extended class `BarChart` as shown in the example below.

```java
// Declaration of input OLAPDataSet by adding OLAPDataColum objects with the OLAPDataColumnFactory
@Override
protected void initializeDataSetConfiguration() {
    this.setInput(new OLAPDataSet());
    try {
        this.getInput().addOLAPDataColumn(
                OLAPDataColumnFactory.createOLAPDataColumnOfType("xAxisStrings", OLAPColumnDataType.STRING, true, "X-Axis Items", "List of items as string to be displayed on the X-Axis of the graph")
        );
        this.getInput().addOLAPDataColumn(
                OLAPDataColumnFactory.createOLAPDataColumnOfType("yAxisValues", OLAPColumnDataType.INTEGER, true, "Y-Axis Values", "List of items as integer to be displayed on the Y-Axis of the graph")
        );
    } catch (OLAPDataColumnException e) {
        e.printStackTrace();
    }
}
```

### Step 5. Create a class that implements the `DataTransformer` interface.
In the project create a class that implements the `DataTransformer` interface class as shown in the example below. The class should be contained in a package within the `src` folder to avoid naming conflicts.

```java
package de.rwthaachen.openlap.visualizers.googlecharts;

import DataSet.OLAPDataSet;
import de.rwthaachen.openlap.visualizer.framework.DataTransformer;
import de.rwthaachen.openlap.visualizer.framework.exceptions.UnTransformableData;
import de.rwthaachen.openlap.visualizer.framework.model.TransformedData;

public class DataTransformerPairList implements DataTransformer {
    public TransformedData<?> transformData(OLAPDataSet olapDataSet) throws UnTransformableData {
		...
    } 
}
```

### Step 6. Implement the method of the `DataTransformer` interface.
The `DataTransformer` interface class provide only one method to be implemented. The example below shows a sample implementation of the data transformer which accepts the input `OLAPDataSet` defined in the `initializeDataSetConfiguration()` method of the extended class `BarChart` in Step 4. This data transformer will transform this input `OLAPDataSet` in a list of string and integer pairs (List<Pair<String, Integer>>) and return it as an object of `TransformedData` class.

```java
public TransformedData<?> transformData(OLAPDataSet olapDataSet) throws UnTransformableData {
    List<OLAPDataColumn> columns = olapDataSet.getColumnsAsList(true);
    List<Pair<String, Integer>> data = new ArrayList<Pair<String, Integer>>();

    List<String> labels = null;
    List<Integer> frequencies = null;

    //Storing the data of column type string into labels list and the data of column type integer into frequencies list
    //The reason for storing them in List is to access those using indexes 
    for(OLAPDataColumn column: columns) {
        if (column.getConfigurationData().getType().equals(OLAPColumnDataType.INTEGER)) {
            frequencies = column.getData();
        } else {
            labels = column.getData();
        }
    }

    //Storing the same indexed labels and frequencies as pairs in the List
    if(labels != null) {
        for (int i = 0; i < labels.size(); i++) {
            data.add(new Pair<String, Integer>(labels.get(i), frequencies.get(i)));
        }
    }
    
    //Creating TransformedData object of type List<Pair<String, Integer>>, setting its DataContent object and returning 
    TransformedData<List<Pair<String, Integer>>> transformedData = new TransformedData<List<Pair<String, Integer>>>();
    transformedData.setDataContent(data);
    return transformedData;
}
```

### Step 7. Implement the remaining abstract methods of the `VisualizationCodeGenerator` class.
Two remaining abstract methods of the `VisualizationCodeGenerator` class should be implemented because now we have defined the `DataTransformer` and we know in what format the data will be available in the `visualizationCode()` method. The example below shows a sample implementation of the bar chart visualization technique using the Google Chart visualization framework to generate the “Visualization Generation Script”.

```java
@Override
protected String visualizationCode(TransformedData<?> transformedData, Map<String, Object> map) throws VisualizationCodeGenerationException {
    List<Pair<String, Integer>> transformedPairList = (List<Pair<String, Integer>>)transformedData.getDataContent();

    //This is used as a postfix for each variable name used in the javascript to avoid conflicts when having multiple visualizations on the same webpage.
    long postfix = (new Date()).getTime();

    //The Map<String, Object> map input parameters provide additional parameters from the client side.
    //Currently the following four parameters are being provided which can be used as shown below.
    int width = (map.containsKey("width")) ? Integer.parseInt(map.get("width").toString()) : 500;
    int height = (map.containsKey("height")) ? Integer.parseInt(map.get("height").toString()) : 350;
    String xLabel = (map.containsKey("xLabel")) ? map.get("xLabel").toString() : "";
    String yLabel = (map.containsKey("yLabel")) ? map.get("yLabel").toString() : "";

    //Generating the "Visualization Generation Script"
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("<script type='text/javascript'> var data_" + postfix + " = google.visualization.arrayToDataTable([['Title','Count'],");

    //Dynamically adding the "Visualization data" sub-section
    for(Pair<String, Integer> pair: transformedPairList) {
        stringBuilder.append("['" + pair.getKey() + "'," + pair.getValue() + "],");
    }
    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
    stringBuilder.append(" ]);");

    //Adding the "Visualization options" sub-section with available four parameters
    stringBuilder.append("var options_" + postfix + " = {is3D:true, vAxis:{title:'" + yLabel + "'}, hAxis:{title:'" + xLabel + "'}, chartArea:{width: '95%', height: '" + (height - 75) + "', left:'50', top:'10'}, legend:{ position:'none' }, width: " + (width - 10) + ", height: " + (height - 10) + ", backgroundColor: { fill:'transparent' }};");

    //Adding the "Visualization generation" sub-section
    stringBuilder.append("var chart_" + postfix + " = new google.visualization.ColumnChart(document.getElementById('chartdiv_" + postfix + "'));google.visualization.events.addListener(chart_" + postfix + ", 'ready', function (){$('#chartdiv_" + postfix + " > div:first-child > div:nth-child(2)').css({ top: '1px', left:'1px'});});chart_" + postfix + ".draw(data_" + postfix + ", options_" + postfix + ");");
    stringBuilder.append("</script>");
    stringBuilder.append("<div id='chartdiv_" + postfix + "'></div>");

    return stringBuilder.toString();
}

@Override
protected String visualizationLibraryScript(){
    //Returning the "Visualization Library Script" section
    return "<script type='text/javascript' src='https://www.google.com/jsapi?autoload=%7B%22modules%22%3A%5B%7B%22name%22%3A%22visualization%22%2C%22version%22%3A%221%22%2C%22packages%22%3A%5B%22corechart%22%5D%7D%5D%7D'></script>";
}
```

#### Step 8. Pack the binaries into a JAR bundle.

The complied binaries must be packed into a JAR bundle. It should be noted that the file name of the JAR bundle should consists of integers and characters only. The JAR bundle can easily be generated in the Intellij IDEA by following the following steps:
* Open the `Run/Debug Configurations`. `Run -> Edit Configurations…`
* Add new configuration by pressing the `+` on the top left.
* Select `Maven` from the available options.
* Set the `Name` to "Generate JAR" (without double quotes).
* On the `Parameters` tab set `Command line` = clean install
* Run the project by pressing `Shift + F10` or from `Run -> Run 'Generate JAR'`
* The JAR bundle will be generated in the `targer` folder within the project directory.
* Rename the generated JAR bundle to contain only integers and characters.

#### Step 7. Upload the JAR bundle to the OpenLAP.
The newly implemented visualization technique is now ready to be uploaded to the OpenLAP through the administration panel including the JAR file and parameters such as:
* Visualization framework name (E.g. Google Charts).
* Visualization framework description (E.g. Visualizations using the Google Charts library).
* List of associated visualization methods and data transformers.
** Visualization method name (E.g. Bar Chart).
** Implementing class name including package that extends the `VisualizationCodeGenerator` abstract class (E.g. de.rwthaachen.openlap.visualizers.googlecharts.BarChart).
** Data transformer name (E.g. Pairs List Data Transformer).
** Implementing class name including package that implements the `DataTransformer` interface class (E.g. de.rwthaachen.openlap.visualizers.googlecharts.DataTransformerPairList).

Note: The same implemented `DataTransformer` can be used with the multiple Visualization methods. All you need to do is provide the new implementation of the ‘VisualizationCodeGenerator’ abstract class (E.g. Pie Chart) and pack it with the JAR Bundle. While uploading the jar file, provide two visualization methods with the same data transformer name and the Implementing class name as shown in an example below.
```                
        {
          "visualizationFrameworks": [
            {
              "name": "Google Charts",
              "creator": "OpenLAP Team",
              "description": "A framework to providing visualizations using Google Charts library",
              "visualizationMethods": [
                {
                  "name": "Pie Chart",
                  "implementingClass": "de.rwthaachen.openlap.visualizers.googlecharts.PieChart",
                  "dataTransformerMethod": {
                    "name": "Pairs List Data Transformer",
                    "implementingClass": "de.rwthaachen.openlap.visualizers.googlecharts.DataTransformerPairList"
                  }
                },
                {
                  "name": "Bar Chart",
                  "implementingClass": "de.rwthaachen.openlap.visualizers.googlecharts.BarChart",
                  "dataTransformerMethod": {
                    "name": "Pairs List Data Transformer",
                    "implementingClass": "de.rwthaachen.openlap.visualizers.googlecharts.DataTransformerPairList"
                  }
                }
              ]
            }
          ]
        }       
```
