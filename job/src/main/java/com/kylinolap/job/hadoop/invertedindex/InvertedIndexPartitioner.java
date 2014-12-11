/*
 * Copyright 2013-2014 eBay Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kylinolap.job.hadoop.invertedindex;

import java.io.IOException;

import com.kylinolap.invertedindex.IIInstance;
import com.kylinolap.invertedindex.IIManager;
import com.kylinolap.invertedindex.IISegment;
import com.kylinolap.invertedindex.index.TableRecord;
import com.kylinolap.invertedindex.index.TableRecordInfo;
import com.kylinolap.metadata.realization.SegmentStatusEnum;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Partitioner;

import com.kylinolap.common.KylinConfig;
import com.kylinolap.job.constant.BatchConstants;
import com.kylinolap.job.hadoop.AbstractHadoopJob;

/**
 * @author yangli9
 * 
 */
public class InvertedIndexPartitioner extends Partitioner<LongWritable, ImmutableBytesWritable> implements Configurable {

    private Configuration conf;
    private TableRecordInfo info;
    private TableRecord rec;

    @Override
    public int getPartition(LongWritable key, ImmutableBytesWritable value, int numPartitions) {
        rec.setBytes(value.get(), value.getOffset(), value.getLength());
        return rec.getShard();
    }

    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
        try {
            KylinConfig config = AbstractHadoopJob.loadKylinPropsAndMetadata(conf);
            IIManager mgr = IIManager.getInstance(config);
            IIInstance ii = mgr.getII(conf.get(BatchConstants.CFG_II_NAME));
            IISegment seg = ii.getSegment(conf.get(BatchConstants.CFG_II_SEGMENT_NAME), SegmentStatusEnum.NEW);
            this.info = new TableRecordInfo(seg);
            this.rec = new TableRecord(this.info);
        } catch (IOException e) {
            throw new RuntimeException("", e);
        }
    }

    @Override
    public Configuration getConf() {
        return conf;
    }

}
