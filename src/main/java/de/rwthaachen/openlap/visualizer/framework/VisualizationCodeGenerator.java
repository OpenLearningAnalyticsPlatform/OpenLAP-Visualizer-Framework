package de.rwthaachen.openlap.visualizer.framework;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rwthaachen.openlap.dataset.OpenLAPDataSet;
import de.rwthaachen.openlap.dataset.OpenLAPDataSetConfigValidationResult;
import de.rwthaachen.openlap.dataset.OpenLAPPortConfig;
import de.rwthaachen.openlap.dataset.OpenLAPPortMapping;
import de.rwthaachen.openlap.visualizer.framework.exceptions.DataSetValidationException;
import de.rwthaachen.openlap.visualizer.framework.exceptions.UnTransformableData;
import de.rwthaachen.openlap.visualizer.framework.exceptions.VisualizationCodeGenerationException;
import de.rwthaachen.openlap.visualizer.framework.model.TransformedData;

import java.util.Map;

/**
 * The abstract class which needs to be extended in order to add new concrete visualization methods
 * The abstract method "initializeDataSetConfiguration" should be overriden with the input OpenLAPDataSet configuration for the visualization method being implemented.
 * Furthermore, the overriden method "visualizationCode" method should return the actual client visualization code
 *
 * @author Bassim Bashir
 */
public abstract class VisualizationCodeGenerator {

    private OpenLAPDataSet input;
    private OpenLAPDataSet output;

    protected abstract void initializeDataSetConfiguration();

    protected abstract String visualizationCode(TransformedData<?> transformedData, Map<String, Object> additionalParams) throws VisualizationCodeGenerationException;

    protected abstract String visualizationLibraryScript();

    public boolean isDataProcessable(OpenLAPPortConfig openlapPortConfig) throws DataSetValidationException {
        if (input == null)
            initializeDataSetConfiguration();

        OpenLAPDataSetConfigValidationResult validationResult = input.validateConfiguration(openlapPortConfig);
        if (validationResult.isValid())
            return true;
        else
            throw new DataSetValidationException(validationResult.getValidationMessage());
    }

    public String generateVisualizationCode(OpenLAPDataSet openLAPDataSet, OpenLAPPortConfig portConfig, DataTransformer dataTransformer, Map<String, Object> additionalParams) throws VisualizationCodeGenerationException, UnTransformableData, DataSetValidationException {
        if (input == null)
            initializeDataSetConfiguration();
        // is the configuration valid?
        if(isDataProcessable(portConfig)) {
            // for each configuration element of the configuration
            for (OpenLAPPortMapping mappingEntry:portConfig.getMapping()) {
                // map the data of the column c.id==element.id to the input
                input.getColumns().get(mappingEntry.getInputPort().getId()).setData(openLAPDataSet.getColumns().get(mappingEntry.getOutputPort().getId()).getData());
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

    public OpenLAPDataSet getInput() {
        if (input == null)
            initializeDataSetConfiguration();
        return input;
    }

    public void setInput(OpenLAPDataSet input) {
        this.input = input;
    }

    public OpenLAPDataSet getOutput() {
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

    public void setOutput(OpenLAPDataSet output) {
        this.output = output;
    }
}
