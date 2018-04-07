package org.deeplearning4j.nn.modelimport.keras.weights;

import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.modelimport.keras.KerasLayer;
import org.deeplearning4j.nn.modelimport.keras.KerasModel;
import org.deeplearning4j.nn.modelimport.keras.layers.convolutional.KerasSpaceToDepth;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

@Slf4j
public class KerasWeightSettingTests {

    @Test
    public void testSimpleLayersWithWeights() throws Exception {
        int[] kerasVersions = new int[]{1, 2};
        String[] backends = new String[]{"tensorflow", "theano"};

        for (int version : kerasVersions) {
            for (String backend : backends) {
                String densePath = "weights/dense_" + backend + "_" + version + ".h5";
                importDense(densePath);
                log.info("***** Successfully imported " + densePath);

                String conv2dPath = "weights/conv2d_" + backend + "_" + version + ".h5";
                importConv2D(conv2dPath);
                log.info("***** Successfully imported " + conv2dPath);

                String lstmPath = "weights/lstm_" + backend + "_" + version + ".h5";
                importLstm(lstmPath);
                log.info("***** Successfully imported " + lstmPath);

                String embeddingLstmPath = "weights/embedding_lstm_" + backend + "_" + version + ".h5";
                importEmbeddingLstm(embeddingLstmPath);
                log.info("***** Successfully imported " + embeddingLstmPath);

                if (version == 2) {
                    String embeddingConv1dPath = "weights/embedding_conv1d_" + backend + "_" + version + ".h5";
                    importEmbeddingConv1D(embeddingConv1dPath);
                    log.info("***** Successfully imported " + embeddingConv1dPath);
                }

                String simpleRnnPath = "weights/simple_rnn_" + backend + "_" + version + ".h5";
                importSimpleRnn(simpleRnnPath);
                log.info("***** Successfully imported " + simpleRnnPath);

                String bidirectionalLstmPath = "weights/bidirectional_lstm_" + backend + "_" + version + ".h5";
                importBidirectionalLstm(bidirectionalLstmPath);
                log.info("***** Successfully imported " + bidirectionalLstmPath);

                String batchToConv2dPath = "weights/batch_to_conv2d_" + backend + "_" + version + ".h5";
                importBatchNormToConv2D(batchToConv2dPath);
                log.info("***** Successfully imported " + batchToConv2dPath);

                if (backend.equals("tensorflow") && version == 2) {
                    String simpleSpaceToBatchPath = "weights/space_to_depth_simple_" + backend + "_" + version + ".h5";
                    importSimpleSpaceToDepth(simpleSpaceToBatchPath);
                    log.info("***** Successfully imported " + simpleSpaceToBatchPath);
                }

                if (backend.equals("tensorflow") && version == 2) {
                    String graphSpaceToBatchPath = "weights/space_to_depth_graph_" + backend + "_" + version + ".h5";
                    importGraphSpaceToDepth(graphSpaceToBatchPath);
                    log.info("***** Successfully imported " + graphSpaceToBatchPath);
                }
            }
        }
    }

    private static void importDense(String modelPath) throws Exception {
        MultiLayerNetwork model = loadMultiLayerNetwork(modelPath, true);

        INDArray weights = model.getLayer(0).getParam("W");
        int[] weightShape = weights.shape();
        assert (weightShape[0] == 4);
        assert (weightShape[1] == 6);

        INDArray bias = model.getLayer(0).getParam("b");
        assert (bias.length() == 6);
    }

    private static void importConv2D(String modelPath) throws Exception {
        MultiLayerNetwork model = loadMultiLayerNetwork(modelPath, false);

        INDArray weights = model.getLayer(0).getParam("W");
        int[] weightShape = weights.shape();
        assert (weightShape[0] == 6);
        assert (weightShape[1] == 5);
        assert (weightShape[2] == 3);
        assert (weightShape[3] == 3);

        INDArray bias = model.getLayer(0).getParam("b");
        assert (bias.length() == 6);
    }

    private static void importBatchNormToConv2D(String modelPath) throws Exception {
        MultiLayerNetwork model = loadMultiLayerNetwork(modelPath, false);
        model.summary();
    }

    private static void importSimpleSpaceToDepth(String modelPath) throws Exception {
        KerasLayer.registerCustomLayer("Lambda", KerasSpaceToDepth.class);
        MultiLayerNetwork model = loadMultiLayerNetwork(modelPath, false);

        INDArray input = Nd4j.zeros(10, 4, 6, 6);
        INDArray output = model.output(input);
        assert Arrays.equals(output.shape(), new int[]{10, 16, 3, 3});
    }

    private static void importGraphSpaceToDepth(String modelPath) throws Exception {
        KerasLayer.registerCustomLayer("Lambda", KerasSpaceToDepth.class);
        ComputationGraph model = loadComputationalGraph(modelPath, false);

        INDArray input[] = new INDArray[]{Nd4j.zeros(10, 4, 6, 6), Nd4j.zeros(10, 16, 3, 3)};
        INDArray[] output = model.output(input);
        log.info(Arrays.toString(output[0].shape()));
        assert Arrays.equals(output[0].shape(), new int[]{10, 32, 3, 3});
    }

    private static void importLstm(String modelPath) throws Exception {
        MultiLayerNetwork model = loadMultiLayerNetwork(modelPath, false);
        model.summary();
        // TODO: check weights
    }

    private static void importEmbeddingLstm(String modelPath) throws Exception {
        MultiLayerNetwork model = loadMultiLayerNetwork(modelPath, false);

        int nIn = 4;
        int nOut = 6;
        int outputDim = 5;
        int inputLength = 10;
        int mb = 42;

        INDArray embeddingWeight = model.getLayer(0).getParam("W");
        int[] embeddingWeightShape = embeddingWeight.shape();
        assert (embeddingWeightShape[0] == nIn);
        assert (embeddingWeightShape[1] == outputDim);

        INDArray inEmbedding = Nd4j.zeros(mb, 1, inputLength);
        INDArray output = model.output(inEmbedding);
        assert Arrays.equals(output.shape(), new int[]{mb, nOut, inputLength});

    }

    private static void importEmbeddingConv1D(String modelPath) throws Exception {
        MultiLayerNetwork model = loadMultiLayerNetwork(modelPath, false);

        int nIn = 4;
        int nOut = 6;
        int outputDim = 5;
        int inputLength = 10;
        int kernel = 3;
        int mb = 42;

        INDArray embeddingWeight = model.getLayer(0).getParam("W");
        int[] embeddingWeightShape = embeddingWeight.shape();
        assert (embeddingWeightShape[0] == nIn);
        assert (embeddingWeightShape[1] == outputDim);

        INDArray inEmbedding = Nd4j.zeros(mb, 1, inputLength);
        INDArray output = model.output(inEmbedding);
        assert Arrays.equals(output.shape(), new int[]{mb, nOut, inputLength - kernel + 1});

    }

    private static void importSimpleRnn(String modelPath) throws Exception {
        MultiLayerNetwork model = loadMultiLayerNetwork(modelPath, false);
        model.summary();
        // TODO: check weights
    }

    private static void importBidirectionalLstm(String modelPath) throws Exception {
        MultiLayerNetwork model = loadMultiLayerNetwork(modelPath, false);
        model.summary();
        // TODO: check weights
    }

    private static MultiLayerNetwork loadMultiLayerNetwork(String modelPath, boolean training) throws Exception {
        ClassPathResource modelResource = new ClassPathResource(modelPath,
                KerasWeightSettingTests.class.getClassLoader());
        File modelFile = File.createTempFile("temp", ".h5");
        Files.copy(modelResource.getInputStream(), modelFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return new KerasModel().modelBuilder().modelHdf5Filename(modelFile.getAbsolutePath())
                .enforceTrainingConfig(training).buildSequential().getMultiLayerNetwork();
    }

    private static ComputationGraph loadComputationalGraph(String modelPath, boolean training) throws Exception {
        ClassPathResource modelResource = new ClassPathResource(modelPath,
                KerasWeightSettingTests.class.getClassLoader());
        File modelFile = File.createTempFile("temp", ".h5");
        Files.copy(modelResource.getInputStream(), modelFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return new KerasModel().modelBuilder().modelHdf5Filename(modelFile.getAbsolutePath())
                .enforceTrainingConfig(training).buildModel().getComputationGraph();
    }

}
