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
package org.hyperledger.besu.ethereum.mainnet.precompiles;

import org.hyperledger.besu.ethereum.core.Gas;
import org.hyperledger.besu.nativelib.bls12_381.LibEthPairings;

import org.apache.tuweni.bytes.Bytes;

public class BLS12PairingPrecompiledContract extends AbstractBLS12PrecompiledContract {

  public BLS12PairingPrecompiledContract() {
    super("BLS12_PAIRING", LibEthPairings.BLS12_PAIR_OPERATION_RAW_VALUE);
  }

  @Override
  public Gas gasRequirement(final Bytes input) {
    final int k = input.size() / 384;
    return Gas.of(43_000L * k + 65_000);
  }
}
