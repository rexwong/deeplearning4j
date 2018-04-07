package org.deeplearning4j.nn.misc.iter;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import java.util.LinkedList;
import java.util.List;

public class WSTestDataSetIterator implements DataSetIterator {
    int cursor = 0;
    int batchSize = 32;

    final INDArray vectors = Nd4j.rand(30, 300);

    @Override
    public DataSet next(int i) {
        final LinkedList<DataSet> parts = new LinkedList<>();
        while(parts.size() < i && hasNext()){
            parts.add(nextOne());
        }
        cursor++;
        return DataSet.merge(parts);
    }


    public DataSet nextOne(){
        final INDArray features = Nd4j.create(1, 1, 10);
        for (int i = 0; i < 10; i++) {
            features.putScalar(1, 1, i, i);
        }

        return new DataSet(
                features,
                vectors.getRow(7),
                Nd4j.ones(1, 10),
                null
        );
    }

    @Override
    public int totalExamples() {
        return 32;
    }

    @Override
    public int inputColumns() {
        return 1;
    }

    @Override
    public int totalOutcomes() {
        return 300;
    }

    @Override
    public boolean resetSupported() {
        return true;
    }

    @Override
    public boolean asyncSupported() {
        return true;
    }

    @Override
    public void reset() {

    }

    @Override
    public int batch() {
        return 0;
    }

    @Override
    public int cursor() {
        return cursor;
    }

    @Override
    public int numExamples() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void setPreProcessor(DataSetPreProcessor dataSetPreProcessor) {

    }

    @Override
    public DataSetPreProcessor getPreProcessor() {
        return null;
    }

    @Override
    public List<String> getLabels() {
        return null;
    }

    @Override
    public boolean hasNext() {
        return cursor < 10;
    }

    @Override
    public DataSet next() {
        return next(batchSize);
    }

    @Override
    public void remove() {

    }
}