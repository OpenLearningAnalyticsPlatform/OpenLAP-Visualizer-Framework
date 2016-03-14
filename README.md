# OpenLAP-Visualizer-Framework

## Introduction

The OpenLAP-Visualizer-Framework provides the API which needs to be implemented by Developers/Researchers to add new visualizations to the OpenLAP-Visualizer. The concrete implementations of this framework can be packed and uploaded to the 
OpenLAP-Visualizer via an endpoint to be made available for all the users of the OpenLAP. 

## Framework Internals

The OpenLAP-Visualizer follows the same principles as the entire OpenLAP and that is to be modular and extensible which gives the Developers/Researchers the possibility to add new `DataTransformers` and `VisualizationMethods` along with their `VisualizationFrameworks` to the OpenLAP-Visualizer component. The framework consists of an abstract class `VisualizationCodeGenerator` and an interface `DataTransformer`. 
Before going further into details, here is a list of terminologies which will be helpful to understand this guide:
<ul>
    <li><strong>VisualizationFramework</strong> : A web visualization library/framework which can be utilized to create interactive visualizations. For example, d3.js, dygraphs etc.</li>
    <li><strong>VisualizationMethod</strong> : A concrete interactive visualization, for example, bar chart, pie chart etc.</li>
    <li><strong>DataTransformer</strong> : A concrete implementation which transforms data received from the client in the form of the OLAPDataSet into a structure understandable by
      the VisualizationMethod that uses it</li>
</ul>


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
         this.setOutput(new OLAPDataSet());
         this.getInput().addOLAPDataColumn(
              OLAPDataColumnFactory.createOLAPDataColumnOfType("xAxisStrings", OLAPColumnDataType.STRING, true)
         );
         this.getInput().addOLAPDataColumn(
              OLAPDataColumnFactory.createOLAPDataColumnOfType("yAxisValues", OLAPColumnDataType.INTEGER, true)
         );
     }
```
 <li>visualizationCode</li>
 The concrete implementations of this abstract method provide the actual visualization code that is send back to the client by the OpenLAP-Visualizer. The parameter that of this method is an instance of `TransformedData` which contains the data
 which was transformed in the format that the `VisualizationCodeGenerator` expects from the `OLAPDataSet`. The example below illustrates how to provide a concrete implementation of the `visualizationCode` abstract method:
```java
    @Override
    protected String visualizationCode(TransformedData<?> transformedData) {
        TransformedGooglePieChartData transformedGooglePieChartData = (TransformedGooglePieChartData)transformedData.getDataContent();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div id=\"chart\" style=\"width: 900px; height: 500px;\"></div>");
        stringBuilder.append("<script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>" +
                "<script type=\"text/javascript\">");
        stringBuilder.append("google.charts.load('current', {'packages':['corechart']});" +
                "google.charts.setOnLoadCallback(drawChart);\n" +
                "function drawChart() {" +
                "var data = google.visualization.arrayToDataTable([");
        int count=0;
        for(String xValue : transformedGooglePieChartData.getLabels()) {
            if(count == transformedGooglePieChartData.getLabels().size())
                stringBuilder.append("['" + xValue + "','" + transformedGooglePieChartData.getFrequencies().get(count)+"']");
            else
                stringBuilder.append("['" + xValue + "','" + transformedGooglePieChartData.getFrequencies().get(count)+"'],");
            count++;
        }
        stringBuilder.append(" ]);"+
                "var options = {" +
                "title: 'Wiki Posts by Platform'" +
                "};" +
                "var chart = new google.visualization.PieChart(document.getElementById('chart'));" +
                "chart.draw(data, options);" +
                "}" +
                "</script>");
        return stringBuilder.toString();
    }
```
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
    public TransformedData<?> transformData(OLAPDataSet olapDataSet) {
        TransformedGooglePieChartData transformedGooglePieChartData = new TransformedGooglePieChartData();
        olapDataSet.getColumnsAsList(true).forEach(olapDataColumn -> {
            //in this Data transformer y axis contains only INTEGERS
            if (olapDataColumn.getConfigurationData().getType().equals(OLAPColumnDataType.INTEGER)) {
                ArrayList<String> frequencies = new ArrayList<>();
                for (Object data : olapDataColumn.getData()) {
                  frequencies.add(data.toString());
                }
                transformedGooglePieChartData.setFrequencies(frequencies);
            } else {
                transformedGooglePieChartData.setLabels(olapDataColumn.getData());
            }
        });
        TransformedData<TransformedGooglePieChartData> transformedData = new TransformedData<>();
        transformedData.setDataContent(transformedGooglePieChartData);
        return transformedData;
    }
```

## Usage Guide
The overall process of creating and uploading new `VisualizationMethods` and `DataTransformers` is as follows:
<ol>
    <li>Create a starter project by using Maven archetype project <a href="https://github.com/OpenLearningAnalyticsPlatform/OpenLAP-Visualizer-Starter">OpenLAP-Visualizer-Starter</a> </li>
    <li>Implement `VisualizationCodeGenerators` and `DataTransformers` based on your requirements</li>
    <li>Package your concrete implementations as a JAR bundle</li>
    <li>Upload the JAR bundle using a REST client to the OpenLAP-Visualizer along with providing the configuration. The configuration is provided in the form of JSON and contains details of which `VisualizationCodeGenerator` and `DataTransformer` are being uploaded.
        An example configuration is shown below:
```                
        {
          "visualizationFrameworks": [
            {
              "name": "Google Charts",
              "creator": "Bassim Bashir",
              "description": "A framework providing great visualizations",
              "visualizationMethods": [
                {
                  "name": "Pie Chart",
                  "implementingClass": "de.rwthaachen.openlap.GooglePieChartVisualizationCodeGenerator",
                  "dataTransformerMethod": {
                    "name": "Pie Chart DataTransformer",
                    "implementingClass": "de.rwthaachen.openlap.PieChartDataTransformer"
                  }
                }
              ]
            }
          ]
        }       
```
If you look at the example configuration above you can see that there are multiple implemntations of the OpenLAP-Visualizer-Framework that can be uploaded in a single go. The configuration specifies which `VisualizationMethod` uses which `DataTransformer` along with other information.
</br>      
        For further information on the parameters refer to the source code <a href="https://github.com/OpenLearningAnalyticsPlatform/OpenLAP-Visualizer-Core/blob/master/src/main/java/de/rwthaachen/openlap/visualizer/dtos/request/UploadVisualizationFrameworksRequest.java">Visualization Frameworks Upload Request</a>
    </li>
    <li>The OpenLAP-Visualizer after verifying your upload will make it available for all clients</li>
<ol>
