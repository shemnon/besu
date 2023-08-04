package org.hyperledger.besu.evmtool;

import static org.hyperledger.besu.evmtool.BenchmarkSubCommand.COMMAND_NAME;
import static picocli.CommandLine.ScopeType.INHERIT;

import org.hyperledger.besu.BesuInfo;
import org.hyperledger.besu.evm.EvmSpecVersion;
import org.hyperledger.besu.evmtool.benchmarks.AltBN128Benchmark;
import org.hyperledger.besu.evmtool.benchmarks.BenchmarkExecutor;
import org.hyperledger.besu.evmtool.benchmarks.ECRecoverBenchmark;
import org.hyperledger.besu.evmtool.benchmarks.ModExpBenchmark;
import org.hyperledger.besu.evmtool.benchmarks.Secp256k1Benchmark;

import java.io.PrintStream;
import java.util.EnumSet;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@CommandLine.Command(
    name = COMMAND_NAME,
    description = "Execute an Ethereum State Test.",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class)
public class BenchmarkSubCommand implements Runnable {
  public static final String COMMAND_NAME = "benchmark";
  private final PrintStream output;

  enum Benchmark {
    AltBn128(new AltBN128Benchmark()),
    //blake2f
    EcRecover(new ECRecoverBenchmark()),
    ModExp(new ModExpBenchmark()),
    Secp256k1(new Secp256k1Benchmark());

    final BenchmarkExecutor benchmarkExecutor;

    Benchmark(final BenchmarkExecutor benchmarkExecutor) {
      this.benchmarkExecutor = benchmarkExecutor;
    }
  }

  @Option(
      names = {"--native"},
      description = "Use the native libraries.",
      scope = INHERIT,
      negatable = true)
  Boolean nativeCode;

  @Option(
      names = {"--fork"},
      paramLabel = "<String>",
      description = "Fork to evaluate, when it impacts gas costing.")
  String fork = EvmSpecVersion.defaultVersion().getName();

  @Parameters EnumSet<Benchmark> benchmarks = EnumSet.noneOf(Benchmark.class);

  @ParentCommand EvmToolCommand parentCommand;

  public BenchmarkSubCommand() {
    // PicoCLI requires this
    this(System.out);
  }

  public BenchmarkSubCommand(final PrintStream output) {
    this.output = output;
  }

  @Override
  public void run() {
    System.out.println(BesuInfo.version());
    var benchmarksToRun = benchmarks.isEmpty() ? EnumSet.allOf(Benchmark.class) : benchmarks;
    for (var benchmark : benchmarksToRun) {
      System.out.println("Benchmarks for " + benchmark);
      benchmark.benchmarkExecutor.runBenchmark(output, nativeCode, fork);
    }
  }
}
