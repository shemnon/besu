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
package org.hyperledger.besu.ethereum.api.jsonrpc.internal.privacy.methods.eea;

import static org.apache.logging.log4j.LogManager.getLogger;
import static org.hyperledger.besu.ethereum.api.jsonrpc.JsonRpcEnclaveErrorConverter.convertEnclaveInvalidReason;
import static org.hyperledger.besu.ethereum.api.jsonrpc.JsonRpcErrorConverter.convertTransactionInvalidReason;
import static org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcError.DECODE_ERROR;

import org.hyperledger.besu.enclave.GoQuorumEnclave;
import org.hyperledger.besu.ethereum.api.jsonrpc.RpcMethod;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.JsonRpcRequestContext;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.methods.JsonRpcMethod;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.privacy.methods.EnclavePublicKeyProvider;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcError;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcErrorResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcResponse;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.response.JsonRpcSuccessResponse;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.hyperledger.besu.ethereum.eth.transactions.TransactionPool;
import org.hyperledger.besu.ethereum.privacy.GoQuorumSendRawTxArgs;
import org.hyperledger.besu.ethereum.rlp.RLP;
import org.hyperledger.besu.ethereum.rlp.RLPException;
import org.hyperledger.besu.ethereum.transaction.TransactionInvalidReason;

import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;

public class GoQuorumSendRawPrivateTransaction implements JsonRpcMethod {

  private static final Logger LOG = getLogger();
  final TransactionPool transactionPool;
  private final EnclavePublicKeyProvider enclavePublicKeyProvider;
  private final GoQuorumEnclave enclave;

  public GoQuorumSendRawPrivateTransaction(
      final GoQuorumEnclave enclave,
      final TransactionPool transactionPool,
      final EnclavePublicKeyProvider enclavePublicKeyProvider) {
    this.enclave = enclave;
    this.transactionPool = transactionPool;
    this.enclavePublicKeyProvider = enclavePublicKeyProvider;
  }

  @Override
  public String getName() {
    return RpcMethod.ETH_SEND_RAW_PRIVATE_TRANSACTION.getMethodName();
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequestContext requestContext) {
    final Object id = requestContext.getRequest().getId();
    final String rawPrivateTransaction = requestContext.getRequiredParameter(0, String.class);

    final GoQuorumSendRawTxArgs rawTxArgs =
        requestContext.getRequiredParameter(1, GoQuorumSendRawTxArgs.class);

    try {
      final Transaction transaction =
          Transaction.readFrom(RLP.input(Bytes.fromHexString(rawPrivateTransaction)));

      checkAndHandlePrivateTransaction(transaction, rawTxArgs, requestContext);

      return transactionPool
          .addLocalTransaction(transaction)
          .either(
              () -> new JsonRpcSuccessResponse(id, transaction.getHash().toString()),
              errorReason -> getJsonRpcErrorResponse(id, errorReason));

    } catch (final JsonRpcErrorResponseException e) {
      return new JsonRpcErrorResponse(id, e.getJsonRpcError());
    } catch (final IllegalArgumentException | RLPException e) {
      LOG.error(e);
      return new JsonRpcErrorResponse(id, DECODE_ERROR);
    } catch (final Exception e) {
      LOG.error(e);
      return new JsonRpcErrorResponse(id, convertEnclaveInvalidReason(e.getMessage()));
    }
  }

  private void checkAndHandlePrivateTransaction(
      final Transaction transaction,
      final GoQuorumSendRawTxArgs rawTxArgs,
      final JsonRpcRequestContext requestContext) {
    // rawTxArgs cannot be null as the call to getRequiredParameter would have failed if it was not
    // available

    if (rawTxArgs.getPrivateFor() == null) {
      LOG.error(JsonRpcError.GOQUORUM_NO_PRIVATE_FOR.getMessage());
      throw new JsonRpcErrorResponseException(JsonRpcError.GOQUORUM_NO_PRIVATE_FOR);
    }

    if (rawTxArgs.getPrivacyFlag() != 0) {
      LOG.error(JsonRpcError.GOQUORUM_ONLY_STANDARD_MODE_SUPPORTED.getMessage());
      throw new JsonRpcErrorResponseException(JsonRpcError.GOQUORUM_ONLY_STANDARD_MODE_SUPPORTED);
    }

    if (rawTxArgs.getPrivateFrom() != null) {
      final String privateFrom = rawTxArgs.getPrivateFrom();
      final String enclavePublicKey =
          enclavePublicKeyProvider.getEnclaveKey(requestContext.getUser());
      if (!privateFrom.equals(enclavePublicKey)) {
        LOG.error(JsonRpcError.PRIVATE_FROM_DOES_NOT_MATCH_ENCLAVE_PUBLIC_KEY.getMessage());
        throw new JsonRpcErrorResponseException(
            JsonRpcError.PRIVATE_FROM_DOES_NOT_MATCH_ENCLAVE_PUBLIC_KEY);
      }
    }

    final Bytes txId = transaction.getPayload();
    if (txId == null || txId.isEmpty()) {
      throw new JsonRpcErrorResponseException(JsonRpcError.GOQUORUM_LOOKUP_ID_NOT_AVAILABLE);
    }
    enclave.sendSignedTransaction(txId.toArray(), rawTxArgs.getPrivateFor());
  }

  JsonRpcErrorResponse getJsonRpcErrorResponse(
      final Object id, final TransactionInvalidReason errorReason) {
    if (errorReason.equals(TransactionInvalidReason.INTRINSIC_GAS_EXCEEDS_GAS_LIMIT)) {
      return new JsonRpcErrorResponse(id, JsonRpcError.PMT_FAILED_INTRINSIC_GAS_EXCEEDS_LIMIT);
    }
    return new JsonRpcErrorResponse(id, convertTransactionInvalidReason(errorReason));
  }
}
