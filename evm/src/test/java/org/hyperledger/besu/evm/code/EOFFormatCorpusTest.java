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

package org.hyperledger.besu.evm.code;

import static org.assertj.core.api.Assertions.assertThat;

import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.evm.Code;
import org.hyperledger.besu.evm.EVM;
import org.hyperledger.besu.evm.MainnetEVMs;
import org.hyperledger.besu.evm.gascalculator.ShanghaiGasCalculator;
import org.hyperledger.besu.evm.internal.EvmConfiguration;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EOFFormatCorpusTest {

  static ObjectMapper mapper = new ObjectMapper();
  static Map<String, EVM> evms =
      Map.ofEntries(
          Map.entry(
              "cancun",
              MainnetEVMs.cancun(
                  new ShanghaiGasCalculator(), BigInteger.ONE, EvmConfiguration.DEFAULT)));

  static Stream<Arguments> corpusTests() throws IOException {
    JsonNode root =
        mapper.readTree(
            EOFFormatCorpusTest.class.getResourceAsStream(
                "/org/hyperledger/besu/evm/code/eofFormatTests.json"));
    return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(root.fields(), Spliterator.ORDERED), false)
        .flatMap(
            entry ->
                StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(
                            entry.getValue().get("results").fields(), Spliterator.ORDERED),
                        false)
                    .map(
                        fork ->
                            Arguments.of(
                                entry.getKey(),
                                fork.getKey().toLowerCase(),
                                entry.getValue().get("code").textValue(),
                                fork.getValue())));
  }

  //  @ParameterizedTest(name = "{0} @ {1}")
  @ParameterizedTest()
  @MethodSource("corpusTests")
  void eofFormat(
      final String name, final String fork, final String codeString, final JsonNode result) {
    EVM evm = evms.get(fork);
    Bytes codeBytes = Bytes.fromHexString(codeString);
    Code code = evm.getCode(Hash.hash(codeBytes), codeBytes);

    boolean expectValid = result.get("result").booleanValue();
    String expectException = expectValid ? null : result.get("exception").textValue();

    switch (code.getEofVersion()) {
      case -1: // invalid EOF code
        assertThat(((CodeInvalid) code).getInvalidReason())
            .containsAnyOf(expectException.split(","));
        // fall through
      case 0: // valid legacy code
        assertThat(expectValid).isFalse();
        break;
      case 1:
        assertThat(expectValid).isTrue();
        break;
    }
  }
}
