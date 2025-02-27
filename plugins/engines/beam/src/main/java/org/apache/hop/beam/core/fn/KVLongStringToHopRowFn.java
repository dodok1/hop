/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.beam.core.fn;

import org.apache.beam.sdk.metrics.Counter;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.apache.hop.beam.core.BeamHop;
import org.apache.hop.beam.core.HopRow;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.JsonRowMeta;
import org.apache.hop.pipeline.Pipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class KVLongStringToHopRowFn extends DoFn<KV<Long, String>, HopRow> {

  private String rowMetaJson;
  private String transformName;
  private List<String> transformPluginClasses;
  private List<String> xpPluginClasses;

  private static final Logger LOG = LoggerFactory.getLogger(KVLongStringToHopRowFn.class);
  private final Counter numErrors = Metrics.counter("main", "BeamSubscribeTransformErrors");

  private IRowMeta rowMeta;
  private transient Counter initCounter;
  private transient Counter inputCounter;
  private transient Counter writtenCounter;

  public KVLongStringToHopRowFn(
      String transformName,
      String rowMetaJson,
      List<String> transformPluginClasses,
      List<String> xpPluginClasses) {
    this.transformName = transformName;
    this.rowMetaJson = rowMetaJson;
    this.transformPluginClasses = transformPluginClasses;
    this.xpPluginClasses = xpPluginClasses;
  }

  @Setup
  public void setUp() {
    try {
      inputCounter = Metrics.counter(Pipeline.METRIC_NAME_INPUT, transformName);
      writtenCounter = Metrics.counter(Pipeline.METRIC_NAME_WRITTEN, transformName);

      // Initialize Hop Beam
      //
      BeamHop.init(transformPluginClasses, xpPluginClasses);
      rowMeta = JsonRowMeta.fromJson(rowMetaJson);

      Metrics.counter(Pipeline.METRIC_NAME_INIT, transformName).inc();
    } catch (Exception e) {
      numErrors.inc();
      LOG.error("Error in setup of KV<Long,String> to Hop Row conversion function", e);
      throw new RuntimeException(
          "Error in setup of KV<Long,String> to Hop Row conversion function", e);
    }
  }

  @ProcessElement
  public void processElement(ProcessContext processContext) {
    try {

      KV<Long, String> kv = processContext.element();
      inputCounter.inc();

      Object[] outputRow = new Object[rowMeta.size()];
      outputRow[0] = kv.getKey();
      outputRow[1] = kv.getValue();

      processContext.output(new HopRow(outputRow));
      writtenCounter.inc();

    } catch (Exception e) {
      numErrors.inc();
      LOG.error("Error in KV<Long,String> to Hop Row conversion function", e);
      throw new RuntimeException("Error in KV<Long,String> to Hop Row conversion function", e);
    }
  }
}
