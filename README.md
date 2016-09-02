# OpenLAP-Visualizer-Framework

## Introduction

The OpenLAP-Visualizer-Framework provides the classes which needs to be implemented by Developers/Researchers to add new visualizations to the OpenLAP. The following video gives the introduction to Open Learning Analytics Platform (OpenLAP) following with the step by step video guide to adding new visualizations techniques to OpenLAP.

<p align="center">
	<a href="http://www.youtube.com/watch?feature=player_embedded&v=e6dwtO0mneo" target="_blank">
		<span><strong>Video Tutorial to add new Visualization techniques to OpenLAP</strong></span>
		<br>
		<img src="http://lanzarote.informatik.rwth-aachen.de/openlap/img/others/visualizer.png" width=480 alt="OpenLAP Introduction and New Visualizer"/>
	</a>
</p>

## Setting up the project

**Step 1.** The JitPack repository must be added to the build file:

Maven:
```xml
<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
</repositories>
```
Gradle:
```gradle
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```

**Step 2.**  The dependency must be added:

Maven:
```xml
	<dependency>
            <groupId>com.github.OpenLearningAnalyticsPlatform</groupId>
            <artifactId>OpenLAP-Visualizer-Framework</artifactId>
            <version>v2.0</version>
        </dependency>
```
Gradle:
```gradle
dependencies {
	        compile 'com.github.OpenLearningAnalyticsPlatform:OpenLAP-Visualizer-Framework:v2.0'
}
```

## Framework Internals

The OpenLAP-Visualizer follows the same principles as the entire OpenLAP and that is to be modular and extensible which gives the Developers/Researchers the possibility to add new `DataTransformers` and `VisualizationMethods` along with their `VisualizationFrameworks` to the OpenLAP-Visualizer component. The framework consists of an abstract class `VisualizationCodeGenerator` and an interface `DataTransformer`. 
Before going further into details, here is a list of terminologies which will be helpful to understand this guide:
<ul>
    <li><strong>VisualizationFramework</strong> : A web visualization library/framework which can be utilized to create interactive visualizations. For example, d3.js, dygraphs etc.</li>
    <li><strong>DataTransformer</strong> : A concrete implementation which transforms data received from the client in the form of the OLAPDataSet into a structure understandable by the VisualizationMethod that uses it</li>
    <li><strong>VisualizationMethod</strong> : A concrete interactive visualization, for example, bar chart, pie chart etc.</li>
</ul>

#### DataTransformer
The `DataTransformer` is an integral part of the OpenLAP-Visualizer-Framework as it transforms data from the `OLAPDataSet` into structure expected by `VisualizationCodeGenerators`. A single `DataTransformer` could be used
by many `VisualizationCodeGenerators`. The `DataTransformer` interface lists all the methods that need to be implemented by a concrete implementation, which is shown below:
```java
public interface DataTransformer {

    /**
     * @param olapDataSet The dataset which needs to be transformed in a
     *                    structure that is understood by the visualization code
     * @return null if the data could not be transformed
     * */
    TransformedData<?> transformData(OLAPDataSet olapDataSet) throws UnTransformableData;

}
```
The sample below shows a concrete implementation of the `DataTransformer` interface:
```java
    @Override
    public TransformedData<?> transformData(OLAPDataSet olapDataSet) throws UnTransformableData {
        List<OLAPDataColumn> columns = olapDataSet.getColumnsAsList(true);
        List<Pair<String, Integer>> data;

        List<String> labels = null;
        List<Integer> frequencies = null;

        data = new ArrayList<Pair<String, Integer>>();

        for(OLAPDataColumn column: columns) {
            if (column.getConfigurationData().getType().equals(OLAPColumnDataType.INTEGER)) {
                frequencies = column.getData();
            } else {
                labels = column.getData();
            }
        }

        if(labels != null) {
            for (int i = 0; i < labels.size(); i++) {
                data.add(new Pair<String, Integer>(labels.get(i), frequencies.get(i)));
            }
        }
        TransformedData<List<Pair<String, Integer>>> transformedData = new TransformedData<List<Pair<String, Integer>>>();
        transformedData.setDataContent(data);
        return transformedData;
    }
```

#### VisualizationCodeGenerator
This abstract class is part of the OpenLAP-Visualizer-Framework and has to be extended by the Developer if he/she wishes to add a new VisualizationMethod. The abstract class already contains some logic which makes sure to perform some checks before calling the relevant concrete implementations of the abstract methods. Furthermore to call the methods in the correct order. The listing below shows an excerpt of the VisualizationCodeGenerator abstract class:

```java
public abstract class VisualizationCodeGenerator {

    private OLAPDataSet input;
    private OLAPDataSet output;

    protected abstract void initializeDataSetConfiguration();

    public boolean isDataProcessable(OLAPPortConfiguration olapPortConfiguration) throws DataSetValidationException {
        //...
    }

    protected abstract String visualizationCode(TransformedData<?> transformedData) throws VisualizationCodeGenerationException;

    public String generateVisualizationCode(OLAPDataSet olapDataSet, DataTransformer dataTransformer) throws VisualizationCodeGenerationException, UnTransformableData {
        //...
    }
}

```
The two abstract methods that need to be overriden are:
 <ul>
 <li>initializeDataSetConfiguration</li>
 A sub class of the `VisualizationCodeGenerator` should provide the OLAPDataSet configuration that the code generator being implemented expects. Defining the OLAPDataSet configuration actually means providing the definition of how the 
 `OLAPDataSet` looks like. For the purpose of the `VisualizationCodeGenerator` it is sufficient to provide only the `input` configuration (without the actual data) as the output of the Visualizer is not in the form of the 
 `OLAPDataSet` but rather the client visualization code. The example below, shows a sample concrete implementation of the `initializeDataSetConfiguration()` method:
```java
     @Override
     protected void initializeDataSetConfiguration() {
         this.setInput(new OLAPDataSet());
         this.getInput().addOLAPDataColumn(
              OLAPDataColumnFactory.createOLAPDataColumnOfType("xAxisStrings", OLAPColumnDataType.STRING, true, "X-Axis Items", "List of items to be displayed on X-Axis of the graph")
         );
         this.getInput().addOLAPDataColumn(
              OLAPDataColumnFactory.createOLAPDataColumnOfType("yAxisValues", OLAPColumnDataType.INTEGER, true, "Y-Axis Values", "List of items to be displayed on Y-Axis of the graph")
         );
     }
```
 <li>visualizationCode</li>
 The concrete implementations of this abstract method provide the actual visualization code that is send back to the client by the OpenLAP-Visualizer. The parameter that of this method is an instance of `TransformedData` which contains the data
 which was transformed in the format that the `VisualizationCodeGenerator` expects from the `OLAPDataSet`. The example below illustrates how to provide a concrete implementation of the `visualizationCode` abstract method:
```java
    @Override
    protected String visualizationCode(TransformedData<?> transformedData, Map<String, Object> map) throws VisualizationCodeGenerationException {
        List<Pair<String, Integer>> transformedPairList = (List<Pair<String, Integer>>)transformedData.getDataContent();

        long postfix = (new Date()).getTime();

		//Additional parameters will be sent using the Map<String, Object> map object. Following are the ones which are available currently.
        int width = (map.containsKey("width")) ? Integer.parseInt(map.get("width").toString()) : 500;
        int height = (map.containsKey("height")) ? Integer.parseInt(map.get("height").toString()) : 350;
        String xLabel = (map.containsKey("xLabel")) ? map.get("xLabel").toString() : "";
        String yLabel = (map.containsKey("yLabel")) ? map.get("yLabel").toString() : "";

		//Start generating the final output for visualizating the indicator
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<script type='text/javascript'> var data_" + postfix + " = google.visualization.arrayToDataTable([['Title','Count'],");

        for(Pair<String, Integer> pair: transformedPairList) {
            stringBuilder.append("['" + pair.getKey() + "'," + pair.getValue() + "],");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append(" ]);");
        stringBuilder.append("var options_" + postfix + " = {is3D:true, vAxis:{title:'" + yLabel + "'}, hAxis:{title:'" + xLabel + "'}, chartArea:{width: '95%', height: '" + (height - 75) + "', left:'50', top:'10'}, legend:{ position:'none' }, width: " + (width - 10) + ", height: " + (height - 10) + ", backgroundColor: { fill:'transparent' }};");
        stringBuilder.append("var chart_" + postfix + " = new google.visualization.ColumnChart(document.getElementById('chartdiv_" + postfix + "'));google.visualization.events.addListener(chart_" + postfix + ", 'ready', function (){$('#chartdiv_" + postfix + " > div:first-child > div:nth-child(2)').css({ top: '1px', left:'1px'});});chart_" + postfix + ".draw(data_" + postfix + ", options_" + postfix + ");");
        stringBuilder.append("</script>");
        stringBuilder.append("<div id='chartdiv_" + postfix + "'></div>");

        return stringBuilder.toString();
    }
```
 </ul>

## Usage Guide
The overall process of creating and uploading new `VisualizationMethods` and `DataTransformers` is as follows:
<ol>
    <li>Create a starter project by using Maven archetype project <a href="https://github.com/OpenLearningAnalyticsPlatform/OpenLAP-Visualizer-Starter">OpenLAP-Visualizer-Starter</a>. Or Import the dependency into a new Java project. </li>
    <li>Create a class that extends the `VisualizationCodeGenerator` and another class which extends `DataTransformer`.</li>
    <li>Declare the input `OLAPDataSet` in the class that extends the `VisualizationCodeGenerator`.</li>
    <li>Implement the abstract methods of both abstract classes.</li>
    <li>Package the binaries of the implementations as a JAR bundle</li>
    <li>Upload the JAR bundle using a REST client to the OpenLAP-Visualizer along with the configuration. The configuration is provided in the form of JSON and contains details of which `VisualizationCodeGenerator` and `DataTransformer` are being uploaded.
        An example configuration is shown below:
```                
        {
          "visualizationFrameworks": [
            {
              "name": "Google Charts",
              "creator": "Bassim Bashir",
              "description": "A framework to providing visualizations using Google Charts library",
              "visualizationMethods": [
                {
                  "name": "Pie Chart",
                  "implementingClass": "de.rwthaachen.openlap.GooglePieChartVisualizationCodeGenerator",
                  "dataTransformerMethod": {
                    "name": "Chart DataTransformer",
                    "implementingClass": "de.rwthaachen.openlap.ChartDataTransformer"
                  }
                },
                {
                  "name": "Bar Chart",
                  "implementingClass": "de.rwthaachen.openlap.GoogleBarChartVisualizationCodeGenerator",
                  "dataTransformerMethod": {
                    "name": "Chart DataTransformer",
                    "implementingClass": "de.rwthaachen.openlap.ChartDataTransformer"
                  }
                }
              ]
            }
          ]
        }       
```
If you look at the example configuration above you can see that there are multiple implementations of the OpenLAP-Visualizer-Framework that can be uploaded in a single go. The configuration specifies which `VisualizationMethod` uses which `DataTransformer` along with other information.
    </li>
    <li>The OpenLAP-Visualizer after verifying your upload will make it available for all clients. If the upload fails for any reason then you will be notified as to cause of the failure. Reasons for failure could be due to a malformed manifest configuration json provided with the upload or if the "implementingClassName" of a VisualizationMethod already exists (i.e. it was previously uploaded) etc.</li>
<ol>
