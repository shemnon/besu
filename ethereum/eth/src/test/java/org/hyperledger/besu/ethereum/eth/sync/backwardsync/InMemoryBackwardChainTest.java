/*
 * Copyright Hyperledger Besu Contributors.
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
package org.hyperledger.besu.ethereum.eth.sync.backwardsync;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hyperledger.besu.ethereum.eth.sync.backwardsync.ChainForTestCreator.prepareChain;
import static org.hyperledger.besu.ethereum.eth.sync.backwardsync.ChainForTestCreator.prepareWrongParentHash;

import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.core.Block;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.BlockHeaderFunctions;
import org.hyperledger.besu.ethereum.core.InMemoryKeyValueStorageProvider;
import org.hyperledger.besu.ethereum.mainnet.MainnetBlockHeaderFunctions;
import org.hyperledger.besu.ethereum.storage.StorageProvider;
import org.hyperledger.besu.services.kvstore.InMemoryKeyValueStorage;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InMemoryBackwardChainTest {

  public static final int HEIGHT = 20_000;
  public static final int ELEMENTS = 20;
  private List<Block> blocks;

  GenericKeyValueStorageFacade<Hash, BlockHeader> headersStorage;
  GenericKeyValueStorageFacade<Hash, Block> blocksStorage;
  GenericKeyValueStorageFacade<Hash, Hash> chainStorage;

  @Before
  public void prepareData() {
    headersStorage =
        new GenericKeyValueStorageFacade<>(
            Hash::toArrayUnsafe,
            new BlocksHeadersConvertor(new MainnetBlockHeaderFunctions()),
            new InMemoryKeyValueStorage());
    blocksStorage =
        new GenericKeyValueStorageFacade<>(
            Hash::toArrayUnsafe,
            new BlocksConvertor(new MainnetBlockHeaderFunctions()),
            new InMemoryKeyValueStorage());
    chainStorage =
        new GenericKeyValueStorageFacade<>(
            Hash::toArrayUnsafe, new HashConvertor(), new InMemoryKeyValueStorage());

    blocks = prepareChain(ELEMENTS, HEIGHT);
  }

  @Test
  public void shouldReturnFirstHeaderCorrectly() {
    BackwardChain backwardChain = createChainFromBlock(blocks.get(blocks.size() - 1));
    backwardChain.prependAncestorsHeader(blocks.get(blocks.size() - 2).getHeader());
    backwardChain.prependAncestorsHeader(blocks.get(blocks.size() - 3).getHeader());
    backwardChain.prependAncestorsHeader(blocks.get(blocks.size() - 4).getHeader());
    BlockHeader firstHeader = backwardChain.getFirstAncestorHeader().orElseThrow();
    assertThat(firstHeader).isEqualTo(blocks.get(blocks.size() - 4).getHeader());
  }

  @Nonnull
  private BackwardChain createChainFromBlock(final Block pivot) {
    final BackwardChain backwardChain =
        new BackwardChain(headersStorage, blocksStorage, chainStorage);
    backwardChain.appendTrustedBlock(pivot);
    return backwardChain;
  }

  @Test
  public void shouldSaveHeadersWhenHeightAndHashMatches() {
    BackwardChain backwardChain = createChainFromBlock(blocks.get(blocks.size() - 1));
    backwardChain.prependAncestorsHeader(blocks.get(blocks.size() - 2).getHeader());
    backwardChain.prependAncestorsHeader(blocks.get(blocks.size() - 3).getHeader());
    backwardChain.prependAncestorsHeader(blocks.get(blocks.size() - 4).getHeader());
    BlockHeader firstHeader = backwardChain.getFirstAncestorHeader().orElseThrow();
    assertThat(firstHeader).isEqualTo(blocks.get(blocks.size() - 4).getHeader());
  }

  @Test
  public void shouldNotSaveHeadersWhenWrongHeight() {
    BackwardChain backwardChain = createChainFromBlock(blocks.get(blocks.size() - 1));
    backwardChain.prependAncestorsHeader(blocks.get(blocks.size() - 2).getHeader());
    backwardChain.prependAncestorsHeader(blocks.get(blocks.size() - 3).getHeader());
    assertThatThrownBy(
            () -> backwardChain.prependAncestorsHeader(blocks.get(blocks.size() - 5).getHeader()))
        .isInstanceOf(BackwardSyncException.class)
        .hasMessageContaining("has a wrong height");
    BlockHeader firstHeader = backwardChain.getFirstAncestorHeader().orElseThrow();
    assertThat(firstHeader).isEqualTo(blocks.get(blocks.size() - 3).getHeader());
  }

  @Test
  public void shouldNotSaveHeadersWhenWrongHash() {
    BackwardChain backwardChain = createChainFromBlock(blocks.get(blocks.size() - 1));
    backwardChain.prependAncestorsHeader(blocks.get(blocks.size() - 2).getHeader());
    backwardChain.prependAncestorsHeader(blocks.get(blocks.size() - 3).getHeader());
    BlockHeader wrongHashHeader = prepareWrongParentHash(blocks.get(blocks.size() - 4).getHeader());
    assertThatThrownBy(() -> backwardChain.prependAncestorsHeader(wrongHashHeader))
        .isInstanceOf(BackwardSyncException.class)
        .hasMessageContaining("we were expecting the parent with hash");
    BlockHeader firstHeader = backwardChain.getFirstAncestorHeader().orElseThrow();
    assertThat(firstHeader).isEqualTo(blocks.get(blocks.size() - 3).getHeader());
  }

  @Test
  public void shouldDropFromTheEnd() {

    BackwardChain backwardChain = createChainFromBlock(blocks.get(blocks.size() - 1));
    backwardChain.prependAncestorsHeader(blocks.get(blocks.size() - 2).getHeader());
    backwardChain.prependAncestorsHeader(blocks.get(blocks.size() - 3).getHeader());

    BlockHeader firstHeader = backwardChain.getFirstAncestorHeader().orElseThrow();
    assertThat(firstHeader).isEqualTo(blocks.get(blocks.size() - 3).getHeader());

    backwardChain.dropFirstHeader();

    firstHeader = backwardChain.getFirstAncestorHeader().orElseThrow();
    assertThat(firstHeader).isEqualTo(blocks.get(blocks.size() - 2).getHeader());

    backwardChain.dropFirstHeader();

    firstHeader = backwardChain.getFirstAncestorHeader().orElseThrow();
    assertThat(firstHeader).isEqualTo(blocks.get(blocks.size() - 1).getHeader());
  }

  @Test
  public void shouldCreateChainFromScheduleAndFunctions() {
    final StorageProvider provider = new InMemoryKeyValueStorageProvider();
    BlockHeaderFunctions functions = new MainnetBlockHeaderFunctions();

    final BackwardChain chain = BackwardChain.from(provider, functions);
    assertThat(chain).isNotNull();

    chain.clear();
  }

  @Test
  public void shouldAddHeaderToQueue() {
    BackwardChain backwardChain = createChainFromBlock(blocks.get(3));
    Optional<Hash> firstHash = backwardChain.getFirstHashToAppend();
    assertThat(firstHash).isNotPresent();
    backwardChain.addNewHash(blocks.get(7).getHash());
    backwardChain.addNewHash(blocks.get(9).getHash());
    backwardChain.addNewHash(blocks.get(9).getHash());
    backwardChain.addNewHash(blocks.get(11).getHash());

    firstHash = backwardChain.getFirstHashToAppend();
    assertThat(firstHash).isPresent();
    assertThat(firstHash.orElseThrow()).isEqualTo(blocks.get(7).getHash());
    firstHash = backwardChain.getFirstHashToAppend();
    assertThat(firstHash).isPresent();
    assertThat(firstHash.orElseThrow()).isEqualTo(blocks.get(9).getHash());
    firstHash = backwardChain.getFirstHashToAppend();
    assertThat(firstHash).isPresent();
    assertThat(firstHash.orElseThrow()).isEqualTo(blocks.get(11).getHash());
  }

  @Test
  public void shouldChangeFirstAncestorIfPivotIsToFar() {
    BackwardChain backwardChain = createChainFromBlock(blocks.get(3));
    backwardChain.appendTrustedBlock(blocks.get(4));

    Optional<BlockHeader> firstAncestorHeader = backwardChain.getFirstAncestorHeader();
    assertThat(firstAncestorHeader).isPresent();
    assertThat(firstAncestorHeader.orElseThrow()).isEqualTo(blocks.get(3).getHeader());
    Optional<Block> pivot = backwardChain.getPivot();
    assertThat(pivot).isPresent();
    assertThat(pivot.orElseThrow()).isEqualTo(blocks.get(4));

    backwardChain.appendTrustedBlock(blocks.get(7));

    firstAncestorHeader = backwardChain.getFirstAncestorHeader();

    assertThat(firstAncestorHeader).isPresent();
    assertThat(firstAncestorHeader.orElseThrow()).isEqualTo(blocks.get(7).getHeader());
    pivot = backwardChain.getPivot();
    assertThat(pivot).isPresent();
    assertThat(pivot.orElseThrow()).isEqualTo(blocks.get(7));
  }
}
