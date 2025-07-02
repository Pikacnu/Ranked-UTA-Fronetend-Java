# UTA2 PlayerOnlineChecker 更新說明

## 概述

PlayerOnlineChecker 已更新以支援 pikacnu JSON 格式，同時保持與舊 UTA2 格式的向後兼容性。

## 主要變更

### 1. 新的 JSON 格式支援

現在支援 pikacnu 格式的結構化消息：

```json
{
  "action": "player_online_status",
  "sessionId": "server-session-id",
  "payload": {
    "playerOnlineStatus": {
      "uuids": ["550e8400-e29b-41d4-a716-446655440000"],
      "connection": "CONNECTED"
    }
  },
  "timestamp": 1672531200000
}
```

### 2. 新的 API 方法

#### 推薦使用的新方法：

- `sendPlayerOnlineStatus(List<String> uuids, String connection)` - 發送多個玩家狀態
- `sendPlayerOnlineStatus(String uuid, String connection)` - 發送單個玩家狀態
- `notifyPlayerConnected(String uuid)` - 通知玩家連線
- `notifyPlayerDisconnected(String uuid)` - 通知玩家離線
- `setServerSessionId(String sessionId)` - 設定伺服器會話 ID

#### 已棄用的舊方法：

- `queryPlayerOnlineStatus(List<String> playerNames)` - 已棄用，請使用 `sendPlayerOnlineStatus`
- `queryPlayerOnlineStatus(String playerName)` - 已棄用，請使用 `sendPlayerOnlineStatus`

### 3. WebSocket 處理器更新

WebSocket 類現在能夠：
- 自動檢測消息格式（pikacnu 或 legacy）
- 處理兩種格式的 `player_online_status` 消息
- 保持與舊系統的兼容性

## 使用範例

```java
// 設定伺服器會話 ID
PlayerOnlineChecker.setServerSessionId("my-server-id");

// 通知玩家加入
String playerUuid = "550e8400-e29b-41d4-a716-446655440000";
PlayerOnlineChecker.notifyPlayerConnected(playerUuid);

// 通知玩家離開
PlayerOnlineChecker.notifyPlayerDisconnected(playerUuid);

// 發送多個玩家狀態
List<String> uuids = Arrays.asList("uuid1", "uuid2", "uuid3");
PlayerOnlineChecker.sendPlayerOnlineStatus(uuids, "CONNECTED");
```

## 向後兼容性

- 舊的方法仍然可用，但已標記為 `@Deprecated`
- WebSocket 自動檢測消息格式，無需手動配置
- 現有代碼無需立即更新，但建議遷移到新 API

## 連線狀態值

支援的連線狀態：
- `"CONNECTED"` - 玩家已連線
- `"DISCONNECTED"` - 玩家已離線

## 遷移指南

### 從舊 API 遷移：

```java
// 舊方式（已棄用）
PlayerOnlineChecker.queryPlayerOnlineStatus("PlayerName");

// 新方式
PlayerOnlineChecker.notifyPlayerConnected("player-uuid");
```

### 建議的實作方式：

```java
public class PlayerEventHandler {
    
    public void onPlayerJoin(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        PlayerOnlineChecker.notifyPlayerConnected(uuid);
    }
    
    public void onPlayerLeave(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        PlayerOnlineChecker.notifyPlayerDisconnected(uuid);
    }
}
```

## 技術細節

### Message 結構

```java
public static class Message {
    public String action;          // 動作類型
    public String sessionId;       // 伺服器會話 ID
    public Payload payload;        // 消息內容
    public long timestamp;         // 時間戳
}
```

### Payload 結構

```java
public static class Payload {
    public PlayerOnlineStatus playerOnlineStatus;
    
    public static class PlayerOnlineStatus {
        public ArrayList<String> uuids;    // 玩家 UUID 列表
        public String connection;          // 連線狀態
    }
}
```

這些變更確保了與 pikacnu 系統的完全兼容性，同時保持了代碼的可維護性和向後兼容性。
