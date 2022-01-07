package com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.actor;

import ai.djl.MalformedModelException;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.TrainingConfig;
import ai.djl.training.dataset.Batch;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.initializer.NormalInitializer;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.optimizer.Adam;
import ai.djl.translate.Batchifier;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.entities.DDPGAction;
import com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.entities.DDPGState;
import com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.experienceCache.DDPGExperienceCache;
import com.github.fenrir.xdeepstrategy.nn.MultilayerPerceptron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;

public class DDPGMLPActor implements DDPGActor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DDPGMLPActor.class);

    private final MultilayerPerceptron mlp;
    private final Trainer mlpTrainer;
    private final MultilayerPerceptron targetMlp;
    private final DDPGExperienceCache experienceCache;

    private static class ActorTranslator implements Translator<DDPGState, DDPGAction> {

        @Override
        public DDPGAction processOutput(TranslatorContext ctx, NDList list) throws Exception {
            return null;
        }

        @Override
        public NDList processInput(TranslatorContext ctx, DDPGState input) throws Exception {
            return null;
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

    private DDPGMLPActor(MultilayerPerceptron mlp,
                         MultilayerPerceptron targetMlp,
                         DDPGExperienceCache experienceCache){
        this.experienceCache = experienceCache;

        this.mlp = mlp;
        this.targetMlp = targetMlp;

        // get trainer from MLP
        this.mlpTrainer = this.mlp.getTrainer();
    }

    /**
     * train the MLP using EasyTrain.trainBatch
     */
    @Override
    public void easyTrain(){
        try {
            Iterator<Batch> batchIterator = this.experienceCache.getDataBatchIterator().iterator();
            if(batchIterator.hasNext()){
                EasyTrain.trainBatch(this.mlpTrainer, batchIterator.next());
            }else{
                LOGGER.warn("there is no more data in experience cache");
            }
        } catch (TranslateException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * copy parameters of MLP to target MLP
     */
    @Override
    public void netSync(){
        try {
            this.targetMlp.copyParameters(this.mlp);
        } catch (IOException | MalformedModelException e) {
            e.printStackTrace();
        }
    }

    public static DDPGActor create(long inputSize, long outputSize, DDPGExperienceCache experienceCache){
        MultilayerPerceptron mlp = MultilayerPerceptron.builder("ddpg_actor_mlp")
                .addHiddenLayer(256, MultilayerPerceptron.ActivationFunction.RELU, null, null)
                .addHiddenLayer(256, MultilayerPerceptron.ActivationFunction.RELU, null, null)
                .addOutputLayer(outputSize, null, null)
                .initializer(new NormalInitializer(), new NormalInitializer())
                .setDataType(DataType.FLOAT64)
                .build();
        mlp.initialize(new Shape(inputSize));

        MultilayerPerceptron targetMlp = MultilayerPerceptron.builder("ddpg_actor_target_mlp")
                .addHiddenLayer(256, MultilayerPerceptron.ActivationFunction.RELU, null, null)
                .addHiddenLayer(256, MultilayerPerceptron.ActivationFunction.RELU, null, null)
                .addOutputLayer(1, null, null)
                .initializer(new NormalInitializer(), new NormalInitializer())
                .setDataType(DataType.FLOAT64)
                .build();
        targetMlp.initialize(new Shape(inputSize));

        DefaultTrainingConfig defaultTrainingConfig = new DefaultTrainingConfig(Loss.l2Loss()) // Mean Square Error
                .optOptimizer(Adam.builder().build()) // Adam optimizer
                .addEvaluator(new Accuracy()) // Use accuracy so we humans can understand how accurate the model is
                .addTrainingListeners(TrainingListener.Defaults.logging());
        mlp.setTrainingConfig(defaultTrainingConfig);

        return new DDPGMLPActor(mlp, targetMlp, experienceCache);
    }

}


