package com.pikacnu;

import java.io.*;
import java.util.Properties;

public class Config {
  private static final String ConfigDir = "./mods/config/";
  private static File configFile;
  private static Properties properties = new Properties();

  public static boolean isLobby = false;
  public static Integer port = 8080;
  public static String host = "localhost";
  public static String path = "/ws";
  public static String serverId = "null";
  public static String minecraftServerIP = "localhost"; // 預設值，根據需要修改
  public static Integer minecraftServerPort = 25565; // 預設值，根據需要修改

  public static void init() {
    File Dir = new File(ConfigDir);
    if (!Dir.exists()) {
      Dir.mkdirs();
    }
    configFile = new File(Dir, "ruta.conf");
    if (!configFile.exists()) {
      try {
        configFile.createNewFile();
        writeDefaultConfig();
      } catch (IOException e) {
        UTA2.LOGGER.error("Failed to create config file: " + ConfigDir, e);
      }
    }
    loadConfig();
  }

  public static void loadConfig() {
    try (FileInputStream fis = new FileInputStream(configFile)) {
      properties.load(fis);

      isLobby = Boolean.parseBoolean(properties.getProperty("isLobby", "false"));
      port = Integer.parseInt(properties.getProperty("port", "25565"));
      host = properties.getProperty("host", "localhost");
      path = properties.getProperty("path", "/");
      serverId = properties.getProperty("serverId", "null");
      minecraftServerIP = properties.getProperty("minecraftServerIP", "localhost");
      minecraftServerPort = Integer.parseInt(properties.getProperty("minecraftServerPort", "25565"));

      UTA2.LOGGER.info("Config loaded successfully");
    } catch (IOException | NumberFormatException e) {
      UTA2.LOGGER.error("Failed to load config, using defaults", e);
      writeDefaultConfig();
    }
  }

  public static void saveConfig() {
    try (FileOutputStream fos = new FileOutputStream(configFile)) {
      // 設定配置值
      properties.setProperty("isLobby", String.valueOf(isLobby));
      properties.setProperty("port", String.valueOf(port));
      properties.setProperty("host", host);
      properties.setProperty("path", path);
      properties.setProperty("serverId", serverId);
      properties.setProperty("minecraftServerIP", minecraftServerIP);
      properties.setProperty("minecraftServerPort", String.valueOf(minecraftServerPort));

      properties.store(fos, "Ranked-UTA-Mod Configuration File");
      UTA2.LOGGER.info("Config saved successfully");
    } catch (IOException e) {
      UTA2.LOGGER.error("Failed to save config", e);
    }
  }

  private static void writeDefaultConfig() {
    properties.setProperty("isLobby", "false");
    properties.setProperty("port", "25565");
    properties.setProperty("host", "localhost");
    properties.setProperty("path", "/");
    properties.setProperty("serverId", "null");
    properties.setProperty("minecraftServerIP", "localhost");
    properties.setProperty("minecraftServerPort", "25565");
    saveConfig();
  }

  // 便利方法：設定值並保存
  public static void setIsLobby(boolean value) {
    isLobby = value;
    saveConfig();
  }

  public static void setPort(Integer value) {
    port = value;
    saveConfig();
  }

  public static void setHost(String value) {
    host = value;
    saveConfig();
  }

  public static void setPath(String value) {
    path = value;
    saveConfig();
  }

  public static void setServerId(String value) {
    serverId = value;
    saveConfig();
  }
}
