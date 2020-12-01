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

import org.hyperledger.besu.ethereum.core.AbstractWorldUpdater;
import org.hyperledger.besu.ethereum.core.Account;
import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.ethereum.core.EvmAccount;
import org.hyperledger.besu.ethereum.core.Hash;
import org.hyperledger.besu.ethereum.core.UpdateTrackingAccount;
import org.hyperledger.besu.ethereum.core.Wei;
import org.hyperledger.besu.ethereum.core.WrappedEvmAccount;
import org.hyperledger.besu.ethereum.rlp.RLP;
import org.hyperledger.besu.ethereum.worldstate.StateTrieAccountValue;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;

public class BonsaiWorldStateUpdater
    extends AbstractWorldUpdater<BonsaiPersistedWorldState, BonsaiAccount>
    implements BonsaiWorldState {

  private final Map<Address, BonsaiValue<BonsaiAccount>> accountsToUpdate = new HashMap<>();
  private final Map<Address, BonsaiValue<Bytes>> codeToUpdate = new HashMap<>();
  private final Set<Address> storageToClear = new HashSet<>();

  // storage sub mapped by _hashed_ key.  This is because in self_destruct calls we need to
  // enumerate the old storage and delete it.  Those are trie stored by hashed key by spec and the
  // alternative was to keep a giant pre-image cache of the entire trie.
  private final Map<Address, Map<Hash, BonsaiValue<UInt256>>> storageToUpdate = new HashMap<>();

  protected BonsaiWorldStateUpdater(final BonsaiPersistedWorldState world) {
    super(world);
  }

  @Override
  public Account get(final Address address) {
    return super.get(address);
  }

  @Override
  protected UpdateTrackingAccount<BonsaiAccount> track(
      final UpdateTrackingAccount<BonsaiAccount> account) {
    return super.track(account);
  }

  @Override
  public EvmAccount getAccount(final Address address) {
    return super.getAccount(address);
  }

  @Override
  public EvmAccount createAccount(final Address address, final long nonce, final Wei balance) {
    BonsaiValue<BonsaiAccount> bonsaiValue = accountsToUpdate.get(address);
    if (bonsaiValue == null) {
      bonsaiValue = new BonsaiValue<>(null, null);
      accountsToUpdate.put(address, bonsaiValue);
    } else if (bonsaiValue.getUpdated() != null) {
      throw new IllegalStateException("Cannot create an account when one already exists");
    }
    final BonsaiAccount newAccount =
        new BonsaiAccount(
            this,
            address,
            Hash.hash(address),
            nonce,
            balance,
            Hash.EMPTY_TRIE_HASH,
            Hash.EMPTY,
            Account.DEFAULT_VERSION,
            true);
    bonsaiValue.setUpdated(newAccount);
    return new WrappedEvmAccount(track(new UpdateTrackingAccount<>(newAccount)));
  }

  Map<Address, BonsaiValue<BonsaiAccount>> getAccountsToUpdate() {
    return accountsToUpdate;
  }

  Map<Address, BonsaiValue<Bytes>> getCodeToUpdate() {
    return codeToUpdate;
  }

  public Set<Address> getStorageToClear() {
    return storageToClear;
  }

  Map<Address, Map<Hash, BonsaiValue<UInt256>>> getStorageToUpdate() {
    return storageToUpdate;
  }

  @Override
  protected BonsaiAccount getForMutation(final Address address) {
    final BonsaiValue<BonsaiAccount> bonsaiValue = accountsToUpdate.get(address);
    if (bonsaiValue == null) {
      final Account account = wrappedWorldView().get(address);
      if (account instanceof BonsaiAccount) {
        final BonsaiAccount mutableAccount = new BonsaiAccount((BonsaiAccount) account, this, true);
        accountsToUpdate.put(address, new BonsaiValue<>((BonsaiAccount) account, mutableAccount));
        return mutableAccount;
      } else {
        return null;
      }
    } else {
      return bonsaiValue.getUpdated();
    }
  }

  @Override
  public Collection<? extends Account> getTouchedAccounts() {
    return getUpdatedAccounts();
  }

  @Override
  public Collection<Address> getDeletedAccountAddresses() {
    return getDeletedAccounts();
  }

  @Override
  public void revert() {
    super.reset();
  }

  @Override
  public void commit() {
    for (final Address deletedAddress : getDeletedAccounts()) {
      storageToClear.add(deletedAddress);
      final BonsaiValue<Bytes> codeValue = codeToUpdate.get(deletedAddress);
      if (codeValue != null) {
        codeValue.setUpdated(null);
      } else {
        final Bytes deletedCode = wrappedWorldView().getCode(deletedAddress);
        if (deletedCode != null) {
          codeToUpdate.put(deletedAddress, new BonsaiValue<>(deletedCode, null));
        }
      }
      final BonsaiValue<BonsaiAccount> accountValue =
          accountsToUpdate.computeIfAbsent(
              deletedAddress,
              __ -> loadAccountFromParent(deletedAddress, new BonsaiValue<>(null, null)));

      // mark all updated storage as to be cleared
      final Map<Hash, BonsaiValue<UInt256>> deletedStorageUpdates =
          storageToUpdate.computeIfAbsent(deletedAddress, k -> new HashMap<>());
      final Iterator<Map.Entry<Hash, BonsaiValue<UInt256>>> iter =
          deletedStorageUpdates.entrySet().iterator();
      while (iter.hasNext()) {
        final Map.Entry<Hash, BonsaiValue<UInt256>> updateEntry = iter.next();
        final BonsaiValue<UInt256> updatedSlot = updateEntry.getValue();
        if (updatedSlot.getOriginal() == null || updatedSlot.getOriginal().isZero()) {
          iter.remove();
        } else {
          updatedSlot.setUpdated(null);
        }
      }

      final BonsaiAccount effective = accountValue.effective();
      if (effective != null) {
        // Enumerate and delete addresses not updated
        wrappedWorldView()
            .getAllAccountStorage(deletedAddress, effective.getStorageRoot())
            .forEach(
                (keyHash, entryValue) -> {
                  final Hash slotHash = Hash.wrap(keyHash);
                  if (!deletedStorageUpdates.containsKey(slotHash)) {
                    final UInt256 value = UInt256.fromBytes(RLP.decodeOne(entryValue));
                    deletedStorageUpdates.put(slotHash, new BonsaiValue<>(value, null));
                  }
                });
      }
      if (deletedStorageUpdates.isEmpty()) {
        storageToUpdate.remove(deletedAddress);
      }

      if (accountValue != null) {
        accountValue.setUpdated(null);
      }
    }

    for (final UpdateTrackingAccount<BonsaiAccount> tracked : getUpdatedAccounts()) {
      final Address updatedAddress = tracked.getAddress();
      BonsaiAccount updatedAccount = tracked.getWrappedAccount();
      if (updatedAccount == null) {
        final BonsaiValue<BonsaiAccount> updatedAccountValue = accountsToUpdate.get(updatedAddress);
        updatedAccount = new BonsaiAccount(this, tracked);
        tracked.setWrappedAccount(updatedAccount);
        if (updatedAccountValue == null) {
          accountsToUpdate.put(updatedAddress, new BonsaiValue<>(null, updatedAccount));
          codeToUpdate.put(updatedAddress, new BonsaiValue<>(null, updatedAccount.getCode()));
        } else {
          updatedAccountValue.setUpdated(updatedAccount);
        }
      } else {
        updatedAccount.setBalance(tracked.getBalance());
        updatedAccount.setNonce(tracked.getNonce());
        updatedAccount.setCode(tracked.getCode());
        if (tracked.getStorageWasCleared()) {
          updatedAccount.clearStorage();
        }
        tracked.getUpdatedStorage().forEach(updatedAccount::setStorageValue);
      }

      if (tracked.codeWasUpdated()) {
        final BonsaiValue<Bytes> pendingCode =
            codeToUpdate.computeIfAbsent(
                updatedAddress, addr -> new BonsaiValue<>(wrappedWorldView().getCode(addr), null));
        pendingCode.setUpdated(updatedAccount.getCode());
      }

      final Map<Hash, BonsaiValue<UInt256>> pendingStorageUpdates =
          storageToUpdate.computeIfAbsent(updatedAddress, __ -> new HashMap<>());
      if (tracked.getStorageWasCleared()) {
        storageToClear.add(updatedAddress);
        pendingStorageUpdates.clear();
      }

      final TreeSet<Map.Entry<UInt256, UInt256>> entries =
          new TreeSet<>(
              Comparator.comparing(
                  (Function<Map.Entry<UInt256, UInt256>, UInt256>) Map.Entry::getKey));
      entries.addAll(updatedAccount.getUpdatedStorage().entrySet());

      for (final Map.Entry<UInt256, UInt256> storageUpdate : entries) {
        final UInt256 keyUInt = storageUpdate.getKey();
        final Hash slotHash = Hash.hash(keyUInt.toBytes());
        final UInt256 value = storageUpdate.getValue();
        final BonsaiValue<UInt256> pendingValue = pendingStorageUpdates.get(slotHash);
        if (pendingValue == null) {
          pendingStorageUpdates.put(
              slotHash, new BonsaiValue<>(updatedAccount.getOriginalStorageValue(keyUInt), value));
        } else {
          pendingValue.setUpdated(value);
        }
      }
      updatedAccount.getUpdatedStorage().clear();

      // TODO maybe add address preimage?
    }
  }

  @Override
  public Bytes getCode(final Address address) {
    final BonsaiValue<Bytes> localCode = codeToUpdate.get(address);
    if (localCode == null) {
      return wrappedWorldView().getCode(address);
    } else {
      return localCode.getUpdated();
    }
  }

  @Override
  public UInt256 getStorageValue(final Address address, final UInt256 storageKey) {
    // TODO maybe log the read into the trie layer?
    final Hash slotHashBytes = Hash.hash(storageKey.toBytes());
    return getStorageValueBySlotHash(address, slotHashBytes).orElse(UInt256.ZERO);
  }

  @Override
  public Optional<UInt256> getStorageValueBySlotHash(final Address address, final Hash slotHash) {
    final Map<Hash, BonsaiValue<UInt256>> localAccountStorage =
        storageToUpdate.computeIfAbsent(address, key -> new HashMap<>());
    final BonsaiValue<UInt256> value = localAccountStorage.get(slotHash);
    if (value != null) {
      return Optional.of(value.getUpdated());
    } else {
      final Optional<UInt256> valueUInt =
          wrappedWorldView().getStorageValueBySlotHash(address, slotHash);
      valueUInt.ifPresent(v -> localAccountStorage.put(slotHash, new BonsaiValue<>(v, v)));
      return valueUInt;
    }
  }

  @Override
  public UInt256 getOriginalStorageValue(final Address address, final UInt256 storageKey) {
    // TODO maybe log the read into the trie layer?
    final Map<Hash, BonsaiValue<UInt256>> localAccountStorage =
        storageToUpdate.computeIfAbsent(address, key -> new HashMap<>());
    final Hash slotHashBytes = Hash.hash(storageKey.toBytes());
    final BonsaiValue<UInt256> value = localAccountStorage.get(slotHashBytes);
    if (value != null) {
      final UInt256 updated = value.getUpdated();
      if (updated != null) {
        return updated;
      }
      final UInt256 original = value.getOriginal();
      if (original != null) {
        return original;
      }
    }
    return getStorageValue(address, storageKey);
  }

  @Override
  public Map<Bytes32, Bytes> getAllAccountStorage(final Address address, final Hash rootHash) {
    final Map<Bytes32, Bytes> results = wrappedWorldView().getAllAccountStorage(address, rootHash);
    storageToUpdate
        .get(address)
        .forEach((key, value) -> results.put(key, value.getUpdated().toBytes()));
    return results;
  }

  public TrieLogLayer generateTrieLog(final Hash blockHash) {
    final TrieLogLayer layer = new TrieLogLayer();
    layer.setBlockHash(blockHash);
    for (final Map.Entry<Address, BonsaiValue<BonsaiAccount>> updatedAccount :
        accountsToUpdate.entrySet()) {
      final BonsaiValue<BonsaiAccount> bonsaiValue = updatedAccount.getValue();
      final BonsaiAccount oldValue = bonsaiValue.getOriginal();
      final StateTrieAccountValue oldAccount =
          oldValue == null
              ? null
              : new StateTrieAccountValue(
                  oldValue.getNonce(),
                  oldValue.getBalance(),
                  oldValue.getStorageRoot(),
                  oldValue.getCodeHash(),
                  oldValue.getVersion());
      final BonsaiAccount newValue = bonsaiValue.getUpdated();
      final StateTrieAccountValue newAccount =
          newValue == null
              ? null
              : new StateTrieAccountValue(
                  newValue.getNonce(),
                  newValue.getBalance(),
                  newValue.getStorageRoot(),
                  newValue.getCodeHash(),
                  newValue.getVersion());
      layer.addAccountChange(updatedAccount.getKey(), oldAccount, newAccount);
    }

    for (final Map.Entry<Address, BonsaiValue<Bytes>> updatedCode : codeToUpdate.entrySet()) {
      layer.addCodeChange(
          updatedCode.getKey(),
          updatedCode.getValue().getOriginal(),
          updatedCode.getValue().getUpdated());
    }

    for (final Map.Entry<Address, Map<Hash, BonsaiValue<UInt256>>> updatesStorage :
        storageToUpdate.entrySet()) {
      final Address address = updatesStorage.getKey();
      for (final Map.Entry<Hash, BonsaiValue<UInt256>> slotUpdate :
          updatesStorage.getValue().entrySet()) {
        layer.addStorageChange(
            address,
            slotUpdate.getKey(),
            slotUpdate.getValue().getOriginal(),
            slotUpdate.getValue().getUpdated());
      }
    }

    return layer;
  }

  public void rollForward(final TrieLogLayer layer) {
    layer
        .streamAccountChanges()
        .forEach(
            entry ->
                rollAccountChange(
                    entry.getKey(), entry.getValue().getOriginal(), entry.getValue().getUpdated()));
    layer
        .streamCodeChanges()
        .forEach(
            entry ->
                rollCodeChange(
                    entry.getKey(), entry.getValue().getOriginal(), entry.getValue().getUpdated()));
    layer
        .streamStorageChanges()
        .forEach(
            entry ->
                entry
                    .getValue()
                    .forEach(
                        (key, value) ->
                            rollStorageChange(
                                entry.getKey(), key, value.getOriginal(), value.getUpdated())));
  }

  public void rollBack(final TrieLogLayer layer) {
    layer
        .streamAccountChanges()
        .forEach(
            entry ->
                rollAccountChange(
                    entry.getKey(), entry.getValue().getUpdated(), entry.getValue().getOriginal()));
    layer
        .streamCodeChanges()
        .forEach(
            entry ->
                rollCodeChange(
                    entry.getKey(), entry.getValue().getUpdated(), entry.getValue().getOriginal()));
    layer
        .streamStorageChanges()
        .forEach(
            entry ->
                entry
                    .getValue()
                    .forEach(
                        (slotHash, value) ->
                            rollStorageChange(
                                entry.getKey(),
                                slotHash,
                                value.getUpdated(),
                                value.getOriginal())));
  }

  private void rollAccountChange(
      final Address address,
      final StateTrieAccountValue expectedValue,
      final StateTrieAccountValue replacementValue) {
    if (Objects.equals(expectedValue, replacementValue)) {
      // non-change, a cached read.
      return;
    }
    BonsaiValue<BonsaiAccount> accountValue = accountsToUpdate.get(address);
    if (accountValue == null) {
      accountValue = loadAccountFromParent(address, accountValue);
    }
    if (accountValue == null) {
      if (expectedValue == null && replacementValue != null) {
        accountsToUpdate.put(
            address,
            new BonsaiValue<>(null, new BonsaiAccount(this, address, replacementValue, true)));
      } else {
        throw new IllegalStateException(
            "Expected to update account, but the account does not exist");
      }
    } else {
      if (expectedValue == null) {
        throw new IllegalStateException("Expected to create account, but the account exists");
      }
      BonsaiAccount.assertCloseEnoughForDiffing(
          accountValue.getUpdated(), expectedValue, "Prior Value in Rolling Change");
      if (replacementValue == null) {
        if (accountValue.getOriginal() == null) {
          accountsToUpdate.remove(address);
        } else {
          accountValue.setUpdated(null);
        }
      } else {
        final BonsaiAccount existingAccount = accountValue.getUpdated();
        existingAccount.setNonce(replacementValue.getNonce());
        existingAccount.setBalance(replacementValue.getBalance());
        existingAccount.setStorageRoot(replacementValue.getStorageRoot());
        // depend on correctly structured layers to set code hash
        existingAccount.setVersion(replacementValue.getVersion());
      }
    }
  }

  private BonsaiValue<BonsaiAccount> loadAccountFromParent(
      final Address address, final BonsaiValue<BonsaiAccount> defaultValue) {
    final Account parentAccount = wrappedWorldView().get(address);
    if (parentAccount instanceof BonsaiAccount) {
      final BonsaiAccount account = (BonsaiAccount) parentAccount;
      final BonsaiValue<BonsaiAccount> loadedAccountValue =
          new BonsaiValue<>(new BonsaiAccount(account), account);
      accountsToUpdate.put(address, loadedAccountValue);
      return loadedAccountValue;
    } else {
      return defaultValue;
    }
  }

  private void rollCodeChange(
      final Address address, final Bytes expectedCode, final Bytes replacementCode) {
    if (Objects.equals(expectedCode, replacementCode)) {
      // non-change, a cached read.
      return;
    }
    BonsaiValue<Bytes> codeValue = codeToUpdate.get(address);
    if (codeValue == null) {
      final Bytes storedCode = wrappedWorldView().getCode(address);
      if (!storedCode.isEmpty()) {
        codeValue = new BonsaiValue<>(storedCode, storedCode);
        codeToUpdate.put(address, codeValue);
      }
    }

    if (codeValue == null) {
      if (expectedCode == null && replacementCode != null) {
        codeToUpdate.put(address, new BonsaiValue<>(null, replacementCode));
      } else {
        throw new IllegalStateException("Expected to update code, but the code does not exist");
      }
    } else {
      if (expectedCode == null) {
        throw new IllegalStateException("Expected to create code, but the code exists");
      }
      if (!codeValue.getUpdated().equals(expectedCode)) {
        throw new IllegalStateException("Old value of code does not match expected value");
      }
      if (replacementCode == null) {
        if (codeValue.getOriginal() == null) {
          codeToUpdate.remove(address);
        } else {
          codeValue.setUpdated(null);
        }
      } else {
        codeValue.setUpdated(replacementCode);
      }
    }
  }

  private Map<Hash, BonsaiValue<UInt256>> maybeCreateStorageMap(
      final Map<Hash, BonsaiValue<UInt256>> storageMap, final Address address) {
    if (storageMap == null) {
      final Map<Hash, BonsaiValue<UInt256>> newMap = new HashMap<>();
      storageToUpdate.put(address, newMap);
      return newMap;
    } else {
      return storageMap;
    }
  }

  private void rollStorageChange(
      final Address address,
      final Hash slotHash,
      final UInt256 expectedValue,
      final UInt256 replacementValue) {
    if (Objects.equals(expectedValue, replacementValue)) {
      // non-change, a cached read.
      return;
    }
    final Map<Hash, BonsaiValue<UInt256>> storageMap = storageToUpdate.get(address);
    BonsaiValue<UInt256> slotValue = storageMap == null ? null : storageMap.get(slotHash);
    if (slotValue == null) {
      final Optional<UInt256> storageValue =
          wrappedWorldView().getStorageValueBySlotHash(address, slotHash);
      if (storageValue.isPresent()) {
        slotValue = new BonsaiValue<>(storageValue.get(), storageValue.get());
        storageToUpdate.computeIfAbsent(address, k -> new HashMap<>()).put(slotHash, slotValue);
      }
    }
    if (slotValue == null) {
      if (expectedValue == null && replacementValue != null) {
        maybeCreateStorageMap(storageMap, address)
            .put(slotHash, new BonsaiValue<>(null, replacementValue));
      } else {
        throw new IllegalStateException(
            "Expected to update storage value, but the slot does not exist");
      }
    } else {
      if (expectedValue == null) {
        throw new IllegalStateException("Expected to create slot, but the slot exists");
      }
      final UInt256 existingSlotValue = slotValue.getUpdated();
      if (!existingSlotValue.equals(expectedValue)) {
        throw new IllegalStateException("Old value of slot does not match expected value");
      }
      if (replacementValue == null) {
        if (slotValue.getOriginal() == null) {
          final Map<Hash, BonsaiValue<UInt256>> thisStorageUpdate =
              maybeCreateStorageMap(storageMap, address);
          thisStorageUpdate.remove(slotHash);
          if (thisStorageUpdate.isEmpty()) {
            storageToUpdate.remove(address);
          }
        } else {
          slotValue.setUpdated(null);
        }
      } else {
        slotValue.setUpdated(replacementValue);
      }
    }
  }

  @Override
  public void reset() {
    storageToClear.clear();
    storageToUpdate.clear();
    codeToUpdate.clear();
    accountsToUpdate.clear();
    super.reset();
  }
}
