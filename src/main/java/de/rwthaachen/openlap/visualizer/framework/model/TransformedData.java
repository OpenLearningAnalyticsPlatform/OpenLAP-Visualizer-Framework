package de.rwthaachen.openlap.visualizer.framework.model;

/**
 * Class which serves as a general model to hold the data transformed by a DataTransformer from a OLAPDataSet into
 * an instance of this class
 *
 * @author Bassim Bashir
 */
public class TransformedData<T> {

    private T dataContent;

    public T getDataContent() {
        return dataContent;
    }

    public void setDataContent(T dataContent) {
        this.dataContent = dataContent;
    }
}
