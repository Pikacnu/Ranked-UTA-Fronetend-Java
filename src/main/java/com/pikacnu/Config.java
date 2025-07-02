package com.pikacnu;

public class Config {
  public static final boolean isLobby = false; // 用於判斷是否是遊戲伺服器 (分為遊戲/大廳伺服器)
  public static Integer port = 8080;
  public static String host = isLobby ? "localhost" : "192.168.0.100";
  public static String path = "/ws";
}
