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
package org.hyperledger.besu.ethereum.vm.operations;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.hyperledger.besu.config.StubGenesisConfigOptions;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.ethereum.core.AddressHelpers;
import org.hyperledger.besu.ethereum.core.ByteCodeBuilder;
import org.hyperledger.besu.ethereum.core.ByteCodeBuilder.Operation;
import org.hyperledger.besu.ethereum.core.TestCodeExecutor;
import org.hyperledger.besu.ethereum.mainnet.MainnetProtocolSchedule;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSchedule;
import org.hyperledger.besu.evm.frame.MessageFrame;
import org.hyperledger.besu.evm.frame.MessageFrame.State;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class TStoreEVMOperationTest {

  private static ProtocolSchedule protocolSchedule;
  private static final UInt256 gasLimit = UInt256.valueOf(50_000);
  private static final Address contractAddress = AddressHelpers.ofValue(28499200);

  @Parameters(name = "ByteCodeBuilder: {0}, Expected Result State: {1}, Expected Return Value: {2}")
  public static Object[][] scenarios() {
    // Tests specified in EIP-1153.
    return new Object[][] {
            // Can tstore
            {null, new ByteCodeBuilder().tstore(1, 1), State.COMPLETED_SUCCESS, 0},
            // Can tload uninitialized
            {null,
                    new ByteCodeBuilder()
                    .tload(1)
                    .dataOnStackToMemory(0)
                    .returnValueAtMemory(32, 0), State.COMPLETED_SUCCESS, 0},
            // Can tload after tstore
            {null,
                    new ByteCodeBuilder()
                    .tstore(1, 2)
                    .tload(1)
                    .dataOnStackToMemory(0)
                    .returnValueAtMemory(32, 0), State.COMPLETED_SUCCESS, 2},
            // Can tload after tstore from different location
            {null,
                    new ByteCodeBuilder()
                    .tstore(1, 2)
                    .tload(2)
                    .dataOnStackToMemory(0)
                    .returnValueAtMemory(32, 0), State.COMPLETED_SUCCESS, 0},
            // Contracts have separate transient storage
            {new ByteCodeBuilder()
                    .tload(1)
                    .dataOnStackToMemory(0)
                    .returnValueAtMemory(32, 0),
                    new ByteCodeBuilder()
                            .tstore(1, 2)
                            .call(Operation.CALL, contractAddress, gasLimit)
                            .returnInnerCallResults(),
                    State.COMPLETED_SUCCESS,
                    0},
            // Reentrant calls access the same transient storage
            {new ByteCodeBuilder()
                    // check if caller is self
                    .callerIs(contractAddress)
                    .conditionalJump(78)
                    // non-reentrant, call self after tstore
                    .tstore(1, 8)
                    .call(Operation.CALL, contractAddress, gasLimit)
                    .returnInnerCallResults()
                    // reentrant, TLOAD and return value
                    .op(Operation.JUMPDEST)
                    .tload(1)
                    .dataOnStackToMemory(0)
                    .returnValueAtMemory(32, 0),
                    new ByteCodeBuilder()
                            .call(Operation.CALL, contractAddress, gasLimit)
                            .returnInnerCallResults(),
                    State.COMPLETED_SUCCESS,
                    8},
            // Successfully returned calls do not revert transient storage writes
            {new ByteCodeBuilder()
                    // check if caller is self
                    .callerIs(contractAddress)
                    .conditionalJump(77)
                    // non-reentrant, call self after tstore
                    .tstore(1, 8)
                    .call(Operation.CALL, contractAddress, gasLimit)
                    .tload(1)
                    .dataOnStackToMemory(0)
                    .returnValueAtMemory(32, 0)
                    // reentrant, TLOAD and return value
                    .op(Operation.JUMPDEST)
                    .tstore(1, 9),
                    new ByteCodeBuilder()
                            .call(Operation.CALL, contractAddress, gasLimit)
                            .returnInnerCallResults(),
                    State.COMPLETED_SUCCESS,
                    9},
            // Revert undoes the transient storage write from the failed call
            {new ByteCodeBuilder()
                    // check if caller is self
                    .callerIs(contractAddress)
                    .conditionalJump(77)
                    // non-reentrant, call self after tstore
                    .tstore(1, 8)
                    .call(Operation.CALL, contractAddress, gasLimit)
                    .tload(1)
                    .dataOnStackToMemory(0)
                    .returnValueAtMemory(32, 0)
                    // reentrant, TLOAD and return value
                    .op(Operation.JUMPDEST)
                    .tstore(1, 9)
                    .op(Operation.REVERT),
                    new ByteCodeBuilder()
                            .call(Operation.CALL, contractAddress, gasLimit)
                            .returnInnerCallResults(),
                    State.COMPLETED_SUCCESS,
                    8},
            // Revert undoes all the transient storage writes to the same key from the failed call
            {new ByteCodeBuilder()
                    // check if caller is self
                    .callerIs(contractAddress)
                    .conditionalJump(77)
                    // non-reentrant, call self after tstore
                    .tstore(1, 8)
                    .call(Operation.CALL, contractAddress, gasLimit)
                    .tload(1)
                    .dataOnStackToMemory(0)
                    .returnValueAtMemory(32, 0)
                    // reentrant, TLOAD and return value
                    .op(Operation.JUMPDEST)
                    .tstore(1, 9)
                    .tstore(1, 10)
                    .op(Operation.REVERT),
                    new ByteCodeBuilder()
                            .call(Operation.CALL, contractAddress, gasLimit)
                            .returnInnerCallResults(),
                    State.COMPLETED_SUCCESS,
                    8},
            // Revert undoes transient storage writes from inner calls that successfully returned
            {new ByteCodeBuilder()
                    // Check call depth
                    .push(0)
                    .op(Operation.CALLDATALOAD)
                    // Store input in mem and reload it to stack
                    .dataOnStackToMemory(5)
                    .push(5)
                    .op(Operation.MLOAD)

                    // See if we're at call depth 1
                    .push(1)
                    .op(Operation.EQ)
                    .conditionalJump(84)

                    // See if we're at call depth 2
                    .push(5)
                    .op(Operation.MLOAD)
                    .push(2)
                    .op(Operation.EQ)
                    .conditionalJump(135)

                    // Call depth = 0, call self after TSTORE 8
                    .tstore(1, 8)

                    // Recursive call with input
                      // Depth++
                      .push(5)
                      .op(Operation.MLOAD)
                      .push(1)
                      .op(Operation.ADD)
                      .callWithInput(contractAddress, gasLimit)

                    // TLOAD and return value
                    .tload(1)
                    .dataOnStackToMemory(0)
                    .returnValueAtMemory(32, 0)

                    // Call depth 1, TSTORE 9 but REVERT after recursion
                    .op(Operation.JUMPDEST)
                    .tstore(1, 9)

                    // Recursive call with input
                      // Depth++
                      .push(5)
                      .op(Operation.MLOAD)
                      .push(1)
                      .op(Operation.ADD)
                      .callWithInput(contractAddress, gasLimit)

                    .op(Operation.REVERT)

                    // Call depth 2, TSTORE 10 and complete
                    .op(Operation.JUMPDEST)
                    .tstore(1, 10)
                    ,
                    new ByteCodeBuilder()
                            // Call with input 0
                            .callWithInput(contractAddress, gasLimit, UInt256.valueOf(0))
                            .returnInnerCallResults(),
                    State.COMPLETED_SUCCESS,
                    8},
            // Transient storage cannot be manipulated in a static context
            {new ByteCodeBuilder()
                    .tstore(1, 8)
                    .tload(1)
                    .dataOnStackToMemory(0)
                    .returnValueAtMemory(32, 0),
                    new ByteCodeBuilder()
                            .call(Operation.STATICCALL, contractAddress, gasLimit)
                            .returnInnerCallResults(),
                    State.COMPLETED_FAILED,
                    0},
    };
  }

  private TestCodeExecutor codeExecutor;

  @Parameter(value = 0)
  public ByteCodeBuilder contractByteCodeBuilder;

  @Parameter(value = 1)
  public ByteCodeBuilder byteCodeBuilder;

  @Parameter(value = 2)
  public State expectedResultState;

  @Parameter(value = 3)
  public int expectedReturnValue;


  @Before
  public void setUp() {
    protocolSchedule =
        MainnetProtocolSchedule.fromConfig(
            new StubGenesisConfigOptions().shanghaiBlock(0), EvmConfiguration.DEFAULT);
    codeExecutor = new TestCodeExecutor(protocolSchedule,
            account -> account.setStorageValue(UInt256.ZERO, UInt256.valueOf(0)));
  }

  @Test
  public void transientStorageExecutionTest() {
    if (contractByteCodeBuilder != null) {

      for (int i = 0; i < contractByteCodeBuilder.toBytes().toArray().length; i++) {
        if (contractByteCodeBuilder.toBytes().toArray()[i] == (byte)91) {
          System.out.println(i);
        }
      }
      codeExecutor.deployContract(
            contractAddress,
            contractByteCodeBuilder.toString());
    }

    final MessageFrame frame =
        codeExecutor.executeCode(
            byteCodeBuilder.toString(),
            gasLimit.toLong());
    assertThat(frame.getState()).isEqualTo(expectedResultState);
    // assertThat(frame.getRemainingGas()).isEqualTo(gasLimit - byteCodeBuilder.gasCost);
    assertThat(frame.getGasRefund()).isEqualTo(0);
    assertThat(UInt256.fromBytes(frame.getOutputData()).toInt()).isEqualTo(expectedReturnValue);
  }
}
