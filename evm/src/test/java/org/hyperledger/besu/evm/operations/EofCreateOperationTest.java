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
 *
 */
package org.hyperledger.besu.evm.operations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.evm.Code;
import org.hyperledger.besu.evm.EVM;
import org.hyperledger.besu.evm.MainnetEVMs;
import org.hyperledger.besu.evm.account.MutableAccount;
import org.hyperledger.besu.evm.code.CodeFactory;
import org.hyperledger.besu.evm.code.CodeInvalid;
import org.hyperledger.besu.evm.frame.BlockValues;
import org.hyperledger.besu.evm.frame.MessageFrame;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.evm.internal.Words;
import org.hyperledger.besu.evm.log.Log;
import org.hyperledger.besu.evm.precompile.MainnetPrecompiledContracts;
import org.hyperledger.besu.evm.processor.ContractCreationProcessor;
import org.hyperledger.besu.evm.processor.MessageCallProcessor;
import org.hyperledger.besu.evm.tracing.OperationTracer;
import org.hyperledger.besu.evm.worldstate.WorldUpdater;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;

class EofCreateOperationTest {

  private final WorldUpdater worldUpdater = mock(WorldUpdater.class);
  private final MutableAccount account = mock(MutableAccount.class);
  private final MutableAccount newAccount = mock(MutableAccount.class);

  private static final Bytes CALL_DATA =
      Bytes.fromHexString(
          "cafebaba600dbaadc0de57aff60061e5cafebaba600dbaadc0de57aff60061e5"); // 32 bytes
  public static final Bytes INNER_CONTRACT =
      bytesFromPrettyPrint(
          """
         # EOF
         ef0001 # Magic and Version ( 1 )
         010004 # Types length ( 4 )
         020001 # Total code sections ( 1 )
           0009 # Code section 0 , 9 bytes
         030001 # Total subcontainers ( 1 )
           0014 # Sub container 0, 20 byte
         040000 # Data section length(  0 )
             00 # Terminator (end of header)
                # Code section 0 types
             00 # 0 inputs\s
             80 # 0 outputs  (Non-returning function)
           0003 # max stack:  3
                # Code section 0
             5f # [0] PUSH0
             35 # [1] CALLDATALOAD
             5f # [2] PUSH0
             5f # [3] PUSH0
             a1 # [4] LOG1
             5f # [5] PUSH0
             5f # [6] PUSH0
           ee00 # [7] RETURNCONTRACT(0)
                    # Subcontainer 0 starts here
             ef0001 # Magic and Version ( 1 )
             010004 # Types length ( 4 )
             020001 # Total code sections ( 1 )
               0001 # Code section 0 , 1 bytes
             040000 # Data section length(  0 )
                 00 # Terminator (end of header)
                    # Code section 0 types
                 00 # 0 inputs
                 80 # 0 outputs  (Non-returning function)
               0000 # max stack:  0
                    # Code section 0
                 00 # [0] STOP
         """);
  public static final String SENDER = "0xdeadc0de00000000000000000000000000000000";

  //  private static final int SHANGHAI_CREATE_GAS = 41240;

  private static Bytes bytesFromPrettyPrint(final String prettyPrint) {
    return Bytes.fromHexString(prettyPrint.replaceAll("#.*?\n", "").replaceAll("\\s", ""));
  }

  @Test
  void innerContractIsCorrect() {
    Code code = CodeFactory.createCode(INNER_CONTRACT, 1, true);
    assertThat(code.isValid()).isTrue();

    final MessageFrame messageFrame = testMemoryFrame(code, CALL_DATA);

    when(account.getNonce()).thenReturn(55L);
    when(account.getBalance()).thenReturn(Wei.ZERO);
    when(worldUpdater.getAccount(any())).thenReturn(account);
    when(worldUpdater.get(any())).thenReturn(account);
    when(worldUpdater.getSenderAccount(any())).thenReturn(account);
    when(worldUpdater.getOrCreate(any())).thenReturn(newAccount);
    when(newAccount.getCode()).thenReturn(Bytes.EMPTY);
    when(worldUpdater.updater()).thenReturn(worldUpdater);

    final EVM evm = MainnetEVMs.prague(EvmConfiguration.DEFAULT);
    final MessageFrame createFrame = messageFrame.getMessageFrameStack().peek();
    assertThat(createFrame).isNotNull();
    final ContractCreationProcessor ccp =
        new ContractCreationProcessor(evm.getGasCalculator(), evm, false, List.of(), 0, List.of());
    ccp.process(createFrame, OperationTracer.NO_TRACING);

    final Log log = createFrame.getLogs().get(0);
    final Bytes calculatedTopic = log.getTopics().get(0);
    assertThat(calculatedTopic).isEqualTo(CALL_DATA);
  }

  @Test
  void eofCreatePassesInCallData() {
    Bytes outerContract =
        bytesFromPrettyPrint(
            String.format(
                """
                  ef0001 # Magic and Version ( 1 )
                  010004 # Types length ( 4 )
                  020001 # Total code sections ( 1 )
                    000e # Code section 0 , 14 bytes
                  030001 # Total subcontainers ( 1 )
                  %04x # Subcontainer 0 size ?
                  040000 # Data section length(  0 )
                      00 # Terminator (end of header)
                         # Code section 0 types
                      00 # 0 inputs\s
                      80 # 0 outputs  (Non-returning function)
                    0004 # max stack:  4
                         # Code section 0
                  61c0de # [0] PUSH2(0xc0de)
                      5f # [3] PUSH0
                      52 # [4] MSTORE
                    6002 # [5] PUSH1(2)
                    601e # [7] PUSH0
                      5f # [9] PUSH0
                      5f # [10] PUSH0
                      ec00 # [11] EOFCREATE(0)
                      00 # [13] STOP
                         # Data section (empty)
                      %s # subcontainer
                  """,
                INNER_CONTRACT.size(), INNER_CONTRACT.toUnprefixedHexString()));
    Code code = CodeFactory.createCode(outerContract, 1, true);
    if (!code.isValid()) {
      System.out.println(outerContract);
      fail(((CodeInvalid) code).getInvalidReason());
    }

    final MessageFrame messageFrame = testMemoryFrame(code, CALL_DATA);

    when(account.getNonce()).thenReturn(55L);
    when(account.getBalance()).thenReturn(Wei.ZERO);
    when(worldUpdater.getAccount(any())).thenReturn(account);
    when(worldUpdater.get(any())).thenReturn(account);
    when(worldUpdater.getSenderAccount(any())).thenReturn(account);
    when(worldUpdater.getOrCreate(any())).thenReturn(newAccount);
    when(newAccount.getCode()).thenReturn(Bytes.EMPTY);
    when(worldUpdater.updater()).thenReturn(worldUpdater);

    final EVM evm = MainnetEVMs.prague(EvmConfiguration.DEFAULT);
    var precompiles = MainnetPrecompiledContracts.prague(evm.getGasCalculator());
    final MessageFrame createFrame = messageFrame.getMessageFrameStack().peek();
    assertThat(createFrame).isNotNull();
    final MessageCallProcessor mcp = new MessageCallProcessor(evm, precompiles);
    final ContractCreationProcessor ccp =
        new ContractCreationProcessor(evm.getGasCalculator(), evm, false, List.of(), 0, List.of());
    while (!createFrame.getMessageFrameStack().isEmpty()) {
      var frame = createFrame.getMessageFrameStack().peek();
      (switch (frame.getType()) {
            case CONTRACT_CREATION -> ccp;
            case MESSAGE_CALL -> mcp;
          })
          .process(frame, OperationTracer.NO_TRACING);
    }

    final Log log = createFrame.getLogs().get(0);
    final String calculatedTopic = log.getTopics().get(0).slice(0, 2).toHexString();
    assertThat(calculatedTopic).isEqualTo("0xc0de");

    assertThat(createFrame.getCreates())
        .containsExactly(Address.fromHexString("0x8c308e96997a8052e3aaab5af624cb827218687a"));
  }

  private MessageFrame testMemoryFrame(
      final Code code, final Bytes initData) {
    return MessageFrame.builder()
        .type(MessageFrame.Type.MESSAGE_CALL)
        .contract(Address.ZERO)
        .inputData(initData)
        .sender(Address.fromHexString(SENDER))
        .value(Wei.ZERO)
        .apparentValue(Wei.ZERO)
        .code(code)
        .completer(__ -> {})
        .address(Address.fromHexString(SENDER))
        .blockHashLookup(n -> Hash.hash(Words.longBytes(n)))
        .blockValues(mock(BlockValues.class))
        .gasPrice(Wei.ZERO)
        .miningBeneficiary(Address.ZERO)
        .originator(Address.ZERO)
        .initialGas(100000L)
        .worldUpdater(worldUpdater)
        .build();
  }
}
