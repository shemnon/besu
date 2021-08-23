/*
 * Copyright contributors to Hyperledger Besu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.besu.evm.tracing;

import org.hyperledger.besu.evm.Gas;
import org.hyperledger.besu.evm.MessageFrame;
import org.hyperledger.besu.evm.OperationTracer;
import org.hyperledger.besu.evm.operations.SStoreOperation;

public class EstimateGasOperationTracer implements OperationTracer {

  private int maxDepth = 0;

  private Gas sStoreStipendNeeded = Gas.ZERO;

  @Override
  public void traceExecution(
      final MessageFrame frame, final OperationTracer.ExecuteOperation executeOperation) {
    try {
      executeOperation.execute();
    } finally {
      if (frame.getCurrentOperation() instanceof SStoreOperation
          && sStoreStipendNeeded.compareTo(Gas.ZERO) == 0) {
        sStoreStipendNeeded =
            ((SStoreOperation) frame.getCurrentOperation()).getMinumumGasRemaining();
      }
      if (maxDepth < frame.getMessageStackDepth()) {
        maxDepth = frame.getMessageStackDepth();
      }
    }
  }

  public int getMaxDepth() {
    return maxDepth;
  }

  public Gas getStipendNeeded() {
    return sStoreStipendNeeded;
  }
}
