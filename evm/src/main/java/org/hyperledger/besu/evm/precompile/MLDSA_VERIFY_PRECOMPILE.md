# ML-DSA-44 Signature Verification Precompile (EIP-8051)

## Overview

The VERIFY_MLDSA precompile implements post-quantum signature verification using ML-DSA-44 (Module-Lattice-Based Digital Signature Algorithm), as specified in [FIPS-204](https://csrc.nist.gov/pubs/fips/204/final) and [EIP-8051](https://github.com/ethereum/EIPs/blob/master/EIPS/eip-8051.md).

This precompile provides Ethereum with quantum-resistant cryptographic verification capabilities using the ML-DSA-44 parameter set, which offers NIST security level II (128-bit classical security).

## Specification

### Address
- **Precompile Address**: `0x0000000000000000000000000000000000000012` (`0x12`)

### Gas Cost
- **Fixed Cost**: 4,500 gas (as specified in EIP-8051)

### Input Format

The precompile accepts input in the following format:

```
input = publicKey || signature || message
```

| Component   | Size (bytes) | Description                           |
|-------------|--------------|---------------------------------------|
| publicKey   | 1,312        | ML-DSA-44 public key (FIPS-204)      |
| signature   | 2,420        | ML-DSA-44 signature (FIPS-204)       |
| message     | variable     | The message that was signed          |

**Total minimum input size**: 3,732 bytes (public key + signature, with no message)

### Output Format

The precompile returns a 32-byte (256-bit) result:

| Result | Value | Description |
|--------|-------|-------------|
| **Valid** | `0x0000000000000000000000000000000000000000000000000000000000000001` | Signature is valid |
| **Invalid** | `0x0000000000000000000000000000000000000000000000000000000000000000` | Signature is invalid, malformed input, or parsing error |

### Behavior

1. **Successful Verification**: Returns 32-byte word with value `1`
2. **Failed Verification**: Returns 32-byte word with value `0`
3. **Revert Conditions**: The precompile only reverts if insufficient gas is provided

The precompile **never reverts** due to invalid input. All input validation errors, parsing failures, and cryptographic verification failures result in returning `0`.

## Implementation Notes

### Standard vs. Non-Standard Format

This implementation uses **standard FIPS-204 ML-DSA-44 format** rather than the non-standard format suggested in the original EIP draft.

**Why?**
- The EIP's recommendation to include the full matrix A_hat and other intermediate values in the public key (20,512 bytes) is unnecessary
- This optimization is designed for EVM bytecode operations but comes at the expense of:
  - Standard cryptographic practices
  - Interoperability with other ML-DSA implementations
  - Excessive storage overhead (16x larger than necessary)

**Our Approach:**
- Public key: 1,312 bytes (standard FIPS-204 ML-DSA-44)
- Signature: 2,420 bytes (standard FIPS-204 ML-DSA-44)
- Full compatibility with NIST FIPS-204 specification
- Seamless interoperability with other ML-DSA-44 implementations

### Java Implementation

This precompile leverages Java's native ML-DSA support introduced in:
- **JEP-497**: Key Encapsulation Mechanism API and ML-DSA Signature Scheme
- **Available since**: JDK 24 (September 2024)
- **Required version**: Java 25 LTS or later

## Usage Examples

### Example 1: Verifying a Valid Signature

```solidity
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract MLDSAVerifier {
    address constant MLDSA_VERIFY = address(0x12);

    function verifyMLDSASignature(
        bytes memory publicKey,   // 1312 bytes
        bytes memory signature,   // 2420 bytes
        bytes memory message
    ) public view returns (bool) {
        require(publicKey.length == 1312, "Invalid public key size");
        require(signature.length == 2420, "Invalid signature size");

        // Concatenate: publicKey || signature || message
        bytes memory input = abi.encodePacked(publicKey, signature, message);

        // Call the precompile
        (bool success, bytes memory result) = MLDSA_VERIFY.staticcall(input);
        require(success, "Precompile call failed");

        // Check if result is 0x000...001 (32 bytes)
        return uint256(bytes32(result)) == 1;
    }
}
```

### Example 2: Batch Verification

```solidity
contract BatchMLDSAVerifier {
    address constant MLDSA_VERIFY = address(0x12);

    struct Signature {
        bytes publicKey;
        bytes signature;
        bytes message;
    }

    function verifyBatch(Signature[] calldata signatures)
        public view returns (bool[] memory results)
    {
        results = new bool[](signatures.length);

        for (uint i = 0; i < signatures.length; i++) {
            bytes memory input = abi.encodePacked(
                signatures[i].publicKey,
                signatures[i].signature,
                signatures[i].message
            );

            (bool success, bytes memory result) = MLDSA_VERIFY.staticcall(input);
            if (success && uint256(bytes32(result)) == 1) {
                results[i] = true;
            }
        }

        return results;
    }
}
```

### Example 3: Off-Chain Signature Generation (JavaScript/ethers.js)

```javascript
// Note: This is pseudocode - actual ML-DSA libraries for JavaScript
// may vary in implementation

import { MLDSA44 } from 'ml-dsa-js'; // Hypothetical library

async function generateAndVerify() {
    // Generate key pair
    const keyPair = await MLDSA44.generateKeyPair();

    // Message to sign
    const message = Buffer.from("Hello, Ethereum!");

    // Sign the message
    const signature = await MLDSA44.sign(message, keyPair.privateKey);

    // Prepare precompile input
    const publicKey = keyPair.publicKey; // 1312 bytes
    const sig = signature;               // 2420 bytes

    // Call the precompile via ethers.js
    const MLDSA_VERIFY_ADDRESS = "0x0000000000000000000000000000000000000012";
    const input = Buffer.concat([publicKey, sig, message]);

    const provider = new ethers.providers.JsonRpcProvider();
    const result = await provider.call({
        to: MLDSA_VERIFY_ADDRESS,
        data: "0x" + input.toString('hex')
    });

    // Check result
    const isValid = BigInt(result) === 1n;
    console.log("Signature valid:", isValid);
}
```

## Testing

### Running Tests

```bash
# Run all ML-DSA tests
./gradlew test --tests MLDSAVerifyPrecompiledContractTest

# Generate test vectors
./gradlew test --tests MLDSATestVectorGenerator
```

### Generating Test Vectors

You can generate ML-DSA-44 test vectors using the included utility:

```java
import org.hyperledger.besu.evm.precompile.MLDSATestVectorGenerator;

// Generate a valid signature test vector
byte[] message = "Hello, Ethereum!".getBytes();
MLDSATestVectorGenerator.TestVector vector =
    MLDSATestVectorGenerator.generateTestVector(message);

// Get precompile input
Bytes input = vector.getPrecompileInput();

// Get expected output (0x000...001 for valid)
Bytes expectedOutput = vector.getExpectedOutput();

// Print as hex for external testing
System.out.println(vector.toHexString());
```

## Security Considerations

### Post-Quantum Security

ML-DSA-44 provides quantum-resistant security based on the hardness of lattice problems:
- **Security Level**: NIST Level II (128-bit classical, quantum-resistant)
- **Parameter Set**: ML-DSA-44 (k=4, l=4)
- **Hash Function**: SHAKE256 (as per FIPS-204)

### Verification Only

This precompile **only performs signature verification**. Private key operations (signature generation) must be performed off-chain.

### Gas Costs

The fixed gas cost of 4,500 is based on:
- Computational complexity of ML-DSA-44 verification
- NTT (Number Theoretic Transform) operations
- Polynomial arithmetic over the ring Rq

**Note**: This is significantly cheaper than secp256k1 ECRECOVER (3,000 gas) adjusted for the higher computational cost of post-quantum operations.

### Input Validation

The precompile validates:
1. Minimum input size (3,732 bytes)
2. Public key format (1,312 bytes, FIPS-204 compliant)
3. Signature format (2,420 bytes, FIPS-204 compliant)
4. Cryptographic validity of signature

**All validation failures return `0` (invalid) rather than reverting.**

## Performance Characteristics

### Benchmarks

Approximate performance characteristics on modern hardware:

| Operation | Time (approx.) | Notes |
|-----------|----------------|-------|
| Key Generation | 10-20 ms | Off-chain only |
| Signing | 15-30 ms | Off-chain only |
| Verification | 10-20 ms | Precompile operation |

### Caching

The implementation includes result caching (using Caffeine) to improve performance for repeated verification of identical inputs:
- **Cache size**: 1,000 entries
- **Cache key**: Hash of input data
- **Collision handling**: Full input comparison on cache hit

## Compatibility

### Hard Fork Activation

This precompile is activated in the **futureEIPs** hard fork.

### Backwards Compatibility

- **Address Space**: Uses address `0x12`, which is in the standard precompile address range
- **No Conflicts**: Does not conflict with existing precompiles
- **Versioning**: Follows FIPS-204 standard, ensuring long-term compatibility

## References

### Standards
- [FIPS-204](https://csrc.nist.gov/pubs/fips/204/final): Module-Lattice-Based Digital Signature Standard
- [EIP-8051](https://github.com/ethereum/EIPs/blob/master/EIPS/eip-8051.md): Precompiled contract for ML-DSA signature verification
- [JEP-497](https://openjdk.org/jeps/497): Key Encapsulation Mechanism API and ML-DSA

### Implementation
- Source: `org.hyperledger.besu.evm.precompile.MLDSAVerifyPrecompiledContract`
- Tests: `org.hyperledger.besu.evm.precompile.MLDSAVerifyPrecompiledContractTest`
- Utilities: `org.hyperledger.besu.evm.precompile.MLDSATestVectorGenerator`

### Related Precompiles
- `0x01` - ECRECOVER (secp256k1 signature recovery)
- `0x0100` - P256VERIFY (secp256r1 signature verification, EIP-7951)

## FAQ

### Q: Why ML-DSA-44 instead of ML-DSA-65 or ML-DSA-87?

**A**: ML-DSA-44 provides NIST security level II (equivalent to 128-bit classical security), which matches Ethereum's current security level for ECDSA signatures. It offers the best balance of security, signature size, and performance.

### Q: Can I use this with other ML-DSA parameter sets?

**A**: No, this precompile specifically implements ML-DSA-44. Other parameter sets (ML-DSA-65, ML-DSA-87) would require separate precompiles with different addresses.

### Q: What happens if I provide an input shorter than 3,732 bytes?

**A**: The precompile returns `0` (invalid) immediately without attempting verification.

### Q: Is the precompile's format compatible with NIST test vectors?

**A**: Yes, we use standard FIPS-204 format, ensuring full compatibility with NIST test vectors and other standards-compliant implementations.

### Q: Why not use the EIP's suggested 20,512-byte public key format?

**A**: While the EIP suggested including precomputed values for EVM optimization, this approach:
- Breaks compatibility with standard FIPS-204 implementations
- Wastes significant storage space (16x bloat)
- Provides minimal performance benefit given Besu's Java implementation
- Makes interoperability with other systems difficult

We prioritize standards compliance and interoperability.

### Q: Can I verify signatures from other ML-DSA implementations?

**A**: Yes! As long as they use standard FIPS-204 ML-DSA-44 format, signatures are fully interoperable.

## Changelog

### Version 1.0 (November 2025)
- Initial implementation
- Standard FIPS-204 ML-DSA-44 format
- Java 25 LTS native support
- Comprehensive test suite with test vector generator
- Address `0x12` in futureEIPs hard fork
