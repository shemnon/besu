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
package org.hyperledger.besu.ethereum.mainnet.precompiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.hyperledger.besu.evm.gascalculator.SpuriousDragonGasCalculator;
import org.hyperledger.besu.evm.frame.MessageFrame;
import org.hyperledger.besu.evm.precompile.ECRECPrecompiledContract;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ECRECPrecompiledContractTest {

  private final ECRECPrecompiledContract contract =
      new ECRECPrecompiledContract(new SpuriousDragonGasCalculator());

  public ECRECPrecompiledContractTest() {}

  private final MessageFrame messageFrame = mock(MessageFrame.class);

  @Parameters
  public static Object[][] parameters() {
    return new Object[][] {
      {
        "acb1c19ac0832320815b5e886c6b73ad7d6177853d44b026f2a7a9e11bb899fc000000000000000000000000000000000000000000000000000000000000001c89ea49159b334f9aebbf54481b69d000d285baa341899db355a4030f6838394e540e9f9fa17bef441e32d98d5f4554cfefdc6a56101352e4b92efafd0d9646e8",
        null
      },
      {
        "0x0049872459827432342344987245982743234234498724598274323423429943000000000000000000000000000000000000000000000000000000000000001be8359c341771db7f9ea3a662a1741d27775ce277961470028e054ed3285aab8e31f63eaac35c4e6178abbc2a1073040ac9bbb0b67f2bc89a2e9593ba9abe8c53",
        "0x0c65a9d9ffc02c7c99e36e32ce0f950c7804ceda"
      },
      {
        "0x82f3df49d3645876de6313df2bbe9fbce593f21341a7b03acdb9423bc171fcc9000000000000000000000000000000000000000000000000000000000000001cba13918f50da910f2d55a7ea64cf716ba31dad91856f45908dde900530377d8a112d60f36900d18eb8f9d3b4f85a697b545085614509e3520e4b762e35d0d6bd",
        "0xc6e93f4c1920eaeaa1e699f76a7a8c18e3056074"
      },
      {
        "0x0fcdd8f8c550589cbae6183bc40713beb8d11898a201d13d6d5e40bc9ebf221d000000000000000000000000000000000000000000000000000000000000001c3824317158d005cbe49614fa05798ea00f2ca9db302a5e92d55bcaecd33d33da3c7de48ebec95be7b5111a7812febed1421f839d4d480c98501b78666aefdcd3",
        "0xfe26206ad0a5897a478dd046c56164553adaea20"
      },
      {
        "0xeb9a6731fa269c24c2535aa00a4b31d7117a2791188120c71aacd97664d1cc16000000000000000000000000000000000000000000000000000000000000001c707c68dd904de055d735f9e5c4dfba46296ad38ff6ba8f0e5e3f4ae83243b84a5b1920d628abc74e4f3a09449011a9664ab9885f74fdc7628d417599207bde74",
        "0x39c0f4fbcd41581d5b440a7c9f964b903037e09e"
      },
      {
        "0xf3ae1d9176371dd31accd73bb6bbaee561a041f5ac291a548880e1abe7b19e38000000000000000000000000000000000000000000000000000000000000001b116bd86d971b70ed540dc7c13756a99ff17644ed433781a3ffcc7359541d02fc4a2358ffc21682ece870e633cc8537f04be4ff75a142ecd35a951fc95fd1de57",
        "0x64849cfbf0353f2c80e2a1e558982f7c4738d9f1"
      },
      {
        "0x69567625d007e5c915eadb8b768f008227d57f4951f9d5711dfc72a8af13d35f000000000000000000000000000000000000000000000000000000000000001ba3ef328e9f95b4a28d9b8c20f3e9fc939bf94ad3611b4e1aaabb442f1c40545f6f83fe2c067d7a35f9bcb655894d15cd6d95d5e05c9fad33b2efa67592c0a4fe",
        "0x5026d2655bc28bee480b2f31934f0970e8acc647"
      },
      {
        "0x5ecdd449eb48c111c596d6b41047547deb4bbb27eafa419d1d041d842f1d81cb000000000000000000000000000000000000000000000000000000000000001c7ee309958bcd2f7a8d0fa904f16358d7b1dd52dc653f62b0eba42eec74335a620bf25c9db104a22c77af694cdb56e115fe259eef0a5e7968698bad0a592f6a2d",
        "0x24bf97810c294811aa8165c9a05717a7731342a0"
      },
      {
        "0x9a996c7083b19106163161ad5117a11703507ac61cad659c5bb164cd9d368672000000000000000000000000000000000000000000000000000000000000001bffc032f93b534eec6f76d194821f9fa70de06f0aac391e7f105401b23ab1a23071b55820c93857ed7e079e72f65798b2d5afad4f999048c1a987152bc5dbf78a",
        "0x9bdeac37b1bf31980c6e8cbc5bdfa38dacbc4c43"
      },
      {
        "0xea3305a892449d8215a5c41c619a42aeef22f888ee1b2c370584a6eb9e16fa27000000000000000000000000000000000000000000000000000000000000001b8efcc5180b219820d57856f018a7de24b0c4c633dfae40a96a5110c4f94b947151f4320da2e79e2d5e5c0efaef659b60b7d3de1debb4e37853746395f9426f2b",
        "0x9ded673711119881765005a33ef7bb1961bfe98d"
      },
      {
        "0xebb2b43c1070b6000aaa866071de173a24dbdec57443b9467c1aa11c0090d20c000000000000000000000000000000000000000000000000000000000000001bb52ebd3ee958d6487d7a6d68faa5df32e8f34a4703201feb6ea7687c1abf5b28439215bef624ef3799919da5980f3793f6aa14fd6dc43df3c5c652a4e2f6149b",
        "0xd4bbacccff27e6e8534d14cd8fcd665d455dddf2"
      },
      {
        "0xb28e4662bf41c1043e06400119c6c7804dcea81b1e4e8555c9db5be41380b1d3000000000000000000000000000000000000000000000000000000000000001ca010458d4c545fca2831e8ee9eb7857955d083feba0080c0b58a1b94233650572bfb5978a642da7b7f255562c47404ee3ad236d9f0319413f2e5a69cfafec2cb",
        "0x5b73664bd5ef270f91775d27f47e436d960aa853"
      },
      {
        "0x694bace9fe95c0370ef0eb3c0c629186616e33469972ff6a7ecce90f3fe2ab2d000000000000000000000000000000000000000000000000000000000000001ca07e9497d29f7c9adae37b9a985c876b5b3ee6d21a9bc83cfcf27531cb5eb67879f317e2c145caaa798bb1b30fd96f32d843636559f195622f2fac69297f935a",
        "0x200d8d10eab444331cb2a0802219771c11443205"
      },
      {
        "0xc3964b44adfd407ce69ccae93c033b6418ab903c674e794681baadf3173e37c1000000000000000000000000000000000000000000000000000000000000001c28e8ff8b5d9e55740da1ab2f7cfb9b527509439b2a99117d40a2158501a5b4354cdda2112aa6a7e89019556011f50abec5c3ae48501a1594b831275a2f031999",
        "0x4135437a672c7d48fefc4be43a26783fe030e9b7"
      },
      {
        "0x0d56ee3eb6ecd6a949793c8afaa905550333dc9063bc58cdcace8a146f23b2af000000000000000000000000000000000000000000000000000000000000001cebdb65ebfd20803c364d393e4df95a3631d906d3ebeb7fae31bc5bdbf9297e6c5d4e7a93d40278c1c3ddd4659c58a0eeb01fa41a3ca78fdb184b4bf7b2e34af1",
        "0xff2726d26e39daf03ae70b7f4cce693329e3a027"
      },
      {
        "0x23fa3e10e5b041840a8bd9055e9f6aca4c9764b6b21658bd6f126e6cf821e963000000000000000000000000000000000000000000000000000000000000001c19552a6e36c2c2c8fa97534c8e0a0575420364acd73f9399a6a375cb6ab8685d619a8afcdc09d88344dce929d90eed48f9caef102acb28c632340386d168cd14",
        "0x6c36670046a1f02fe217d95c5e78f5c84efd703e"
      },
      {
        "0xe4e5120a2c41c1beac3a04fe7895fb93db7e53e33a4f279cf196ac8d12e4eccc000000000000000000000000000000000000000000000000000000000000001bc113be6e205a4851f4aa56b499aee3fe75389382fb7b4da2eb894537addf20980dac9e071a562749cce0ea9da2f4cac450509ec03ebcf23898c52b3375c29169",
        "0x52d2883c39100d09a6b8d793e0f693fd7f2bac51"
      },
      {
        "0x7b4ba1f9fd096f738c26df77a1a87a87153d5eb253a4622cc41ca1e57f678370000000000000000000000000000000000000000000000000000000000000001bbdb9a84a3eaea6f3a16bb084387c1dd1bf91c0be1300cc99ece1cf8d239d7e0d37f24155f143635a761427684207ffabfa00dda09d502056768a4e57e0f336c5",
        "0xc65a2a9ef32f747865a119d1c044ad836a8f6d73"
      },
      {
        "0x4c16d10f1bfbaa34814a39d017cdcc18590d08a52ee01be38b43742cf0f76ee9000000000000000000000000000000000000000000000000000000000000001c75d276f5646653753fdcec2ab92d27e58a59c614aab6cc3568834a4ef36819bd54bcaf3c605ed948a20e56cfe58c1595d727877b0bfa9b9b6553879ba0e08a2b",
        "0x5cfe5b8bf54dee364ef6f411aac9bbb6eb051202"
      },
      {
        "0x8e4ae0eb9cb54e2378b3d161eeedc36654269cd1bef02d68751cc1b0021b709f000000000000000000000000000000000000000000000000000000000000001c01f53df1ac64d831f7cb45bcf9037052162900e3d7fb4d62934112a8c4b257d50c0c36cc064d72ba8a3ba57b1d2eee88b0628907dfe2cf0088dbf69d89c2f1c7",
        "0xf34405682c4deb55da399822373b632cf5ca25c0"
      },
      {
        "0xb755e67b6ba34376ac93bc8f81b072df191aed5c5e1bc7131cf1556731fad13e000000000000000000000000000000000000000000000000000000000000001b61b542768763d72e298e93cf8635ac430c2155835c079eb508e47d0cff5fb1fa62fb0cf03fd42a35bd6ade27fd10ba4e22975a61c259e1e71664892cb9f89f6c",
        "0x617c0e2ac02fd3e876fd5bd8581e73937df53c5d"
      },
      {
        "0x361a00e41d8e97cb935320dad3ba8b071f198e4c2424a2dbe110ab59a9a74511000000000000000000000000000000000000000000000000000000000000001c703cb0a9ee3a332e5b53664ee0495a757fbf1c168971a618f0010a894e8f00152d0fd7a61bbcfde075675e868593ff695298cb1dd7640ebdccd2864bd4f5fd71",
        "0x2b43183e036afb83fed9ad70f1bfadda5497dbba"
      },
      {
        "0xff94a2c7dbfbb566784a5d0df05c461f050dbc826f622940ca96ea949f9e1a87000000000000000000000000000000000000000000000000000000000000001b66489a58f64e058f150ffdb7e22a0993f4299073fc2b0840b082850138990fb54fcaca68705d1108a555ed4733a959c8e302510098c80b5e3c9353b1a86a64de",
        "0x81b6898913b2d49aec6e0bbcc8c192cff4d0eed8"
      },
      {
        "0xf655a5caf4dba704f63f5e17e5a94535ea7d277f2b8f4d69951291319f5c2c1b000000000000000000000000000000000000000000000000000000000000001b66987335ff22966a6084e88a4dbdc96f2cfb126aba2f6a477c18bf0fb3c42f2a5f1ef94acf1184da542f3f7271032a931bfcf3afeb3a9dafca95d5aff68bdee5",
        "0xb48bc9d6546fb5156954699062774443a7676986"
      },
      {
        "0x19e7a1205c185da64a45bb5e04f35c54cd092b70bb1a1527f8372f1ae67b4bb7000000000000000000000000000000000000000000000000000000000000001becde2323e521681126ea23af56953a18166158919d3fa445e93fcf957f60185a4738fce3d006827c432e5f6c1b8d7e3450fb434172b6ab4d619fe49a09ed4b68",
        "0x13cb19ae4e86177abfcc9d44139dcebb993ebdc1"
      },
      {
        "0x42d1878efb373fb18df168ce9687fae3cf8c1afa3063fb15b44f309adae25894000000000000000000000000000000000000000000000000000000000000001b15439bd45f426bdab38723290e9691371196e2105bee21c9dc36f77b3797fb6849f66ca42c6a4d5dde4b2996f68082c930fa6466e16077b074a63551d0a36bd4",
        "0x6d78fd01abb065be75ffeef89a9df8f87926daf1"
      },
      {
        "0x91f5c73a9e6b54b896940478ac0995d6d757feb38790cfeeaa0ca0c4c0c72db7000000000000000000000000000000000000000000000000000000000000001b5d26a45936c8b69117451625649c356a7a40627635f60e038313afd5a9e8082323aa5968522ea56d32beff94e628e36ebc0ceb68cf89f7141175b3dca19e2446",
        "0xd213383f34edd2a9d6e89dc6ed2bdedb2b875a3f"
      },
      {
        "0x654643af5d8045129adcbd4e2fe3a0270eae3072765e952ae1a8d239905f1848000000000000000000000000000000000000000000000000000000000000001cb3fdc5124a4193b35ba4eee7a3a764ea3ef14b0bacc61ce0a32e52258c64a31b2f66dc6529567230fc420f48e650a589146a409b85cb7b7d1d25cd7782198873",
        "0x0bcfc1ef1f89de5d6bcd354d797faf3047ace632"
      },
      {
        "0xa3dcb3d6e954a0da9e57fef6322a489935535fb892eb60fc2aee0153207441f2000000000000000000000000000000000000000000000000000000000000001c58d31b5c643c2e2376e5420c250ab85ac58a730523449998d08c55ce7ef1a8871777e9f81e84fa66f656615e414b7f89e5800d429f618cf2fe88aa67631db651",
        "0xe2faafdf582764fa4b65004ef676466552f4bfe3"
      },
      {
        "0x98eec766a1da31be4aa5b56a3c1d388610a4a37f7b8ef17011c78c135c999783000000000000000000000000000000000000000000000000000000000000001c30a0d88c1733418d1248ffecf0b3585abde3999aa4c4b4496dce559a6a9cc1c81c1de713d501fef22da4bfd4afa7bbb65d0b2217ac03dbc3bc3c75156593a8f9",
        "0xa32c72d11ac3723ad0129c6f395c7d118db9e5db"
      },
      {
        "0xd646626db60c8f37f0d67df1f7795860dc97f70fb26e85b7dd67fdd8865d70a1000000000000000000000000000000000000000000000000000000000000001c3f4c0a2e4c038aac868040990b4334a6c007e8394837ef2b467ac417075b502e2b67b91a1a52df65501d16645b7835849f70aa50a72d25ab9baeda90528003f4",
        "0x75cc7deb0bea00df88dc37a6a69038489b7a00da"
      },
      {
        "0x4a3842955bc493c1478b8e5d091f5ea38b6e1fbfe29e2a76af6b642829a674d5000000000000000000000000000000000000000000000000000000000000001b9fdc9d14a459294ecdf918920dbc4f5bf957425563fcd2fcda6db19ef1ae37a144ec3663dbec38c0acb40623552b4062981c635842434e5529a6961eb468a815",
        "0x8b38952f5858f58d8b448da2bfc7cd5f203d9874"
      },
      {
        "0x1fa23790ba1f754c40131e8ebd40ad95e8c3f9ed32308395907bd9b6f451286d000000000000000000000000000000000000000000000000000000000000001cb9eb00b69db8a908c0fb24cf9a54fecd06b568efac82ffa38df3c0a088825f6002e574107158e2a83da1227343a536966fd4e92b298e473893ccbaf20ca3b479",
        "0x01e86c2dc7888aaf69c067c3d81d3324b221ac4e"
      },
      {
        "0x88f8b41d758a53d0065691f5c771b999c241508a26b6d49b0698f99675602372000000000000000000000000000000000000000000000000000000000000001b5f97bee8c1a2660617aa6d00b48cae9dc866e7dd92471a18e34f863841e4e7954a20c134006aa0a19fa7d1eecbdd3a7213029fc1b173157d2354a62e6add5a0a",
        "0xd7f38f4cdbf5cde5a68111970cb377003f971dd6"
      },
      {
        "0x85bced90e31303e32e094be2d0210d8ae7f4ff29a793bd3e5959fc86a79b2738000000000000000000000000000000000000000000000000000000000000001b04efc53658e9a47738a7078783dd6ef305b9912443fb6008451026e80a38a8f64d0abf58ef63bdf5ed576c6f0e99bdd3dc7f73f17549303222b144868bf17b58",
        "0xb72b25e2dc98013d1e429180a95ce8d763b92ad3"
      },
      {
        "0xb0df0e432f097fc53dfb8d14165a63a24313cff79254a7cbe554ab7eebd43bc6000000000000000000000000000000000000000000000000000000000000001bee9e7ad060f9c969b7d289c5006eb9b03cebb508af9e5647228d48030d1bf2c87d0d66c0cefaef9eb15617a891963d51bb1e19bc7cb33544e919c94bd2235ea6",
        "0x270433b68a15b4979186f44c3594041e8f6fae80"
      },
      {
        "0x70d6236cd7c57cb44dd1f99e20e785668b6cdead0b007458d0ed4e9655d7f990000000000000000000000000000000000000000000000000000000000000001bb262ec123fde934f67facf354ca12eb19b6b453a8bcd7beeb0566743fe4ed78607baec6194afcc4f66cb3c42518f0eda63e5f94df9121242e4e128133ede0024",
        "0xa6182f0f4ceb72de969d20684f71660c2cd2ada0"
      },
      {
        "0xabcbaaca73cafb7fbf215909dd5750ec1ded7a3e151ff830df92590fad8cbf30000000000000000000000000000000000000000000000000000000000000001c0168786a30ab53bff4fac2f46c7f2cad22e96c0e80be8c6f96f1bf987b134c726caf9c6b4fafb3689506f8ed360cb61be6529c4de87e1d9bf553f7f3734beb6a",
        "0xecd91ebd69948e971744c52ceff54b5681c6d419"
      },
      {
        "0x4e166b5f7df865f51522a4ccc9616a4d8c9b83df29dbde6dc96dc94a13e4a7e4000000000000000000000000000000000000000000000000000000000000001ba43b80dd0721a715082425bf9252322c4b61671102e860b7371871abe5bd6a5e5c8abaf399cb7e6f39405a0583031f73b40ef41963b3827f3d20d5103ccd3517",
        "0x8cd8416864cc82c1061c00a2571e2b6ef1bc8f9f"
      },
      {
        "0x515ac051195596523c6664ac8d8e48345ea5932cda58d22ac49339dc234127e1000000000000000000000000000000000000000000000000000000000000001ba780c9c0aea4501f09cf5a75d133e9c89386ffea400719dc82d9586975cfb6ab41adfa6195e155b7a2a7f598c32a754010d5ce98e967a2f812f77050e72d67cd",
        "0xa91aefd1bddca65485567c7014ebf4b48917e3de"
      },
      {
        "0x3c5ad848eecf1a48d12f17ee1ef6a4ee45f3c1ae7cb5b843fbf70d0f9b7b4a13000000000000000000000000000000000000000000000000000000000000001c50cad8bc91ef3924d32ce6194c79a6104686de2e2f0e67c3353262489aa7f6451532f54f6b99e8f4ce54377a86a515088bfe440eebec374402122c69fb691f5f",
        "0xd5be345de0dd0d15d34899173b7c375d31140f24"
      },
      {
        "0xfcde24b61bbd3ece34d2f7d99751e57e0d840cbcbd58fa1777a5e6be8f74bcd6000000000000000000000000000000000000000000000000000000000000001b378491f640df70d59dfba12f7135fec6e7b0ecd67bd07a29e3644f85071438ae0a737efc36831bd13576d2c517b2b4694181ee687f40732d428cc5478a8fa7f5",
        "0xfe4701547b7dbaba4c0702f226dc48a2663e18bc"
      },
      {
        "0x83326209a1fc9e7f4e111a20ed12e2ce6d0639e8eeeb75fe403a9bb180288a47000000000000000000000000000000000000000000000000000000000000001cad837bf4ac65dc6741fd911ecc41ce1d137e135540bd1035a06cc732d23b3fe243b3d70317177dd578027952eeb6e701280beb3685cb44a9cbc6685cd3149aa7",
        "0x66036bc0f0fdcfbf8d814da7f3f16885132a43ec"
      },
      {
        "0x4ab6fb0952a3e801814a72bdeb8630aeb9edce54f0b08c8284bc7efa0d737ff0000000000000000000000000000000000000000000000000000000000000001cc8fc37b1724ce16d169b4856fe2113ff305290859070a3cb5cfbb93f90a248e3382c9ca18fa7f926e914cf781999c2c8de6ea1fa4e8536030699005b45efce86",
        "0xa4e7416ef65b74c3b61a1eee53a33eb1d2b51a63"
      },
      {
        "0x80d7c38d08128721cb1d6b78f26b2f53b7bdcb2448a6d87668f4fe4e19cf5dcb000000000000000000000000000000000000000000000000000000000000001be5dc7cddccc9c61eac363691c6305420bc393a4ca17f1e10bc5192ff63b5d03f02536812a3fd90cc50208fc3b886efca921c520b4a66ef00d3228cf02fbad675",
        "0xabb83304268fec4160d80e3e45d1376c0b458636"
      },
      {
        "0x1e4c87808973dd4b705d20186d4daf911f7002006aeca1ae1a476ec23a3eb67b000000000000000000000000000000000000000000000000000000000000001b942c0416506f34ae4a5aff5bde5fbe4e093f4fca798e4d8be460a84c5cbe8a1a0fdc6113fa280b9c4e736147ccd5bf8d383353556692f27405e77344b7c9b253",
        "0x78bbe6f432b755a9bc0bf9798ea82e2574d01930"
      },
      {
        "0x5a174e433de66595c112432074d5d6fd07239edb8f3e1bc6b78814d4ce41cf22000000000000000000000000000000000000000000000000000000000000001c964fc2baba65aef4a47358132fd87ac92dbdf08e7fab49605293e43f47a76fac320d603d6e841fea09a4dd7b7bddb8b9c6bbb30b81893ee655bb8a16e3939d51",
        "0xd309698bda9fbc6ba8f56ec71cd0fe50374ab147"
      },
      {
        "0x390fe5599dcc23aabd594ae17f08b27a3f686f5a12d3ebd2624811738976e21a000000000000000000000000000000000000000000000000000000000000001c4d32732c7278ba4060cd00c4bbef14189604470147910f6f254cfe04bff7638346a2c66f0acc61b8a612d05ceddda4f2dd9ce79a7f36597bc74797d3d28d6cc7",
        "0xf658c09eaa7cdf744e34b676a92de4cdbef19306"
      },
      {
        "0xe0a1f6a2b34ed7cabe5829e4a0fb68989c8b520cb69376f4e6b754181cd02c4a000000000000000000000000000000000000000000000000000000000000001bcb22c52a26b97580036849796289293ff7e7016c13e948d99f2dfb5176d7ec01441aad675e9119fb30019f6c73adcdc4bcecb077c9164026230b5bf71cbbcbb2",
        "0x4566e04fe531b85ad7103ac1d234e36fcc4c80c7"
      },
      {
        "0x925023d3dde43d2691bacaa0202dad390a73acd5c67dbc3b73c8667696103cb2000000000000000000000000000000000000000000000000000000000000001c61cb8f8271a58b25ac3fe24e2b1a91b613a5e0da5fea8a27c4c4f38c46c7d74901be5ad35bbf74b5f23bcd03f5cb06dca74054738411e86fad9d985ee43d6003",
        "0xe22dc594d5a836902a398fef392e4acae4eb64d8"
      },
      {
        "0x809e822931959c627d36bf34ebc22415614f20c1b98839665187154233ab35c6000000000000000000000000000000000000000000000000000000000000001baa4df42f677f348f44151cfd059b3fc0e43468301acc0e32c68fe1175428308129eb46f79704e3169f336f7a269a026f43a316b7318198b9880fe25c069c4628",
        "0x6825015903052024e1c39b5ec4686d8f17650623"
      },
      {
        "0x3e84e7a38dd4802b3469a55ffeba8e3b09cd6f2d50c446b217ca943ed09c7143000000000000000000000000000000000000000000000000000000000000001bd10fcd30c21a2f7eae829e5445a7e8ac1f4d64ebb5e007c087b64e369e502a25101bbc6e6e3c2b8a0a7b0cc936be004570de3c58e6dcfa460507861aac2d7675",
        "0xc19cb83c80aca377fe7d32114f0391c64fe5a043"
      },
      {
        "0x4c6dcfab6ae4a6f8b9f812d361d7064ca6802a6d391a5e936c7e80b53723f172000000000000000000000000000000000000000000000000000000000000001ce680d9f9b0fbb6a7edba040c9e03415530b7d954910790ecda3892ab881a7a8a2ffa92fcb1824d6098f4ab4704630fac4f0f34dcc36795af6984f9302551fcfd",
        "0x5f8e0630b06762a24670bd407f79bceee4ec0841"
      },
      {
        "0x0c77a6a08a98cc812e7a5275421336d78cd6063747a3fa90385391df9f17d32f000000000000000000000000000000000000000000000000000000000000001bfa375b6bd93d3a73787ded81e2d470f511db2e127f950d63308d23a34d2eafc5594a8c4cb868f796a37b2a1c55eaea2d9fe6aa77d41c9f9b01a0ce240d605c1c",
        "0x4caad3e55d7c8e67c3a9c0397d743b8cf207ce2f"
      },
      {
        "0x131d7ad7d9ef2b50c379b4cc5d84df851d652d6012cbc612e600a7f99455b241000000000000000000000000000000000000000000000000000000000000001c90e090540445c5533d4391e7bf4e5b11d6985105bdb7944abdcee09753a4b3e0697d19c3b03c7433672132d175a991111be1ecae5ea0d236eb751355e628beeb",
        "0x763f039273833269c26cfd79fe4713da5d74a45b"
      },
      {
        "0x8370b9d2e6dd2d7ab1a0adf391a4f8380229de45fb017f812091b4226d7ebefc000000000000000000000000000000000000000000000000000000000000001ca7184b28a17f1fcb25953de968f5bf5c4b67ca115155f00cb4ee7c7a838d82cd44b55aa67acfd3fc8939b607eec5fe256f8098ab65db1c3db04c0efffff4c34c",
        "0x014f31c9e429c69c270f4ccff594508abd520420"
      },
      {
        "0xc9d7002487ee5026fadffeed13893e827378acfca9171b89817c53dee584d289000000000000000000000000000000000000000000000000000000000000001bb5a3c506e8c38cd916bf20c237906befc29ae91d5474700c66ed47263222a487116fc65be6ad78140feb2df974d4568cc5aade5bf78ce14ec0df274b0983aafa",
        "0x6bafa3e7d355788ade4f5c3ce4ab865a490118da"
      },
      {
        "0x8dac2bb0d2e8ba5923577b2b12a0b9eb6b1af5c35ac6ec381ecf8ace2de84172000000000000000000000000000000000000000000000000000000000000001c549c2c7d6f644a5a9df73dfb88cbf1902fb6ad2ce7de27b18560f58d11c1de6658ba07f35e5676c33e71c6fbabfa30c7654f997394a8c713f7eedd2da78b25d1",
        "0xc7d1544579c6a0002966ca562923c5cfcb5e21d0"
      },
      {
        "0xfd7bdacc8bf6f0901fbb76d18dd3a9580b88cbbe8c5c667feb497ab2ec1aaf60000000000000000000000000000000000000000000000000000000000000001b01fb675ce7c18d34abf4d21993a36bd3b213fbd29f5077d178a84f6f5dbf7c4422f1d63423dc3bb682eed2f3c96c9ecbde516f1e26364556ad9950e431217240",
        "0xf18c3dbf2aca83afc71b520e8ac3d4d016fa9ec1"
      },
      {
        "0xe24beed8117992ce468610a4c7ea3b8bbce67831d5459993ad80ef75ff74399f000000000000000000000000000000000000000000000000000000000000001c65d5763121e91655fa84d7ae74b422d61db272cc6584622673e9993af10f5d7f5181a54f089809da91d008fca5fb6d7af9bb41d064b839f7643881a70c124bae",
        "0x22a09a6e7578d42f9af290f6417d2bc04ce3380f"
      },
      {
        "0xd0490e2210e00722e024000245b28c5cb699c1597881a6e0dff13adfd7f00b7b000000000000000000000000000000000000000000000000000000000000001bc95aa8a187ed486f3f4a2c4fbc58b4f1e8fac5f6aad373ebce0d5e91d8e8bea67008db4c559da979b5b07a60890fd4cbe8db164d0102e395de7ea27aa5224fab",
        "0x0431be799b7f179e4431ae1cb629c17edabdf917"
      },
      {
        "0x96a49e2d7833c3dee1b5ba8da56088b3fc900ba28b80e1120341796271bbc8c7000000000000000000000000000000000000000000000000000000000000001b488f5bf61a104009d5b1c2102454199afb712742c4d563ce3dc6a1aaf5e177d7605652e31020e876ce65c1fd3f75ddbb163cb636065639b1f03e7b90c256e2f9",
        "0x1ef26cd19e0b83e4f56c88b68293c1cd9444c684"
      },
      {
        "0x5f764e0f288b12167977651452f233de59ed6163eb6b17796195c96c603e4313000000000000000000000000000000000000000000000000000000000000001b5bb97625154686b77f4c3480fa06e314798503d157c5db85a15218f076d19239052a96f028d44331020106277591db48c0b07e7c501c181a842ccdd20b3089a6",
        "0xe04d0368b19ef078d6040abc1eac64869a510e8d"
      },
      {
        "0x5d811d11af03c52f95907d5f8fe1cf9ca6d25e44031588a08673ee39fc720c29000000000000000000000000000000000000000000000000000000000000001c963a39edda9392c83b390b7540b6b901bf1bf0f678c59603c0b8ec02f41633945ce49973a3e15591eebe3ff1b677696c1609509e547ba84ad711e13aa229c5e3",
        "0x730594bd0a7389acc19aad47741533abd0f19e81"
      },
      {
        "0xed6b74c00def739b829b1d11f44190c075804414bcf78c30b6ea781d5f842b5d000000000000000000000000000000000000000000000000000000000000001c115e46eac1f6abaa48f4ed42713e8a10eb748d638331e83ab1f5b1aa672c3faa7f5892be39cfa345c785a369dd147d5c8bd79e37d4fc392782495231d52752aa",
        "0x150e6c28a10d4b01ef1c5c733a2e5831fd75f684"
      },
      {
        "0xbc80fc2f142d3c139a6bdddccfb8d793be4bfff086904e4016ef5fdf4f552186000000000000000000000000000000000000000000000000000000000000001b8b5fc96d3cd453c51bf480ca1f564c6a88e3310bd938993acd6f4d2e1fa30857640660bb208f15a9cbc3bb2c1de892dea23628040c84cd48bf035cf1440e6ff2",
        "0xffb0f15492e9bb8dc8a8b3caf74cf30fbff51add"
      },
      {
        "0xd14c00d0637bd1f80b8a29695375c4c4fbc25abeb0215190b937ac5d77138389000000000000000000000000000000000000000000000000000000000000001ceb7236075f80b65a43318e377fce6b4403a9fdf37e9d5e4563942625cba9603a67e8aae700ac3a877381cdcc71ddda0fbbf16a807e9f89ada558fef5b89c5eb5",
        "0x5cba0a3e82a42466cbdd0cef06165f3936be541f"
      },
      {
        "0x964f1ec157e2214d09985d8d96997cedf59d6c75c1e01f9b6038d841f9a02f41000000000000000000000000000000000000000000000000000000000000001b61154b748a60ab1171413ad6d8cf8bf3d21c5c8da5c5025385565cc685519ff45358e7cb911a227fc36ce4d14bb11144735e90ad80d074f59b453bf2d8273b6e",
        "0xfc7eaab26c1dfbb2a112d34794e0ec67ce76d471"
      },
      {
        "0xd98b755dd654a52b968830f43c01f7b312c87b210c614bb13b27f3a1c3be430a000000000000000000000000000000000000000000000000000000000000001b8d0eb91bf9a3e3c7dd67e1703acf6f7d558e8e4407b30bc8db31b8b3c29ef907188c479e1285d9d5e74ca4e863114e70de6cd66f381e0adb1b387829aca1191d",
        "0xe157b225a52436ac28043a4940a300bca589d0d9"
      },
      {
        "0xae7c9444d3777ab5966ce98a996cc590bd0560e5b7b4cf7c603c294e5f83ef64000000000000000000000000000000000000000000000000000000000000001b4462a10e115f9beb9a1ae7e1f9bc135ba18c3cb47d4562b63f34a2ffc3f0e104454e9de9b8f86ef018f185f6131cea90967c13377be39162fd70326cba74e088",
        "0x726871df1a80253abcdc39b8efbb019485f0aa92"
      },
      {
        "0x59d4a98ff7238c392d366f4e1b59c0e0f062b8069d6aa7217d4271e6e06ea1b7000000000000000000000000000000000000000000000000000000000000001c180b92c72691caa41d149d3c8c4b02d8e637d447fbf702f80353e1c89aade51b27d59ae0d0bad8148134e17744de8a1ed126a381b2ba0039336b815b093e8178",
        "0xb70a82e0b8d80c113f31797bef325f9894e33d53"
      },
      {
        "0x1d651cc72be8cafc2f831a5030188dc23bce6648be7045ff9eb3dad7f0d0458b000000000000000000000000000000000000000000000000000000000000001c15b9fcdba84fb454a47cbf830705a7fbb03bb8547cde5695338bab38213048505363b8bf99f730d497983ab0eb3986f9bb7750a98e2db08fc24f8e094c23f8e1",
        "0x6f8763efc3d08828982d41e37a9be12d795d81db"
      },
      {
        "0xeb3558fc388e8d245f892684fe0bd166bf5e1ec061f778436c36f7ba27fb5e88000000000000000000000000000000000000000000000000000000000000001cda01d3af46bfeb57e495ff722d0612132b66a3e2f7358e2d6a210ffa36a91c4671a5a1eeeb5f03f9b97fbd4076266a3aec7c2c64c13cf533298ea440e54aac04",
        "0x85f54483bb76aaf73fa1ee8313de109f309aaf33"
      },
      {
        "0x85543dcd0a1c590cfe63cc1c6e732a124b2138d4ebc7aa257ce4ff5ab7838a67000000000000000000000000000000000000000000000000000000000000001b737c920964ae5e15f3be8548774dd965f5b5af522f3e811cad247bda80c13e753bc58cb3f973fb67b3856a3a6f5aca1847ed06711ed722f11f8124c025adac1e",
        "0xf1383ccac546c49072c73fb3a0a4da4ca54d4b30"
      },
      {
        "0x334ea2be8566052b9c0f52fac3005b8fa96950dc17a2998f4ccc8a68b6f483a9000000000000000000000000000000000000000000000000000000000000001b5bb08f9a15c2deb532774b0e29d9782f7be619f57d15467d4282148eacf114d943c430c811c5979351ce3e77d1843e7677a1eeca350e3952958323ee5c5406e8",
        "0xeaef2c85d19516bb3434a8bc364ecd270fba994c"
      },
      {
        "0x9797b28b78d1a41f655e4670e797c4b4054b7aefa4c0b7cfabecfdedba772ab5000000000000000000000000000000000000000000000000000000000000001b8c69b0dc1f4fa557d658d9f2a2ae2a87abba4dc30a918c2249cc0df2cd520aed3f5012f755ad6320af14746a7531a70503200e5818a243e7d9d7389dcd47db08",
        "0x7a3177965811818903f69d2202464de03d43b6c7"
      },
      {
        "0x185faa83fbab6980289744d05f6fbac7a9eea9078ed82af38b2f542922a57355000000000000000000000000000000000000000000000000000000000000001b345a728c2c87c11adc137d19d1c7a3acc2195b7591fb7449b3fd9ef476b32a005df1738096a09ec334cbbc43bfbc42be19777d6583f82ea499f59f8ac652a879",
        "0xc07d2a4f05c1617f3314e29e3dd98c083b9cc55c"
      },
      {
        "0xc281aefd2405635d3229c7bd155abf3ae22abd2a6b60b9f6358aa3b349fe76de000000000000000000000000000000000000000000000000000000000000001b5bc6959194476096e223df55cefc3ca991e5886fe80751a5ad40b1ad9999d6045426b00332eceed4152c733e8dbeb04987718a458e4369bfc6c30e538a95641f",
        "0x6722fa2ba638738b50b9cb45f4f7722ff8264b62"
      },
      {
        "0x53fdbd4b17fa328ed653476b7ce5beaa9f6b99affe3e64cf6fd99dc440379878000000000000000000000000000000000000000000000000000000000000001b0960d87cebc136087ed15f005349b20dcef74ea4f234d1ce9e10fb9e5fb0e4ee28d0755d8a6c66eb49eddb6b3ea8061a4265b83052d5e4ef9c7b6a3cccbd0441",
        "0x2c3bc458c2aa4e62ba5c4300cfcbc344da9ee93e"
      },
      {
        "0x6edc1c049d2a86ca8fe892bf24a48dd3c5b630b03adb2a3b2855c1f7935264b9000000000000000000000000000000000000000000000000000000000000001b9a2f685a2089728baaf03c379703097d412504fd3399c2f79a18e1d13c07ee945c5da9bc00581bf2b1977dd5809683ec4e06a9a4a7a32561ec7aabbd54b04dbc",
        "0xff6e669dee405c6dc021a7e1ba2f5feb96c97adf"
      },
      {
        "0x65f6ec58597df636a59d070d48b29db68fc229801db026a4dce11e59ca6d23b1000000000000000000000000000000000000000000000000000000000000001b5bee25353248c9d12df421c3db9eb3141918e2a69f766d7f11edc1ba66852d205ca06336f49919bb3f45e81a463d96b0dd5071bf7edb7663e20e3fbc505e232c",
        "0xba0093215db426d68708d1e95156ef0aaf90b588"
      },
      {
        "0x3546335f92dbc0883248693660061db1cb8283d2dd1969f0260915983d7ce30b000000000000000000000000000000000000000000000000000000000000001c27b51e23abfab6a82bf3bf83c23b222a01ecfdadda3587b58d965eebfab7dc5a7b3006e95e02b19c2fdfcb85794bad59cd2708b43cf658e4696b292c95cb6fc8",
        "0x130dc34dc7b806a3e7aa2a2b2db8cd9dec66fd89"
      },
      {
        "0x237d94f90f917d6eaf1987353f5907950ed6c8136112f029494e0f769ccddbcf000000000000000000000000000000000000000000000000000000000000001b14919b0374173a158b04213c718d74573c438891cca416f9e198704d56320daf7ab172688fdf770b1553fd7ec4e27a9bb57a84374562734904b8ade0f31fdb41",
        "0xd50240c7d2ae4a7c30ebd3948460581f72385732"
      },
      {
        "0xf2c26852616ca8ca4fbefad89111e5326f2c78158e609488461ab8fe3bc6573c000000000000000000000000000000000000000000000000000000000000001c0ed26622b205d658331ccea6286cf5d623687047e61b81c7cccae5181eb1c4b827312bb9af5c55c23f75cc72b20ff10a301ac3023f5823757c0fc9c4b9f827a2",
        "0x674fadf0fcf26febc6a21a70d27dbf2219d374dd"
      },
      {
        "0xd238468a4cf3eb5939950c0dfd1ed56f13f61784ea1dcc4ec472470370f8e4fd000000000000000000000000000000000000000000000000000000000000001b059cc3c246d17f6b044b3b15ee1f3bffc59865701703c2b6e9a9e282d65e3771788c55359b3f9879245ede61f801a4bcaa18bd6a90216b952e06815a407bc7ef",
        "0x8e76a1cd7db91aff5c8ed95f17148f9a8d4c81f6"
      },
      {
        "0x5dafec9564fbff6371b45399a361407b8e0f11ae8b62b784e121974957db9ef9000000000000000000000000000000000000000000000000000000000000001c2a71af5be39a199bb3b668994f28be40de1c0f5d59fa7b193cb72d4d357073f42e21a423a8b3bc8443ec9a05af6f2bf709f23c98c2c6c3a4d728873000d0b3c3",
        "0xeb10e6f82e42a18ad05f623ed291f7bba26c1166"
      },
      {
        "0x8eef9ad23caf2ddfb868faa610cee0de186722d20c8d8bd15d8258fd278f4b81000000000000000000000000000000000000000000000000000000000000001b890ae7c25d8c93d9b232d664f46357bfc21b529b427d9f3c84fe1e24cb5eabe515146ea64479f18acfebad6f556437d826833650fab32f940f5eeeeff0865aea",
        "0x6ed176182fdb30cc2d9f553179603ba45fadbc4f"
      },
      {
        "0x7e38048ef47052bc73329abe12d51de302b690ec6e5c612a6dc545a16e578bdc000000000000000000000000000000000000000000000000000000000000001c1879fbfaeec45b117adfd62abc9fdc3ad575f1d7f09b388f02a2ad5ec37287504761f9cee67e6b6d75c6b53ddc966939df3f5f605083e8c93c06a64812b08ab7",
        "0x45505cf5182b9bfaee12c1b5b3e3d09377567b5c"
      },
      {
        "0x9981e530d595012980ad4f1cd860f6d524a2f34f6aa132932796cd4be96d6e5d000000000000000000000000000000000000000000000000000000000000001ce08be9670177846e05696271da3960eb617869935456a14f634c3f604543dff36f1b05d985d111ae7cdaf679f75e9151030e09f0c8148c6ce3e02c1847415002",
        "0xcb69ddd0bf6c51348567444a93d8a3f9d995f363"
      },
      {
        "0xcb7f1bf45f76e3dca564764290cbdaa64a7622fac589799a70199845682566b2000000000000000000000000000000000000000000000000000000000000001be6c9c8dc4b408518b89e5a048d36ad1426b3ace96b8518a5a9e88f286103758e0cd9a5ddac0f848eefb204dd403bffb063a718c8e1fca4c4ec94155269370b77",
        "0xc97af81301f96ad50d9b7c64e322ef1527702278"
      },
      {
        "0x21e69437899f6a37302b239fc0261c7ffabb970e8a48096475e998d2871f5bd2000000000000000000000000000000000000000000000000000000000000001b89501dd1567e56253ba097ffc663718fac90818e56ca6b393b69e9925162ce5a7262088cff050243662adfb2aac50143bf5aff60f904f2279d6f6cce5adbebdb",
        "0xf00f8df597663bf073bcbf43f75d0e5b6caae7cb"
      },
      {
        "0xf524d92f0d374b4d104786fb521265f6034cb6e2e6370d9d83464aaf8c87b0a6000000000000000000000000000000000000000000000000000000000000001b94332385b44da4cb31cc83c8d1caa85c99a85b58a812f9d3b8ab62e32c9dfc7e17f2b4d58e52bccb42bc069e8e6445a87deed25e094fe1cdce663623493c21c8",
        "0x532f5d1695f4305a7193eecf7f17c4ae7fbd5c1c"
      },
      {
        "0x739c5a643ca04c7bf21ea78cc89270a2d66662f1441879f6f0246a3b71416996000000000000000000000000000000000000000000000000000000000000001ba6f422a6fec65ac04fe923c8e7e92cd65a26b914f06124276dfdfb7ad396eb9b7613f03ab9c71d48af2c93d430a805cb9600991d92e76b504b63d55d7dd187a3",
        "0x0b67296d0486d16d83f4f3ddb8e07a0a57c36631"
      },
      {
        "0x33693f2a0c99ea981b5b11acd2c6bfbd58e43170c5a31f79b5a8eb3321858e71000000000000000000000000000000000000000000000000000000000000001b5b33ddf363736a3abd5a356eb610d2f9fd09ef6125fdcd4ff4c7775b39e87349225b33e5afebde52fe30eb987d72318e9cf095d892e7fbdcd9e5892786f49d1c",
        "0xe5c7698dc89c75475d5d067e124696b81a0d53f5"
      },
      {
        "0xdb9ffb31945e1cbe86910f30bf7129e276f0ed9d7cd623f7b74a245324ce9c82000000000000000000000000000000000000000000000000000000000000001c8b9f853439623cc3af1f15ba21c639bfb3d8ba8302fa9d1603587a537b32fcd1308adf297dde649d208fc0b323f3c36e9404ba6dc8a74510d4082c867fbe1205",
        "0x988cfd3652a9bacc3debc441f02a8bac7913e048"
      },
      {
        "0x3930cff06ebe66193b61bd2d099c001a4f1c8597276b6324ced78b401746ed82000000000000000000000000000000000000000000000000000000000000001be2f6e9aa05b5af82987c65c31c131c152e4fa58f6b52a140519b11654c3cc8970472559ab09821cfb51b6d0c96ca9e5421f78f98af0d4aa601c49a6ceb9d4a78",
        "0xfcbbfb74050fee34c54f3b18a50d61a679f5f320"
      },
      {
        "0xadf263dddf4ad5f323822773b76e64abc06230aec4bde5d8b7be5ea1193f2b6d000000000000000000000000000000000000000000000000000000000000001c8caf0f4834f493ade134f733cd97752b4b1f0a5c8ffee47b6ec05f46857557bf20de09970e1c57404cf7ca7efbcaa95c47d99a2eba76aa72bd1b44bb0486e368",
        "0xe21d622e34869e656a1e28f278a6b8226082d153"
      },
      {
        "0x9b3ef1d284641e6c3be8dddfcf29844b393729155edb1d146dd6023dd336dc46000000000000000000000000000000000000000000000000000000000000001ba08e0795bbf5850b36b4326bb104aa14a49d44eb18e31b034b1694c5bbbcfd9732580cd88ea1277b202c6d3a575e45fbb6b2e0a489b74343035bdeef5c32863a",
        "0x7bbba2e9fca9f2ade01d6c9c0f63a12307e3fcfd"
      },
      {
        "0x7eed0e79c71b7baf9b2598a7a53bcdbaffb985ec48e1560bf963aa631d5d8c92000000000000000000000000000000000000000000000000000000000000001b21a8126ccf776ed394576e3c50e10e489951886b7e8b1a9d640b5ab4c63253737f76e9955dc860d49295a3782130cabe694eb2ee34ef0dc3367b07b3efe9f3ac",
        "0x6df661c685e9433e2f85961d15c1a1c3f2121417"
      },
      {
        "0xead4ef63f6bd001593e8088cdd8ab3c0c35ba808bebb091f43240ecbd81db3b9000000000000000000000000000000000000000000000000000000000000001b30d5d51ab3a1987baf4fb41ad5a2467b4a4a1000c909d51caf389e191441634135d94ea50996b6db1b1182a2c28162058ae53c3ef52695e5cc6b17018039d90e",
        "0xbf14cdbb0f88457c9e6225c0b87b0b0b3d38e953"
      },
      {
        "0x0c24abcfa29ff1f068a19f2f1db51b6da6ab042ac0ea3d74c87465b2075377ff000000000000000000000000000000000000000000000000000000000000001b18e975c578187caeda96b6029a7ec7aa037757cfd182b5f8d71f872233c2264a6adbb4465a68fbf4a574071cc4973f5fd5e7b721c86cb7e0f47f595222e37289",
        "0xcd6eb47247a12aea535d6fd96cad1f4c7bcb409c"
      },
      {
        "0xda13687f911cf8ede5e0a4317d8b9bf691b56bc2f3f4e463c8c2eb1f61a54469000000000000000000000000000000000000000000000000000000000000001bf6e5df315197d9fe994fae7e05e33be4bd090f9533f36c6285b80478cd21c38533928bb06d48795a86c12f5ccb95758e891d8b1b2d62106e85ae36cb8414d56b",
        "0x9765c8a57ade562c30166b7e18aef179a22da185"
      },
    };
  }

  @Parameter public String input;

  @Parameter(1)
  public String expectedResult;

  @Test
  public void shouldRecoverAddress() {
    final Bytes input = Bytes.fromHexString(this.input);
    final Bytes expected =
        expectedResult == null ? Bytes.EMPTY : Bytes32.fromHexString(expectedResult);
    assertThat(contract.compute(input, messageFrame)).isEqualTo(expected);
  }
}
