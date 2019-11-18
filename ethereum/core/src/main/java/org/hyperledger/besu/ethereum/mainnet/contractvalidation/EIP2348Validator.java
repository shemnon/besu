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
 *
 */

package org.hyperledger.besu.ethereum.mainnet.contractvalidation;

import java.util.BitSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.besu.ethereum.mainnet.ContractValidationRule;
import org.hyperledger.besu.ethereum.vm.EVM;
import org.hyperledger.besu.ethereum.vm.MessageFrame;
import org.hyperledger.besu.ethereum.vm.Operation;
import org.hyperledger.besu.ethereum.vm.OperationRegistry;
import org.hyperledger.besu.ethereum.vm.operations.JumpDestOperation;
import org.hyperledger.besu.ethereum.vm.operations.JumpOperation;
import org.hyperledger.besu.ethereum.vm.operations.JumpiOperation;
import org.hyperledger.besu.ethereum.vm.operations.PushOperation;
import org.hyperledger.besu.util.bytes.BytesValue;

public class EIP2348Validator implements ContractValidationRule {

  private static final Logger LOG = LogManager.getLogger();

  public static final int EVM_MAGIC_NUMBER = 0x00000001;

  @Override
  public boolean validate(final MessageFrame frame, final EVM evm) {
    final BytesValue outputData = frame.getOutputData();
    // no magic number means no validation checks
    if (outputData.getInt(0) != EVM_MAGIC_NUMBER) {
      return true;
    }

    final OperationRegistry operationRegistry = evm.getOperationRegistry();
    // cleanup later - EVM returns current account creation version
    final int accountVersion = 0;

    final byte[] contractData = outputData.getArrayUnsafe();

    final BitSet expectAJumpDest = new BitSet(contractData.length);
    final BitSet notAJumpDest = new BitSet(contractData.length);
    notAJumpDest.set(0, contractData.length);

    Operation lastOperation = null;

    int i = 4;
    while (i < contractData.length) {
      final byte opcode = contractData[i];

      // cleanup later when BEGINDATA is in the operation registry
      if (opcode == 0xB6) {
        break;
      }

      // if the operation is not in the registry validation fails
      final Operation op = operationRegistry.get(opcode, accountVersion);
      if (op == null) {
        // invalid operation
        LOG.trace("Invalid operation 0x{} at code index {}", Integer.toHexString(opcode & 0xff), i);
        return false;
      }

      // static jump dest analysis data gathering
      // cleanup later with isJump, isJumpDest, and isPush methods
      if (op instanceof JumpDestOperation) {
        // set an actual jump dest
        notAJumpDest.clear(i);
      } else if (op instanceof JumpOperation || op instanceof JumpiOperation) {
        // calculate a static jumpdest
        if (lastOperation instanceof PushOperation) {
          final int pushSize = lastOperation.getOpSize() - 1;
          final int staticJumpDest =
              BytesValue.wrap(contractData, i - pushSize, pushSize).getInt(0);
          expectAJumpDest.set(staticJumpDest);
        }
      }

      // save last op for push/jump[i] analysis
      lastOperation = op;
      i += op.getOpSize();
    }

    // If an expected jump dest is not a jump dest validation fails
    if (expectAJumpDest.intersects(notAJumpDest)) {
      expectAJumpDest.and(notAJumpDest);
      LOG.trace(
          "Static jump analysis failed, location {} is not a jump dest",
          expectAJumpDest.nextSetBit(0));
      return false;
    }

    // the code passed the gauntlet, declare it valid.
    return true;
  }
}
