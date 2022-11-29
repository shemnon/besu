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
package org.hyperledger.besu.evm.precompile;

import static org.hyperledger.besu.evm.internal.Words.clampedMultiply;
import static org.hyperledger.besu.evm.internal.Words.clampedToInt;
import static org.hyperledger.besu.evm.internal.Words.clampedToLong;

import org.hyperledger.besu.evm.frame.MessageFrame;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;

import java.math.BigInteger;
import java.util.Arrays;
import javax.annotation.Nonnull;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.MutableBytes;

// The big integer modular exponentiation precompiled contract defined in EIP-198.
public class BigIntegerModularExponentiationPrecompiledContract
    extends AbstractPrecompiledContract {

  public static final int BASE_OFFSET = 96;
  private static final int PARAMETER_LENGTH = 32;
  private static final int BASE_LENGTH_OFFSET = 0;
  private static final int EXPONENT_LENGTH_OFFSET = 32;
  private static final int MODULUS_LENGTH_OFFSET = 64;

  public BigIntegerModularExponentiationPrecompiledContract(final GasCalculator gasCalculator) {
    super("BigIntModExp", gasCalculator);
  }

  @Override
  public long gasRequirement(final Bytes input) {
    return gasCalculator().modExpGasCost(input);
  }

  @Nonnull
  @Override
  public PrecompileContractResult computePrecompile(
      final Bytes input, @Nonnull final MessageFrame messageFrame) {
    final int baseLength = clampedToInt(baseLength(input));
    final int exponentLength = clampedToInt(exponentLength(input));
    final int modulusLength = clampedToInt(modulusLength(input));
    // If baseLength and modulusLength are zero
    // we could have a massively overflowing exp because it wouldn't have been filtered out at the
    // gas cost phase
    if ((baseLength == 0) && (modulusLength == 0)) {
      return PrecompileContractResult.success(Bytes.EMPTY);
    }
    final int exponentOffset = BASE_OFFSET + baseLength;
    final int modulusOffset = exponentOffset + exponentLength;
    final BigInteger base = extractParameter(input, BASE_OFFSET, baseLength);
    final BigInteger exp = extractParameter(input, exponentOffset, exponentLength);
    final BigInteger mod = extractParameter(input, modulusOffset, modulusLength);

    final Bytes modExp;
    // Result must be the length of the modulus.
    final MutableBytes result = MutableBytes.create(modulusLength);
    if (mod.compareTo(BigInteger.ZERO) == 0) {
      modExp = MutableBytes.EMPTY;
    } else {
      // BigInteger zero-pads positive values whose most significant bit is a 1 if
      // the padding was not there.
      modExp = Bytes.wrap(base.modPow(exp, mod).toByteArray()).trimLeadingZeros();
    }

    modExp.copyTo(result, result.size() - modExp.size());
    return PrecompileContractResult.success(result);
  }

  // Equation to estimate the multiplication complexity.
  public static long multiplicationComplexity(final long x) {
    if (x <= 64) {
      return square(x);
    } else if (x <= 1024) {
      return (square(x) / 4) + (x * 96) - 3072;
    } else {
      return (square(x) / 16) + (480 * x) - 199680;
    }
  }

  public static long baseLength(final Bytes input) {
    return extractParameterLong(input, BASE_LENGTH_OFFSET, PARAMETER_LENGTH);
  }

  public static long exponentLength(final Bytes input) {
    return extractParameterLong(input, EXPONENT_LENGTH_OFFSET, PARAMETER_LENGTH);
  }

  public static long modulusLength(final Bytes input) {
    return extractParameterLong(input, MODULUS_LENGTH_OFFSET, PARAMETER_LENGTH);
  }

  public static BigInteger extractParameter(final Bytes input, final int offset, final int length) {
    if (offset > input.size() || length == 0) {
      return BigInteger.ZERO;
    }
    final byte[] raw = Arrays.copyOfRange(input.toArray(), offset, offset + length);
    return new BigInteger(1, raw);
  }

  public static long extractParameterLong(final Bytes input, final int offset, final int length) {
    if (offset >= input.size() || length == 0) {
      return 0;
    }
    Bytes num;
    if (offset + length <= input.size()) {
      num = input.slice(offset, length).trimLeadingZeros();
    } else {
      MutableBytes mut = MutableBytes.create(length);
      input.slice(offset).copyTo(mut, 0);
      num = mut.trimLeadingZeros();
    }
    return clampedToLong(num);
  }

  private static long square(final long n) {
    return clampedMultiply(n, n);
  }
}
