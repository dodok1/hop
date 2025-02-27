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
 *
 */

package org.apache.hop.beam.gui;

import org.apache.commons.lang.StringUtils;
import org.apache.hop.beam.engines.dataflow.BeamDataFlowPipelineEngine;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.extension.ExtensionPoint;
import org.apache.hop.core.extension.IExtensionPoint;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.execution.ExecutionState;
import org.apache.hop.ui.hopgui.perspective.execution.PipelineExecutionViewer;

@ExtensionPoint(
    id = "PipelineExecutionViewerUpdateXP",
    extensionPointId = "PipelineExecutionViewerUpdate",
    description = "Update the toolbar icons we add in the Beam GUI plugin")
public final class PipelineExecutionViewerUpdateXP
    implements IExtensionPoint<PipelineExecutionViewer> {
  @Override
  public void callExtensionPoint(
      ILogChannel log, IVariables variables, PipelineExecutionViewer viewer) throws HopException {

    String jobId = null;
    ExecutionState executionState = viewer.getExecutionState();
    if (executionState != null) {
      jobId = executionState.getDetails().get(BeamDataFlowPipelineEngine.DETAIL_DATAFLOW_JOB_ID);
    }
    // Enable/Disable the toolbar icon in the pipeline execution viewer.
    //
    viewer
        .getToolBarWidgets()
        .enableToolbarItem(
            HopBeamGuiPlugin.TOOLBAR_ID_PIPELINE_EXECUTION_VIEWER_VISIT_GCP_DATAFLOW,
            StringUtils.isNotEmpty(jobId));
  }
}
