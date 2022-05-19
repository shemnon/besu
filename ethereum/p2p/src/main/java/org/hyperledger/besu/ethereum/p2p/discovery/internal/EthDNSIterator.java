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

package org.hyperledger.besu.ethereum.p2p.discovery.internal;

import org.hyperledger.besu.ethereum.p2p.network.DefaultP2PNetwork;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.NamingException;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

public class EthDNSIterator implements Iterator<NodeRecord> {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultP2PNetwork.class);

  static final Pattern enrtree =
      Pattern.compile("\"?enrtree-root:v1 e=(\\S*) l=(\\S*) seq=(\\S*) sig=(\\S*)\"?");

  private final String enrDnsUrl;
  private String dnsRoot;
  private NodeRecord nextENR = null;
  private final Deque<String> pendingDNS = new ArrayDeque<>();
  private SimpleResolver dnsResolver;

  public EthDNSIterator(final String enrDnsUrl) {
    this.enrDnsUrl = enrDnsUrl;
    try {
      dnsResolver = new SimpleResolver((String)null);
//      // less than half as fast as UDP, but more reliable as packet size can be large.
//      dnsResolver.setTCP(true);
    } catch (final UnknownHostException e) {
      LOG.warn("error setting up resolver", e);
    }
    reset();
  }

  public void reset() {
    final URI uri = URI.create(enrDnsUrl);
    dnsRoot = uri.getHost();

    final String treeRoot = getTxtEntry(dnsRoot);
    // TODO validate root entry
    //    BaseEncoding.base32()
    // semi-strict EIP1459 parsing, we could enumerate base10, base32 and base64 to be super strict.
    final Matcher m = enrtree.matcher(treeRoot);

    if (!m.matches()) {
      LOG.warn("DNS TXT entry for {} does not look like a enrtree-root: '{}'", dnsRoot, treeRoot);
      nextENR = null;
      return;
    }
    pendingDNS.add(m.group(1));
    nextEntry();
  }

  private void nextEntry() {
    while (!pendingDNS.isEmpty()) {
      final String nextEntry = pendingDNS.removeLast();
      final String nextDnsLookup = nextEntry + "." + dnsRoot;

      final String entry = getTxtEntry(nextDnsLookup);
      if (entry.startsWith("\"enrtree-branch:")) {
        final ArrayList<String> entriesList =
            new ArrayList<>(List.of(entry.substring(16, entry.length() - 1).split(",")));
        // TODO validate hash
        Collections.shuffle(entriesList);
        pendingDNS.addAll(entriesList);
      } else if (entry.startsWith(("\"enr:"))) {
        final String enr = entry.substring(5, entry.length() - 1);
        nextENR = NodeRecordFactory.DEFAULT.fromBase64(enr);
        return;
      } else {
        LOG.debug("Bad ENR entry at {}: {}", nextDnsLookup, entry);
      }
    }
    nextENR = null;
  }

  public static void main(final String[] __) throws NamingException {
    final EthDNSIterator iter =
        new EthDNSIterator(
            "enrtree://AKA3AM6LPBYEUDMVNU3BSVQJ5AD45Y7YPOHJLEF6W26QOE4VTUDPE@all.mainnet.ethdisco.net");
//            "enrtree://AKA3AM6LPBYEUDMVNU3BSVQJ5AD45Y7YPOHJLEF6W26QOE4VTUDPE@all.rinkeby.ethdisco.net");

    int count = 0;
    final Instant start = Instant.now();
    while (iter.hasNext()) {
      final NodeRecord nr = iter.next();
      System.out.println(nr);
      System.out.println(nr.get("eth"));
      count++;
    }
    final Instant end = Instant.now();
    final Duration d = Duration.between(start, end);
    System.out.printf(
        "%d dns enteries in %d s.  %.3f entries/second",
        count, d.getSeconds(), count * 1000.0 / d.toMillis());
  }

  private String getTxtEntry(final String dns) {
    try {
      final List<Record> answer =
          dnsResolver
              .send(
                  Message.newQuery(
                      Record.newRecord(Name.fromString(dns, Name.root), Type.TXT, DClass.IN)))
              .getSection(Section.ANSWER);
      if (answer.isEmpty()) {
        return "";
      } else {
        return answer.get(0).rdataToString().replaceAll("\" \"", "");
      }
    } catch (final SocketTimeoutException e) {
      if (!dnsResolver.getTCP()) {
        dnsResolver.setTCP(true);
        return getTxtEntry(dns);
      } else {
        return "";
      }
    } catch (final Exception e) {
      e.printStackTrace();
      return "";
    }
  }

  @Override
  public boolean hasNext() {
    return nextENR != null;
  }

  @Override
  public NodeRecord next() {
    final NodeRecord thisRecord = nextENR;
    if (thisRecord == null) {
      throw new NoSuchElementException();
    }
    nextEntry();
    return thisRecord;
  }

  public ExecutorService forAll(final Consumer<NodeRecord> consumer) {
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    final Runnable task = new Runnable() {
      @Override
      public void run() {
        if (EthDNSIterator.this.hasNext()) {
          try {
          consumer.accept(EthDNSIterator.this.next());
            } catch (Exception e) {
            LOG.warn(" grumble ", e);
          }
          executorService.submit(this);
        } else {
          executorService.shutdown();
          LOG.error("burp");
        }
      }
    };
    executorService.submit(task);
    return executorService;
  }

}
