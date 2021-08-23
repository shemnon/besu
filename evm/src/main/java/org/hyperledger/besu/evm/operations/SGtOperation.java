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
package org.hyperledger.besu.evm.operations;

import org.hyperledger.besu.evm.EVM;
import org.hyperledger.besu.evm.GasCalculator;
import org.hyperledger.besu.evm.MessageFrame;
import org.hyperledger.besu.evm.Operation;

import java.math.BigInteger;

import org.apache.tuweni.units.bigints.UInt256;

public class SGtOperation extends AbstractFixedCostOperation {

  public SGtOperation(final GasCalculator gasCalculator) {
    super(0x13, "SGT", 2, 1, false, 1, gasCalculator, gasCalculator.getVeryLowTierGasCost());
  }

  @Override
  public Operation.OperationResult executeFixedCostOperation(
      final MessageFrame frame, final EVM evm) {
    final UInt256 value0 = frame.popStackItem();
    final UInt256 value1 = frame.popStackItem();

    final BigInteger b0 = value0.toSignedBigInteger();
    final BigInteger b1 = value1.toSignedBigInteger();

    final UInt256 result = b0.compareTo(b1) > 0 ? UInt256.ONE : UInt256.ZERO;

    frame.pushStackItem(result);

    return successResponse;
  }
}
