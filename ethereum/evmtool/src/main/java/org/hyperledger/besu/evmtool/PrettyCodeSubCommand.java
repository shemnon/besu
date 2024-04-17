package org.hyperledger.besu.evmtool;

/*
 * Copyright contributors to Hyperledger Besu
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
 *
 */
import static org.hyperledger.besu.evmtool.PrettyCodeSubCommand.COMMAND_NAME;

import org.hyperledger.besu.evm.Code;
import org.hyperledger.besu.evm.code.CodeFactory;
import org.hyperledger.besu.evm.code.CodeInvalid;
import org.hyperledger.besu.evm.code.CodeV1;
import org.hyperledger.besu.util.LogConfigurator;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import picocli.CommandLine;

@CommandLine.Command(
    name = COMMAND_NAME,
    description = "Pretty Prints EOF Code",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class)
public class PrettyCodeSubCommand implements Runnable {
  public static final String COMMAND_NAME = "pretty-print";
  @CommandLine.ParentCommand private final EvmToolCommand parentCommand;

  // picocli does it magically
  @CommandLine.Parameters private final List<String> codeList = new ArrayList<>();

  public PrettyCodeSubCommand() {
    this(null);
  }

  public PrettyCodeSubCommand(final EvmToolCommand parentCommand) {
    this.parentCommand = parentCommand;
  }

  @Override
  public void run() {
    LogConfigurator.setLevel("", "OFF");

    for (var hexCode : codeList) {
      Code code = CodeFactory.createCode(Bytes.fromHexString(hexCode), 1);
      if (code instanceof CodeInvalid codeInvalid) {
        parentCommand.out.println("EOF code is invalid - " + codeInvalid.getInvalidReason());
      } else if (code instanceof CodeV1 codev1) {
        codev1.prettyPrint(parentCommand.out);
      } else {
        parentCommand.out.println(
            "Pretty printing of legacy EVM is not supported. Patches welcome!");
      }
    }
  }
}
