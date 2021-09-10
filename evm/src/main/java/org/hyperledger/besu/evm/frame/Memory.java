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
package org.hyperledger.besu.evm.frame;

import java.util.Arrays;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

/**
 * An EVM memory implementation.
 *
 * <p>Note: this is meant to map to I in Section 9.1 "Basics" and Section 9.4.1 "Machine State" in
 * the Yellow Paper Revision 59dccd.
 */
public class Memory {

  /**
   * The data stored within the memory.
   *
   * <p>Note that the current Ethereum spec don't put a limit on memory, so we could theoretically
   * overflow this. A byte array implementation limits us to 2 GiB. But that would cost over 51
   * trillion gas. So this is likely a reasonable limitation, at least at first.
   */
  private byte[] data;

  private int activeWords;

  public Memory() {
    data = new byte[0];
    updateSize();
  }

  private void updateSize() {
    activeWords = data.length / Bytes32.SIZE;
  }

  /**
   * For use in memoryExpansionGasCost() of GasCost. Returns the number of new active words that
   * accommodate at least the number of specified bytes from the provided memory offset.
   *
   * <p>Not that this has to return a UInt256 for Gas calculation, in case someone writes code that
   * require a crazy amount of data. Such allocation should get prohibitive however, and we will end
   * up with an Out-of-Gas error.
   *
   * @param location The offset in memory from which we want to accommodate {@code numBytes}.
   * @param numBytes The minimum number of bytes in memory.
   * @return The number of active words that accommodate at least the number of specified bytes.
   */
  long calculateNewActiveWords(final long location, final long numBytes) {
    if (numBytes == 0) {
      return activeWords;
    }

    try {
      final long byteSize = Math.addExact(Math.addExact(location, numBytes), 31);
      long wordSize = byteSize / 32;
      return Math.max(wordSize, activeWords);
    } catch (ArithmeticException ae) {
      return Long.MAX_VALUE >> 5;
    }
  }

  /**
   * Expands the active words to accommodate the specified byte position.
   *
   * @param offset The location in memory to start with.
   * @param numBytes The number of bytes to get.
   */
  void ensureCapacityForBytes(final long offset, final long numBytes) {
    // Do not increase the memory capacity if no bytes are being written
    // regardless of what the address may be.
    if (numBytes == 0) {
      return;
    }
    final long lastByteIndex = Math.addExact(offset, numBytes);
    final long lastWordRequired = ((lastByteIndex - 1) / Bytes32.SIZE);
    maybeExpandCapacity((int) lastWordRequired + 1);
  }

  /**
   * Expands the memory to the specified number of active words.
   *
   * @param newActiveWords The new number of active words to expand to.
   */
  private void maybeExpandCapacity(final int newActiveWords) {
    if (activeWords >= newActiveWords) return;

    // Require full capacity to guarantee we don't resize more than once.
    final byte[] newData = new byte[newActiveWords * Bytes32.SIZE];
    System.arraycopy(data, 0, newData, 0, data.length);
    data = newData;
    updateSize();
  }

  /**
   * Returns true if the object is equal to this memory instance; otherwise false.
   *
   * @param other The object to compare this memory instance with.
   * @return True if the object is equal to this memory instance.
   */
  @Override
  public boolean equals(final Object other) {
    if (other == null) return false;
    if (other == this) return true;
    if (!(other instanceof Memory)) return false;

    final Memory that = (Memory) other;
    return Arrays.equals(this.data, that.data);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(data);
  }

  /**
   * Returns the current number of active bytes stored in memory.
   *
   * @return The current number of active bytes stored in memory.
   */
  int getActiveBytes() {
    return data.length;
  }

  /**
   * Returns the current number of active words stored in memory.
   *
   * @return The current number of active words stored in memory.
   */
  int getActiveWords() {
    return activeWords;
  }

  /**
   * Returns a copy of bytes from memory.
   *
   * @param start The location in memory to start with.
   * @param length The number of bytes to get.
   * @return A fresh copy of the bytes from memory starting at {@code location} and extending {@code
   *     numBytes}.
   */
  public Bytes getBytes(final long start, final long length) {
    // Note: if length == 0, we don't require any memory expansion, whatever location is. So
    // we must call asByteIndex(location) after this check so as it doesn't throw if the location
    // is too big but the length is 0 (which is somewhat nonsensical, but is exercise by some
    // tests).
    if (length == 0) {
      return Bytes.EMPTY;
    }

    ensureCapacityForBytes(start, length);
    return Bytes.of(Arrays.copyOfRange(data, (int) start, (int) (start + length)));
  }

  /**
   * Copy the bytes from the provided number of bytes from the provided value to memory from the
   * provided offset.
   *
   * <p>Note that this method will extend memory to accommodate the location assigned and bytes
   * copied and so never fails.
   *
   * @param memOffset the location in memory at which to start copying the bytes of {@code value}.
   * @param offset the location in the source to start copying.
   * @param length the number of bytes to set in memory. Note that this value may differ from {@code
   *     value.size()}: if {@code numBytes < value.size()} bytes, only {@code numBytes} will be
   *     copied from {@code value}; if {@code numBytes < value.size()}, then only the bytes in
   *     {@code value} will be copied, but the memory will be expanded if necessary to cover {@code
   *     numBytes} (in other words, {@link #getActiveWords()} will return a value consistent with
   *     having set {@code numBytes} bytes, even if less than that have been concretely set due to
   *     {@code value} being smaller).
   * @param bytes the bytes to copy to memory from {@code location}.
   */
  public void setBytes(
      final long memOffset, final long offset, final long length, final Bytes bytes) {

    if (offset >= bytes.size()) {
      clearBytes(memOffset, length);
      return;
    }

    final Bytes toCopy =
        bytes.slice((int) offset, Math.min((int) length, bytes.size() - (int) offset));
    setBytes(memOffset, length, toCopy);
  }

  /**
   * Copy the bytes from the provided number of bytes from the provided value to memory from the
   * provided offset.
   *
   * <p>Note that this method will extend memory to accommodate the location assigned and bytes
   * copied and so never fails.
   *
   * @param start the location in memory at which to start copying the bytes of {@code value}.
   * @param length the number of bytes to set in memory. Note that this value may differ from {@code
   *     value.size()}: if {@code numBytes < value.size()} bytes, only {@code numBytes} will be
   *     copied from {@code value}; if {@code numBytes > value.size()}, then only the bytes in
   *     {@code value} will be copied, but the memory will be expanded if necessary to cover {@code
   *     numBytes} (in other words, {@link #getActiveWords()} will return a value consistent with
   *     having set {@code numBytes} bytes, even if less than that have been concretely set due to
   *     {@code value} being smaller).
   * @param taintedValue the bytes to copy to memory from {@code location}.
   */
  public void setBytes(final long start, final long length, final Bytes taintedValue) {
    if (length == 0) {
      return;
    }

    final int srcLength = taintedValue.size();
    final long end = Math.addExact(start, length);

    ensureCapacityForBytes(start, length);
    if (srcLength >= length) {
      System.arraycopy(taintedValue.toArrayUnsafe(), 0, data, (int) start, (int) length);
    } else {
      Arrays.fill(data, (int) start, (int) end, (byte) 0);
      System.arraycopy(taintedValue.toArrayUnsafe(), 0, data, (int) start, srcLength);
    }
  }

  /**
   * Clears (set to 0) some contiguous number of bytes in memory.
   *
   * @param location The location in memory from which to start clearing the bytes.
   * @param numBytes The number of bytes to clear.
   */
  private void clearBytes(final long location, final long numBytes) {
    if (numBytes == 0) {
      return;
    }

    ensureCapacityForBytes(location, numBytes);
    Arrays.fill(data, (int) location, (int) (location + numBytes), (byte) 0);
  }

  /**
   * Sets a single byte in memory at the provided location.
   *
   * @param start the location of the byte to set.
   * @param value the value to set for the byte at {@code location}.
   */
  void setByte(final long start, final byte value) {
    ensureCapacityForBytes(start, 1);
    data[(int) start] = value;
  }

  /**
   * Returns a copy of the 32-bytes word that begins at the specified memory location.
   *
   * @param start The memory location the 256-bit word begins at.
   * @return a copy of the 32-bytes word that begins at the specified memory location.
   */
  public Bytes32 getWord(final int start) {
    ensureCapacityForBytes(start, Bytes32.SIZE);
    return Bytes32.wrap(Arrays.copyOfRange(data, start, start + Bytes32.SIZE));
  }

  /**
   * Sets a 32-bytes word in memory at the provided location.
   *
   * <p>Note that this method will extend memory to accommodate the location assigned and bytes
   * copied and so never fails.
   *
   * @param start the location at which to start setting the bytes.
   * @param bytes the 32 bytes to copy at {@code location}.
   */
  public void setWord(final int start, final Bytes32 bytes) {
    ensureCapacityForBytes(start, Bytes32.SIZE);

    System.arraycopy(bytes.toArrayUnsafe(), 0, data, start, Bytes32.SIZE);
  }

  @Override
  public String toString() {
    return Bytes.wrap(data).toHexString();
  }
}
