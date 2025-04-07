/*
 * Copyright ConsenSys AG.
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
package org.hyperledger.besu.evm.operation;

import static org.hyperledger.besu.evm.internal.Words.clampedAdd;
import static org.hyperledger.besu.evm.internal.Words.clampedToInt;
import static org.hyperledger.besu.evm.internal.Words.clampedToLong;

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.evm.Code;
import org.hyperledger.besu.evm.EVM;
import org.hyperledger.besu.evm.account.Account;
import org.hyperledger.besu.evm.frame.MessageFrame;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;

import java.util.function.Supplier;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

/** The Create operation. */
public class CreateOperation extends AbstractCreateOperation {

  /**
   * Instantiates a new Create operation.
   *
   * @param gasCalculator the gas calculator
   */
  public CreateOperation(final GasCalculator gasCalculator) {
    super(0xF0, "CREATE", 3, 1, gasCalculator, 0, 0);
  }

  @Override
  public long cost(final MessageFrame frame, final Supplier<Code> unused) {
    final long inputOffset = getInputOffset(frame);
    final long inputSize = getInputSize(frame);
    return clampedAdd(
        clampedAdd(
            gasCalculator().txCreateCost(),
            gasCalculator().memoryExpansionGasCost(frame, inputOffset, inputSize)),
        gasCalculator().initcodeCost(clampedToInt(inputSize)));
  }

  @Override
  protected Address generateTargetContractAddress(final MessageFrame frame, final Code initcode) {
    final Account sender = frame.getWorldUpdater().get(frame.getRecipientAddress());
    // Decrement nonce by 1 to normalize the effect of transaction execution
    return Address.contractAddress(frame.getRecipientAddress(), sender.getNonce() - 1L);
  }

  @Override
  protected Code getInitCode(final MessageFrame frame, final EVM evm) {
    final long inputOffset = getInputOffset(frame);
    final long inputSize = getInputSize(frame);
    final Bytes inputData = frame.readMemory(inputOffset, inputSize);
    // Never cache CREATEx initcode. The amount of reuse is very low, and caching mostly
    // addresses disk loading delay, and we already have the code.
    return evm.getCodeUncached(inputData, 0);
  }

  @Override
  protected long getInputOffset(final MessageFrame frame) {
    return clampedToLong(frame.getStackItem(1));
  }

  @Override
  protected long getInputSize(final MessageFrame frame) {
    return clampedToLong(frame.getStackItem(2));
  }

  @Override
  protected Wei getValue(final MessageFrame frame) {
    return Wei.wrap(frame.getStackItem(0));
  }

  @Override
  protected Bytes32 getSalt(final MessageFrame frame) {
    return Bytes32.ZERO;
  }
}
