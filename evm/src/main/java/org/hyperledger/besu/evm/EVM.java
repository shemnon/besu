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
package org.hyperledger.besu.evm;

import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.evm.frame.ExceptionalHaltReason;
import org.hyperledger.besu.evm.frame.MessageFrame;
import org.hyperledger.besu.evm.frame.MessageFrame.State;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;
import org.hyperledger.besu.evm.internal.CodeCache;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.evm.internal.FixedStack.OverflowException;
import org.hyperledger.besu.evm.internal.FixedStack.UnderflowException;
import org.hyperledger.besu.evm.operation.InvalidOperation;
import org.hyperledger.besu.evm.operation.Operation;
import org.hyperledger.besu.evm.operation.Operation.OperationResult;
import org.hyperledger.besu.evm.operation.OperationRegistry;
import org.hyperledger.besu.evm.operation.StopOperation;
import org.hyperledger.besu.evm.operation.VirtualOperation;
import org.hyperledger.besu.evm.tracing.OperationTracer;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.BiFunction;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EVM {
  protected static final OperationResult OVERFLOW_RESPONSE =
      new OperationResult(
          OptionalLong.of(0L), Optional.of(ExceptionalHaltReason.TOO_MANY_STACK_ITEMS));
  protected static final OperationResult UNDERFLOW_RESPONSE =
      new OperationResult(
          OptionalLong.of(0L), Optional.of(ExceptionalHaltReason.INSUFFICIENT_STACK_ITEMS));
  private static final Logger LOG = LoggerFactory.getLogger(EVM.class);
  private final OperationRegistry operations;
  private final GasCalculator gasCalculator;
  private final Operation endOfScriptStop;
  private final CodeCache codeCache;

  /** @deprecated Use EVMBuilder */
  @Deprecated(since = "22.10.0", forRemoval = true)
  public EVM(
      final OperationRegistry operations,
      final GasCalculator gasCalculator,
      final EvmConfiguration evmConfiguration) {
    this(operations, gasCalculator, evmConfiguration.getJumpDestCacheWeightBytes());
  }

  EVM(
      final OperationRegistry operations,
      final GasCalculator gasCalculator,
      final long jumpDestCacheWeightBytes) {
    this.operations = operations;
    this.gasCalculator = gasCalculator;
    this.endOfScriptStop = new VirtualOperation(new StopOperation(gasCalculator));
    this.codeCache = new CodeCache(jumpDestCacheWeightBytes);
  }

  private static void logState(final MessageFrame frame, final long currentGasCost) {
    if (LOG.isTraceEnabled()) {
      final StringBuilder builder = new StringBuilder();
      builder.append("Depth: ").append(frame.getMessageStackDepth()).append("\n");
      builder.append("Operation: ").append(frame.getCurrentOperation().getName()).append("\n");
      builder.append("PC: ").append(frame.getPC()).append("\n");
      builder.append("Gas cost: ").append(currentGasCost).append("\n");
      builder.append("Gas Remaining: ").append(frame.getRemainingGas()).append("\n");
      builder.append("Depth: ").append(frame.getMessageStackDepth()).append("\n");
      builder.append("Stack:");
      for (int i = 0; i < frame.stackSize(); ++i) {
        builder.append("\n\t").append(i).append(" ").append(frame.getStackItem(i));
      }
      LOG.trace(builder.toString());
    }
  }

  public GasCalculator getGasCalculator() {
    return gasCalculator;
  }

  public void runToHalt(final MessageFrame frame, final OperationTracer operationTracer) {
    while (frame.getState() == MessageFrame.State.CODE_EXECUTING) {
      executeNextOperation(frame, operationTracer);
    }
  }

  private void executeNextOperation(
      final MessageFrame frame, final OperationTracer operationTracer) {
    frame.setCurrentOperation(operationAtOffset(frame.getCode(), frame.getPC()));
    operationTracer.traceExecution(
        frame,
        () -> {
          OperationResult result;
          final Operation operation = frame.getCurrentOperation();
          try {
            result = operation.execute(frame, this);
          } catch (final OverflowException oe) {
            result = OVERFLOW_RESPONSE;
          } catch (final UnderflowException ue) {
            result = UNDERFLOW_RESPONSE;
          }
          logState(frame, result.getGasCost().orElse(0L));
          final Optional<ExceptionalHaltReason> haltReason = result.getHaltReason();
          if (haltReason.isPresent()) {
            LOG.trace("MessageFrame evaluation halted because of {}", haltReason.get());
            frame.setExceptionalHaltReason(haltReason);
            frame.setState(State.EXCEPTIONAL_HALT);
          } else if (result.getGasCost().isPresent()) {
            frame.decrementRemainingGas(result.getGasCost().getAsLong());
          }
          if (frame.getState() == State.CODE_EXECUTING) {
            final int currentPC = frame.getPC();
            final int opSize = result.getPcIncrement();
            frame.setPC(currentPC + opSize);
          }

          return result;
        });
  }

  @VisibleForTesting
  public Operation operationAtOffset(final Code code, final int offset) {
    final Bytes bytecode = code.getBytes();
    // If the length of the program code is shorter than the offset halt execution.
    if (offset >= bytecode.size()) {
      return endOfScriptStop;
    }

    final byte opcode = bytecode.get(offset);
    final Operation operation = operations.get(opcode);
    return Objects.requireNonNullElseGet(operation, () -> new InvalidOperation(opcode, null));
  }

  public Code getCode(final Hash codeHash, final Bytes codeBytes) {
    Code result = codeCache.getIfPresent(codeHash);
    if (result == null) {
      result = new Code(codeBytes, codeHash);
      codeCache.put(codeHash, result);
    }
    return result;
  }

  public static class Builder {

    BiFunction<GasCalculator, Optional<BigInteger>, List<Operation>> operationsSupplier;
    GasCalculator gasCalculator;
    Optional<BigInteger> chainId = Optional.empty();
    long jumpDestCacheWeightBytes = 32_000L;

    public Builder() {}

    public EVM build() {
      Preconditions.checkNotNull(gasCalculator, "GasCalculator must be set before building");
      final OperationRegistry operations = new OperationRegistry();
      for (final var op : operationsSupplier.apply(gasCalculator, chainId)) {
        operations.put(op);
      }
      return new EVM(operations, gasCalculator, jumpDestCacheWeightBytes);
    }

    public Builder operationsSupplier(
        final BiFunction<GasCalculator, Optional<BigInteger>, List<Operation>> operationsSupplier) {
      this.operationsSupplier = operationsSupplier;
      return this;
    }

    public Builder gasCalculator(final GasCalculator gasCalculator) {
      this.gasCalculator = gasCalculator;
      return this;
    }

    public Builder jumpDestCacheWeightBytes(final long jumpDestCacheWeightBytes) {
      this.jumpDestCacheWeightBytes = jumpDestCacheWeightBytes;
      return this;
    }

    public Builder chainId(final Optional<BigInteger> chainId) {
      this.chainId = chainId;
      return this;
    }
  }
}
