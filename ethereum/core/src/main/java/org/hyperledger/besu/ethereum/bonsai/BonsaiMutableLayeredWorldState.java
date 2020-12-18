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
import org.hyperledger.besu.ethereum.core.Address;
import org.hyperledger.besu.ethereum.core.Hash;
import org.hyperledger.besu.ethereum.core.MutableWorldState;
import org.hyperledger.besu.ethereum.core.WorldUpdater;
import org.hyperledger.besu.ethereum.rlp.BytesValueRLPOutput;
import org.hyperledger.besu.ethereum.trie.StoredMerklePatriciaTrie;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;

/** A World State backed first by trie log layer and then by another world state. */
public class BonsaiMutableLayeredWorldState extends BonsaiLayeredWorldState
    implements MutableWorldState {

  private final BonsaiWorldStateArchive archive;
  private final Hash parentRootHash;
  private final Supplier<BonsaiWorldStateUpdater> updaterMemento =
      Suppliers.memoize(() -> new BonsaiWorldStateUpdater(BonsaiMutableLayeredWorldState.this));
  private final Map<Address, Account> cachedAccounts = new HashMap<>();

  BonsaiMutableLayeredWorldState(
      final BonsaiWorldStateArchive archive,
      final BonsaiWorldView parent,
      final Hash parentRootHash) {
    super(parent, null, new TrieLogLayer());
    this.archive = archive;
    this.parentRootHash = parentRootHash;
  }

  public Optional<Bytes> getStorageTrieNode(final Address address, final Bytes location) {
    return getStateTrieNode(Bytes.concatenate(Hash.hash(address), location));
  }

  private void writeStorageTrieNode(
      final Address updatedAddress, final Bytes location, final Bytes value) {
    writeTrieNode(Bytes.concatenate(Hash.hash(updatedAddress), location), value);
  }

  private void writeTrieNode(final Bytes location, final Bytes value) {
    trieLog.addAccountTrieNode(location, getStateTrieNode(location).orElse(null), value);
  }

  @Override
  public Account get(final Address address) {
    return cachedAccounts.computeIfAbsent(
        address,
        addr ->
            trieLog
                .getAccount(addr)
                .map(
                    stateTrieAccountValue ->
                        (Account)
                            new BonsaiAccount(
                                BonsaiMutableLayeredWorldState.this,
                                addr,
                                stateTrieAccountValue,
                                false))
                .orElseGet(() -> parent.get(address)));
  }

  @Override
  public MutableWorldState copy() {
    throw new UnsupportedOperationException("LOL, no");
  }

  @Override
  public void persist(final Hash blockHash) {
//    if ( // "0x5297f2a4a699ba7d038a229a8eb7ab29d0073b37376ff0311f2bd9c608411830"
    //    .equals(blockHash.toHexString())
    //    ||
//    "0x9a256544469a112ce2e1ac2bce091e381ca1ec5db4b22db47092fbd6a8b2fef4"
//        .equals(blockHash.toHexString())
    //  || "0x448bae8a48930498f17454895a5a0eee55835acea327a6262627bbe6d53d7bb0"
    //        .equals(blockHash.toHexString())
//    ) {
//      System.out.println("Dumping " + blockHash.toHexString());
//    }
    boolean success = false;
    final BonsaiWorldStateUpdater updater = updaterMemento.get();

    try {
      //      // first clear storage
      //      for (final Address address : updater.getStorageToClear()) {
      //        // because we are clearing persisted values we need the account root as persisted
      //        final BonsaiAccount oldAccount = (BonsaiAccount) get(address);
      //        if (oldAccount == null) {
      //          // This is when an account is both created and deleted within the scope of the
      // same
      //          // block.  A not-uncommon DeFi bot pattern.
      //          continue;
      //        }
      //        final StoredMerklePatriciaTrie<Bytes, Bytes> storageTrie =
      //            new StoredMerklePatriciaTrie<>(
      //                (location, key) -> getStorageTrieNode(address, location, key),
      //                oldAccount.getStorageRoot(),
      //                Function.identity(),
      //                Function.identity());
      //        Map<Bytes32, Bytes> entriesToDelete = storageTrie.entriesFrom(Bytes32.ZERO, 256);
      //        while (!entriesToDelete.isEmpty()) {
      ////          entriesToDelete
      ////              .keySet()
      ////              .forEach(k -> storageTx.remove(Bytes.concatenate(address,
      // k).toArrayUnsafe()));
      //          entriesToDelete.keySet().forEach(storageTrie::remove);
      //          if (entriesToDelete.size() == 256) {
      //            entriesToDelete = storageTrie.entriesFrom(Bytes32.ZERO, 256);
      //          } else {
      //            break;
      //          }
      //        }
      //      }

      // second update account storage state.  This must be done before updating the accounts so
      // that we can get the storage state hash
      for (final Map.Entry<Address, Map<Hash, BonsaiValue<UInt256>>> storageAccountUpdate :
          updater.getStorageToUpdate().entrySet()) {
        final Address updatedAddress = storageAccountUpdate.getKey();
        final BonsaiValue<BonsaiAccount> accountValue =
            updater.getAccountsToUpdate().get(updatedAddress);
        final BonsaiAccount accountOriginal = accountValue.getOriginal();
        final Hash storageRoot =
            (accountOriginal == null) ? Hash.EMPTY_TRIE_HASH : accountOriginal.getStorageRoot();
        final StoredMerklePatriciaTrie<Bytes, Bytes> storageTrie =
            new StoredMerklePatriciaTrie<>(
                (location, hash) -> getStorageTrieNode(updatedAddress, location),
                storageRoot,
                Function.identity(),
                Function.identity());

        // for manicured tries and composting, collect branches here (not implemented)

        for (final Map.Entry<Hash, BonsaiValue<UInt256>> storageUpdate :
            storageAccountUpdate.getValue().entrySet()) {
          final Hash keyHash = storageUpdate.getKey();
          //          final byte[] writeAddress = Bytes.concatenate(updatedAddress,
          // keyHash).toArrayUnsafe();
          final UInt256 updatedStorage = storageUpdate.getValue().getUpdated();
          if (updatedStorage == null || updatedStorage.equals(UInt256.ZERO)) {
            //            storageTx.remove(writeAddress);
            storageTrie.remove(keyHash);
          } else {
            final Bytes32 updatedStorageBytes = updatedStorage.toBytes();
            //            storageTx.put(writeAddress, updatedStorageBytes.toArrayUnsafe());
            storageTrie.put(keyHash, BonsaiWorldView.encodeTrieValue(updatedStorageBytes));
          }
        }

        final BonsaiAccount accountUpdated = accountValue.getUpdated();
        if (accountUpdated != null) {
          storageTrie.commit(
              (location, key, value) -> writeStorageTrieNode(updatedAddress, location, value));
          final Hash newStorageRoot = Hash.wrap(storageTrie.getRootHash());
          accountUpdated.setStorageRoot(newStorageRoot);
        }
        // for manicured tries and composting, trim and compost here
      }

      // Third update the code.  This has the side effect of ensuring a code hash is calculated.
      //      for (final Map.Entry<Address, BonsaiValue<Bytes>> codeUpdate :
      //          updater.getCodeToUpdate().entrySet()) {
      //        final Bytes updatedCode = codeUpdate.getValue().getUpdated();
      //        if (updatedCode == null || updatedCode.size() == 0) {
      //          codeTx.remove(codeUpdate.getKey().toArrayUnsafe());
      //        } else {
      //          codeTx.put(codeUpdate.getKey().toArrayUnsafe(), updatedCode.toArrayUnsafe());
      //        }
      //      }

      // next collect the branches that will be trimmed
      final StoredMerklePatriciaTrie<Bytes, Bytes> accountTrie =
          new StoredMerklePatriciaTrie<>(
              (location, hash) -> getStateTrieNode(location),
              parentRootHash,
              Function.identity(),
              Function.identity());

      // now add the accounts
      for (final Map.Entry<Address, BonsaiValue<BonsaiAccount>> accountUpdate :
          updater.getAccountsToUpdate().entrySet()) {
        final Bytes accountKey = accountUpdate.getKey();
        final BonsaiValue<BonsaiAccount> bonsaiValue = accountUpdate.getValue();
        final BonsaiAccount updatedAccount = bonsaiValue.getUpdated();
        if (updatedAccount == null) {
          final Hash addressHash = Hash.hash(accountKey);
          accountTrie.remove(addressHash);
          //          accountTx.remove(accountKey.toArrayUnsafe());
        } else {
          final Hash addressHash = updatedAccount.getAddressHash();
          final Bytes accountValue = updatedAccount.serializeAccount();
          //          accountTx.put(accountKey.toArrayUnsafe(), accountValue.toArrayUnsafe());
          accountTrie.put(addressHash, accountValue);
        }
      }

      accountTrie.commit((location, hash, value) -> writeTrieNode(location, value));
      worldStateRootHash = Hash.wrap(accountTrie.getRootHash());
//      if ( // "0x5297f2a4a699ba7d038a229a8eb7ab29d0073b37376ff0311f2bd9c608411830"
        //              .equals(blockHash.toHexString())
//          "0x9a256544469a112ce2e1ac2bce091e381ca1ec5db4b22db47092fbd6a8b2fef4"
//              .equals(blockHash.toHexString())
      //      trieBranchTx.put(WORLD_ROOT_KEY, worldStateRootHash.toArrayUnsafe());
      //          ||
      //          "0x448bae8a48930498f17454895a5a0eee55835acea327a6262627bbe6d53d7bb0"
      //              .equals(blockHash.toHexString())
//      ) {
        //        final var dumper = new DumpVisitor<Bytes>();
        //        accountTrie.acceptAtRoot(dumper);
//        System.out.println("----");
//      }

      // for manicured tries and composting, trim and compost branches here

      if (blockHash != null) {
        updater.importIntoTrieLog(trieLog, blockHash);
        trieLog.setBlockHash(blockHash);
        trieLog.freeze();
        archive.addLayeredWorldState(
            new BonsaiLayeredWorldState(parent, worldStateRootHash, trieLog));
        final BytesValueRLPOutput rlpLog = new BytesValueRLPOutput();
        trieLog.writeTo(rlpLog);
      }

      success = true;
    } finally {
      if (success) {
        //        accountTx.commit();
        //        codeTx.commit();
        //        storageTx.commit();
        //        trieBranchTx.commit();
        //        trieLogTx.commit();
        updater.reset();
        //      } else {
        //        accountTx.rollback();
        //        codeTx.rollback();
        //        storageTx.rollback();
        //        trieBranchTx.rollback();
        //        trieLogTx.rollback();
      }
    }
    //    var ancestor = this.parent;
    //    while (ancestor instanceof BonsaiLayeredWorldState) {
    //      ancestor = ((BonsaiLayeredWorldState) ancestor).parent;
    //    }
    //    if (ancestor instanceof MutableWorldState) {
    //      ((MutableWorldState) ancestor).persist(blockhash);
    //    } else {
    //      throw new RuntimeException("Could not persist");
    //    }
  }

  @Override
  public WorldUpdater updater() {
    return updaterMemento.get();
  }
}
