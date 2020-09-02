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

package org.hyperledger.besu.ethereum.bonsai;

import org.hyperledger.besu.ethereum.core.Account;
import org.hyperledger.besu.ethereum.core.AccountStorageEntry;
import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.ethereum.core.EvmAccount;
import org.hyperledger.besu.ethereum.core.Hash;
import org.hyperledger.besu.ethereum.core.ModificationNotAllowedException;
import org.hyperledger.besu.ethereum.core.MutableAccount;
import org.hyperledger.besu.ethereum.core.Wei;
import org.hyperledger.besu.ethereum.rlp.BytesValueRLPOutput;
import org.hyperledger.besu.ethereum.rlp.RLP;
import org.hyperledger.besu.ethereum.rlp.RLPException;
import org.hyperledger.besu.ethereum.rlp.RLPInput;

import java.util.Map;
import java.util.NavigableMap;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;

public class BonsaiAccount implements MutableAccount, EvmAccount {
  private final BonsaiMutableWorldState context;
  private final boolean mutable;

  private final Address address;
  private final Hash addressHash;
  private long nonce;
  private Wei balance;
  private final Hash storageRoot;
  private final Hash codeHash;
  private Bytes code;
  private int version;

  private BonsaiAccount(
      final BonsaiMutableWorldState context,
      final Address address,
      final Hash addressHash,
      final long nonce,
      final Wei balance,
      final Hash storageRoot,
      final Hash codeHash,
      final int version) {
    this.context = context;
    this.address = address;
    this.addressHash = addressHash;
    this.nonce = nonce;
    this.balance = balance;
    this.storageRoot = storageRoot;
    this.codeHash = codeHash;
    this.version = version;

    mutable = false;
  }

  BonsaiAccount(
      final BonsaiMutableWorldState context,
      final Address address,
      final Hash addressHash,
      final long nonce,
      final Wei balance,
      final Hash storageRoot,
      final Hash codeHash,
      final int version,
      final boolean mutable) {
    this.context = context;
    this.address = address;
    this.addressHash = addressHash;
    this.nonce = nonce;
    this.balance = balance;
    this.storageRoot = storageRoot;
    this.codeHash = codeHash;
    this.version = version;

    this.mutable = mutable;
  }

  private BonsaiAccount(final BonsaiAccount account, final boolean mutable) {
    this.context = account.context;
    this.address = account.address;
    this.addressHash = account.addressHash;
    this.nonce = account.nonce;
    this.balance = account.balance;
    this.storageRoot = account.storageRoot;
    this.codeHash = account.codeHash;
    this.version = account.version;

    this.mutable = mutable;
  }

  @Override
  public Address getAddress() {
    return address;
  }

  @Override
  public Hash getAddressHash() {
    return addressHash;
  }

  @Override
  public long getNonce() {
    return nonce;
  }

  @Override
  public Wei getBalance() {
    return balance;
  }

  @Override
  public Bytes getCode() {
    if (code == null) {
      code = context.getCode(address, codeHash);
    }
    return code;
  }

  @Override
  public Hash getCodeHash() {
    return codeHash;
  }

  @Override
  public int getVersion() {
    return version;
  }

  @Override
  public UInt256 getStorageValue(final UInt256 key) {
    throw new RuntimeException("LOL no");
  }

  @Override
  public UInt256 getOriginalStorageValue(final UInt256 key) {
    throw new RuntimeException("LOL no");
  }

  @Override
  public NavigableMap<Bytes32, AccountStorageEntry> storageEntriesFrom(
      final Bytes32 startKeyHash, final int limit) {
    throw new RuntimeException("LOL no");
  }

  Bytes serializeAccount() {
    final BytesValueRLPOutput out = new BytesValueRLPOutput();
    out.startList();

    out.writeLongScalar(nonce);
    out.writeUInt256Scalar(balance);
    out.writeBytes(storageRoot);
    out.writeBytes(codeHash);

    if (version != Account.DEFAULT_VERSION) {
      // version of zero is never written out.
      out.writeIntScalar(version);
    }

    out.endList();
    return out.encoded();
  }

  static BonsaiAccount fromRLP(
      final BonsaiMutableWorldState context,
      final Address address,
      final Bytes encoded, boolean mutable)
      throws RLPException {
    final RLPInput in = RLP.input(encoded);
    in.enterList();

    final long nonce = in.readLongScalar();
    final Wei balance = Wei.of(in.readUInt256Scalar());
    final Hash storageRoot = Hash.wrap(in.readBytes32());
    final Hash codeHash = Hash.wrap(in.readBytes32());
    final int version;
    if (!in.isEndOfCurrentList()) {
      version = in.readIntScalar();
    } else {
      version = Account.DEFAULT_VERSION;
    }

    in.leaveList();

    return new BonsaiAccount(
        context, address, Hash.hash(address), nonce, balance, storageRoot, codeHash, version, mutable);
  }

  @Override
  public void setNonce(final long value) {
    if (!mutable) {
      throw new UnsupportedOperationException("Account is immutable");
    }
    nonce = value;
  }

  @Override
  public void setBalance(final Wei value) {
    if (!mutable) {
      throw new UnsupportedOperationException("Account is immutable");
    }
    balance = value;
  }

  @Override
  public void setCode(final Bytes code) {
    if (!mutable) {
      throw new UnsupportedOperationException("Account is immutable");
    }
    this.code = code;
  }

  @Override
  public void setVersion(final int version) {
    if (!mutable) {
      throw new UnsupportedOperationException("Account is immutable");
    }
    this.version = version;
  }

  @Override
  public void setStorageValue(final UInt256 key, final UInt256 value) {
    throw new RuntimeException("LOL no");
  }

  @Override
  public void clearStorage() {
    // FIXME throw new RuntimeException("LOL no");
  }

  @Override
  public Map<UInt256, UInt256> getUpdatedStorage() {
    return null;
  }

  @Override
  public MutableAccount getMutable() throws ModificationNotAllowedException {
    if (mutable) {
      return this;
    } else {
      throw new ModificationNotAllowedException();
    }
  }

  public MutableAccount mutableCopy() {
    return new BonsaiAccount(this, true);
  }
}
