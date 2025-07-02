package com.uta2.helpers;

import com.uta2.ws.WebSocket;
import net.minecraft.server.MinecraftServer;
import com.google.gson.Gson;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class PlayerOnlineChecker {
    private static final Gson gson = new Gson();
    private static MinecraftServer server;
    private static String serverSessionId = "uta2-server"; // 預設 session ID

    public static void setServer(MinecraftServer minecraftServer) {
        server = minecraftServer;
    }

    public static void setServerSessionId(String sessionId) {
        serverSessionId = sessionId;
    }

    public static boolean isPlayerOnlineLocally(String playerName) {
        if (server == null) return false;
        return server.getPlayerManager().getPlayer(playerName) != null;
    }

    public static boolean isPlayerOnlineLocally(UUID uuid) {
        if (server == null) return false;
        return server.getPlayerManager().getPlayer(uuid) != null;
    }

    /**
     * 發送玩家線上狀態查詢（使用 pikacnu 格式）
     * @param uuids 玩家 UUID 列表
     * @param connection 連線狀態
     */
    public static void sendPlayerOnlineStatus(List<String> uuids, String connection) {
        Message message = createPlayerOnlineStatusMessage(uuids, connection);
        WebSocket.send(message.toJson());
    }

    /**
     * 發送單個玩家線上狀態
     * @param uuid 玩家 UUID
     * @param connection 連線狀態（"CONNECTED" 或 "DISCONNECTED"）
     */
    public static void sendPlayerOnlineStatus(String uuid, String connection) {
        ArrayList<String> uuids = new ArrayList<>();
        uuids.add(uuid);
        sendPlayerOnlineStatus(uuids, connection);
    }

    /**
     * 通知玩家加入伺服器
     * @param uuid 玩家 UUID
     */
    public static void notifyPlayerConnected(String uuid) {
        sendPlayerOnlineStatus(uuid, "CONNECTED");
    }

    /**
     * 通知玩家離開伺服器
     * @param uuid 玩家 UUID
     */
    public static void notifyPlayerDisconnected(String uuid) {
        sendPlayerOnlineStatus(uuid, "DISCONNECTED");
    }

    private static Message createPlayerOnlineStatusMessage(List<String> uuids, String connection) {
        Payload payload = new Payload();
        payload.playerOnlineStatus = new Payload.PlayerOnlineStatus();
        payload.playerOnlineStatus.uuids = new ArrayList<>(uuids);
        payload.playerOnlineStatus.connection = connection;
        
        Message message = new Message();
        message.action = "player_online_status";
        message.sessionId = serverSessionId;
        message.payload = payload;
        message.timestamp = System.currentTimeMillis();
        
        return message;
    }

    // 保持舊的方法以兼容性（已棄用）
    @Deprecated
    public static void queryPlayerOnlineStatus(List<String> playerNames) {
        // 轉換為新格式
        sendPlayerOnlineStatus(playerNames, "CONNECTED");
    }

    @Deprecated
    public static void queryPlayerOnlineStatus(String playerName) {
        // 轉換為新格式
        sendPlayerOnlineStatus(playerName, "CONNECTED");
    }

    /**
     * Message 類（基於 pikacnu 格式）
     */
    public static class Message {
        public String action;
        public String sessionId;
        public Payload payload;
        public long timestamp;

        public Message() {}

        public String toJson() {
            return gson.toJson(this);
        }
    }

    /**
     * Payload 類（基於 pikacnu 格式）
     */
    public static class Payload {
        public PlayerOnlineStatus playerOnlineStatus;

        public static class PlayerOnlineStatus {
            public ArrayList<String> uuids;
            public String connection;

            public PlayerOnlineStatus() {}
        }
    }
}
