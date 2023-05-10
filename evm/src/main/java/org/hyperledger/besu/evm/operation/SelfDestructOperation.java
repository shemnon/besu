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

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.evm.EVM;
import org.hyperledger.besu.evm.account.Account;
import org.hyperledger.besu.evm.account.MutableAccount;
import org.hyperledger.besu.evm.frame.ExceptionalHaltReason;
import org.hyperledger.besu.evm.frame.MessageFrame;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;
import org.hyperledger.besu.evm.internal.Words;

/** The Self destruct operation. */
public class SelfDestructOperation extends AbstractOperation {

  final boolean eip6780Semantics;

  /**
   * Instantiates a new Self destruct operation.
   *
   * @param gasCalculator the gas calculator
   */
  public SelfDestructOperation(final GasCalculator gasCalculator) {
    this(gasCalculator, false);
  }

  /**
   * Instantiates a new Self destruct operation, with an optional EIP-6780 semantics flag. EIP-6780
   * will only remove an account if the account was created within the current transaction. All
   * other semantics remain.
   *
   * @param gasCalculator the gas calculator
   * @param eip6780Semantics Enforce EIP6780 semantics.
   */
  public SelfDestructOperation(final GasCalculator gasCalculator, final boolean eip6780Semantics) {
    super(0xFF, "SELFDESTRUCT", 1, 0, gasCalculator);
    this.eip6780Semantics = eip6780Semantics;
  }

  @Override
  public OperationResult execute(final MessageFrame frame, final EVM evm) {
    final Address recipientAddress = Words.toAddress(frame.popStackItem());

    // because of weird EIP150/158 reasons we care about a null account, so we can't merge this.
    final Account recipientNullable = frame.getWorldUpdater().get(recipientAddress);
    final Wei inheritance = frame.getWorldUpdater().get(frame.getRecipientAddress()).getBalance();

    final boolean accountIsWarm =
        frame.warmUpAddress(recipientAddress) || gasCalculator().isPrecompile(recipientAddress);

    final long cost =
        gasCalculator().selfDestructOperationGasCost(recipientNullable, inheritance)
            + (accountIsWarm ? 0L : gasCalculator().getColdAccountAccessCost());

    if (frame.isStatic()) {
      return new OperationResult(cost, ExceptionalHaltReason.ILLEGAL_STATE_CHANGE);
    } else if (frame.getRemainingGas() < cost) {
      return new OperationResult(cost, ExceptionalHaltReason.INSUFFICIENT_GAS);
    }

    final Address address = frame.getRecipientAddress();
    final MutableAccount account = frame.getWorldUpdater().getAccount(address).getMutable();

    if (!eip6780Semantics || account.isNewAccount()) {
      frame.addSelfDestruct(address);
    }

    final MutableAccount recipient =
        frame.getWorldUpdater().getOrCreate(recipientAddress).getMutable();

    if (!account.getAddress().equals(recipient.getAddress())) {
      recipient.incrementBalance(account.getBalance());
    }

    // add refund in message frame
    frame.addRefund(recipient.getAddress(), account.getBalance());

    account.setBalance(Wei.ZERO);

    frame.setState(MessageFrame.State.CODE_SUCCESS);
    return new OperationResult(cost, null);
  }
}
