/*-
 *  * Copyright 2017 Skymind, Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 */

package org.datavec.api.writable;

import com.google.common.collect.Lists;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.collection.CollectionRecordReader;
import org.datavec.api.util.ndarray.RecordConverter;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RecordConverterTest {
    @Test
    public void toRecords_PassInClassificationDataSet_ExpectNDArrayAndIntWritables() {
        INDArray feature1 = Nd4j.create(new double[]{4, -5.7, 10, -0.1});
        INDArray feature2 = Nd4j.create(new double[]{11, .7, -1.3, 4});
        INDArray label1 = Nd4j.create(new double[]{0, 0, 1, 0});
        INDArray label2 = Nd4j.create(new double[]{0, 1, 0, 0});
        DataSet dataSet = new DataSet(Nd4j.vstack(Lists.newArrayList(feature1, feature2)),
                Nd4j.vstack(Lists.newArrayList(label1, label2)));

        List<List<Writable>> writableList = RecordConverter.toRecords(dataSet);

        assertEquals(2, writableList.size());
        testClassificationWritables(feature1, 2, writableList.get(0));
        testClassificationWritables(feature2, 1, writableList.get(1));
    }

    @Test
    public void toRecords_PassInRegressionDataSet_ExpectNDArrayAndDoubleWritables() {
        INDArray feature = Nd4j.create(new double[]{4, -5.7, 10, -0.1});
        INDArray label = Nd4j.create(new double[]{.5, 2, 3, .5});
        DataSet dataSet = new DataSet(feature, label);

        List<List<Writable>> writableList = RecordConverter.toRecords(dataSet);
        List<Writable> results = writableList.get(0);
        NDArrayWritable ndArrayWritable = (NDArrayWritable) results.get(0);

        assertEquals(1, writableList.size());
        assertEquals(5, results.size());
        assertEquals(feature, ndArrayWritable.get());
        for (int i = 0; i < label.shape()[1]; i++) {
            DoubleWritable doubleWritable = (DoubleWritable) results.get(i + 1);
            assertEquals(label.getDouble(i), doubleWritable.get(), 0);
        }
    }

    private void testClassificationWritables(INDArray expectedFeatureVector, int expectLabelIndex,
                                             List<Writable> writables) {
        NDArrayWritable ndArrayWritable = (NDArrayWritable) writables.get(0);
        IntWritable intWritable = (IntWritable) writables.get(1);

        assertEquals(2, writables.size());
        assertEquals(expectedFeatureVector, ndArrayWritable.get());
        assertEquals(expectLabelIndex, intWritable.get());
    }


    @Test
    public void testNDArrayWritableConcat() {
        List<Writable> l = Arrays.<Writable>asList(new DoubleWritable(1),
                new NDArrayWritable(Nd4j.create(new double[]{2, 3, 4})), new DoubleWritable(5),
                new NDArrayWritable(Nd4j.create(new double[]{6, 7, 8})), new IntWritable(9),
                new IntWritable(1));

        INDArray exp = Nd4j.create(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 1});
        INDArray act = RecordConverter.toArray(l);

        assertEquals(exp, act);
    }

    @Test
    public void testNDArrayWritableConcatToMatrix(){

        List<Writable> l1 = Arrays.<Writable>asList(new DoubleWritable(1), new NDArrayWritable(Nd4j.create(new double[]{2, 3, 4})), new DoubleWritable(5));
        List<Writable> l2 = Arrays.<Writable>asList(new DoubleWritable(6), new NDArrayWritable(Nd4j.create(new double[]{7, 8, 9})), new DoubleWritable(10));

        INDArray exp = Nd4j.create(new double[][]{
                {1,2,3,4,5},
                {6,7,8,9,10}});

        INDArray act = RecordConverter.toMatrix(Arrays.asList(l1,l2));

        assertEquals(exp, act);
    }
}
