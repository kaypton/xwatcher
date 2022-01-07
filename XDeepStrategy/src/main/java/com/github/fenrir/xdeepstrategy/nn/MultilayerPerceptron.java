package com.github.fenrir.xdeepstrategy.nn;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.*;
import ai.djl.nn.core.Linear;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.Trainer;
import ai.djl.training.TrainingConfig;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.initializer.Initializer;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.optimizer.Adam;
import ai.djl.translate.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.nio.file.Paths;

public class MultilayerPerceptron {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultilayerPerceptron.class);

    public enum ActivationFunction {
        RELU,
        TANH
    }

    private Model model;
    private TrainingConfig trainingConfig = null;
    private Trainer trainer = null;

    private MultilayerPerceptron(SequentialBlock sequentialBlock, String name, DataType dataType){
        this.model = Model.newInstance(name);
        this.model.setBlock(sequentialBlock);
        this.model.setDataType(dataType);
    }

    public void saveModel(String modelPath, String modelName) throws IOException {
        this.model.save(Paths.get(modelPath), modelName);
    }

    public void setDefaultTrainingConfig(){
        // softmaxCrossEntropyLoss is a standard loss for classification problems
        this.trainingConfig = new DefaultTrainingConfig(Loss.l2Loss()) // Softmax Cross Entropy loss function
                .optOptimizer(Adam.builder().build()) // Adam optimizer
                .addEvaluator(new Accuracy()) // Use accuracy so we humans can understand how accurate the model is
                .addTrainingListeners(TrainingListener.Defaults.logging());
    }

    public void setTrainingConfig(TrainingConfig config){
        if(this.trainingConfig != null){
            LOGGER.warn("training config has already been set");
            return;
        }
        this.trainingConfig = config;
    }

    public <I, O> Predictor<I, O> getPredictor(Translator<I, O> translator){
        return this.model.newPredictor(translator);
    }

    public Trainer getTrainer(){
        if(this.trainer == null){
            this.setTrainer();
        }
        return this.trainer;
    }

    public void initialize(Shape shape){
        if(this.model.getBlock().isInitialized()){
            LOGGER.warn("model has already been initialized");
            return;
        }
        this.model.getBlock().initialize(
                this.model.getNDManager(),
                this.model.getDataType(),
                shape
        );
    }

    public void copyParameters(MultilayerPerceptron mlp) throws IOException, MalformedModelException {
        this.model.getBlock().loadParameters(
                this.model.getNDManager(),
                new DataInputStream(new ByteArrayInputStream(mlp.getParametersByteArray()))
        );
    }

    public byte[] getParametersByteArray() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(bos);
        // including child block parameters
        this.model.getBlock().saveParameters(os);
        return bos.toByteArray();
    }

    private void setTrainer(){
        if(this.trainer != null){
            LOGGER.warn("trainer has already been set");
            return;
        }
        if(this.trainingConfig == null){
            throw new IllegalStateException("training config has not been set");
        }
        this.trainer = this.model.newTrainer(this.trainingConfig);
    }

    /**
     *
     * @param name mlp model name
     * @return MultilayerPerceptronBuilder the mlp builder
     */
    public static MultilayerPerceptronBuilder builder(String name){
        return new MultilayerPerceptronBuilder(name);
    }

    /**
     * MultilayerPerceptronBuilder
     */
    public static class MultilayerPerceptronBuilder {
        private static final Logger LOGGER = LoggerFactory.getLogger(MultilayerPerceptronBuilder.class);

        private final SequentialBlock net;

        private final String modelName;

        private boolean outputLayerSet = false;
        private DataType dataType = null;

        public MultilayerPerceptronBuilder(String name){
            this.net = new SequentialBlock();
            this.modelName = name;
        }

        public MultilayerPerceptronBuilder setDataType(DataType dataType){
            if(this.dataType != null){
                LOGGER.warn("data type has already been set");
                return this;
            }
            this.dataType = dataType;
            return this;
        }

        public MultilayerPerceptronBuilder addBatchFlattenInputLayer(long size){
            Block batchFlattenBlock = Blocks.batchFlattenBlock(size);
            this.net.add(batchFlattenBlock);
            return this;
        }

        public MultilayerPerceptronBuilder addOutputLayerActiveFunction(ActivationFunction func){
            if(!this.outputLayerSet){
                throw new IllegalStateException("should set output layer before output layer active function");
            }
            return this.addActiveFunction(func);
        }

        private MultilayerPerceptronBuilder addActiveFunction(ActivationFunction func){
            switch (func) {
                case RELU:
                    this.net.add(Activation::relu);
                case TANH:
                    this.net.add(Activation::tanh);
            }
            return this;
        }

        public MultilayerPerceptronBuilder addOutputLayer(long unitNum,
                                                          Initializer weightInitializer,
                                                          Initializer biasInitializer){
            Block block = Linear.builder().setUnits(unitNum).build();
            if(weightInitializer != null){
                block.setInitializer(weightInitializer, Parameter.Type.WEIGHT);
            }
            if(biasInitializer != null){
                block.setInitializer(biasInitializer, Parameter.Type.BIAS);
            }
            this.outputLayerSet = true;
            this.net.add(block);
            return this;
        }

        public MultilayerPerceptronBuilder addHiddenLayer(long unitNum,
                                                          @NotNull ActivationFunction func,
                                                          Initializer weightInitializer,
                                                          Initializer biasInitializer){
            if(this.outputLayerSet){
                throw new IllegalStateException("output layer has already been set, should not add hidden layer after output layer");
            }
            Block block = Linear.builder().setUnits(unitNum).build();
            if(weightInitializer != null){
                block.setInitializer(weightInitializer, Parameter.Type.WEIGHT);
            }
            if(biasInitializer != null){
                block.setInitializer(biasInitializer, Parameter.Type.BIAS);
            }
            this.net.add(block);
            return this.addActiveFunction(func);
        }

        public MultilayerPerceptronBuilder initializer(Initializer weightInitializer,
                                                       Initializer biasInitializer){
            if(weightInitializer != null)
                this.net.setInitializer(weightInitializer, Parameter.Type.WEIGHT);
            if(biasInitializer != null)
                this.net.setInitializer(biasInitializer, Parameter.Type.BIAS);
            return this;
        }

        public MultilayerPerceptron build(){
            if(!this.outputLayerSet){
                throw new IllegalStateException("output layer has not been set");
            }
            if(this.dataType == null){
                throw new IllegalStateException("data type has not been set");
            }
            return new MultilayerPerceptron(this.net, this.modelName, this.dataType);
        }
    }
}
