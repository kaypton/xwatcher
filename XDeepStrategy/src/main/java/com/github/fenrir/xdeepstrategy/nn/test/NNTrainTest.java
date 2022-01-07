package com.github.fenrir.xdeepstrategy.nn.test;

import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.training.EasyTrain;
import ai.djl.training.dataset.ArrayDataset;
import ai.djl.training.dataset.Dataset;
import ai.djl.training.initializer.NormalInitializer;
import ai.djl.translate.TranslateException;
import com.github.fenrir.xdeepstrategy.nn.MultilayerPerceptron;

import java.io.IOException;

public class NNTrainTest {
    public static void main(String[] args) throws TranslateException, IOException {
        NDManager manager = NDManager.newBaseManager();

        // create a MLP
        MultilayerPerceptron mlp = MultilayerPerceptron.builder("test_mlp")
                //.addBatchFlattenInputLayer(2)
                .addHiddenLayer(2, MultilayerPerceptron.ActivationFunction.RELU, null, null)
                .addHiddenLayer(2, MultilayerPerceptron.ActivationFunction.RELU, null, null)
                .addOutputLayer(1, null, null)
                .initializer(new NormalInitializer(), new NormalInitializer())
                .setDataType(DataType.FLOAT32)
                .build();
        mlp.initialize(new Shape(2, 2));

        Dataset dataset = new ArrayDataset.Builder()
                .setSampling(5, true, true)
                .setData(manager.randomNormal(new Shape(100000, 2, 2), DataType.FLOAT32))
                .optLabels(manager.randomNormal(new Shape(100000, 1), DataType.FLOAT32))
                .build();

        mlp.setDefaultTrainingConfig();
        EasyTrain.fit(mlp.getTrainer(), 2, dataset, null);
    }
}
