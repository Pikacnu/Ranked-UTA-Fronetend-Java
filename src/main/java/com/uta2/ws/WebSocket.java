package com.uta2.ws;

import com.uta2.config.Config;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocket {
    private static WebSocketClient client;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static boolean isConnected = false;
    private static final Gson gson = new Gson();

    public static void connect() {
        try {
            URI uri = new URI("ws://" + Config.wsHost + ":" + Config.wsPort + Config.wsPath);
            client = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("[UTA2] WebSocket connected!");
                    isConnected = true;
                    // Send authentication
                    JsonObject authData = new JsonObject();
                    authData.addProperty("action", "AUTH");
                    authData.addProperty("serverName", Config.serverName);
                    authData.addProperty("token", Config.token);
                    send(gson.toJson(authData));
                }

                @Override
                public void onMessage(String message) {
                    System.out.println("[UTA2] Received message: " + message);
                    // Handle incoming messages
                    handleMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("[UTA2] WebSocket disconnected. Reason: " + reason);
                    isConnected = false;
                    scheduleReconnect();
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("[UTA2] WebSocket error: " + ex.getMessage());
                    // Error does not always mean disconnection, onClose will handle it
                }
            };
            client.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private static void handleMessage(String message) {
        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String action = null;
            
            // 檢查是否為 pikacnu 格式（包含 sessionId 和 payload）
            boolean isPikacnuFormat = json.has("sessionId") && json.has("payload");
            
            if (isPikacnuFormat) {
                // pikacnu 格式：從 action 字段直接獲取
                action = json.get("action").getAsString();
                // 取得 payload 用於進一步處理
                JsonObject payload = json.getAsJsonObject("payload");
                handlePikacnuMessage(action, payload, json);
            } else {
                // 舊的 UTA2 格式：直接從 action 字段獲取
                action = json.get("action").getAsString();
                handleLegacyMessage(action, json);
            }
        } catch (Exception e) {
            System.err.println("[UTA2] Error handling message: " + e.getMessage());
        }
    }

    /**
     * 處理 pikacnu 格式的消息
     */
    private static void handlePikacnuMessage(String action, JsonObject payload, JsonObject fullMessage) {
        System.out.println("[UTA2] Handling pikacnu format message: " + action);
        
        switch (action) {
            case "player_online_status":
                handlePikacnuPlayerOnlineStatus(payload);
                break;
            case "connect":
            case "disconnect":
            case "command":
            case "error":
            case "kill":
            case "damage":
            case "game_status":
            case "map_choose":
            case "storage_data":
            case "clientId":
            case "team_info":
            case "team_join":
            case "heartbeat":
            case "handshake":
            case "request_data":
            case "get_player_data":
            case "party":
            case "party_disbanded":
            case "queue":
            case "queue_leave":
            case "queue_match":
            case "whitelist":
            case "whitelist_change":
            case "whitelist_remove":
            case "whitelist_check":
            case "transfer":
                System.out.println("[UTA2] Received pikacnu action: " + action + " (not yet implemented)");
                break;
            default:
                System.out.println("[UTA2] Unknown pikacnu action: " + action);
        }
    }

    /**
     * 處理舊的 UTA2 格式消息
     */
    private static void handleLegacyMessage(String action, JsonObject json) {
        switch (action) {
            case "UPDATE_PLAYER_DATA":
                handleUpdatePlayerData(json);
                break;
            case "UPDATE_PARTY_DATA":
                handleUpdatePartyData(json);
                break;
            case "WHITELIST_ADD":
                handleWhitelistAdd(json);
                break;
            case "WHITELIST_REMOVE":
                handleWhitelistRemove(json);
                break;
            case "SEND_TO_SERVER":
                handleSendToServer(json);
                break;
            case "LOBBY_DATA":
                handleLobbyData(json);
                break;
            case "PLAYER_ONLINE_STATUS":
                handlePlayerOnlineStatus(json);
                break;
            case "RUN_COMMAND":
                handleRunCommand(json);
                break;
            case "GET_DATA_CALLBACK":
                handleGetDataCallback(json);
                break;
            default:
                System.out.println("[UTA2] Unknown legacy action: " + action);
        }
    }

    /**
     * 處理 pikacnu 格式的玩家線上狀態
     */
    private static void handlePikacnuPlayerOnlineStatus(JsonObject payload) {
        try {
            if (payload.has("playerOnlineStatus")) {
                JsonObject playerOnlineStatus = payload.getAsJsonObject("playerOnlineStatus");
                
                if (playerOnlineStatus.has("uuids") && playerOnlineStatus.has("connection")) {
                    var uuids = playerOnlineStatus.getAsJsonArray("uuids");
                    String connection = playerOnlineStatus.get("connection").getAsString();
                    
                    System.out.println("[UTA2] Player online status update:");
                    System.out.println("  Connection: " + connection);
                    System.out.println("  UUIDs: " + uuids.toString());
                    
                    // 在這裡可以添加具體的處理邏輯
                    // 例如更新本地玩家狀態緩存等
                }
            }
        } catch (Exception e) {
            System.err.println("[UTA2] Error handling pikacnu player online status: " + e.getMessage());
        }
    }

    // Message handlers
    private static void handleUpdatePlayerData(JsonObject json) {
        try {
            String uuidStr = json.get("uuid").getAsString();
            UUID uuid = UUID.fromString(uuidStr);
            
            com.uta2.data.PlayerData playerData = com.uta2.data.PlayerDatabase.getPlayer(uuid);
            if (playerData != null) {
                if (json.has("score")) {
                    playerData.setScore(json.get("score").getAsInt());
                }
                if (json.has("partyId")) {
                    playerData.setPartyId(json.get("partyId").getAsString());
                }
                if (json.has("isQueuing")) {
                    playerData.setQueuing(json.get("isQueuing").getAsBoolean());
                }
            }
        } catch (Exception e) {
            System.err.println("[UTA2] Error handling UPDATE_PLAYER_DATA: " + e.getMessage());
        }
    }

    private static void handleUpdatePartyData(JsonObject json) {
        try {
            // Handle party data updates from backend
            System.out.println("[UTA2] Received party data update: " + json.toString());
        } catch (Exception e) {
            System.err.println("[UTA2] Error handling UPDATE_PARTY_DATA: " + e.getMessage());
        }
    }

    private static void handleWhitelistAdd(JsonObject json) {
        try {
            String playerName = json.get("playerName").getAsString();
            boolean success = com.uta2.helpers.WhiteListManager.add(playerName);
            System.out.println("[UTA2] Whitelist add " + playerName + ": " + (success ? "success" : "failed"));
        } catch (Exception e) {
            System.err.println("[UTA2] Error handling WHITELIST_ADD: " + e.getMessage());
        }
    }

    private static void handleWhitelistRemove(JsonObject json) {
        try {
            String playerName = json.get("playerName").getAsString();
            boolean success = com.uta2.helpers.WhiteListManager.remove(playerName);
            System.out.println("[UTA2] Whitelist remove " + playerName + ": " + (success ? "success" : "failed"));
        } catch (Exception e) {
            System.err.println("[UTA2] Error handling WHITELIST_REMOVE: " + e.getMessage());
        }
    }

    private static void handleSendToServer(JsonObject json) {
        try {
            // Handle sending player to another server
            System.out.println("[UTA2] Received send to server request: " + json.toString());
        } catch (Exception e) {
            System.err.println("[UTA2] Error handling SEND_TO_SERVER: " + e.getMessage());
        }
    }

    private static void handleLobbyData(JsonObject json) {
        try {
            // Handle lobby data from backend
            System.out.println("[UTA2] Received lobby data: " + json.toString());
        } catch (Exception e) {
            System.err.println("[UTA2] Error handling LOBBY_DATA: " + e.getMessage());
        }
    }

    private static void handlePlayerOnlineStatus(JsonObject json) {
        try {
            // Handle player online status response
            System.out.println("[UTA2] Received player online status: " + json.toString());
        } catch (Exception e) {
            System.err.println("[UTA2] Error handling PLAYER_ONLINE_STATUS: " + e.getMessage());
        }
    }

    private static void handleRunCommand(JsonObject json) {
        try {
            // Execute commands on the server
            if (json.has("commands")) {
                var commands = json.get("commands").getAsJsonArray();
                for (var commandElement : commands) {
                    String command = commandElement.getAsString();
                    // Execute command - this would need server context
                    System.out.println("[UTA2] Would execute command: " + command);
                }
            } else if (json.has("command")) {
                String command = json.get("command").getAsString();
                System.out.println("[UTA2] Would execute command: " + command);
            }
        } catch (Exception e) {
            System.err.println("[UTA2] Error handling RUN_COMMAND: " + e.getMessage());
        }
    }

    private static void handleGetDataCallback(JsonObject json) {
        try {
            String function = json.get("function").getAsString();
            String data = json.get("data").getAsString();
            
            // Execute the function with the data
            String fullCommand = function.replace("{data}", data);
            System.out.println("[UTA2] Would execute function: " + fullCommand);
            
        } catch (Exception e) {
            System.err.println("[UTA2] Error handling GET_DATA_CALLBACK: " + e.getMessage());
        }
    }

    public static void send(String message) {
        if (isConnected && client != null && client.isOpen()) {
            client.send(message);
        } else {
            System.err.println("[UTA2] Cannot send message, WebSocket is not connected.");
        }
    }

    public static void disconnect() {
        if (client != null) {
            client.close();
        }
        scheduler.shutdown();
    }

    public static boolean isConnected() {
        return isConnected;
    }

    private static void scheduleReconnect() {
        if (!scheduler.isShutdown()) {
            scheduler.schedule(WebSocket::connect, 10, TimeUnit.SECONDS);
        }
    }
}
