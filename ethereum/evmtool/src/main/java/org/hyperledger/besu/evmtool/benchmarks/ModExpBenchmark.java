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
 */
package org.hyperledger.besu.evmtool.benchmarks;

import org.hyperledger.besu.evm.precompile.BigIntegerModularExponentiationPrecompiledContract;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.tuweni.bytes.Bytes;

/** Benchmark Modular Exponentiation */
public class ModExpBenchmark extends BenchmarkExecutor {

  /** Benchmark with default math warmups and iterations */
  public ModExpBenchmark() {
    super(MATH_WARMUP, MATH_ITERATIONS);
  }

  @Override
  public void runBenchmark(
      final PrintStream output, final Boolean attemptNative, final String fork) {
    final Map<String, Bytes> testcases = new LinkedHashMap<>();
    testcases.put(
        "eip_example1",
        Bytes.fromHexString(
            "0000000000000000000000000000000000000000000000000000000000000001"
                + "0000000000000000000000000000000000000000000000000000000000000020"
                + "0000000000000000000000000000000000000000000000000000000000000020"
                + "03"
                + "fffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2e"
                + "fffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2f"));
    testcases.put(
        "eip_example2",
        Bytes.fromHexString(
            "0000000000000000000000000000000000000000000000000000000000000000"
                + "0000000000000000000000000000000000000000000000000000000000000020"
                + "0000000000000000000000000000000000000000000000000000000000000020"
                + "fffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2e"
                + "fffffffffffffffffffffffffffffffffffffffffffffffffffffffefffffc2f"));
    testcases.put(
        "even-modulus-1",
        Bytes.fromHexString(
            "0000000000000000000000000000000000000000000000000000000000000003000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000003c2040160024518968061546464452029984405379963244433254832348165045983839181603600983245188119082741180552472823434126339186042653363989253518144015007104659260895861906540664936284389736130979465984263224400655133820675791338815252163142936101184074977707018635613728563507946963514079867296353079585444179075183175315953500862929638853495671020263166123912364061541662955981749424030252416330220564030645929402440479045589943839631409636357644094127401186503107365632233122371688639562179885062893961510897259834525401728081797411184820573804096446362924176080926052008070342876591822173915634645231182811305568049542955744148010693635112077769594038286419007643605441221736768092092706367136248982357930831050114862462054469804238839007660797444001134145988063087013657265067255538916788266246587774231646489345179567885975873995767749906187041937713976494258573504873269320274358396975658022816526738914029788471066595929777423497242252763504644221066495966982158816143636722736200299105017320500139589998102103536908651755056175006587609525845133254470473698921229697550145309121304642224273213418856615560961843245211567388930209839462064588566503446549415643465733541976019955544090933949692508519895293721888001835024288589127818157262958206047538648802179821901844091151603223229973835165715768557428338775055435040494382003663260090655173262271219880999454966135210800173588881197223814096778846598765822161982688788574265773201009190341430440400154076998270731076034021917712825351270109621323258567738829791704547404897555410847468529100600344699859551947494210268509149049419749744679372826553895949980386864014015247047775654329663958663391477680679444996526407026021137970100258833584773507489900747148344826790699325870056679710341571267983857125765234149331990149745571500943300684008078036054281629105618499442731182983775330888524074001975745730722737461725166625564316976464879780901853271273641654911641084141502464"));
    testcases.put(
        "even-modulus-2",
        Bytes.fromHexString(
            "000000000000000000000000000000000000000000000000000000000000001000000000000000000000000000000000000000000000000000000000000000d80000000000000000000000000000000000000000000000000000000000000010e0060000a921212121212121ff0000212b212121ffff1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f00feffff212121212121ffffffff1fe1e0e0e01e1f1f169f1f1f1f490afcefffffffffffffffff82828282828282828282828282828282828282828200ffff28ff2b212121ffff1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1f1fffffffffff0afceffffff7ffffffffff7c8282828282a1828282828282828282828282828200ffff28ff2b212121ffff1f1f1f1f1f1fd11f1f1f1f1f1f1f1f1f1f1fffffffffffffffff21212121212121fb2121212121ffff1f1f1f1f1f1f1f1fffaf82828282828200ffff28ff2b218282000000000000000000"));
    testcases.put(
        "nagydani-1-square",
        Bytes.fromHexString(
            "000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000040e09ad9675465c53a109fac66a445c91b292d2bb2c5268addb30cd82f80fcb0033ff97c80a5fc6f39193ae969c6ede6710a6b7ac27078a06d90ef1c72e5c85fb502fc9e1f6beb81516545975218075ec2af118cd8798df6e08a147c60fd6095ac2bb02c2908cf4dd7c81f11c289e4bce98f3553768f392a80ce22bf5c4f4a248c6b"));
    testcases.put(
        "nagydani-1-qube",
        Bytes.fromHexString(
            "000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000040e09ad9675465c53a109fac66a445c91b292d2bb2c5268addb30cd82f80fcb0033ff97c80a5fc6f39193ae969c6ede6710a6b7ac27078a06d90ef1c72e5c85fb503fc9e1f6beb81516545975218075ec2af118cd8798df6e08a147c60fd6095ac2bb02c2908cf4dd7c81f11c289e4bce98f3553768f392a80ce22bf5c4f4a248c6b"));
    testcases.put(
        "nagydani-1-pow0x10001",
        Bytes.fromHexString(
            "000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000030000000000000000000000000000000000000000000000000000000000000040e09ad9675465c53a109fac66a445c91b292d2bb2c5268addb30cd82f80fcb0033ff97c80a5fc6f39193ae969c6ede6710a6b7ac27078a06d90ef1c72e5c85fb5010001fc9e1f6beb81516545975218075ec2af118cd8798df6e08a147c60fd6095ac2bb02c2908cf4dd7c81f11c289e4bce98f3553768f392a80ce22bf5c4f4a248c6b"));
    testcases.put(
        "nagydani-2-square",
        Bytes.fromHexString(
            "000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000080cad7d991a00047dd54d3399b6b0b937c718abddef7917c75b6681f40cc15e2be0003657d8d4c34167b2f0bbbca0ccaa407c2a6a07d50f1517a8f22979ce12a81dcaf707cc0cebfc0ce2ee84ee7f77c38b9281b9822a8d3de62784c089c9b18dcb9a2a5eecbede90ea788a862a9ddd9d609c2c52972d63e289e28f6a590ffbf5102e6d893b80aeed5e6e9ce9afa8a5d5675c93a32ac05554cb20e9951b2c140e3ef4e433068cf0fb73bc9f33af1853f64aa27a0028cbf570d7ac9048eae5dc7b28c87c31e5810f1e7fa2cda6adf9f1076dbc1ec1238560071e7efc4e9565c49be9e7656951985860a558a754594115830bcdb421f741408346dd5997bb01c287087"));
    testcases.put(
        "nagydani-2-qube",
        Bytes.fromHexString(
            "000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000080cad7d991a00047dd54d3399b6b0b937c718abddef7917c75b6681f40cc15e2be0003657d8d4c34167b2f0bbbca0ccaa407c2a6a07d50f1517a8f22979ce12a81dcaf707cc0cebfc0ce2ee84ee7f77c38b9281b9822a8d3de62784c089c9b18dcb9a2a5eecbede90ea788a862a9ddd9d609c2c52972d63e289e28f6a590ffbf5103e6d893b80aeed5e6e9ce9afa8a5d5675c93a32ac05554cb20e9951b2c140e3ef4e433068cf0fb73bc9f33af1853f64aa27a0028cbf570d7ac9048eae5dc7b28c87c31e5810f1e7fa2cda6adf9f1076dbc1ec1238560071e7efc4e9565c49be9e7656951985860a558a754594115830bcdb421f741408346dd5997bb01c287087"));
    testcases.put(
        "nagydani-2-pow0x10001",
        Bytes.fromHexString(
            "000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000030000000000000000000000000000000000000000000000000000000000000080cad7d991a00047dd54d3399b6b0b937c718abddef7917c75b6681f40cc15e2be0003657d8d4c34167b2f0bbbca0ccaa407c2a6a07d50f1517a8f22979ce12a81dcaf707cc0cebfc0ce2ee84ee7f77c38b9281b9822a8d3de62784c089c9b18dcb9a2a5eecbede90ea788a862a9ddd9d609c2c52972d63e289e28f6a590ffbf51010001e6d893b80aeed5e6e9ce9afa8a5d5675c93a32ac05554cb20e9951b2c140e3ef4e433068cf0fb73bc9f33af1853f64aa27a0028cbf570d7ac9048eae5dc7b28c87c31e5810f1e7fa2cda6adf9f1076dbc1ec1238560071e7efc4e9565c49be9e7656951985860a558a754594115830bcdb421f741408346dd5997bb01c287087"));
    testcases.put(
        "nagydani-3-square",
        Bytes.fromHexString(
            "000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000100c9130579f243e12451760976261416413742bd7c91d39ae087f46794062b8c239f2a74abf3918605a0e046a7890e049475ba7fbb78f5de6490bd22a710cc04d30088179a919d86c2da62cf37f59d8f258d2310d94c24891be2d7eeafaa32a8cb4b0cfe5f475ed778f45907dc8916a73f03635f233f7a77a00a3ec9ca6761a5bbd558a2318ecd0caa1c5016691523e7e1fa267dd35e70c66e84380bdcf7c0582f540174e572c41f81e93da0b757dff0b0fe23eb03aa19af0bdec3afb474216febaacb8d0381e631802683182b0fe72c28392539850650b70509f54980241dc175191a35d967288b532a7a8223ce2440d010615f70df269501944d4ec16fe4a3cb02d7a85909174757835187cb52e71934e6c07ef43b4c46fc30bbcd0bc72913068267c54a4aabebb493922492820babdeb7dc9b1558fcf7bd82c37c82d3147e455b623ab0efa752fe0b3a67ca6e4d126639e645a0bf417568adbb2a6a4eef62fa1fa29b2a5a43bebea1f82193a7dd98eb483d09bb595af1fa9c97c7f41f5649d976aee3e5e59e2329b43b13bea228d4a93f16ba139ccb511de521ffe747aa2eca664f7c9e33da59075cc335afcd2bf3ae09765f01ab5a7c3e3938ec168b74724b5074247d200d9970382f683d6059b94dbc336603d1dfee714e4b447ac2fa1d99ecb4961da2854e03795ed758220312d101e1e3d87d5313a6d052aebde75110363d"));
    testcases.put(
        "nagydani-3-qube",
        Bytes.fromHexString(
            "000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000100c9130579f243e12451760976261416413742bd7c91d39ae087f46794062b8c239f2a74abf3918605a0e046a7890e049475ba7fbb78f5de6490bd22a710cc04d30088179a919d86c2da62cf37f59d8f258d2310d94c24891be2d7eeafaa32a8cb4b0cfe5f475ed778f45907dc8916a73f03635f233f7a77a00a3ec9ca6761a5bbd558a2318ecd0caa1c5016691523e7e1fa267dd35e70c66e84380bdcf7c0582f540174e572c41f81e93da0b757dff0b0fe23eb03aa19af0bdec3afb474216febaacb8d0381e631802683182b0fe72c28392539850650b70509f54980241dc175191a35d967288b532a7a8223ce2440d010615f70df269501944d4ec16fe4a3cb03d7a85909174757835187cb52e71934e6c07ef43b4c46fc30bbcd0bc72913068267c54a4aabebb493922492820babdeb7dc9b1558fcf7bd82c37c82d3147e455b623ab0efa752fe0b3a67ca6e4d126639e645a0bf417568adbb2a6a4eef62fa1fa29b2a5a43bebea1f82193a7dd98eb483d09bb595af1fa9c97c7f41f5649d976aee3e5e59e2329b43b13bea228d4a93f16ba139ccb511de521ffe747aa2eca664f7c9e33da59075cc335afcd2bf3ae09765f01ab5a7c3e3938ec168b74724b5074247d200d9970382f683d6059b94dbc336603d1dfee714e4b447ac2fa1d99ecb4961da2854e03795ed758220312d101e1e3d87d5313a6d052aebde75110363d"));
    testcases.put(
        "nagydani-3-pow0x10001",
        Bytes.fromHexString(
            "000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000000030000000000000000000000000000000000000000000000000000000000000100c9130579f243e12451760976261416413742bd7c91d39ae087f46794062b8c239f2a74abf3918605a0e046a7890e049475ba7fbb78f5de6490bd22a710cc04d30088179a919d86c2da62cf37f59d8f258d2310d94c24891be2d7eeafaa32a8cb4b0cfe5f475ed778f45907dc8916a73f03635f233f7a77a00a3ec9ca6761a5bbd558a2318ecd0caa1c5016691523e7e1fa267dd35e70c66e84380bdcf7c0582f540174e572c41f81e93da0b757dff0b0fe23eb03aa19af0bdec3afb474216febaacb8d0381e631802683182b0fe72c28392539850650b70509f54980241dc175191a35d967288b532a7a8223ce2440d010615f70df269501944d4ec16fe4a3cb010001d7a85909174757835187cb52e71934e6c07ef43b4c46fc30bbcd0bc72913068267c54a4aabebb493922492820babdeb7dc9b1558fcf7bd82c37c82d3147e455b623ab0efa752fe0b3a67ca6e4d126639e645a0bf417568adbb2a6a4eef62fa1fa29b2a5a43bebea1f82193a7dd98eb483d09bb595af1fa9c97c7f41f5649d976aee3e5e59e2329b43b13bea228d4a93f16ba139ccb511de521ffe747aa2eca664f7c9e33da59075cc335afcd2bf3ae09765f01ab5a7c3e3938ec168b74724b5074247d200d9970382f683d6059b94dbc336603d1dfee714e4b447ac2fa1d99ecb4961da2854e03795ed758220312d101e1e3d87d5313a6d052aebde75110363d"));
    testcases.put(
        "nagydani-4-square",
        Bytes.fromHexString(
            "000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000200db34d0e438249c0ed685c949cc28776a05094e1c48691dc3f2dca5fc3356d2a0663bd376e4712839917eb9a19c670407e2c377a2de385a3ff3b52104f7f1f4e0c7bf7717fb913896693dc5edbb65b760ef1b00e42e9d8f9af17352385e1cd742c9b006c0f669995cb0bb21d28c0aced2892267637b6470d8cee0ab27fc5d42658f6e88240c31d6774aa60a7ebd25cd48b56d0da11209f1928e61005c6eb709f3e8e0aaf8d9b10f7d7e296d772264dc76897ccdddadc91efa91c1903b7232a9e4c3b941917b99a3bc0c26497dedc897c25750af60237aa67934a26a2bc491db3dcc677491944bc1f51d3e5d76b8d846a62db03dedd61ff508f91a56d71028125035c3a44cbb041497c83bf3e4ae2a9613a401cc721c547a2afa3b16a2969933d3626ed6d8a7428648f74122fd3f2a02a20758f7f693892c8fd798b39abac01d18506c45e71432639e9f9505719ee822f62ccbf47f6850f096ff77b5afaf4be7d772025791717dbe5abf9b3f40cff7d7aab6f67e38f62faf510747276e20a42127e7500c444f9ed92baf65ade9e836845e39c4316d9dce5f8e2c8083e2c0acbb95296e05e51aab13b6b8f53f06c9c4276e12b0671133218cc3ea907da3bd9a367096d9202128d14846cc2e20d56fc8473ecb07cecbfb8086919f3971926e7045b853d85a69d026195c70f9f7a823536e2a8f4b3e12e94d9b53a934353451094b8102df3143a0057457d75e8c708b6337a6f5a4fd1a06727acf9fb93e2993c62f3378b37d56c85e7b1e00f0145ebf8e4095bd723166293c60b6ac1252291ef65823c9e040ddad14969b3b340a4ef714db093a587c37766d68b8d6b5016e741587e7e6bf7e763b44f0247e64bae30f994d248bfd20541a333e5b225ef6a61199e301738b1e688f70ec1d7fb892c183c95dc543c3e12adf8a5e8b9ca9d04f9445cced3ab256f29e998e69efaa633a7b60e1db5a867924ccab0a171d9d6e1098dfa15acde9553de599eaa56490c8f411e4985111f3d40bddfc5e301edb01547b01a886550a61158f7e2033c59707789bf7c854181d0c2e2a42a93cf09209747d7082e147eb8544de25c3eb14f2e35559ea0c0f5877f2f3fc92132c0ae9da4e45b2f6c866a224ea6d1f28c05320e287750fbc647368d41116e528014cc1852e5531d53e4af938374daba6cee4baa821ed07117253bb3601ddd00d59a3d7fb2ef1f5a2fbba7c429f0cf9a5b3462410fd833a69118f8be9c559b1000cc608fd877fb43f8e65c2d1302622b944462579056874b387208d90623fcdaf93920ca7a9e4ba64ea208758222ad868501cc2c345e2d3a5ea2a17e5069248138c8a79c0251185d29ee73e5afab5354769142d2bf0cb6712727aa6bf84a6245fcdae66e4938d84d1b9dd09a884818622080ff5f98942fb20acd7e0c916c2d5ea7ce6f7e173315384518f"));
    testcases.put(
        "nagydani-4-qube",
        Bytes.fromHexString(
            "000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000200db34d0e438249c0ed685c949cc28776a05094e1c48691dc3f2dca5fc3356d2a0663bd376e4712839917eb9a19c670407e2c377a2de385a3ff3b52104f7f1f4e0c7bf7717fb913896693dc5edbb65b760ef1b00e42e9d8f9af17352385e1cd742c9b006c0f669995cb0bb21d28c0aced2892267637b6470d8cee0ab27fc5d42658f6e88240c31d6774aa60a7ebd25cd48b56d0da11209f1928e61005c6eb709f3e8e0aaf8d9b10f7d7e296d772264dc76897ccdddadc91efa91c1903b7232a9e4c3b941917b99a3bc0c26497dedc897c25750af60237aa67934a26a2bc491db3dcc677491944bc1f51d3e5d76b8d846a62db03dedd61ff508f91a56d71028125035c3a44cbb041497c83bf3e4ae2a9613a401cc721c547a2afa3b16a2969933d3626ed6d8a7428648f74122fd3f2a02a20758f7f693892c8fd798b39abac01d18506c45e71432639e9f9505719ee822f62ccbf47f6850f096ff77b5afaf4be7d772025791717dbe5abf9b3f40cff7d7aab6f67e38f62faf510747276e20a42127e7500c444f9ed92baf65ade9e836845e39c4316d9dce5f8e2c8083e2c0acbb95296e05e51aab13b6b8f53f06c9c4276e12b0671133218cc3ea907da3bd9a367096d9202128d14846cc2e20d56fc8473ecb07cecbfb8086919f3971926e7045b853d85a69d026195c70f9f7a823536e2a8f4b3e12e94d9b53a934353451094b8103df3143a0057457d75e8c708b6337a6f5a4fd1a06727acf9fb93e2993c62f3378b37d56c85e7b1e00f0145ebf8e4095bd723166293c60b6ac1252291ef65823c9e040ddad14969b3b340a4ef714db093a587c37766d68b8d6b5016e741587e7e6bf7e763b44f0247e64bae30f994d248bfd20541a333e5b225ef6a61199e301738b1e688f70ec1d7fb892c183c95dc543c3e12adf8a5e8b9ca9d04f9445cced3ab256f29e998e69efaa633a7b60e1db5a867924ccab0a171d9d6e1098dfa15acde9553de599eaa56490c8f411e4985111f3d40bddfc5e301edb01547b01a886550a61158f7e2033c59707789bf7c854181d0c2e2a42a93cf09209747d7082e147eb8544de25c3eb14f2e35559ea0c0f5877f2f3fc92132c0ae9da4e45b2f6c866a224ea6d1f28c05320e287750fbc647368d41116e528014cc1852e5531d53e4af938374daba6cee4baa821ed07117253bb3601ddd00d59a3d7fb2ef1f5a2fbba7c429f0cf9a5b3462410fd833a69118f8be9c559b1000cc608fd877fb43f8e65c2d1302622b944462579056874b387208d90623fcdaf93920ca7a9e4ba64ea208758222ad868501cc2c345e2d3a5ea2a17e5069248138c8a79c0251185d29ee73e5afab5354769142d2bf0cb6712727aa6bf84a6245fcdae66e4938d84d1b9dd09a884818622080ff5f98942fb20acd7e0c916c2d5ea7ce6f7e173315384518f"));
    testcases.put(
        "nagydani-4-pow0x10001",
        Bytes.fromHexString(
            "000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000030000000000000000000000000000000000000000000000000000000000000200db34d0e438249c0ed685c949cc28776a05094e1c48691dc3f2dca5fc3356d2a0663bd376e4712839917eb9a19c670407e2c377a2de385a3ff3b52104f7f1f4e0c7bf7717fb913896693dc5edbb65b760ef1b00e42e9d8f9af17352385e1cd742c9b006c0f669995cb0bb21d28c0aced2892267637b6470d8cee0ab27fc5d42658f6e88240c31d6774aa60a7ebd25cd48b56d0da11209f1928e61005c6eb709f3e8e0aaf8d9b10f7d7e296d772264dc76897ccdddadc91efa91c1903b7232a9e4c3b941917b99a3bc0c26497dedc897c25750af60237aa67934a26a2bc491db3dcc677491944bc1f51d3e5d76b8d846a62db03dedd61ff508f91a56d71028125035c3a44cbb041497c83bf3e4ae2a9613a401cc721c547a2afa3b16a2969933d3626ed6d8a7428648f74122fd3f2a02a20758f7f693892c8fd798b39abac01d18506c45e71432639e9f9505719ee822f62ccbf47f6850f096ff77b5afaf4be7d772025791717dbe5abf9b3f40cff7d7aab6f67e38f62faf510747276e20a42127e7500c444f9ed92baf65ade9e836845e39c4316d9dce5f8e2c8083e2c0acbb95296e05e51aab13b6b8f53f06c9c4276e12b0671133218cc3ea907da3bd9a367096d9202128d14846cc2e20d56fc8473ecb07cecbfb8086919f3971926e7045b853d85a69d026195c70f9f7a823536e2a8f4b3e12e94d9b53a934353451094b81010001df3143a0057457d75e8c708b6337a6f5a4fd1a06727acf9fb93e2993c62f3378b37d56c85e7b1e00f0145ebf8e4095bd723166293c60b6ac1252291ef65823c9e040ddad14969b3b340a4ef714db093a587c37766d68b8d6b5016e741587e7e6bf7e763b44f0247e64bae30f994d248bfd20541a333e5b225ef6a61199e301738b1e688f70ec1d7fb892c183c95dc543c3e12adf8a5e8b9ca9d04f9445cced3ab256f29e998e69efaa633a7b60e1db5a867924ccab0a171d9d6e1098dfa15acde9553de599eaa56490c8f411e4985111f3d40bddfc5e301edb01547b01a886550a61158f7e2033c59707789bf7c854181d0c2e2a42a93cf09209747d7082e147eb8544de25c3eb14f2e35559ea0c0f5877f2f3fc92132c0ae9da4e45b2f6c866a224ea6d1f28c05320e287750fbc647368d41116e528014cc1852e5531d53e4af938374daba6cee4baa821ed07117253bb3601ddd00d59a3d7fb2ef1f5a2fbba7c429f0cf9a5b3462410fd833a69118f8be9c559b1000cc608fd877fb43f8e65c2d1302622b944462579056874b387208d90623fcdaf93920ca7a9e4ba64ea208758222ad868501cc2c345e2d3a5ea2a17e5069248138c8a79c0251185d29ee73e5afab5354769142d2bf0cb6712727aa6bf84a6245fcdae66e4938d84d1b9dd09a884818622080ff5f98942fb20acd7e0c916c2d5ea7ce6f7e173315384518f"));
    testcases.put(
        "nagydani-5-square",
        Bytes.fromHexString(
            "000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000400c5a1611f8be90071a43db23cc2fe01871cc4c0e8ab5743f6378e4fef77f7f6db0095c0727e20225beb665645403453e325ad5f9aeb9ba99bf3c148f63f9c07cf4fe8847ad5242d6b7d4499f93bd47056ddab8f7dee878fc2314f344dbee2a7c41a5d3db91eff372c730c2fdd3a141a4b61999e36d549b9870cf2f4e632c4d5df5f024f81c028000073a0ed8847cfb0593d36a47142f578f05ccbe28c0c06aeb1b1da027794c48db880278f79ba78ae64eedfea3c07d10e0562668d839749dc95f40467d15cf65b9cfc52c7c4bcef1cda3596dd52631aac942f146c7cebd46065131699ce8385b0db1874336747ee020a5698a3d1a1082665721e769567f579830f9d259cec1a836845109c21cf6b25da572512bf3c42fd4b96e43895589042ab60dd41f497db96aec102087fe784165bb45f942859268fd2ff6c012d9d00c02ba83eace047cc5f7b2c392c2955c58a49f0338d6fc58749c9db2155522ac17914ec216ad87f12e0ee95574613942fa615898c4d9e8a3be68cd6afa4e7a003dedbdf8edfee31162b174f965b20ae752ad89c967b3068b6f722c16b354456ba8e280f987c08e0a52d40a2e8f3a59b94d590aeef01879eb7a90b3ee7d772c839c85519cbeaddc0c193ec4874a463b53fcaea3271d80ebfb39b33489365fc039ae549a17a9ff898eea2f4cb27b8dbee4c17b998438575b2b8d107e4a0d66ba7fca85b41a58a8d51f191a35c856dfbe8aef2b00048a694bbccff832d23c8ca7a7ff0b6c0b3011d00b97c86c0628444d267c951d9e4fb8f83e154b8f74fb51aa16535e498235c5597dac9606ed0be3173a3836baa4e7d756ffe1e2879b415d3846bccd538c05b847785699aefde3e305decb600cd8fb0e7d8de5efc26971a6ad4e6d7a2d91474f1023a0ac4b78dc937da0ce607a45974d2cac1c33a2631ff7fe6144a3b2e5cf98b531a9627dea92c1dc82204d09db0439b6a11dd64b484e1263aa45fd9539b6020b55e3baece3986a8bffc1003406348f5c61265099ed43a766ee4f93f5f9c5abbc32a0fd3ac2b35b87f9ec26037d88275bd7dd0a54474995ee34ed3727f3f97c48db544b1980193a4b76a8a3ddab3591ce527f16d91882e67f0103b5cda53f7da54d489fc4ac08b6ab358a5a04aa9daa16219d50bd672a7cb804ed769d218807544e5993f1c27427104b349906a0b654df0bf69328afd3013fbe430155339c39f236df5557bf92f1ded7ff609a8502f49064ec3d1dbfb6c15d3a4c11a4f8acd12278cbf68acd5709463d12e3338a6eddb8c112f199645e23154a8e60879d2a654e3ed9296aa28f134168619691cd2c6b9e2eba4438381676173fc63c2588a3c5910dc149cf3760f0aa9fa9c3f5faa9162b0bf1aac9dd32b706a60ef53cbdb394b6b40222b5bc80eea82ba8958386672564cae3794f977871ab62337cf02e30049201ec12937e7ce79d0f55d9c810e20acf52212aca1d3888949e0e4830aad88d804161230eb89d4d329cc83570fe257217d2119134048dd2ed167646975fc7d77136919a049ea74cf08ddd2b896890bb24a0ba18094a22baa351bf29ad96c66bbb1a598f2ca391749620e62d61c3561a7d3653ccc8892c7b99baaf76bf836e2991cb06d6bc0514568ff0d1ec8bb4b3d6984f5eaefb17d3ea2893722375d3ddb8e389a8eef7d7d198f8e687d6a513983df906099f9a2d23f4f9dec6f8ef2f11fc0a21fac45353b94e00486f5e17d386af42502d09db33cf0cf28310e049c07e88682aeeb00cb833c5174266e62407a57583f1f88b304b7c6e0c84bbe1c0fd423072d37a5bd0aacf764229e5c7cd02473460ba3645cd8e8ae144065bf02d0dd238593d8e230354f67e0b2f23012c23274f80e3ee31e35e2606a4a3f31d94ab755e6d163cff52cbb36b6d0cc67ffc512aeed1dce4d7a0d70ce82f2baba12e8d514dc92a056f994adfb17b5b9712bd5186f27a2fda1f7039c5df2c8587fdc62f5627580c13234b55be4df3056050e2d1ef3218f0dd66cb05265fe1acfb0989d8213f2c19d1735a7cf3fa65d88dad5af52dc2bba22b7abf46c3bc77b5091baab9e8f0ddc4d5e581037de91a9f8dcbc69309be29cc815cf19a20a7585b8b3073edf51fc9baeb3e509b97fa4ecfd621e0fd57bd61cac1b895c03248ff12bdbc57509250df3517e8a3fe1d776836b34ab352b973d932ef708b14f7418f9eceb1d87667e61e3e758649cb083f01b133d37ab2f5afa96d6c84bcacf4efc3851ad308c1e7d9113624fce29fab460ab9d2a48d92cdb281103a5250ad44cb2ff6e67ac670c02fdafb3e0f1353953d6d7d5646ca1568dea55275a050ec501b7c6250444f7219f1ba7521ba3b93d089727ca5f3bbe0d6c1300b423377004954c5628fdb65770b18ced5c9b23a4a5a6d6ef25fe01b4ce278de0bcc4ed86e28a0a68818ffa40970128cf2c38740e80037984428c1bd5113f40ff47512ee6f4e4d8f9b8e8e1b3040d2928d003bd1c1329dc885302fbce9fa81c23b4dc49c7c82d29b52957847898676c89aa5d32b5b0e1c0d5a2b79a19d67562f407f19425687971a957375879d90c5f57c857136c17106c9ab1b99d80e69c8c954ed386493368884b55c939b8d64d26f643e800c56f90c01079d7c534e3b2b7ae352cefd3016da55f6a85eb803b85e2304915fd2001f77c74e28746293c46e4f5f0fd49cf988aafd0026b8e7a3bab2da5cdce1ea26c2e29ec03f4807fac432662b2d6c060be1c7be0e5489de69d0a6e03a4b9117f9244b34a0f1ecba89884f781c6320412413a00c4980287409a2a78c2cd7e65cecebbe4ec1c28cac4dd95f6998e78fc6f1392384331c9436aa10e10e2bf8ad2c4eafbcf276aa7bae64b74428911b3269c749338b0fc5075ad"));
    testcases.put(
        "nagydani-5-qube",
        Bytes.fromHexString(
            "000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000000000000000400c5a1611f8be90071a43db23cc2fe01871cc4c0e8ab5743f6378e4fef77f7f6db0095c0727e20225beb665645403453e325ad5f9aeb9ba99bf3c148f63f9c07cf4fe8847ad5242d6b7d4499f93bd47056ddab8f7dee878fc2314f344dbee2a7c41a5d3db91eff372c730c2fdd3a141a4b61999e36d549b9870cf2f4e632c4d5df5f024f81c028000073a0ed8847cfb0593d36a47142f578f05ccbe28c0c06aeb1b1da027794c48db880278f79ba78ae64eedfea3c07d10e0562668d839749dc95f40467d15cf65b9cfc52c7c4bcef1cda3596dd52631aac942f146c7cebd46065131699ce8385b0db1874336747ee020a5698a3d1a1082665721e769567f579830f9d259cec1a836845109c21cf6b25da572512bf3c42fd4b96e43895589042ab60dd41f497db96aec102087fe784165bb45f942859268fd2ff6c012d9d00c02ba83eace047cc5f7b2c392c2955c58a49f0338d6fc58749c9db2155522ac17914ec216ad87f12e0ee95574613942fa615898c4d9e8a3be68cd6afa4e7a003dedbdf8edfee31162b174f965b20ae752ad89c967b3068b6f722c16b354456ba8e280f987c08e0a52d40a2e8f3a59b94d590aeef01879eb7a90b3ee7d772c839c85519cbeaddc0c193ec4874a463b53fcaea3271d80ebfb39b33489365fc039ae549a17a9ff898eea2f4cb27b8dbee4c17b998438575b2b8d107e4a0d66ba7fca85b41a58a8d51f191a35c856dfbe8aef2b00048a694bbccff832d23c8ca7a7ff0b6c0b3011d00b97c86c0628444d267c951d9e4fb8f83e154b8f74fb51aa16535e498235c5597dac9606ed0be3173a3836baa4e7d756ffe1e2879b415d3846bccd538c05b847785699aefde3e305decb600cd8fb0e7d8de5efc26971a6ad4e6d7a2d91474f1023a0ac4b78dc937da0ce607a45974d2cac1c33a2631ff7fe6144a3b2e5cf98b531a9627dea92c1dc82204d09db0439b6a11dd64b484e1263aa45fd9539b6020b55e3baece3986a8bffc1003406348f5c61265099ed43a766ee4f93f5f9c5abbc32a0fd3ac2b35b87f9ec26037d88275bd7dd0a54474995ee34ed3727f3f97c48db544b1980193a4b76a8a3ddab3591ce527f16d91882e67f0103b5cda53f7da54d489fc4ac08b6ab358a5a04aa9daa16219d50bd672a7cb804ed769d218807544e5993f1c27427104b349906a0b654df0bf69328afd3013fbe430155339c39f236df5557bf92f1ded7ff609a8502f49064ec3d1dbfb6c15d3a4c11a4f8acd12278cbf68acd5709463d12e3338a6eddb8c112f199645e23154a8e60879d2a654e3ed9296aa28f134168619691cd2c6b9e2eba4438381676173fc63c2588a3c5910dc149cf3760f0aa9fa9c3f5faa9162b0bf1aac9dd32b706a60ef53cbdb394b6b40222b5bc80eea82ba8958386672564cae3794f977871ab62337cf03e30049201ec12937e7ce79d0f55d9c810e20acf52212aca1d3888949e0e4830aad88d804161230eb89d4d329cc83570fe257217d2119134048dd2ed167646975fc7d77136919a049ea74cf08ddd2b896890bb24a0ba18094a22baa351bf29ad96c66bbb1a598f2ca391749620e62d61c3561a7d3653ccc8892c7b99baaf76bf836e2991cb06d6bc0514568ff0d1ec8bb4b3d6984f5eaefb17d3ea2893722375d3ddb8e389a8eef7d7d198f8e687d6a513983df906099f9a2d23f4f9dec6f8ef2f11fc0a21fac45353b94e00486f5e17d386af42502d09db33cf0cf28310e049c07e88682aeeb00cb833c5174266e62407a57583f1f88b304b7c6e0c84bbe1c0fd423072d37a5bd0aacf764229e5c7cd02473460ba3645cd8e8ae144065bf02d0dd238593d8e230354f67e0b2f23012c23274f80e3ee31e35e2606a4a3f31d94ab755e6d163cff52cbb36b6d0cc67ffc512aeed1dce4d7a0d70ce82f2baba12e8d514dc92a056f994adfb17b5b9712bd5186f27a2fda1f7039c5df2c8587fdc62f5627580c13234b55be4df3056050e2d1ef3218f0dd66cb05265fe1acfb0989d8213f2c19d1735a7cf3fa65d88dad5af52dc2bba22b7abf46c3bc77b5091baab9e8f0ddc4d5e581037de91a9f8dcbc69309be29cc815cf19a20a7585b8b3073edf51fc9baeb3e509b97fa4ecfd621e0fd57bd61cac1b895c03248ff12bdbc57509250df3517e8a3fe1d776836b34ab352b973d932ef708b14f7418f9eceb1d87667e61e3e758649cb083f01b133d37ab2f5afa96d6c84bcacf4efc3851ad308c1e7d9113624fce29fab460ab9d2a48d92cdb281103a5250ad44cb2ff6e67ac670c02fdafb3e0f1353953d6d7d5646ca1568dea55275a050ec501b7c6250444f7219f1ba7521ba3b93d089727ca5f3bbe0d6c1300b423377004954c5628fdb65770b18ced5c9b23a4a5a6d6ef25fe01b4ce278de0bcc4ed86e28a0a68818ffa40970128cf2c38740e80037984428c1bd5113f40ff47512ee6f4e4d8f9b8e8e1b3040d2928d003bd1c1329dc885302fbce9fa81c23b4dc49c7c82d29b52957847898676c89aa5d32b5b0e1c0d5a2b79a19d67562f407f19425687971a957375879d90c5f57c857136c17106c9ab1b99d80e69c8c954ed386493368884b55c939b8d64d26f643e800c56f90c01079d7c534e3b2b7ae352cefd3016da55f6a85eb803b85e2304915fd2001f77c74e28746293c46e4f5f0fd49cf988aafd0026b8e7a3bab2da5cdce1ea26c2e29ec03f4807fac432662b2d6c060be1c7be0e5489de69d0a6e03a4b9117f9244b34a0f1ecba89884f781c6320412413a00c4980287409a2a78c2cd7e65cecebbe4ec1c28cac4dd95f6998e78fc6f1392384331c9436aa10e10e2bf8ad2c4eafbcf276aa7bae64b74428911b3269c749338b0fc5075ad"));
    testcases.put(
        "nagydani-5-pow0x10001",
        Bytes.fromHexString(
            "000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000000030000000000000000000000000000000000000000000000000000000000000400c5a1611f8be90071a43db23cc2fe01871cc4c0e8ab5743f6378e4fef77f7f6db0095c0727e20225beb665645403453e325ad5f9aeb9ba99bf3c148f63f9c07cf4fe8847ad5242d6b7d4499f93bd47056ddab8f7dee878fc2314f344dbee2a7c41a5d3db91eff372c730c2fdd3a141a4b61999e36d549b9870cf2f4e632c4d5df5f024f81c028000073a0ed8847cfb0593d36a47142f578f05ccbe28c0c06aeb1b1da027794c48db880278f79ba78ae64eedfea3c07d10e0562668d839749dc95f40467d15cf65b9cfc52c7c4bcef1cda3596dd52631aac942f146c7cebd46065131699ce8385b0db1874336747ee020a5698a3d1a1082665721e769567f579830f9d259cec1a836845109c21cf6b25da572512bf3c42fd4b96e43895589042ab60dd41f497db96aec102087fe784165bb45f942859268fd2ff6c012d9d00c02ba83eace047cc5f7b2c392c2955c58a49f0338d6fc58749c9db2155522ac17914ec216ad87f12e0ee95574613942fa615898c4d9e8a3be68cd6afa4e7a003dedbdf8edfee31162b174f965b20ae752ad89c967b3068b6f722c16b354456ba8e280f987c08e0a52d40a2e8f3a59b94d590aeef01879eb7a90b3ee7d772c839c85519cbeaddc0c193ec4874a463b53fcaea3271d80ebfb39b33489365fc039ae549a17a9ff898eea2f4cb27b8dbee4c17b998438575b2b8d107e4a0d66ba7fca85b41a58a8d51f191a35c856dfbe8aef2b00048a694bbccff832d23c8ca7a7ff0b6c0b3011d00b97c86c0628444d267c951d9e4fb8f83e154b8f74fb51aa16535e498235c5597dac9606ed0be3173a3836baa4e7d756ffe1e2879b415d3846bccd538c05b847785699aefde3e305decb600cd8fb0e7d8de5efc26971a6ad4e6d7a2d91474f1023a0ac4b78dc937da0ce607a45974d2cac1c33a2631ff7fe6144a3b2e5cf98b531a9627dea92c1dc82204d09db0439b6a11dd64b484e1263aa45fd9539b6020b55e3baece3986a8bffc1003406348f5c61265099ed43a766ee4f93f5f9c5abbc32a0fd3ac2b35b87f9ec26037d88275bd7dd0a54474995ee34ed3727f3f97c48db544b1980193a4b76a8a3ddab3591ce527f16d91882e67f0103b5cda53f7da54d489fc4ac08b6ab358a5a04aa9daa16219d50bd672a7cb804ed769d218807544e5993f1c27427104b349906a0b654df0bf69328afd3013fbe430155339c39f236df5557bf92f1ded7ff609a8502f49064ec3d1dbfb6c15d3a4c11a4f8acd12278cbf68acd5709463d12e3338a6eddb8c112f199645e23154a8e60879d2a654e3ed9296aa28f134168619691cd2c6b9e2eba4438381676173fc63c2588a3c5910dc149cf3760f0aa9fa9c3f5faa9162b0bf1aac9dd32b706a60ef53cbdb394b6b40222b5bc80eea82ba8958386672564cae3794f977871ab62337cf010001e30049201ec12937e7ce79d0f55d9c810e20acf52212aca1d3888949e0e4830aad88d804161230eb89d4d329cc83570fe257217d2119134048dd2ed167646975fc7d77136919a049ea74cf08ddd2b896890bb24a0ba18094a22baa351bf29ad96c66bbb1a598f2ca391749620e62d61c3561a7d3653ccc8892c7b99baaf76bf836e2991cb06d6bc0514568ff0d1ec8bb4b3d6984f5eaefb17d3ea2893722375d3ddb8e389a8eef7d7d198f8e687d6a513983df906099f9a2d23f4f9dec6f8ef2f11fc0a21fac45353b94e00486f5e17d386af42502d09db33cf0cf28310e049c07e88682aeeb00cb833c5174266e62407a57583f1f88b304b7c6e0c84bbe1c0fd423072d37a5bd0aacf764229e5c7cd02473460ba3645cd8e8ae144065bf02d0dd238593d8e230354f67e0b2f23012c23274f80e3ee31e35e2606a4a3f31d94ab755e6d163cff52cbb36b6d0cc67ffc512aeed1dce4d7a0d70ce82f2baba12e8d514dc92a056f994adfb17b5b9712bd5186f27a2fda1f7039c5df2c8587fdc62f5627580c13234b55be4df3056050e2d1ef3218f0dd66cb05265fe1acfb0989d8213f2c19d1735a7cf3fa65d88dad5af52dc2bba22b7abf46c3bc77b5091baab9e8f0ddc4d5e581037de91a9f8dcbc69309be29cc815cf19a20a7585b8b3073edf51fc9baeb3e509b97fa4ecfd621e0fd57bd61cac1b895c03248ff12bdbc57509250df3517e8a3fe1d776836b34ab352b973d932ef708b14f7418f9eceb1d87667e61e3e758649cb083f01b133d37ab2f5afa96d6c84bcacf4efc3851ad308c1e7d9113624fce29fab460ab9d2a48d92cdb281103a5250ad44cb2ff6e67ac670c02fdafb3e0f1353953d6d7d5646ca1568dea55275a050ec501b7c6250444f7219f1ba7521ba3b93d089727ca5f3bbe0d6c1300b423377004954c5628fdb65770b18ced5c9b23a4a5a6d6ef25fe01b4ce278de0bcc4ed86e28a0a68818ffa40970128cf2c38740e80037984428c1bd5113f40ff47512ee6f4e4d8f9b8e8e1b3040d2928d003bd1c1329dc885302fbce9fa81c23b4dc49c7c82d29b52957847898676c89aa5d32b5b0e1c0d5a2b79a19d67562f407f19425687971a957375879d90c5f57c857136c17106c9ab1b99d80e69c8c954ed386493368884b55c939b8d64d26f643e800c56f90c01079d7c534e3b2b7ae352cefd3016da55f6a85eb803b85e2304915fd2001f77c74e28746293c46e4f5f0fd49cf988aafd0026b8e7a3bab2da5cdce1ea26c2e29ec03f4807fac432662b2d6c060be1c7be0e5489de69d0a6e03a4b9117f9244b34a0f1ecba89884f781c6320412413a00c4980287409a2a78c2cd7e65cecebbe4ec1c28cac4dd95f6998e78fc6f1392384331c9436aa10e10e2bf8ad2c4eafbcf276aa7bae64b74428911b3269c749338b0fc5075ad"));

    final BigIntegerModularExponentiationPrecompiledContract contract =
        new BigIntegerModularExponentiationPrecompiledContract(gasCalculatorForFork(fork));

    if (attemptNative != null
        && (!attemptNative
            || !BigIntegerModularExponentiationPrecompiledContract.maybeEnableNative())) {
      BigIntegerModularExponentiationPrecompiledContract.disableNative();
    }
    output.println(
        BigIntegerModularExponentiationPrecompiledContract.isNative()
            ? "Native ModExp"
            : "Java modExp");

    for (final Map.Entry<String, Bytes> testCase : testcases.entrySet()) {
      final double execTime = runPrecompileBenchmark(testCase.getValue(), contract);

      long gasCost = contract.gasRequirement(testCase.getValue());
      output.printf(
          "ModEXP %-22s %,6d gas @%,7.1f µs /%,8.1f MGps%n",
          testCase.getKey(), gasCost, execTime * 1_000_000, gasCost / execTime / 1_000_000);
    }
  }
}
