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
package org.hyperledger.besu.datatypes;

import static com.google.common.base.Preconditions.checkArgument;

import java.math.BigInteger;
import java.util.Arrays;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.bytes.DelegatingBytes;
import org.apache.tuweni.units.bigints.UInt256;

/** A particular quantity of Wei, the Ethereum currency. */
public final class Wei extends DelegatingBytes implements Quantity {

  /** The constant ZERO. */
  public static final Wei ZERO = of(0);

  /** The constant ONE. */
  public static final Wei ONE = of(1);

  /** The constant MAX_WEI. */
  public static final Wei MAX_WEI = of(UInt256.MAX_VALUE);

  /**
   * Instantiates a new Wei.
   *
   * @param value the value
   */
  private Wei(final UInt256 value) {
    super(value);
  }

  private Wei(final Bytes bytes) {
    super(bytes);
  }

  private Wei(final long v) {
    super(Bytes.ofUnsignedLong(v));
  }

  private Wei(final BigInteger v) {
    super(Bytes.wrap(v.toByteArray()));
    checkArgument(size() <= 32, "Wei values must fit in a uint256");
  }

  private Wei(final String hexString) {
    super(Bytes.fromHexStringLenient(hexString));
    checkArgument(size() <= 32, "Wei values must fit in a uint256");
  }

  /**
   * Wei of value.
   *
   * @param value the value
   * @return the wei
   */
  public static Wei of(final long value) {
    return new Wei(value);
  }

  /**
   * Wei of value.
   *
   * @param value the value
   * @return the wei
   */
  public static Wei of(final BigInteger value) {
    return new Wei(value);
  }

  /**
   * Wei of value.
   *
   * @param value the value
   * @return the wei
   */
  public static Wei of(final UInt256 value) {
    return new Wei(value);
  }

  /**
   * Wei of value.
   *
   * @param value the value
   * @return the wei
   */
  public static Wei ofNumber(final Number value) {
    return new Wei((BigInteger) value);
  }

  /**
   * Wrap wei.
   *
   * @param value the value
   * @return the wei
   */
  public static Wei wrap(final Bytes value) {
    return new Wei(value);
  }

  /**
   * From hex string to wei.
   *
   * @param str the str
   * @return the wei
   */
  public static Wei fromHexString(final String str) {
    return new Wei(str);
  }

  /**
   * From eth to wei.
   *
   * @param eth the eth
   * @return the wei
   */
  public static Wei fromEth(final long eth) {
    return Wei.of(BigInteger.valueOf(eth).multiply(BigInteger.TEN.pow(18)));
  }

  @Override
  public Number getValue() {
    return getAsBigInteger();
  }

  @Override
  public BigInteger getAsBigInteger() {
    return toBigInteger();
  }

  @Override
  public String toShortHexString() {
    return super.isZero() ? "0x0" : super.toShortHexString();
  }

  /**
   * From quantity to wei.
   *
   * @param quantity the quantity
   * @return the wei
   */
  public static Wei fromQuantity(final Quantity quantity) {
    return Wei.wrap((Bytes) quantity);
  }

  /**
   * Wei to human-readable string.
   *
   * @return the string
   */
  public String toHumanReadableString() {
    final BigInteger amount = toBigInteger();
    final int numOfDigits = amount.toString().length();
    final Unit preferredUnit = Unit.getPreferred(numOfDigits);
    final double res = amount.doubleValue() / preferredUnit.divisor;
    return String.format("%1." + preferredUnit.decimals + "f %s", res, preferredUnit);
  }

  /** The enum Unit. */
  enum Unit {
    /** Wei unit. */
    Wei(0, 0),
    /** K wei unit. */
    KWei(3),
    /** M wei unit. */
    MWei(6),
    /** G wei unit. */
    GWei(9),
    /** Szabo unit. */
    Szabo(12),
    /** Finney unit. */
    Finney(15),
    /** Ether unit. */
    Ether(18),
    /** K ether unit. */
    KEther(21),
    /** M ether unit. */
    MEther(24),
    /** G ether unit. */
    GEther(27),
    /** T ether unit. */
    TEther(30);

    /** The Pow. */
    final int pow;
    /** The Divisor. */
    final double divisor;
    /** The Decimals. */
    final int decimals;

    Unit(final int pow) {
      this(pow, 2);
    }

    Unit(final int pow, final int decimals) {
      this.pow = pow;
      this.decimals = decimals;
      this.divisor = Math.pow(10, pow);
    }

    /**
     * Gets preferred.
     *
     * @param numOfDigits the num of digits
     * @return the preferred
     */
    static Unit getPreferred(final int numOfDigits) {
      return Arrays.stream(values())
          .filter(u -> numOfDigits <= u.pow + 3)
          .findFirst()
          .orElse(TEther);
    }

    @Override
    public String toString() {
      return name().toLowerCase();
    }
  }

  /**
   * //DOCME
   *
   * @param wei //DOCME
   * @return //DOCME
   */
  public Wei addExact(final Wei wei) {
    try {
      return of(new BigInteger(1, toArrayUnsafe()).add(new BigInteger(1, wei.toArrayUnsafe())));
    } catch (IllegalArgumentException iae) {
      throw new ArithmeticException("UInt256 overflow");
    }
  }

  /**
   * //DOCME
   *
   * @param wei //DOCME
   * @return //DOCME
   */
  public Wei add(final Wei wei) {
    byte[] results =
        new BigInteger(1, toArrayUnsafe())
            .add(new BigInteger(1, wei.toArrayUnsafe()))
            .toByteArray();
    if (results.length <= 32) {
      return new Wei(Bytes.wrap(results));
    } else {
      return new Wei(Bytes.wrap(results, results.length - 32, 32));
    }
  }

  /**
   * //DOCME
   *
   * @param wei //DOCME
   * @return //DOCME
   */
  public Wei subtract(final Wei wei) {
    BigInteger result =
        new BigInteger(1, toArrayUnsafe()).add(new BigInteger(1, wei.toArrayUnsafe()));
    byte[] resultBytes = result.toByteArray();
    if (resultBytes.length <= 32) {
      if (result.signum() >= 0) {
        return new Wei(Bytes.wrap(resultBytes));
      } else {
        return new Wei(Bytes32.leftPad(Bytes.wrap(resultBytes), (byte) -1));
      }
    } else {
      return new Wei(Bytes.wrap(resultBytes, resultBytes.length - 32, 32));
    }
  }

  /**
   * //DOCME
   *
   * @param multiplcand //DOCME
   * @return //DOCME
   */
  public Wei multiply(final long multiplcand) {
    byte[] results =
        new BigInteger(1, toArrayUnsafe()).multiply(BigInteger.valueOf(multiplcand)).toByteArray();
    if (results.length <= 32) {
      return new Wei(Bytes.wrap(results));
    } else {
      return new Wei(Bytes.wrap(results, results.length - 32, 32));
    }
  }

  /**
   * //DOCME
   *
   * @param divisor //DOCME
   * @return //DOCME
   */
  public Wei divide(final long divisor) {
    byte[] results =
        new BigInteger(1, toArrayUnsafe()).divide(BigInteger.valueOf(divisor)).toByteArray();
    if (results.length <= 32) {
      return new Wei(Bytes.wrap(results));
    } else {
      return new Wei(Bytes.wrap(results, results.length - 32, 32));
    }
  }

  /**
   * //DOCME
   *
   * @param other //DOCME
   * @return //DOCME
   */
  public Wei min(final Wei other) {
    return lessThan(other) ? this : other;
  }

  /**
   * //DOCME
   *
   * @param other //DOCME
   * @return //DOCME
   */
  public Wei max(final Wei other) {
    return greaterThan(other) ? this : other;
  }

  /**
   * //DOCME
   *
   * @param other //DOCME
   * @return //DOCME
   */
  public boolean greaterOrEqualThan(final Wei other) {
    return this.compareTo(other) >= 0;
  }

  /**
   * //DOCME
   *
   * @param other //DOCME
   * @return //DOCME
   */
  public boolean lessThan(final Wei other) {
    return this.compareTo(other) < 0;
  }

  /**
   * //DOCME
   *
   * @param other //DOCME
   * @return //DOCME
   */
  public boolean greaterThan(final Wei other) {
    return this.compareTo(other) > 0;
  }

  /**
   * //DOCME
   *
   * @return //DOCME
   */
  public String toDecimalString() {
    return toBigInteger().toString(10);
  }
}
