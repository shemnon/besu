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

package org.hyperledger.besu.ethereum.api.jsonrpc.internal.results;

import org.hyperledger.besu.datatypes.Hash;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.tuweni.bytes.Bytes;

@JsonPropertyOrder({"blockHash", "kzgs", "blobs"})
public class BlobsBundleV1 {

  private final Hash blockHash;

  private final List<Bytes> kzgs;

  private final List<Bytes> blobs;

  public BlobsBundleV1(final Hash blockHash, final List<Bytes> kzgs, final List<Bytes> blobs) {
    this.blockHash = blockHash;
    this.kzgs = kzgs;
    this.blobs = blobs;
  }

  @JsonGetter("blockHash")
  public Hash getBlockHash() {
    return blockHash;
  }

  @JsonGetter("kzgs")
  public List<Bytes> getKzgs() {
    return kzgs;
  }

  @JsonGetter("blobs")
  public List<Bytes> getBlobs() {
    return blobs;
  }
}