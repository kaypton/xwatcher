package com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.critic;

import ai.djl.ndarray.NDList;
import ai.djl.training.TrainingConfig;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.components.DDPGCriticInput;
import com.github.fenrir.xdeepstrategy.nn.MultilayerPerceptron;

public class DDPGMLPCritic {

    private final MultilayerPerceptron mlp;
    private final MultilayerPerceptron targetMlp;

    private static class CriticTranslator implements Translator<DDPGCriticInput, Double> {

        @Override
        public Double processOutput(TranslatorContext ctx, NDList list) throws Exception {
            return null;
        }

        @Override
        public NDList processInput(TranslatorContext ctx, DDPGCriticInput input) throws Exception {
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

    public DDPGMLPCritic(MultilayerPerceptron mlp,
                         MultilayerPerceptron targetMlp,
                         TrainingConfig trainingConfig){
        this.mlp = mlp;
        this.targetMlp = targetMlp;

        this.mlp.setTrainingConfig(trainingConfig);
    }
}
