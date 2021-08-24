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
package org.hyperledger.besu.ethereum.core;

import org.hyperledger.besu.evm.WorldUpdater;
import org.hyperledger.besu.evm.WorldView;

public interface MutableWorldView extends WorldView {

  /**
   * Creates a updater for this mutable world view.
   *
   * @return a new updater for this mutable world view. On commit, change made to this updater will
   *     become visible on this view.
   */
  WorldUpdater updater();
}
