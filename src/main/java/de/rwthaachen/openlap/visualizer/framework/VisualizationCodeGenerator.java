package de.rwthaachen.openlap.visualizer.framework;

import DataSet.OLAPDataSet;
import DataSet.OLAPDataSetConfigurationValidationResult;
import DataSet.OLAPPortConfiguration;
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

    public boolean isDataProcessable(OLAPPortConfiguration olapPortConfiguration) throws DataSetValidationException {
        if (input == null)
            initializeDataSetConfiguration();

        OLAPDataSetConfigurationValidationResult validationResult = input.validateConfiguration(olapPortConfiguration);
        if (validationResult.isValid())
            return true;
        else
            throw new DataSetValidationException(validationResult.getValidationMessage());
    }

    protected abstract String visualizationCode(TransformedData<?> transformedData, Map<String, Object> additionalParams) throws VisualizationCodeGenerationException;

    public String generateVisualizationCode(OLAPDataSet olapDataSet, DataTransformer dataTransformer, Map<String, Object> additionalParams) throws VisualizationCodeGenerationException, UnTransformableData {
        if (input == null)
            initializeDataSetConfiguration();

        TransformedData<?> transformedData = dataTransformer.transformData(olapDataSet);
        if (transformedData == null)
            throw new UnTransformableData("Data could not be transformed.");
        else
            return visualizationCode(transformedData, additionalParams);
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
