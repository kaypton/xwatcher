package com.github.fenrir.xdeepstrategy.nn.test;

import ai.djl.MalformedModelException;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.training.initializer.NormalInitializer;
import com.github.fenrir.xdeepstrategy.nn.MultilayerPerceptron;

import java.io.IOException;
import java.util.Arrays;

public class NNCreateTest {
    static public void main(String[] args) throws IOException, MalformedModelException {
        MultilayerPerceptron mlp = MultilayerPerceptron.builder("test_mlp")
                //.addBatchFlattenInputLayer(1)
                .addHiddenLayer(2, MultilayerPerceptron.ActivationFunction.RELU, null, null)
                .addHiddenLayer(2, MultilayerPerceptron.ActivationFunction.RELU, null, null)
                .addOutputLayer(1, null, null)
                .initializer(new NormalInitializer(), new NormalInitializer())
                .setDataType(DataType.FLOAT64)
                .build();
        MultilayerPerceptron mlp2 = MultilayerPerceptron.builder("test_mlp")
                //.addBatchFlattenInputLayer(1)
                .addHiddenLayer(2, MultilayerPerceptron.ActivationFunction.RELU, null, null)
                .addHiddenLayer(2, MultilayerPerceptron.ActivationFunction.RELU, null, null)
                .addOutputLayer(1, null, null)
                .initializer(new NormalInitializer(), new NormalInitializer())
                .setDataType(DataType.FLOAT64)
                .build();
        mlp.initialize(new Shape(2, 2));
        System.out.println("mlp:" + Arrays.toString(mlp.getParametersByteArray()));
        mlp2.initialize(new Shape(2, 2));
        System.out.println("mlp2:" + Arrays.toString(mlp2.getParametersByteArray()));
        mlp2.copyParameters(mlp);
        System.out.println("mlp2:" + Arrays.toString(mlp2.getParametersByteArray()));
    }
}
