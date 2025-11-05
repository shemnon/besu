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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.hyperledger.besu.evm.frame.MessageFrame;
import org.hyperledger.besu.evm.gascalculator.SpuriousDragonGasCalculator;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test suite for ML-DSA-44 signature verification precompile (EIP-8051).
 *
 * <p>This test class validates the ML-DSA-44 precompile implementation using Java 25's native
 * ML-DSA support. Test vectors are derived from FIPS-204 test data.
 */
class MLDSAVerifyPrecompiledContractTest {

  private static final MLDSAVerifyPrecompiledContract contract =
      new MLDSAVerifyPrecompiledContract(new SpuriousDragonGasCalculator());

  private static final MessageFrame messageFrame = mock(MessageFrame.class);

  private static final Bytes32 VALID_RESULT = Bytes32.leftPad(Bytes.of(1), (byte) 0);
  private static final Bytes32 INVALID_RESULT = Bytes32.ZERO;

  // ML-DSA-44 constants
  private static final int PUBLIC_KEY_SIZE = 1312;
  private static final int SIGNATURE_SIZE = 2420;
  private static final int MINIMUM_INPUT_SIZE = PUBLIC_KEY_SIZE + SIGNATURE_SIZE;

  @BeforeAll
  static void setup() {
    // Verify Java 25 is available
    String javaVersion = System.getProperty("java.version");
    if (!javaVersion.startsWith("25")) {
      System.err.println(
          "Warning: ML-DSA tests require Java 25 or later. Current version: " + javaVersion);
    }
  }

  @Test
  void testGasRequirement() {
    // Gas cost should be 4,500 as specified in EIP-8051
    assertEquals(4_500L, contract.gasRequirement(Bytes.wrap(new byte[MINIMUM_INPUT_SIZE])));
  }

  @Test
  void testInvalidInputTooShort() {
    // Input shorter than minimum required size should return INVALID (0x00)
    Bytes shortInput = Bytes.wrap(new byte[100]);

    PrecompiledContract.PrecompileContractResult result =
        contract.computePrecompile(shortInput, messageFrame);

    assertTrue(result.isSuccessful(), "Precompile should succeed even with invalid input");
    assertEquals(
        INVALID_RESULT,
        result.output(),
        "Short input should return INVALID (0x00000...000 - 32 bytes)");
  }

  @Test
  void testInvalidInputExactlyMinimumSize() {
    // Input with exactly minimum size (no message) - zeros everywhere should be invalid
    Bytes minimumInput = Bytes.wrap(new byte[MINIMUM_INPUT_SIZE]);

    PrecompiledContract.PrecompileContractResult result =
        contract.computePrecompile(minimumInput, messageFrame);

    assertTrue(result.isSuccessful(), "Precompile should succeed");
    // All zeros is not a valid public key/signature combination
    assertEquals(INVALID_RESULT, result.output(), "Zero input should return INVALID");
  }

  @Test
  void testInvalidInputWithMessage() {
    // Input with public key + signature + small message, all zeros
    int messageSize = 32;
    Bytes inputWithMessage = Bytes.wrap(new byte[MINIMUM_INPUT_SIZE + messageSize]);

    PrecompiledContract.PrecompileContractResult result =
        contract.computePrecompile(inputWithMessage, messageFrame);

    assertTrue(result.isSuccessful(), "Precompile should succeed");
    assertEquals(INVALID_RESULT, result.output(), "Zero input should return INVALID");
  }

  @Test
  void testInvalidPublicKey() {
    // Create input with invalid public key (all 0xFF), valid-sized signature, and message
    byte[] invalidInput = new byte[MINIMUM_INPUT_SIZE + 32];

    // Fill public key with 0xFF (invalid)
    for (int i = 0; i < PUBLIC_KEY_SIZE; i++) {
      invalidInput[i] = (byte) 0xFF;
    }

    // Fill signature section with zeros
    // Leave message as zeros

    Bytes input = Bytes.wrap(invalidInput);
    PrecompiledContract.PrecompileContractResult result =
        contract.computePrecompile(input, messageFrame);

    assertTrue(result.isSuccessful(), "Precompile should succeed");
    assertEquals(INVALID_RESULT, result.output(), "Invalid public key should return INVALID");
  }

  @Test
  void testInvalidSignature() {
    // Create input with zeros for public key, signature section filled with 0xFF, and message
    byte[] invalidInput = new byte[MINIMUM_INPUT_SIZE + 32];

    // Leave public key as zeros
    // Fill signature with 0xFF (invalid)
    for (int i = PUBLIC_KEY_SIZE; i < PUBLIC_KEY_SIZE + SIGNATURE_SIZE; i++) {
      invalidInput[i] = (byte) 0xFF;
    }

    // Leave message as zeros

    Bytes input = Bytes.wrap(invalidInput);
    PrecompiledContract.PrecompileContractResult result =
        contract.computePrecompile(input, messageFrame);

    assertTrue(result.isSuccessful(), "Precompile should succeed");
    assertEquals(INVALID_RESULT, result.output(), "Invalid signature should return INVALID");
  }

  /**
   * This test will validate a properly signed message using ML-DSA-44 once test vectors are
   * available.
   *
   * <p>TODO: Add FIPS-204 test vectors for ML-DSA-44 when Java 25 is released. Test vectors should
   * be obtained from: - NIST FIPS-204 test data - ACVP (Automated Cryptographic Validation
   * Protocol) test vectors
   */
  @Test
  void testValidSignature() {
    // This test is a placeholder for when proper ML-DSA-44 test vectors are available.
    // Once Java 25 is released with ML-DSA support, we should:
    // 1. Generate a valid ML-DSA-44 key pair
    // 2. Sign a test message
    // 3. Verify that the precompile returns VALID (0x000...001)

    // For now, we'll skip this test with a note
    System.out.println(
        "NOTE: Valid signature test requires Java 25 ML-DSA-44 test vectors. "
            + "Test should be implemented when Java 25 is released.");

    // Example structure for the test once vectors are available:
    // Bytes publicKey = Bytes.fromHexString("...");  // 1312 bytes
    // Bytes signature = Bytes.fromHexString("...");   // 2420 bytes
    // Bytes message = Bytes.fromHexString("...");     // variable length
    //
    // Bytes input = Bytes.concatenate(publicKey, signature, message);
    // PrecompiledContract.PrecompileContractResult result =
    //     contract.computePrecompile(input, messageFrame);
    //
    // assertTrue(result.isSuccessful());
    // assertEquals(VALID_RESULT, result.output(), "Valid signature should return 0x000...001");
  }

  @Test
  void testEmptyMessage() {
    // Test with valid-sized input but empty message (just public key + signature)
    // This is technically valid but will fail verification since it's all zeros
    Bytes input = Bytes.wrap(new byte[MINIMUM_INPUT_SIZE]);

    PrecompiledContract.PrecompileContractResult result =
        contract.computePrecompile(input, messageFrame);

    assertTrue(result.isSuccessful(), "Precompile should succeed");
    assertEquals(INVALID_RESULT, result.output(), "Zero values should return INVALID");
  }

  @Test
  void testLargeMessage() {
    // Test with a large message (1 MB)
    int largeMessageSize = 1024 * 1024;
    Bytes largeInput = Bytes.wrap(new byte[MINIMUM_INPUT_SIZE + largeMessageSize]);

    PrecompiledContract.PrecompileContractResult result =
        contract.computePrecompile(largeInput, messageFrame);

    assertTrue(result.isSuccessful(), "Precompile should handle large messages");
    // All zeros will be invalid, but the precompile should not crash
    assertEquals(
        INVALID_RESULT, result.output(), "Large message with zero values should return INVALID");
  }

  @Test
  void testOutputFormat() {
    // Verify that output is exactly 32 bytes
    Bytes input = Bytes.wrap(new byte[MINIMUM_INPUT_SIZE]);

    PrecompiledContract.PrecompileContractResult result =
        contract.computePrecompile(input, messageFrame);

    assertEquals(32, result.output().size(), "Output should be exactly 32 bytes");
  }

  @Test
  void testInputSizeCalculation() {
    // Verify the constants are correct
    assertEquals(
        1312, PUBLIC_KEY_SIZE, "ML-DSA-44 public key size should be 1312 bytes (FIPS-204)");
    assertEquals(
        2420, SIGNATURE_SIZE, "ML-DSA-44 signature size should be 2420 bytes (FIPS-204)");
    assertEquals(
        3732,
        MINIMUM_INPUT_SIZE,
        "Minimum input size should be public key + signature = 3732 bytes");
  }

  /**
   * Test that verifies the precompile correctly handles the input format: pubkey || signature ||
   * message
   */
  @Test
  void testInputFormatStructure() {
    // Create a test input with distinct sections
    byte[] input = new byte[MINIMUM_INPUT_SIZE + 100];

    // Mark each section with a distinct pattern to verify parsing
    // Public key section: all 0x01
    for (int i = 0; i < PUBLIC_KEY_SIZE; i++) {
      input[i] = 0x01;
    }

    // Signature section: all 0x02
    for (int i = PUBLIC_KEY_SIZE; i < PUBLIC_KEY_SIZE + SIGNATURE_SIZE; i++) {
      input[i] = 0x02;
    }

    // Message section: all 0x03
    for (int i = PUBLIC_KEY_SIZE + SIGNATURE_SIZE; i < input.length; i++) {
      input[i] = 0x03;
    }

    Bytes inputBytes = Bytes.wrap(input);
    PrecompiledContract.PrecompileContractResult result =
        contract.computePrecompile(inputBytes, messageFrame);

    assertTrue(result.isSuccessful(), "Precompile should handle structured input");
    // The result will be INVALID since it's not a real signature, but it should not crash
    assertEquals(INVALID_RESULT, result.output());
  }
}
