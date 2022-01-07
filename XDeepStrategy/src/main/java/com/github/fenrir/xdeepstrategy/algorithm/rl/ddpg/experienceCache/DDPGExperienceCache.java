package com.github.fenrir.xdeepstrategy.algorithm.rl.ddpg.experienceCache;

import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.training.dataset.Batch;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.dataset.Record;
import ai.djl.translate.TranslateException;
import ai.djl.util.Progress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;

public class DDPGExperienceCache extends RandomAccessDataset {
    private final LinkedList<DDPGExperienceCacheItem> cacheLinkedList = new LinkedList<>();
    private int maxSize;
    private final NDManager manager = NDManager.newBaseManager();

    public DDPGExperienceCache(BaseBuilder<?> builder, int maxSize){
        super(builder);
        this.maxSize = maxSize;
    }

    public void put(DDPGExperienceCacheItem item){
        if(cacheLinkedList.size() >= this.maxSize){
            cacheLinkedList.removeFirst();
        }
        cacheLinkedList.addLast(item);
    }

    public Iterable<Batch> getDataBatchIterator() throws TranslateException, IOException {
        return super.getData(this.manager);
    }

    @Override
    public Record get(NDManager manager, long index) {
        NDList data = new NDList();
        NDList label = new NDList();

        DDPGExperienceCacheItem item = this.cacheLinkedList.get((int) index);

        data.add(item.getState0().toNDArray());
        data.add(item.getAction().toNDArray());
        data.add(item.getReward().toNDArray());
        data.add(item.getState1().toNDArray());

        data.attach(this.manager);
        label.attach(this.manager);

        return new Record(data, label);
    }

    @Override
    protected long availableSize() {
        return this.cacheLinkedList.size();
    }

    @Override
    public void prepare(Progress progress) {}

    public static final class Builder extends BaseBuilder<DDPGExperienceCache.Builder> {
        private static final Logger LOGGER = LoggerFactory.getLogger(Builder.class);

        private int maxSize = -1;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder setMaxSize(int maxSize){
            this.maxSize = maxSize;
            return this;
        }

        public DDPGExperienceCache build() {
            if (this.maxSize == -1) {
                throw new IllegalArgumentException("Please set max cache size");
            }
            return new DDPGExperienceCache(this, this.maxSize);
        }
    }
}
