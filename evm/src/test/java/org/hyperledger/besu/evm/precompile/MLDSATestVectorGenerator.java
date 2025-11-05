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

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.HexFormat;

import org.apache.tuweni.bytes.Bytes;

/**
 * Utility class for generating ML-DSA-44 test vectors using Java's native ML-DSA support (JEP-497,
 * available since JDK 24).
 *
 * <p>This class provides methods to generate valid ML-DSA-44 key pairs, sign messages, and create
 * test vectors for the VERIFY_MLDSA precompile.
 */
public class MLDSATestVectorGenerator {

  private static final String MLDSA44_ALGORITHM = "ML-DSA-44";
  private static final HexFormat HEX = HexFormat.of();

  /**
   * Generates a new ML-DSA-44 key pair.
   *
   * @return a new KeyPair for ML-DSA-44
   * @throws NoSuchAlgorithmException if ML-DSA-44 is not available
   */
  public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance(MLDSA44_ALGORITHM);
    keyGen.initialize(2, new SecureRandom()); // ML-DSA-44 is security level 2
    return keyGen.generateKeyPair();
  }

  /**
   * Signs a message using ML-DSA-44.
   *
   * @param message the message to sign
   * @param privateKey the private key to use for signing
   * @return the signature bytes
   * @throws NoSuchAlgorithmException if ML-DSA-44 is not available
   * @throws InvalidKeyException if the key is invalid
   * @throws SignatureException if signing fails
   */
  public static byte[] signMessage(final byte[] message, final PrivateKey privateKey)
      throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature signer = Signature.getInstance(MLDSA44_ALGORITHM);
    signer.initSign(privateKey);
    signer.update(message);
    return signer.sign();
  }

  /**
   * Verifies a signature using ML-DSA-44.
   *
   * @param message the message that was signed
   * @param signature the signature to verify
   * @param publicKey the public key to use for verification
   * @return true if the signature is valid, false otherwise
   * @throws NoSuchAlgorithmException if ML-DSA-44 is not available
   * @throws InvalidKeyException if the key is invalid
   * @throws SignatureException if verification fails
   */
  public static boolean verifySignature(
      final byte[] message, final byte[] signature, final PublicKey publicKey)
      throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature verifier = Signature.getInstance(MLDSA44_ALGORITHM);
    verifier.initVerify(publicKey);
    verifier.update(message);
    return verifier.verify(signature);
  }

  /**
   * Extracts the raw public key bytes from a PublicKey object.
   *
   * @param publicKey the public key
   * @return the raw public key bytes (1,312 bytes for ML-DSA-44)
   */
  public static byte[] getRawPublicKey(final PublicKey publicKey) {
    // For ML-DSA, the encoded form may include X.509 wrapper
    // We need to extract just the raw key bytes
    byte[] encoded = publicKey.getEncoded();

    // Check if this is X.509 encoded (starts with SEQUENCE tag 0x30)
    if (encoded[0] == 0x30) {
      // Parse X.509 structure to extract raw key
      // For ML-DSA-44, the raw key is 1,312 bytes
      // The X.509 structure is: SEQUENCE { SEQUENCE { OID }, BIT STRING }
      // We need to skip the DER encoding and extract the BIT STRING content
      return extractRawKeyFromX509(encoded);
    }

    // If already raw, return as-is
    return encoded;
  }

  /**
   * Extracts raw key bytes from X.509 encoded public key.
   *
   * @param x509Encoded the X.509 encoded key
   * @return the raw key bytes
   */
  private static byte[] extractRawKeyFromX509(final byte[] x509Encoded) {
    // Simple DER parser for X.509 SubjectPublicKeyInfo
    // SEQUENCE {
    //   SEQUENCE { OID },
    //   BIT STRING (raw key)
    // }

    int offset = 0;

    // Skip outer SEQUENCE tag and length
    if (x509Encoded[offset++] != 0x30) {
      throw new IllegalArgumentException("Invalid X.509 encoding: expected SEQUENCE");
    }
    offset += getLengthFieldSize(x509Encoded, offset);

    // Skip inner SEQUENCE (algorithm identifier)
    if (x509Encoded[offset++] != 0x30) {
      throw new IllegalArgumentException("Invalid X.509 encoding: expected inner SEQUENCE");
    }
    int innerSeqLength = getLength(x509Encoded, offset);
    offset += getLengthFieldSize(x509Encoded, offset);
    offset += innerSeqLength;

    // Now we're at the BIT STRING
    if (x509Encoded[offset++] != 0x03) {
      throw new IllegalArgumentException("Invalid X.509 encoding: expected BIT STRING");
    }
    int bitStringLength = getLength(x509Encoded, offset);
    offset += getLengthFieldSize(x509Encoded, offset);

    // Skip the "unused bits" byte (should be 0)
    offset++;
    bitStringLength--;

    // Extract the raw key
    byte[] rawKey = new byte[bitStringLength];
    System.arraycopy(x509Encoded, offset, rawKey, 0, bitStringLength);
    return rawKey;
  }

  private static int getLength(final byte[] data, final int offset) {
    int firstByte = data[offset] & 0xFF;
    if ((firstByte & 0x80) == 0) {
      // Short form: length is in the first byte
      return firstByte;
    } else {
      // Long form: first byte tells us how many length bytes follow
      int numLengthBytes = firstByte & 0x7F;
      int length = 0;
      for (int i = 1; i <= numLengthBytes; i++) {
        length = (length << 8) | (data[offset + i] & 0xFF);
      }
      return length;
    }
  }

  private static int getLengthFieldSize(final byte[] data, final int offset) {
    int firstByte = data[offset] & 0xFF;
    if ((firstByte & 0x80) == 0) {
      return 1; // Short form
    } else {
      return 1 + (firstByte & 0x7F); // Long form
    }
  }

  /**
   * Creates a complete test vector for the VERIFY_MLDSA precompile.
   *
   * @param message the message to sign
   * @return a TestVector containing all necessary data
   * @throws Exception if test vector generation fails
   */
  public static TestVector generateTestVector(final byte[] message) throws Exception {
    KeyPair keyPair = generateKeyPair();
    byte[] signature = signMessage(message, keyPair.getPrivate());
    byte[] rawPublicKey = getRawPublicKey(keyPair.getPublic());

    return new TestVector(rawPublicKey, signature, message, true);
  }

  /**
   * Creates a test vector with an invalid signature.
   *
   * @param message the message
   * @return a TestVector with an invalid signature
   * @throws Exception if test vector generation fails
   */
  public static TestVector generateInvalidSignatureTestVector(final byte[] message)
      throws Exception {
    KeyPair keyPair = generateKeyPair();
    byte[] signature = signMessage(message, keyPair.getPrivate());

    // Corrupt the signature
    signature[0] ^= 0xFF;

    byte[] rawPublicKey = getRawPublicKey(keyPair.getPublic());
    return new TestVector(rawPublicKey, signature, message, false);
  }

  /**
   * Creates a test vector with a valid signature but different message.
   *
   * @param originalMessage the original message that was signed
   * @param wrongMessage the wrong message to include in the test vector
   * @return a TestVector that should fail verification
   * @throws Exception if test vector generation fails
   */
  public static TestVector generateWrongMessageTestVector(
      final byte[] originalMessage, final byte[] wrongMessage) throws Exception {
    KeyPair keyPair = generateKeyPair();
    byte[] signature = signMessage(originalMessage, keyPair.getPrivate());
    byte[] rawPublicKey = getRawPublicKey(keyPair.getPublic());

    return new TestVector(rawPublicKey, signature, wrongMessage, false);
  }

  /** Test vector data structure. */
  public static class TestVector {
    private final byte[] publicKey;
    private final byte[] signature;
    private final byte[] message;
    private final boolean shouldVerify;

    /**
     * Creates a new test vector.
     *
     * @param publicKey the public key (1,312 bytes for ML-DSA-44)
     * @param signature the signature (2,420 bytes for ML-DSA-44)
     * @param message the message
     * @param shouldVerify whether this signature should verify successfully
     */
    public TestVector(
        final byte[] publicKey,
        final byte[] signature,
        final byte[] message,
        final boolean shouldVerify) {
      this.publicKey = publicKey;
      this.signature = signature;
      this.message = message;
      this.shouldVerify = shouldVerify;
    }

    public byte[] getPublicKey() {
      return publicKey;
    }

    public byte[] getSignature() {
      return signature;
    }

    public byte[] getMessage() {
      return message;
    }

    public boolean shouldVerify() {
      return shouldVerify;
    }

    /**
     * Gets the complete precompile input (pubkey || signature || message).
     *
     * @return the precompile input bytes
     */
    public Bytes getPrecompileInput() {
      return Bytes.concatenate(
          Bytes.wrap(publicKey), Bytes.wrap(signature), Bytes.wrap(message));
    }

    /**
     * Gets the expected precompile output.
     *
     * @return 32-byte result (1 for valid, 0 for invalid)
     */
    public Bytes getExpectedOutput() {
      if (shouldVerify) {
        // Valid: 0x0000...0001 (32 bytes)
        byte[] result = new byte[32];
        result[31] = 1;
        return Bytes.wrap(result);
      } else {
        // Invalid: 0x0000...0000 (32 bytes)
        return Bytes.wrap(new byte[32]);
      }
    }

    @Override
    public String toString() {
      return String.format(
          "TestVector{publicKey=%d bytes, signature=%d bytes, message=%d bytes, shouldVerify=%b}",
          publicKey.length, signature.length, message.length, shouldVerify);
    }

    /**
     * Prints the test vector in a format suitable for external test files.
     *
     * @return formatted test vector string
     */
    public String toHexString() {
      return String.format(
          "{\n"
              + "  \"publicKey\": \"%s\",\n"
              + "  \"signature\": \"%s\",\n"
              + "  \"message\": \"%s\",\n"
              + "  \"shouldVerify\": %b,\n"
              + "  \"input\": \"%s\",\n"
              + "  \"expectedOutput\": \"%s\"\n"
              + "}",
          HEX.formatHex(publicKey),
          HEX.formatHex(signature),
          HEX.formatHex(message),
          shouldVerify,
          getPrecompileInput().toHexString(),
          getExpectedOutput().toHexString());
    }
  }

  /**
   * Main method for generating test vectors from command line.
   *
   * @param args command line arguments
   */
  public static void main(final String[] args) {
    try {
      System.out.println("Generating ML-DSA-44 test vectors...\n");

      // Generate valid signature test vector
      TestVector valid = generateTestVector("Hello, Ethereum!".getBytes());
      System.out.println("=== Valid Signature Test Vector ===");
      System.out.println(valid);
      System.out.println(valid.toHexString());
      System.out.println();

      // Generate invalid signature test vector
      TestVector invalid = generateInvalidSignatureTestVector("Hello, Ethereum!".getBytes());
      System.out.println("=== Invalid Signature Test Vector ===");
      System.out.println(invalid);
      System.out.println(invalid.toHexString());
      System.out.println();

      // Generate wrong message test vector
      TestVector wrongMessage =
          generateWrongMessageTestVector(
              "Original message".getBytes(), "Different message".getBytes());
      System.out.println("=== Wrong Message Test Vector ===");
      System.out.println(wrongMessage);
      System.out.println(wrongMessage.toHexString());

    } catch (Exception e) {
      System.err.println("Error generating test vectors: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
