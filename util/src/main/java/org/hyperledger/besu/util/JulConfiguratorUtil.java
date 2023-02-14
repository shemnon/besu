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
package org.hyperledger.besu.util;

import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


/** The Log4j2 configurator util. */
public class JulConfiguratorUtil {

  private JulConfiguratorUtil() {}

  /**
   * Sets all levels.
   *
   * @param parentLogger the parent logger
   * @param levelName the level
   */
  public static void setAllLevels(final String parentLogger, final String levelName) {
    if (levelName == null) {
      Logger.getLogger(parentLogger).setUseParentHandlers(true);
    } else {
      Level level = Level.parse(levelName);
      Handler[] handlers = Logger.getLogger(parentLogger).getHandlers();
      for (int index = 0; index < handlers.length; index++) {
        handlers[index].setLevel(level);
      }
    }
  }

//  /**
//   * Sets level to specified logger.
//   *
//   * @param loggerName the logger name
//   * @param level the level
//   */
//  public static void setLevel(final String loggerName, final Level level) {
//    final LoggerContext loggerContext = getLoggerContext();
//    if (Strings.isEmpty(loggerName)) {
//      setRootLevel(loggerContext, level);
//    } else if (setLevel(loggerName, level, loggerContext.getConfiguration())) {
//      loggerContext.updateLoggers();
//    }
//  }
//
//  private static boolean setLevel(
//      final String loggerName, final Level level, final Configuration config) {
//    boolean set;
//    LoggerConfig loggerConfig = config.getLoggerConfig(loggerName);
//    if (!loggerName.equals(loggerConfig.getName())) {
//      loggerConfig = new LoggerConfig(loggerName, level, true);
//      config.addLogger(loggerName, loggerConfig);
//      loggerConfig.setLevel(level);
//      set = true;
//    } else {
//      set = setLevel(loggerConfig, level);
//    }
//    return set;
//  }
//
//  private static boolean setLevel(final LoggerConfig loggerConfig, final Level level) {
//    final boolean set = !loggerConfig.getLevel().equals(level);
//    if (set) {
//      loggerConfig.setLevel(level);
//    }
//    return set;
//  }
//
//  private static void setRootLevel(final LoggerContext loggerContext, final Level level) {
//    final LoggerConfig loggerConfig = loggerContext.getConfiguration().getRootLogger();
//    if (!loggerConfig.getLevel().equals(level)) {
//      loggerConfig.setLevel(level);
//      loggerContext.updateLoggers();
//    }
//  }
//
//  /** Reconfigure. */
//  public static void reconfigure() {
//    getLoggerContext().reconfigure();
//  }
//
//  private static LoggerContext getLoggerContext() {
//    final Set<org.apache.logging.log4j.spi.LoggerContext> loggerContexts =
//        ((Log4jLoggerFactory) LoggerFactory.getILoggerFactory()).getLoggerContexts();
//    return (LoggerContext) loggerContexts.iterator().next();
//  }
//
//  /** Shutdown. */
//  public static void shutdown() {
//    getLoggerContext().terminate();
//  }
}
