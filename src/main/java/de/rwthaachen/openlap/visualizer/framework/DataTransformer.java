package de.rwthaachen.openlap.visualizer.framework;

import de.rwthaachen.openlap.dataset.OpenLAPDataSet;
import de.rwthaachen.openlap.visualizer.framework.exceptions.UnTransformableData;
import de.rwthaachen.openlap.visualizer.framework.model.TransformedData;

/**
 * The interface which defines the methods of a DataTransformer which the concrete implementations should implement
 *
 * @author Bassim Bashir
 */
public interface DataTransformer {

    /**
     * @param olapDataSet The dataset which needs to be transformed in a
     *                    dataset that is understood by the visualization code
     * @return null if the data could not be transformed
     */
    TransformedData<?> transformData(OpenLAPDataSet olapDataSet) throws UnTransformableData;

}

