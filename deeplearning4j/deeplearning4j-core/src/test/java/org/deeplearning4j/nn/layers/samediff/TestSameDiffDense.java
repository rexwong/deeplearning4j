package org.deeplearning4j.nn.layers.samediff;

import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.TestUtils;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.gradient.Gradient;
import org.deeplearning4j.nn.layers.samediff.testlayers.SameDiffDense;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.params.DefaultParamInitializer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.junit.Test;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

@Slf4j
public class TestSameDiffDense {

    @Test
    public void testSameDiffDenseBasic() {
        //Only run test for CPU backend for now:
        assumeTrue("CPU".equalsIgnoreCase(Nd4j.getExecutioner().getEnvironmentInformation().getProperty("backend")));

        int nIn = 3;
        int nOut = 4;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .list()
                .layer(new SameDiffDense.Builder().nIn(nIn).nOut(nOut).build())
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        Map<String, INDArray> pt1 = net.getLayer(0).paramTable();
        assertNotNull(pt1);
        assertEquals(2, pt1.size());
        assertNotNull(pt1.get(DefaultParamInitializer.WEIGHT_KEY));
        assertNotNull(pt1.get(DefaultParamInitializer.BIAS_KEY));

        assertArrayEquals(new long[]{nIn, nOut}, pt1.get(DefaultParamInitializer.WEIGHT_KEY).shape());
        assertArrayEquals(new long[]{1, nOut}, pt1.get(DefaultParamInitializer.BIAS_KEY).shape());
    }

    @Test
    public void testSameDiffDenseForward() {
        //Only run test for CPU backend for now:
        assumeTrue("CPU".equalsIgnoreCase(Nd4j.getExecutioner().getEnvironmentInformation().getProperty("backend")));

        for (int minibatch : new int[]{5, 1}) {
            int nIn = 3;
            int nOut = 4;

            Activation[] afns = new Activation[]{
                    Activation.TANH,
                    Activation.SIGMOID,
                    Activation.ELU,
                    Activation.IDENTITY,
                    Activation.SOFTPLUS,
                    Activation.SOFTSIGN,
                    Activation.CUBE,
                    Activation.HARDTANH,
                    Activation.RELU
            };

            for (Activation a : afns) {
                log.info("Starting test - " + a);
                MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                        .list()
                        .layer(new SameDiffDense.Builder().nIn(nIn).nOut(nOut)
                                .activation(a)
                                .build())
                        .build();

                MultiLayerNetwork net = new MultiLayerNetwork(conf);
                net.init();

                assertNotNull(net.paramTable());

                MultiLayerConfiguration conf2 = new NeuralNetConfiguration.Builder()
                        .list()
                        .layer(new DenseLayer.Builder().activation(a).nIn(nIn).nOut(nOut).build())
                        .build();

                MultiLayerNetwork net2 = new MultiLayerNetwork(conf2);
                net2.init();

                net.params().assign(net2.params());

                //Check params:
                assertEquals(net2.params(), net.params());
                Map<String, INDArray> params1 = net.paramTable();
                Map<String, INDArray> params2 = net2.paramTable();
                assertEquals(params2, params1);

                INDArray in = Nd4j.rand(minibatch, nIn);
                INDArray out = net.output(in);
                INDArray outExp = net2.output(in);

                assertEquals(outExp, out);

                //Also check serialization:
                MultiLayerNetwork netLoaded = TestUtils.testModelSerialization(net);
                INDArray outLoaded = netLoaded.output(in);

                assertEquals(outExp, outLoaded);
            }
        }
    }

    @Test
    public void testSameDiffDenseForwardMultiLayer() {
        //Only run test for CPU backend for now:
        assumeTrue("CPU".equalsIgnoreCase(Nd4j.getExecutioner().getEnvironmentInformation().getProperty("backend")));

        for (int minibatch : new int[]{5, 1}) {
            int nIn = 3;
            int nOut = 4;

            Activation[] afns = new Activation[]{
                    Activation.TANH,
                    Activation.SIGMOID,
                    Activation.ELU,
                    Activation.IDENTITY,
                    Activation.SOFTPLUS,
                    Activation.SOFTSIGN,
                    Activation.CUBE,    //https://github.com/deeplearning4j/nd4j/issues/2426
                    Activation.HARDTANH,
                    Activation.RELU      //JVM crash
            };

            for (Activation a : afns) {
                log.info("Starting test - " + a);
                MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                        .seed(12345)
                        .list()
                        .layer(new SameDiffDense.Builder().nIn(nIn).nOut(nOut)
                                .weightInit(WeightInit.XAVIER)
                                .activation(a).build())
                        .layer(new SameDiffDense.Builder().nIn(nOut).nOut(nOut)
                                .weightInit(WeightInit.XAVIER)
                                .activation(a).build())
                        .layer(new OutputLayer.Builder().nIn(nOut).nOut(nOut)
                                .weightInit(WeightInit.XAVIER)
                                .activation(a).build())
                        .build();

                MultiLayerNetwork net = new MultiLayerNetwork(conf);
                net.init();

                assertNotNull(net.paramTable());

                MultiLayerConfiguration conf2 = new NeuralNetConfiguration.Builder()
                        .seed(12345)
                        .weightInit(WeightInit.XAVIER)
                        .list()
                        .layer(new DenseLayer.Builder().activation(a).nIn(nIn).nOut(nOut).build())
                        .layer(new DenseLayer.Builder().activation(a).nIn(nOut).nOut(nOut).build())
                        .layer(new OutputLayer.Builder().nIn(nOut).nOut(nOut)
                                .activation(a).build())
                        .build();

                MultiLayerNetwork net2 = new MultiLayerNetwork(conf2);
                net2.init();

//                net.params().assign(net2.params());
                assertEquals(net2.params(), net.params());

                //Check params:
                assertEquals(net2.params(), net.params());
                Map<String, INDArray> params1 = net.paramTable();
                Map<String, INDArray> params2 = net2.paramTable();
                assertEquals(params2, params1);

                INDArray in = Nd4j.rand(minibatch, nIn);
                INDArray out = net.output(in);
                INDArray outExp = net2.output(in);

                assertEquals(outExp, out);

                //Also check serialization:
                MultiLayerNetwork netLoaded = TestUtils.testModelSerialization(net);
                INDArray outLoaded = netLoaded.output(in);

                assertEquals(outExp, outLoaded);
            }
        }
    }

    @Test(expected = UnsupportedOperationException.class)   //Backprop not yet supported
    public void testSameDiffDenseBackward() {

        int nIn = 3;
        int nOut = 4;

        for (int minibatch : new int[]{5, 1}) {

            Activation[] afns = new Activation[]{
                    Activation.TANH,
                    Activation.SIGMOID,
                    Activation.ELU, Activation.IDENTITY, Activation.SOFTPLUS, Activation.SOFTSIGN,
                    Activation.HARDTANH,
//                    Activation.CUBE,    //https://github.com/deeplearning4j/nd4j/issues/2426
//                    Activation.RELU      //JVM crash
            };

            for (Activation a : afns) {
                log.info("Starting test - " + a);
                MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                        .list()
                        .layer(new SameDiffDense.Builder().nIn(nIn).nOut(nOut)
                                .activation(a)
                                .build())
                        .layer(new OutputLayer.Builder().nIn(nOut).nOut(nOut).activation(Activation.SOFTMAX)
                                .lossFunction(LossFunctions.LossFunction.MCXENT).build())
                        .build();

                MultiLayerNetwork net = new MultiLayerNetwork(conf);
                net.init();

                MultiLayerConfiguration conf2 = new NeuralNetConfiguration.Builder()
                        .list()
                        .layer(new DenseLayer.Builder().activation(a).nIn(nIn).nOut(nOut).build())
                        .layer(new OutputLayer.Builder().nIn(nOut).nOut(nOut).activation(Activation.SOFTMAX)
                                .lossFunction(LossFunctions.LossFunction.MCXENT).build())
                        .build();

                MultiLayerNetwork net2 = new MultiLayerNetwork(conf2);
                net2.init();

                net.params().assign(net2.params());

                //Check params:
                assertEquals(net2.params(), net.params());
                assertEquals(net2.paramTable(), net.paramTable());

                INDArray in = Nd4j.rand(minibatch, nIn);
                INDArray l = TestUtils.randomOneHot(minibatch, nOut, 12345);
                net.setInput(in);
                net2.setInput(in);
                net.setLabels(l);
                net2.setLabels(l);

                net.computeGradientAndScore();
                net2.computeGradientAndScore();

                Gradient g = net.gradient();
                Gradient g2 = net2.gradient();

                Map<String,INDArray> m1 = g.gradientForVariable();
                Map<String,INDArray> m2 = g2.gradientForVariable();

                assertEquals(m2.keySet(), m1.keySet());

                for(String s : m1.keySet()){
                    INDArray i1 = m1.get(s);
                    INDArray i2 = m2.get(s);

                    assertEquals(s, i2, i1);
                }

                assertEquals(g2.gradient(), g.gradient());
            }
        }
    }
}
