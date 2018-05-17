package org.deeplearning4j.nn.modelexport.solr.ltr.model;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Explanation;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.ltr.feature.Feature;
import org.apache.solr.ltr.model.AdapterModel;
import org.apache.solr.ltr.model.ModelException;
import org.apache.solr.ltr.norm.Normalizer;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelGuesser;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * An <a href="https://lucene.apache.org/solr/7_3_0/solr-ltr/org/apache/solr/ltr/model/LTRScoringModel.html">
 * org.apache.solr.ltr.model.LTRScoringModel</a> that computes scores using a {@link MultiLayerNetwork} or
 * {@link ComputationGraph} model.
 * <p>
 * Example configuration (snippet):
 * <pre>{
  "class" : "org.deeplearning4j.nn.modelexport.solr.ltr.model.ScoringModel",
  "name" : "myModel",
  "features" : [
    { "name" : "documentRecency", ... },
    { "name" : "isBook", ... },
    { "name" : "originalScore", ... }
  ],
  "params" : {
    "serializedModelFileName" : "mySerializedModel"
  }
}</pre>
 * <p>
 * Apache Solr Reference Guide:
 * <ul>
 * <li> <a href="https://lucene.apache.org/solr/guide/7_3/learning-to-rank.html">Learning To Rank</a>
 * </ul>
 */
public class ScoringModel extends AdapterModel {

  private String serializedModelFileName;
  protected Model model;

  public ScoringModel(String name, List<Feature> features, List<Normalizer> norms, String featureStoreName,
      List<Feature> allFeatures, Map<String,Object> params) {
    super(name, features, norms, featureStoreName, allFeatures, params);
  }

  public void setSerializedModelFileName(String serializedModelFileName) {
    this.serializedModelFileName = serializedModelFileName;
  }

  @Override
  public void init(SolrResourceLoader solrResourceLoader) throws ModelException {
    super.init(solrResourceLoader);
    try {
      model = restoreModel(openInputStream());
    } catch (Exception e) {
      throw new ModelException("Failed to restore model from given file (" + serializedModelFileName + ")", e);
    }
    validate();
  }

  protected InputStream openInputStream() throws IOException {
    return solrResourceLoader.openResource(serializedModelFileName);
  }

  /**
   * Uses the {@link ModelGuesser#loadModelGuess(InputStream)} method.
   */
  protected Model restoreModel(InputStream inputStream) throws Exception {
    final File instanceDir = solrResourceLoader.getInstancePath().toFile();
    return ModelGuesser.loadModelGuess(inputStream, instanceDir);
  }

  @Override
  protected void validate() throws ModelException {
    super.validate();
    if (serializedModelFileName == null) {
      throw new ModelException("no serializedModelFileName configured for model "+name);
    }
    if (model != null) {
      validateModel();
    }
  }

  protected void validateModel() throws ModelException {
    try {
      final float[] mockModelFeatureValuesNormalized = new float[features.size()];
      score(mockModelFeatureValuesNormalized);
    } catch (Exception exception) {
      throw new ModelException("score(...) test failed for model "+name, exception);
    }
  }

  @Override
  public float score(float[] modelFeatureValuesNormalized) {
    return outputScore(model, modelFeatureValuesNormalized);
  }

  /**
   * Currently supports {@link MultiLayerNetwork} and {@link ComputationGraph} models.
   * Pull requests to support additional <code>org.deeplearning4j</code> models are welcome.
   */
  public static float outputScore(Model model, float[] modelFeatureValuesNormalized) {

    final INDArray input = Nd4j.create(modelFeatureValuesNormalized);

    if (model instanceof MultiLayerNetwork) {
      final MultiLayerNetwork multiLayerNetwork = (MultiLayerNetwork)model;
      final INDArray output = multiLayerNetwork.output(input);
      return output.getFloat(0);
    }

    if (model instanceof ComputationGraph) {
      final ComputationGraph computationGraph = (ComputationGraph)model;
      final INDArray output = computationGraph.outputSingle(input);
      return output.getFloat(0);
    }

    final String message;
    if (model.getClass().getName().startsWith("org.deeplearning4j")) {
      message = model.getClass().getName()+" models are not yet supported and " +
        "pull requests are welcome: https://github.com/deeplearning4j/deeplearning4j";
    } else {
      message = model.getClass().getName()+" models are unsupported.";
    }

    throw new UnsupportedOperationException(message);
  }

  @Override
  public Explanation explain(LeafReaderContext context, int doc, float finalScore,
      List<Explanation> featureExplanations) {

    final StringBuilder sb = new StringBuilder();

    sb.append("(name=").append(getName());
    sb.append(",class=").append(getClass().getSimpleName());
    sb.append(",featureValues=[");
    for (int i = 0; i < featureExplanations.size(); i++) {
      Explanation featureExplain = featureExplanations.get(i);
      if (i > 0) {
        sb.append(',');
      }
      final String key = features.get(i).getName();
      sb.append(key).append('=').append(featureExplain.getValue());
    }
    sb.append("])");

    return Explanation.match(finalScore, sb.toString());
  }

}
