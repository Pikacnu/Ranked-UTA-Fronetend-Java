package com.uta2.config;

public class Config {
    // true: 大廳伺服器 (Lobby) | false: 遊戲伺服器 (Game)
    public static final boolean isLobby = true;

    // WebSocket 伺服器連線資訊
    public static final String wsHost = "127.0.0.1";
    public static final int wsPort = 8080;
    public static final String wsPath = "/ws";

    // 本伺服器的唯一識別名稱
    public static final String serverName = "lobby-1";

    // 用於和後端進行連線驗證的權杖
    public static final String token = "your-secret-token";
}
