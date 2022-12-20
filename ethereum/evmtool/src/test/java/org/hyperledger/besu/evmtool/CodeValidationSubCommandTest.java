package org.hyperledger.besu.evmtool;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;
import picocli.CommandLine;

public class CodeValidationSubCommandTest {

  static final String CODE_STOP_ONLY = "0xef0001 010001 020001 0001 030000 00 00000000 00";
  static final String CODE_RETF_ONLY = "0xef0001 010001 020001 0001 030000 00 00000000 b1";
  static final String CODE_BAD_MAGIC = "0xefffff 010001 020001 0001 030000 00 00000000 b1";
  static final String CODE_MULTIPLE =
      CODE_STOP_ONLY + "\n" + CODE_BAD_MAGIC + "\n" + CODE_RETF_ONLY + "\n";

  @Test
  public void testSingleValidViaInput() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final ByteArrayInputStream bais = new ByteArrayInputStream(CODE_STOP_ONLY.getBytes(UTF_8));
    final CodeValidateSubCommand codeValidateSubCommand =
        new CodeValidateSubCommand(bais, new PrintStream(baos));
    codeValidateSubCommand.run();
    assertThat(baos.toString(UTF_8)).contains("OK 00\n");
  }

  @Test
  public void testSingleInvalidViaInput() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final ByteArrayInputStream bais = new ByteArrayInputStream(CODE_BAD_MAGIC.getBytes(UTF_8));
    final CodeValidateSubCommand codeValidateSubCommand =
        new CodeValidateSubCommand(bais, new PrintStream(baos));
    codeValidateSubCommand.run();
    assertThat(baos.toString(UTF_8)).contains("err: layout - EOF header byte 1 incorrect\n");
  }

  @Test
  public void testMultipleViaInput() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final ByteArrayInputStream bais = new ByteArrayInputStream(CODE_MULTIPLE.getBytes(UTF_8));
    final CodeValidateSubCommand codeValidateSubCommand =
        new CodeValidateSubCommand(bais, new PrintStream(baos));
    codeValidateSubCommand.run();
    assertThat(baos.toString(UTF_8))
        .contains("OK 00\n" + "err: layout - EOF header byte 1 incorrect\n" + "OK b1\n");
  }

  @Test
  public void testSingleValidViaCli() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
    final CodeValidateSubCommand codeValidateSubCommand =
        new CodeValidateSubCommand(bais, new PrintStream(baos));
    final CommandLine cmd = new CommandLine(codeValidateSubCommand);
    cmd.parseArgs(CODE_STOP_ONLY);
    codeValidateSubCommand.run();
    assertThat(baos.toString(UTF_8)).contains("OK 00\n");
  }

  @Test
  public void testSingleInvalidViaCli() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
    final CodeValidateSubCommand codeValidateSubCommand =
        new CodeValidateSubCommand(bais, new PrintStream(baos));
    final CommandLine cmd = new CommandLine(codeValidateSubCommand);
    cmd.parseArgs(CODE_BAD_MAGIC);
    codeValidateSubCommand.run();
    assertThat(baos.toString(UTF_8)).contains("err: layout - EOF header byte 1 incorrect\n");
  }

  @Test
  public void testMultipleViaCli() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
    final CodeValidateSubCommand codeValidateSubCommand =
        new CodeValidateSubCommand(bais, new PrintStream(baos));
    final CommandLine cmd = new CommandLine(codeValidateSubCommand);
    cmd.parseArgs(CODE_STOP_ONLY, CODE_BAD_MAGIC, CODE_RETF_ONLY);
    codeValidateSubCommand.run();
    assertThat(baos.toString(UTF_8))
        .contains("OK 00\n" + "err: layout - EOF header byte 1 incorrect\n" + "OK b1\n");
  }

  @Test
  public void testCliEclipsesInput() {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final ByteArrayInputStream bais = new ByteArrayInputStream(CODE_STOP_ONLY.getBytes(UTF_8));
    final CodeValidateSubCommand codeValidateSubCommand =
        new CodeValidateSubCommand(bais, new PrintStream(baos));
    final CommandLine cmd = new CommandLine(codeValidateSubCommand);
    cmd.parseArgs(CODE_RETF_ONLY);
    codeValidateSubCommand.run();
    assertThat(baos.toString(UTF_8)).contains("OK b1\n");
  }
}
