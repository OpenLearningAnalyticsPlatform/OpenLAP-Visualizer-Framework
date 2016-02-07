package de.rwthaachen.openlap.visualizer.framework;

import DataSet.OLAPDataSet;
import DataSet.OLAPDataSetConfigurationValidationResult;
import DataSet.OLAPPortConfiguration;
import de.rwthaachen.openlap.visualizer.framework.exceptions.DataSetValidationException;
import de.rwthaachen.openlap.visualizer.framework.exceptions.UnTransformableData;
import de.rwthaachen.openlap.visualizer.framework.exceptions.VisualizationCodeGenerationException;
import de.rwthaachen.openlap.visualizer.framework.model.TransformedData;

public abstract class VisualizationCodeGenerator {

    private OLAPDataSet input;
    private OLAPDataSet output;

    protected abstract void initializeDataSetConfiguration();

    public boolean isDataProcessable(OLAPPortConfiguration olapPortConfiguration) throws DataSetValidationException {
        if(input ==null || output==null)
            initializeDataSetConfiguration();

        OLAPDataSetConfigurationValidationResult validationResult = input.validateConfiguration(olapPortConfiguration);
        if (validationResult.isValid())
            return true;
        else
            throw new DataSetValidationException(validationResult.getValidationMessage());
    }

    protected abstract String visualizationCode(TransformedData<?> transformedData);

    public String generateVisualizationCode(OLAPDataSet olapDataSet, DataTransformer dataTransformer) throws VisualizationCodeGenerationException {
        if(input==null || output==null)
            initializeDataSetConfiguration();

        TransformedData transformedData = dataTransformer.transformData(olapDataSet);
        if (transformedData == null)
            //TODO
            throw new UnTransformableData("Data could not be transformed"); //add a json dump of olapdataset
            // via adding a toString
        else
            //TODO: Escape the HTML code
            return visualizationCode(transformedData);
    }

    public OLAPDataSet getInput() {
        return input;
    }

    public void setInput(OLAPDataSet input) {
        this.input = input;
    }

    public OLAPDataSet getOutput() {
        return output;
    }

    public void setOutput(OLAPDataSet output) {
        this.output = output;
    }
}
