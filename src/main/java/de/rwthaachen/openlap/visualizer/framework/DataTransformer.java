package de.rwthaachen.openlap.visualizer.framework;

import DataSet.OLAPDataSet;
import de.rwthaachen.openlap.visualizer.framework.exceptions.UnTransformableData;
import de.rwthaachen.openlap.visualizer.framework.model.TransformedData;

public interface DataTransformer {

    /**
     * @param olapDataSet The dataset which needs to be transformed in a
     *                    dataset that is understood by the visualization code
     * @return null if the data could not be transformed
     * */
    TransformedData<?> transformData(OLAPDataSet olapDataSet) throws UnTransformableData;

}
