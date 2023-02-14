package org.hyperledger.besu.util;

@SuppressWarnings("CatchAndPrintStackTrace")
public class LogConfigurator {

  public static void setLevel(final String parentLogger, final String level) {
    try {
      Log4j2ConfiguratorUtil.setAllLevels(
          parentLogger, level == null ? null : org.apache.logging.log4j.Level.toLevel(level));
    } catch (Throwable t) {
      // This is expected when Log4j is not in the classpath, so ignore
    }
    try {
      JulConfiguratorUtil.setAllLevels(parentLogger, level);
    } catch (Throwable t) {
      // This is expected when java.util.logging support is not in the classpath, so ignore
    }
  }

  public static void reconfigure() {
    try {
      Log4j2ConfiguratorUtil.reconfigure();
    } catch (Throwable t) {
      // This is expected when Log4j is not in the classpath, so ignore
    }
  }

  public static void shutdown() {
    try {
      Log4j2ConfiguratorUtil.shutdown();
    } catch (Throwable t) {
      // This is expected when Log4j is not in the classpath, so ignore
    }

  }
}
