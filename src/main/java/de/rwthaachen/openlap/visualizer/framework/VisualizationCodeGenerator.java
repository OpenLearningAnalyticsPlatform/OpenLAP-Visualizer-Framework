package de.rwthaachen.openlap.visualizer.framework;

import DataSet.OLAPDataSet;
import DataSet.OLAPDataSetConfigurationValidationResult;
import DataSet.OLAPPortConfiguration;
import DataSet.OLAPPortMapping;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rwthaachen.openlap.visualizer.framework.exceptions.DataSetValidationException;
import de.rwthaachen.openlap.visualizer.framework.exceptions.UnTransformableData;
import de.rwthaachen.openlap.visualizer.framework.exceptions.VisualizationCodeGenerationException;
import de.rwthaachen.openlap.visualizer.framework.model.TransformedData;

import java.util.Map;

/**
 * The abstract class which needs to be extended in order to add new concrete visualization methods
 * The abstract method "initializeDataSetConfiguration" should be overriden with the input OLAPDataSet configuration for the visualization method being implemented.
 * Furthermore, the overriden method "visualizationCode" method should return the actual client visualization code
 *
 * @author Bassim Bashir
 */
public abstract class VisualizationCodeGenerator {

    private OLAPDataSet input;
    private OLAPDataSet output;

    protected abstract void initializeDataSetConfiguration();

    protected abstract String visualizationCode(TransformedData<?> transformedData, Map<String, Object> additionalParams) throws VisualizationCodeGenerationException;

    protected abstract String visualizationLibraryScript();

    public boolean isDataProcessable(OLAPPortConfiguration olapPortConfiguration) throws DataSetValidationException {
        if (input == null)
            initializeDataSetConfiguration();

        OLAPDataSetConfigurationValidationResult validationResult = input.validateConfiguration(olapPortConfiguration);
        if (validationResult.isValid())
            return true;
        else
            throw new DataSetValidationException(validationResult.getValidationMessage());
    }

    public String generateVisualizationCode(OLAPDataSet olapDataSet, OLAPPortConfiguration portConfiguration, DataTransformer dataTransformer, Map<String, Object> additionalParams) throws VisualizationCodeGenerationException, UnTransformableData, DataSetValidationException {
        if (input == null)
            initializeDataSetConfiguration();
        // is the configuration valid?
        if(isDataProcessable(portConfiguration)) {
            // for each configuration element of the configuration
            for (OLAPPortMapping mappingEntry:portConfiguration.getMapping()) {
                // map the data of the column c.id==element.id to the input
                input.getColumns().get(mappingEntry.getInputPort().getId()).setData(olapDataSet.getColumns().get(mappingEntry.getOutputPort().getId()).getData());
            }
            TransformedData<?> transformedData = dataTransformer.transformData(input);
            if (transformedData == null)
                throw new UnTransformableData("Data could not be transformed.");
            else
                return visualizationCode(transformedData, additionalParams);
        }else{
            return "Data could not be transformed.";
        }
    }

    public String getVisualizationLibraryScript(){
        return visualizationLibraryScript();
    }

    public OLAPDataSet getInput() {
        if (input == null)
            initializeDataSetConfiguration();
        return input;
    }

    public void setInput(OLAPDataSet input) {
        this.input = input;
    }

    public OLAPDataSet getOutput() {
        return output;
    }

    public String getOutputAsJsonString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this.output);
        }catch (JsonProcessingException | NullPointerException exception){
            return "";
        }
    }

    public String getInputAsJsonString(){
        if (input == null)
            initializeDataSetConfiguration();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this.input);
        }catch (JsonProcessingException exception){
            return "";
        }

    }

    public void setOutput(OLAPDataSet output) {
        this.output = output;
    }
}
