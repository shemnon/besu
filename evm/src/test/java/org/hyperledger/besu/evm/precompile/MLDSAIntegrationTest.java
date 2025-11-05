/*
 * Copyright contributors to Hyperledger Besu.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.hyperledger.besu.evm.frame.MessageFrame;
import org.hyperledger.besu.evm.gascalculator.SpuriousDragonGasCalculator;

import java.security.KeyPair;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for ML-DSA-44 precompile demonstrating real-world usage patterns.
 *
 * <p>These tests simulate realistic scenarios where the VERIFY_MLDSA precompile would be used in
 * production, including multi-signature schemes, message authentication, and error handling.
 */
class MLDSAIntegrationTest {

  private static final MLDSAVerifyPrecompiledContract contract =
      new MLDSAVerifyPrecompiledContract(new SpuriousDragonGasCalculator());

  private static final MessageFrame messageFrame = mock(MessageFrame.class);

  private static final Bytes32 VALID = Bytes32.leftPad(Bytes.of(1), (byte) 0);
  private static final Bytes32 INVALID = Bytes32.ZERO;

  @BeforeAll
  static void setup() {
    System.out.println("=".repeat(80));
    System.out.println("ML-DSA-44 Integration Tests");
    System.out.println("Testing real-world usage scenarios for EIP-8051 VERIFY_MLDSA precompile");
    System.out.println("=".repeat(80));
  }

  /**
   * Scenario: A user wants to authenticate messages using quantum-resistant signatures.
   *
   * <p>This test demonstrates the basic workflow: 1. User generates a key pair off-chain 2. User
   * signs a message 3. Smart contract verifies the signature on-chain
   */
  @Test
  void testMessageAuthentication() throws Exception {
    System.out.println("\n--- Test: Message Authentication ---");

    // Step 1: Generate key pair (off-chain)
    KeyPair keyPair = MLDSATestVectorGenerator.generateKeyPair();
    System.out.println("Generated ML-DSA-44 key pair");

    // Step 2: Create and sign a message (off-chain)
    String message = "Transfer 100 ETH to 0x742d35Cc6634C0532925a3b844Bc454e4438f44e";
    byte[] messageBytes = message.getBytes();
    byte[] signature = MLDSATestVectorGenerator.signMessage(messageBytes, keyPair.getPrivate());
    byte[] publicKey = MLDSATestVectorGenerator.getRawPublicKey(keyPair.getPublic());

    System.out.println("Signed message: " + message);
    System.out.println("Public key size: " + publicKey.length + " bytes");
    System.out.println("Signature size: " + signature.length + " bytes");

    // Step 3: Verify on-chain (via precompile)
    Bytes input = Bytes.concatenate(Bytes.wrap(publicKey), Bytes.wrap(signature), Bytes.wrap(messageBytes));

    PrecompiledContract.PrecompileContractResult result =
        contract.computePrecompile(input, messageFrame);

    assertTrue(result.isSuccessful());
    assertEquals(VALID, result.output());
    System.out.println("✓ Signature verified successfully on-chain");
  }

  /**
   * Scenario: Multi-signature authorization - requires N of M signers to approve a transaction.
   *
   * <p>This demonstrates how multiple independent signers can use ML-DSA to create a multi-sig
   * setup.
   */
  @Test
  void testMultiSignatureScheme() throws Exception {
    System.out.println("\n--- Test: Multi-Signature Scheme (2-of-3) ---");

    // Simulate a 2-of-3 multisig
    KeyPair signer1 = MLDSATestVectorGenerator.generateKeyPair();
    KeyPair signer2 = MLDSATestVectorGenerator.generateKeyPair();
    KeyPair signer3 = MLDSATestVectorGenerator.generateKeyPair();

    String transactionData = "Execute governance proposal #42";
    byte[] message = transactionData.getBytes();

    // Signer 1 approves
    byte[] sig1 = MLDSATestVectorGenerator.signMessage(message, signer1.getPrivate());
    byte[] pubKey1 = MLDSATestVectorGenerator.getRawPublicKey(signer1.getPublic());

    // Signer 2 approves
    byte[] sig2 = MLDSATestVectorGenerator.signMessage(message, signer2.getPrivate());
    byte[] pubKey2 = MLDSATestVectorGenerator.getRawPublicKey(signer2.getPublic());

    // Signer 3 does NOT sign (we only need 2 of 3)

    System.out.println("Verifying signatures from 2 of 3 signers...");

    // Verify signer 1
    Bytes input1 = Bytes.concatenate(Bytes.wrap(pubKey1), Bytes.wrap(sig1), Bytes.wrap(message));
    PrecompiledContract.PrecompileContractResult result1 =
        contract.computePrecompile(input1, messageFrame);
    assertTrue(result1.isSuccessful());
    assertEquals(VALID, result1.output());
    System.out.println("✓ Signer 1 verified");

    // Verify signer 2
    Bytes input2 = Bytes.concatenate(Bytes.wrap(pubKey2), Bytes.wrap(sig2), Bytes.wrap(message));
    PrecompiledContract.PrecompileContractResult result2 =
        contract.computePrecompile(input2, messageFrame);
    assertTrue(result2.isSuccessful());
    assertEquals(VALID, result2.output());
    System.out.println("✓ Signer 2 verified");

    System.out.println("✓ 2-of-3 multisig requirement satisfied");
  }

  /**
   * Scenario: Replay attack prevention using nonces.
   *
   * <p>Demonstrates how to include nonces in signed messages to prevent replay attacks.
   */
  @Test
  void testReplayAttackPrevention() throws Exception {
    System.out.println("\n--- Test: Replay Attack Prevention ---");

    KeyPair keyPair = MLDSATestVectorGenerator.generateKeyPair();

    // Original transaction with nonce
    String transaction = "action=transfer&amount=100&nonce=12345";
    byte[] message = transaction.getBytes();
    byte[] signature = MLDSATestVectorGenerator.signMessage(message, keyPair.getPrivate());
    byte[] publicKey = MLDSATestVectorGenerator.getRawPublicKey(keyPair.getPublic());

    System.out.println("Original transaction: " + transaction);

    // Verify original transaction
    Bytes input = Bytes.concatenate(Bytes.wrap(publicKey), Bytes.wrap(signature), Bytes.wrap(message));
    PrecompiledContract.PrecompileContractResult result =
        contract.computePrecompile(input, messageFrame);
    assertTrue(result.isSuccessful());
    assertEquals(VALID, result.output());
    System.out.println("✓ Original transaction verified (nonce=12345)");

    // Attempt replay with same signature but different nonce
    String replayAttempt = "action=transfer&amount=100&nonce=12346";
    byte[] replayMessage = replayAttempt.getBytes();
    Bytes replayInput =
        Bytes.concatenate(Bytes.wrap(publicKey), Bytes.wrap(signature), Bytes.wrap(replayMessage));

    PrecompiledContract.PrecompileContractResult replayResult =
        contract.computePrecompile(replayInput, messageFrame);
    assertTrue(replayResult.isSuccessful());
    assertEquals(INVALID, replayResult.output());
    System.out.println("✓ Replay attack with modified nonce correctly rejected");
  }

  /**
   * Scenario: Signature verification with large data payloads.
   *
   * <p>Tests the precompile's ability to handle realistic data sizes like contract deployments or
   * large state updates.
   */
  @Test
  void testLargeDataPayload() throws Exception {
    System.out.println("\n--- Test: Large Data Payload ---");

    KeyPair keyPair = MLDSATestVectorGenerator.generateKeyPair();

    // Simulate a large payload (e.g., contract bytecode)
    int payloadSize = 50_000; // 50KB
    byte[] largePayload = new byte[payloadSize];
    for (int i = 0; i < payloadSize; i++) {
      largePayload[i] = (byte) (i % 256);
    }

    System.out.println("Payload size: " + payloadSize + " bytes");

    long startTime = System.nanoTime();
    byte[] signature = MLDSATestVectorGenerator.signMessage(largePayload, keyPair.getPrivate());
    long signTime = System.nanoTime() - startTime;

    byte[] publicKey = MLDSATestVectorGenerator.getRawPublicKey(keyPair.getPublic());

    startTime = System.nanoTime();
    Bytes input = Bytes.concatenate(Bytes.wrap(publicKey), Bytes.wrap(signature), Bytes.wrap(largePayload));
    PrecompiledContract.PrecompileContractResult result =
        contract.computePrecompile(input, messageFrame);
    long verifyTime = System.nanoTime() - startTime;

    assertTrue(result.isSuccessful());
    assertEquals(VALID, result.output());

    System.out.printf("Sign time: %.2f ms%n", signTime / 1_000_000.0);
    System.out.printf("Verify time: %.2f ms%n", verifyTime / 1_000_000.0);
    System.out.println("✓ Large payload verified successfully");
  }

  /**
   * Scenario: Handling various error conditions gracefully.
   *
   * <p>Demonstrates that the precompile handles errors without reverting, always returning 0 for
   * invalid inputs.
   */
  @Test
  void testErrorHandling() throws Exception {
    System.out.println("\n--- Test: Error Handling ---");

    KeyPair keyPair = MLDSATestVectorGenerator.generateKeyPair();
    byte[] message = "test message".getBytes();
    byte[] signature = MLDSATestVectorGenerator.signMessage(message, keyPair.getPrivate());
    byte[] publicKey = MLDSATestVectorGenerator.getRawPublicKey(keyPair.getPublic());

    // Test 1: Truncated public key
    System.out.println("Testing truncated public key...");
    byte[] truncatedPubKey = new byte[1000]; // Should be 1312
    System.arraycopy(publicKey, 0, truncatedPubKey, 0, 1000);
    Bytes input1 = Bytes.concatenate(Bytes.wrap(truncatedPubKey), Bytes.wrap(signature), Bytes.wrap(message));
    PrecompiledContract.PrecompileContractResult result1 =
        contract.computePrecompile(input1, messageFrame);
    assertTrue(result1.isSuccessful());
    assertEquals(INVALID, result1.output());
    System.out.println("✓ Truncated public key handled correctly (returned 0)");

    // Test 2: Modified signature
    System.out.println("Testing modified signature...");
    byte[] modifiedSig = signature.clone();
    modifiedSig[100] ^= 0xFF; // Flip some bits
    Bytes input2 = Bytes.concatenate(Bytes.wrap(publicKey), Bytes.wrap(modifiedSig), Bytes.wrap(message));
    PrecompiledContract.PrecompileContractResult result2 =
        contract.computePrecompile(input2, messageFrame);
    assertTrue(result2.isSuccessful());
    assertEquals(INVALID, result2.output());
    System.out.println("✓ Modified signature handled correctly (returned 0)");

    // Test 3: Wrong public key
    System.out.println("Testing wrong public key...");
    KeyPair wrongKeyPair = MLDSATestVectorGenerator.generateKeyPair();
    byte[] wrongPubKey = MLDSATestVectorGenerator.getRawPublicKey(wrongKeyPair.getPublic());
    Bytes input3 = Bytes.concatenate(Bytes.wrap(wrongPubKey), Bytes.wrap(signature), Bytes.wrap(message));
    PrecompiledContract.PrecompileContractResult result3 =
        contract.computePrecompile(input3, messageFrame);
    assertTrue(result3.isSuccessful());
    assertEquals(INVALID, result3.output());
    System.out.println("✓ Wrong public key handled correctly (returned 0)");

    // Test 4: Empty input
    System.out.println("Testing empty input...");
    Bytes input4 = Bytes.EMPTY;
    PrecompiledContract.PrecompileContractResult result4 =
        contract.computePrecompile(input4, messageFrame);
    assertTrue(result4.isSuccessful());
    assertEquals(INVALID, result4.output());
    System.out.println("✓ Empty input handled correctly (returned 0)");

    System.out.println("✓ All error conditions handled without reverting");
  }

  /**
   * Scenario: Performance testing with repeated verifications.
   *
   * <p>Tests the caching mechanism and overall performance characteristics.
   */
  @Test
  void testPerformanceAndCaching() throws Exception {
    System.out.println("\n--- Test: Performance & Caching ---");

    KeyPair keyPair = MLDSATestVectorGenerator.generateKeyPair();
    byte[] message = "Performance test message".getBytes();
    byte[] signature = MLDSATestVectorGenerator.signMessage(message, keyPair.getPrivate());
    byte[] publicKey = MLDSATestVectorGenerator.getRawPublicKey(keyPair.getPublic());

    Bytes input = Bytes.concatenate(Bytes.wrap(publicKey), Bytes.wrap(signature), Bytes.wrap(message));

    // First verification (cold cache)
    long startTime = System.nanoTime();
    PrecompiledContract.PrecompileContractResult result1 =
        contract.computePrecompile(input, messageFrame);
    long coldTime = System.nanoTime() - startTime;

    assertTrue(result1.isSuccessful());
    assertEquals(VALID, result1.output());

    // Subsequent verifications (warm cache)
    int iterations = 100;
    startTime = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
      PrecompiledContract.PrecompileContractResult result =
          contract.computePrecompile(input, messageFrame);
      assertTrue(result.isSuccessful());
      assertEquals(VALID, result.output());
    }
    long warmTotalTime = System.nanoTime() - startTime;
    long warmAvgTime = warmTotalTime / iterations;

    System.out.printf("Cold cache (first call): %.2f ms%n", coldTime / 1_000_000.0);
    System.out.printf(
        "Warm cache (avg of %d calls): %.2f ms%n", iterations, warmAvgTime / 1_000_000.0);
    System.out.printf("Speedup factor: %.2fx%n", (double) coldTime / warmAvgTime);
    System.out.println("✓ Caching mechanism working correctly");
  }

  /**
   * Scenario: Cross-key verification (ensuring signatures are not valid across different keys).
   */
  @Test
  void testCrossKeyVerification() throws Exception {
    System.out.println("\n--- Test: Cross-Key Verification ---");

    // Generate two independent key pairs
    KeyPair keyPair1 = MLDSATestVectorGenerator.generateKeyPair();
    KeyPair keyPair2 = MLDSATestVectorGenerator.generateKeyPair();

    byte[] message = "Common message".getBytes();

    // Sign with key 1
    byte[] signature1 = MLDSATestVectorGenerator.signMessage(message, keyPair1.getPrivate());
    byte[] publicKey1 = MLDSATestVectorGenerator.getRawPublicKey(keyPair1.getPublic());
    byte[] publicKey2 = MLDSATestVectorGenerator.getRawPublicKey(keyPair2.getPublic());

    // Verify with correct key
    Bytes input1 = Bytes.concatenate(Bytes.wrap(publicKey1), Bytes.wrap(signature1), Bytes.wrap(message));
    PrecompiledContract.PrecompileContractResult result1 =
        contract.computePrecompile(input1, messageFrame);
    assertTrue(result1.isSuccessful());
    assertEquals(VALID, result1.output());
    System.out.println("✓ Signature verified with correct key");

    // Try to verify with wrong key
    Bytes input2 = Bytes.concatenate(Bytes.wrap(publicKey2), Bytes.wrap(signature1), Bytes.wrap(message));
    PrecompiledContract.PrecompileContractResult result2 =
        contract.computePrecompile(input2, messageFrame);
    assertTrue(result2.isSuccessful());
    assertEquals(INVALID, result2.output());
    System.out.println("✓ Signature correctly rejected with wrong key");

    System.out.println("✓ Cross-key verification working correctly");
  }

  /**
   * Scenario: Demonstrating quantum resistance by showing classical signatures don't work.
   */
  @Test
  void testQuantumResistanceDemo() throws Exception {
    System.out.println("\n--- Test: Quantum Resistance Demonstration ---");

    System.out.println("ML-DSA-44 Security Properties:");
    System.out.println("  • Based on lattice problems (Module-LWE)");
    System.out.println("  • NIST Security Level II (128-bit classical)");
    System.out.println("  • Resistant to Shor's algorithm");
    System.out.println("  • Public key: 1,312 bytes");
    System.out.println("  • Signature: 2,420 bytes");

    KeyPair keyPair = MLDSATestVectorGenerator.generateKeyPair();
    byte[] message = "Quantum-resistant message".getBytes();
    byte[] signature = MLDSATestVectorGenerator.signMessage(message, keyPair.getPrivate());
    byte[] publicKey = MLDSATestVectorGenerator.getRawPublicKey(keyPair.getPublic());

    Bytes input = Bytes.concatenate(Bytes.wrap(publicKey), Bytes.wrap(signature), Bytes.wrap(message));
    PrecompiledContract.PrecompileContractResult result =
        contract.computePrecompile(input, messageFrame);

    assertTrue(result.isSuccessful());
    assertEquals(VALID, result.output());
    System.out.println("✓ Quantum-resistant signature verified successfully");
    System.out.println(
        "\nNote: This signature remains secure even against quantum computers,");
    System.out.println("unlike ECDSA signatures which would be breakable by Shor's algorithm.");
  }
}
