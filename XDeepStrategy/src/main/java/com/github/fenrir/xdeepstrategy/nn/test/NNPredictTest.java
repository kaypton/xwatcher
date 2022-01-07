package com.github.fenrir.xdeepstrategy.nn.test;

import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.training.initializer.NormalInitializer;
import ai.djl.translate.Batchifier;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import com.github.fenrir.xdeepstrategy.nn.MultilayerPerceptron;

class TestTranslator implements Translator<NDArray, NDArray> {

    @Override
    public NDArray processOutput(TranslatorContext ctx, NDList list) throws Exception {
        System.out.println("processOutput =========");
        for(NDArray array : list){
            System.out.println(array);
        }
        System.out.println("processOutput =========");
        return null;
    }

    @Override
    public NDList processInput(TranslatorContext ctx, NDArray input) throws Exception {
        System.out.println("processInput =========");
        System.out.println(input);
        System.out.println("processInput =========");
        NDList list = new NDList();

        list.add(input);

        return list;
    }

    @Override
    public Batchifier getBatchifier() {
        return Batchifier.STACK;
    }

    @Override
    public void prepare(TranslatorContext ctx) throws Exception {
        Translator.super.prepare(ctx);
    }
}

public class NNPredictTest {
    public static void main(String[] args) throws TranslateException {

        NDManager manager = NDManager.newBaseManager();

        // create a MLP
        MultilayerPerceptron mlp = MultilayerPerceptron.builder("test_mlp")
                //.addBatchFlattenInputLayer(2)
                .addHiddenLayer(2, MultilayerPerceptron.ActivationFunction.RELU, null, null)
                .addHiddenLayer(2, MultilayerPerceptron.ActivationFunction.RELU, null, null)
                .addOutputLayer(1, null, null)
                //.initializer(new NormalInitializer(), new NormalInitializer())
                .setDataType(DataType.FLOAT64)
                .build();
        mlp.initialize(new Shape(2, 2));

        // get predictor of the MLP
        Predictor<NDArray, NDArray> predictor = mlp.getPredictor(new TestTranslator());

        // create input data
        NDArray array = manager.zeros(new Shape(2, 2, 2), DataType.FLOAT64);
        predictor.predict(array);
        array.close();
    }
}
