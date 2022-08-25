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
package org.hyperledger.besu.evm;

import org.hyperledger.besu.evm.gascalculator.BerlinGasCalculator;
import org.hyperledger.besu.evm.gascalculator.ByzantiumGasCalculator;
import org.hyperledger.besu.evm.gascalculator.ConstantinopleGasCalculator;
import org.hyperledger.besu.evm.gascalculator.FrontierGasCalculator;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;
import org.hyperledger.besu.evm.gascalculator.IstanbulGasCalculator;
import org.hyperledger.besu.evm.gascalculator.LondonGasCalculator;
import org.hyperledger.besu.evm.gascalculator.PetersburgGasCalculator;
import org.hyperledger.besu.evm.gascalculator.SpuriousDragonGasCalculator;
import org.hyperledger.besu.evm.gascalculator.TangerineWhistleGasCalculator;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.evm.operation.AddModOperation;
import org.hyperledger.besu.evm.operation.AddOperation;
import org.hyperledger.besu.evm.operation.AddressOperation;
import org.hyperledger.besu.evm.operation.AndOperation;
import org.hyperledger.besu.evm.operation.BalanceOperation;
import org.hyperledger.besu.evm.operation.BaseFeeOperation;
import org.hyperledger.besu.evm.operation.BlockHashOperation;
import org.hyperledger.besu.evm.operation.ByteOperation;
import org.hyperledger.besu.evm.operation.CallCodeOperation;
import org.hyperledger.besu.evm.operation.CallDataCopyOperation;
import org.hyperledger.besu.evm.operation.CallDataLoadOperation;
import org.hyperledger.besu.evm.operation.CallDataSizeOperation;
import org.hyperledger.besu.evm.operation.CallOperation;
import org.hyperledger.besu.evm.operation.CallValueOperation;
import org.hyperledger.besu.evm.operation.CallerOperation;
import org.hyperledger.besu.evm.operation.ChainIdOperation;
import org.hyperledger.besu.evm.operation.CodeCopyOperation;
import org.hyperledger.besu.evm.operation.CodeSizeOperation;
import org.hyperledger.besu.evm.operation.CoinbaseOperation;
import org.hyperledger.besu.evm.operation.Create2Operation;
import org.hyperledger.besu.evm.operation.CreateOperation;
import org.hyperledger.besu.evm.operation.DelegateCallOperation;
import org.hyperledger.besu.evm.operation.DifficultyOperation;
import org.hyperledger.besu.evm.operation.DivOperation;
import org.hyperledger.besu.evm.operation.DupOperation;
import org.hyperledger.besu.evm.operation.EqOperation;
import org.hyperledger.besu.evm.operation.ExpOperation;
import org.hyperledger.besu.evm.operation.ExtCodeCopyOperation;
import org.hyperledger.besu.evm.operation.ExtCodeHashOperation;
import org.hyperledger.besu.evm.operation.ExtCodeSizeOperation;
import org.hyperledger.besu.evm.operation.GasLimitOperation;
import org.hyperledger.besu.evm.operation.GasOperation;
import org.hyperledger.besu.evm.operation.GasPriceOperation;
import org.hyperledger.besu.evm.operation.GtOperation;
import org.hyperledger.besu.evm.operation.InvalidOperation;
import org.hyperledger.besu.evm.operation.IsZeroOperation;
import org.hyperledger.besu.evm.operation.JumpDestOperation;
import org.hyperledger.besu.evm.operation.JumpOperation;
import org.hyperledger.besu.evm.operation.JumpiOperation;
import org.hyperledger.besu.evm.operation.Keccak256Operation;
import org.hyperledger.besu.evm.operation.LogOperation;
import org.hyperledger.besu.evm.operation.LtOperation;
import org.hyperledger.besu.evm.operation.MLoadOperation;
import org.hyperledger.besu.evm.operation.MSizeOperation;
import org.hyperledger.besu.evm.operation.MStore8Operation;
import org.hyperledger.besu.evm.operation.MStoreOperation;
import org.hyperledger.besu.evm.operation.ModOperation;
import org.hyperledger.besu.evm.operation.MulModOperation;
import org.hyperledger.besu.evm.operation.MulOperation;
import org.hyperledger.besu.evm.operation.NotOperation;
import org.hyperledger.besu.evm.operation.NumberOperation;
import org.hyperledger.besu.evm.operation.Operation;
import org.hyperledger.besu.evm.operation.OrOperation;
import org.hyperledger.besu.evm.operation.OriginOperation;
import org.hyperledger.besu.evm.operation.PCOperation;
import org.hyperledger.besu.evm.operation.PopOperation;
import org.hyperledger.besu.evm.operation.PrevRanDaoOperation;
import org.hyperledger.besu.evm.operation.PushOperation;
import org.hyperledger.besu.evm.operation.ReturnDataCopyOperation;
import org.hyperledger.besu.evm.operation.ReturnDataSizeOperation;
import org.hyperledger.besu.evm.operation.ReturnOperation;
import org.hyperledger.besu.evm.operation.RevertOperation;
import org.hyperledger.besu.evm.operation.SDivOperation;
import org.hyperledger.besu.evm.operation.SGtOperation;
import org.hyperledger.besu.evm.operation.SLoadOperation;
import org.hyperledger.besu.evm.operation.SLtOperation;
import org.hyperledger.besu.evm.operation.SModOperation;
import org.hyperledger.besu.evm.operation.SStoreOperation;
import org.hyperledger.besu.evm.operation.SarOperation;
import org.hyperledger.besu.evm.operation.SelfBalanceOperation;
import org.hyperledger.besu.evm.operation.SelfDestructOperation;
import org.hyperledger.besu.evm.operation.ShlOperation;
import org.hyperledger.besu.evm.operation.ShrOperation;
import org.hyperledger.besu.evm.operation.SignExtendOperation;
import org.hyperledger.besu.evm.operation.StaticCallOperation;
import org.hyperledger.besu.evm.operation.StopOperation;
import org.hyperledger.besu.evm.operation.SubOperation;
import org.hyperledger.besu.evm.operation.SwapOperation;
import org.hyperledger.besu.evm.operation.TimestampOperation;
import org.hyperledger.besu.evm.operation.XorOperation;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

/** Provides EVMs supporting the appropriate operations for mainnet hard forks. */
public abstract class MainnetEVMs {

  public static final Optional<BigInteger> DEV_NET_CHAIN_ID = Optional.of(BigInteger.valueOf(1337));

  public static EVM frontier(final EvmConfiguration evmConfiguration) {
    return frontier(new FrontierGasCalculator(), evmConfiguration);
  }

  public static EVM frontier(
      final GasCalculator gasCalculator, final EvmConfiguration evmConfiguration) {
    return new EVM.Builder()
        .operationsSupplier(MainnetEVMs::frontierOperations)
        .gasCalculator(gasCalculator)
        .jumpDestCacheWeightBytes(evmConfiguration.getJumpDestCacheWeightBytes())
        .build();
  }

  public static List<Operation> frontierOperations(
      final GasCalculator gasCalculator, final Optional<BigInteger> chainId) {
    final List<Operation> ops = new ArrayList<>();
    ops.add(new MulOperation(gasCalculator));
    ops.add(new AddOperation(gasCalculator));
    ops.add(new SubOperation(gasCalculator));
    ops.add(new DivOperation(gasCalculator));
    ops.add(new SDivOperation(gasCalculator));
    ops.add(new ModOperation(gasCalculator));
    ops.add(new SModOperation(gasCalculator));
    ops.add(new ExpOperation(gasCalculator));
    ops.add(new AddModOperation(gasCalculator));
    ops.add(new MulModOperation(gasCalculator));
    ops.add(new SignExtendOperation(gasCalculator));
    ops.add(new LtOperation(gasCalculator));
    ops.add(new GtOperation(gasCalculator));
    ops.add(new SLtOperation(gasCalculator));
    ops.add(new SGtOperation(gasCalculator));
    ops.add(new EqOperation(gasCalculator));
    ops.add(new IsZeroOperation(gasCalculator));
    ops.add(new AndOperation(gasCalculator));
    ops.add(new OrOperation(gasCalculator));
    ops.add(new XorOperation(gasCalculator));
    ops.add(new NotOperation(gasCalculator));
    ops.add(new ByteOperation(gasCalculator));
    ops.add(new Keccak256Operation(gasCalculator));
    ops.add(new AddressOperation(gasCalculator));
    ops.add(new BalanceOperation(gasCalculator));
    ops.add(new OriginOperation(gasCalculator));
    ops.add(new CallerOperation(gasCalculator));
    ops.add(new CallValueOperation(gasCalculator));
    ops.add(new CallDataLoadOperation(gasCalculator));
    ops.add(new CallDataSizeOperation(gasCalculator));
    ops.add(new CallDataCopyOperation(gasCalculator));
    ops.add(new CodeSizeOperation(gasCalculator));
    ops.add(new CodeCopyOperation(gasCalculator));
    ops.add(new GasPriceOperation(gasCalculator));
    ops.add(new ExtCodeCopyOperation(gasCalculator));
    ops.add(new ExtCodeSizeOperation(gasCalculator));
    ops.add(new BlockHashOperation(gasCalculator));
    ops.add(new CoinbaseOperation(gasCalculator));
    ops.add(new TimestampOperation(gasCalculator));
    ops.add(new NumberOperation(gasCalculator));
    ops.add(new DifficultyOperation(gasCalculator));
    ops.add(new GasLimitOperation(gasCalculator));
    ops.add(new PopOperation(gasCalculator));
    ops.add(new MLoadOperation(gasCalculator));
    ops.add(new MStoreOperation(gasCalculator));
    ops.add(new MStore8Operation(gasCalculator));
    ops.add(new SLoadOperation(gasCalculator));
    ops.add(new SStoreOperation(gasCalculator, SStoreOperation.FRONTIER_MINIMUM));
    ops.add(new JumpOperation(gasCalculator));
    ops.add(new JumpiOperation(gasCalculator));
    ops.add(new PCOperation(gasCalculator));
    ops.add(new MSizeOperation(gasCalculator));
    ops.add(new GasOperation(gasCalculator));
    ops.add(new JumpDestOperation(gasCalculator));
    ops.add(new ReturnOperation(gasCalculator));
    ops.add(new InvalidOperation(gasCalculator));
    ops.add(new StopOperation(gasCalculator));
    ops.add(new SelfDestructOperation(gasCalculator));
    ops.add(new CreateOperation(gasCalculator));
    ops.add(new CallOperation(gasCalculator));
    ops.add(new CallCodeOperation(gasCalculator));

    // Register the PUSH1, PUSH2, ..., PUSH32 operations.
    for (int i = 1; i <= 32; ++i) {
      ops.add(new PushOperation(i, gasCalculator));
    }

    // Register the DUP1, DUP2, ..., DUP16 operations.
    for (int i = 1; i <= 16; ++i) {
      ops.add(new DupOperation(i, gasCalculator));
    }

    // Register the SWAP1, SWAP2, ..., SWAP16 operations.
    for (int i = 1; i <= 16; ++i) {
      ops.add(new SwapOperation(i, gasCalculator));
    }

    // Register the LOG0, LOG1, ..., LOG4 operations.
    for (int i = 0; i < 5; ++i) {
      ops.add(new LogOperation(i, gasCalculator));
    }
    return ops;
  }

  public static EVM homestead(final EvmConfiguration evmConfiguration) {
    return homestead(new FrontierGasCalculator(), evmConfiguration);
  }

  public static EVM homestead(
      final GasCalculator gasCalculator, final EvmConfiguration evmConfiguration) {
    return new EVM.Builder()
        .operationsSupplier(MainnetEVMs::homesteadOperations)
        .gasCalculator(gasCalculator)
        .jumpDestCacheWeightBytes(evmConfiguration.getJumpDestCacheWeightBytes())
        .build();
  }

  public static List<Operation> homesteadOperations(
      final GasCalculator gasCalculator, final Optional<BigInteger> chainId) {
    final List<Operation> ops = frontierOperations(gasCalculator, chainId);
    ops.add(new DelegateCallOperation(gasCalculator));
    return ops;
  }

  public static EVM spuriousDragon(final EvmConfiguration evmConfiguration) {
    return homestead(new SpuriousDragonGasCalculator(), evmConfiguration);
  }

  public static EVM tangerineWhistle(final EvmConfiguration evmConfiguration) {
    return homestead(new TangerineWhistleGasCalculator(), evmConfiguration);
  }

  public static EVM byzantium(final EvmConfiguration evmConfiguration) {
    return byzantium(new ByzantiumGasCalculator(), evmConfiguration);
  }

  public static EVM byzantium(
      final GasCalculator gasCalculator, final EvmConfiguration evmConfiguration) {
    return new EVM.Builder()
        .operationsSupplier(MainnetEVMs::byzantiumOperations)
        .gasCalculator(gasCalculator)
        .jumpDestCacheWeightBytes(evmConfiguration.getJumpDestCacheWeightBytes())
        .build();
  }

  public static List<Operation> byzantiumOperations(
      final GasCalculator gasCalculator, final Optional<BigInteger> chainId) {
    final List<Operation> ops = homesteadOperations(gasCalculator, chainId);
    ops.add(new ReturnDataCopyOperation(gasCalculator));
    ops.add(new ReturnDataSizeOperation(gasCalculator));
    ops.add(new RevertOperation(gasCalculator));
    ops.add(new StaticCallOperation(gasCalculator));
    return ops;
  }

  public static EVM constantinople(final EvmConfiguration evmConfiguration) {
    return constantinople(new ConstantinopleGasCalculator(), evmConfiguration);
  }

  public static EVM constantinople(
      final GasCalculator gasCalculator, final EvmConfiguration evmConfiguration) {
    return new EVM.Builder()
        .operationsSupplier(MainnetEVMs::constantinopleOperations)
        .gasCalculator(gasCalculator)
        .jumpDestCacheWeightBytes(evmConfiguration.getJumpDestCacheWeightBytes())
        .build();
  }

  public static List<Operation> constantinopleOperations(
      final GasCalculator gasCalculator, final Optional<BigInteger> chainId) {
    final List<Operation> ops = byzantiumOperations(gasCalculator, chainId);
    ops.add(new Create2Operation(gasCalculator));
    ops.add(new SarOperation(gasCalculator));
    ops.add(new ShlOperation(gasCalculator));
    ops.add(new ShrOperation(gasCalculator));
    ops.add(new ExtCodeHashOperation(gasCalculator));
    return ops;
  }

  public static EVM petersburg(final EvmConfiguration evmConfiguration) {
    return constantinople(new PetersburgGasCalculator(), evmConfiguration);
  }

  public static EVM istanbul(final EvmConfiguration evmConfiguration) {
    return istanbul(DEV_NET_CHAIN_ID, evmConfiguration);
  }

  public static EVM istanbul(
      final Optional<BigInteger> chainId, final EvmConfiguration evmConfiguration) {
    return istanbul(new IstanbulGasCalculator(), chainId, evmConfiguration);
  }

  public static EVM istanbul(
      final GasCalculator gasCalculator,
      final Optional<BigInteger> chainId,
      final EvmConfiguration evmConfiguration) {
    return new EVM.Builder()
        .chainId(chainId)
        .operationsSupplier(MainnetEVMs::istanbulOperations)
        .gasCalculator(gasCalculator)
        .jumpDestCacheWeightBytes(evmConfiguration.getJumpDestCacheWeightBytes())
        .build();
  }

  public static List<Operation> istanbulOperations(
      final GasCalculator gasCalculator, final Optional<BigInteger> chainId) {
    final List<Operation> ops = constantinopleOperations(gasCalculator, chainId);
    ops.add(
        new ChainIdOperation(
            gasCalculator,
            Bytes32.leftPad(Bytes.of(chainId.map(BigInteger::toByteArray).orElse(new byte[0])))));
    ops.add(new SelfBalanceOperation(gasCalculator));
    ops.add(new SStoreOperation(gasCalculator, SStoreOperation.EIP_1706_MINIMUM));
    return ops;
  }

  public static EVM berlin(final EvmConfiguration evmConfiguration) {
    return berlin(DEV_NET_CHAIN_ID, evmConfiguration);
  }

  public static EVM berlin(
      final Optional<BigInteger> chainId, final EvmConfiguration evmConfiguration) {
    return istanbul(new BerlinGasCalculator(), chainId, evmConfiguration);
  }

  public static EVM london(final EvmConfiguration evmConfiguration) {
    return london(DEV_NET_CHAIN_ID, evmConfiguration);
  }

  public static EVM london(
      final Optional<BigInteger> chainId, final EvmConfiguration evmConfiguration) {
    return london(new LondonGasCalculator(), chainId, evmConfiguration);
  }

  public static EVM london(
      final GasCalculator gasCalculator,
      final Optional<BigInteger> chainId,
      final EvmConfiguration evmConfiguration) {
    return new EVM.Builder()
        .chainId(chainId)
        .operationsSupplier(MainnetEVMs::londonOperations)
        .gasCalculator(gasCalculator)
        .jumpDestCacheWeightBytes(evmConfiguration.getJumpDestCacheWeightBytes())
        .build();
  }

  public static List<Operation> londonOperations(
      final GasCalculator gasCalculator, final Optional<BigInteger> chainId) {
    final List<Operation> ops = istanbulOperations(gasCalculator, chainId);
    ops.add(new BaseFeeOperation(gasCalculator));
    return ops;
  }

  public static EVM paris(
      final Optional<BigInteger> chainId, final EvmConfiguration evmConfiguration) {
    return paris(new LondonGasCalculator(), chainId, evmConfiguration);
  }

  public static EVM paris(
      final GasCalculator gasCalculator,
      final Optional<BigInteger> chainId,
      final EvmConfiguration evmConfiguration) {
    return new EVM.Builder()
        .chainId(chainId)
        .operationsSupplier(MainnetEVMs::parisOperations)
        .gasCalculator(gasCalculator)
        .jumpDestCacheWeightBytes(evmConfiguration.getJumpDestCacheWeightBytes())
        .build();
  }

  public static List<Operation> parisOperations(
      final GasCalculator gasCalculator, final Optional<BigInteger> chainId) {
    final List<Operation> ops = londonOperations(gasCalculator, chainId);
    ops.add(new PrevRanDaoOperation(gasCalculator));
    return ops;
  }
}
