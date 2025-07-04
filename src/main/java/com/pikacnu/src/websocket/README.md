# WebSocket 重構說明

## 重構目標
將原有的單一 `websocket.java` 檔案重構為模組化的架構，將不同的 Action 處理邏輯拆分到獨立的 Handler 類別中。

## 新的目錄結構

```
src/main/java/com/pikacnu/src/
├── websocket.java                    # 向後相容的主要入口點
├── websocket_old.java               # 原有的實作（備份）
└── websocket/                       # 新的模組化架構
    ├── WebSocketClient.java         # 主要的 WebSocket 客戶端類別
    └── handler/                     # Handler 資料夾
        ├── BaseHandler.java         # 抽象基底 Handler 類別
        ├── HandshakeHandler.java    # 處理 HANDSHAKE action
        ├── HeartbeatHandler.java    # 處理 HEARTBEAT action
        ├── CommandHandler.java      # 處理 COMMAND action
        ├── TeamJoinHandler.java     # 處理 TEAM_JOIN action
        ├── TransferHandler.java     # 處理 TRANSFER action
        ├── WhitelistHandler.java    # 處理 WHILELIST_CHANGE action
        ├── PlayerDataHandler.java   # 處理 GET_PLAYER_DATA action
        ├── QueueMatchHandler.java   # 處理 QUEUE_MATCH action
        ├── RequestDataHandler.java  # 處理 REQUEST_DATA action
        ├── ErrorHandler.java        # 處理 ERROR action
        ├── DisconnectHandler.java   # 處理 DISCONNECT action
        └── PlayerOnlineStatusHandler.java # 處理 PLAYER_ONLINE_STATUS action
```

## 架構說明

### BaseHandler 抽象類別
所有的 Handler 都繼承自 `BaseHandler` 類別，並實作以下方法：
- `handle(Action action, Status status, String sessionId, Payload payload)` - 處理特定 Action 的邏輯
- `getActionType()` - 返回此 Handler 處理的 Action 類型
- `canHandle(Action action)` - 檢查是否可以處理指定的 Action

### WebSocketClient 類別
新的主要客戶端類別，負責：
- WebSocket 連接管理
- 訊息解析和分發
- Handler 管理和初始化
- 任務佇列管理

### 向後相容性
原有的 `websocket.java` 檔案保持 API 相容性，所有現有的程式碼都可以無縫遷移到新架構。

## 使用方式

### 基本使用（與原有相同）
```java
// 初始化
WebSocket.init(minecraftServer);

// 發送訊息
WebSocket.sendMessage(message);

// 檢查連接狀態
boolean connected = WebSocket.isConnected();

// 添加任務
WebSocket.addTask("action", function);
```

### 新架構直接使用
```java
// 使用新的 WebSocketClient
WebSocketClient.init(minecraftServer);
WebSocketClient.sendMessage(message);
```

## 新增 Handler 的方式

1. 建立新的 Handler 類別，繼承 `BaseHandler`
2. 實作 `handle()` 和 `getActionType()` 方法
3. 在 `WebSocketClient.initializeHandlers()` 中註冊新的 Handler

範例：
```java
public class CustomHandler extends BaseHandler {
    public CustomHandler(MinecraftServer server) {
        super(server);
    }
    
    @Override
    public void handle(Action action, Status status, String sessionId, Payload payload) {
        // 處理邏輯
    }
    
    @Override
    public Action getActionType() {
        return Action.CUSTOM_ACTION;
    }
}
```

## 優點

1. **模組化**: 每個 Action 的處理邏輯都在獨立的類別中
2. **可維護性**: 易於修改和擴展特定 Action 的處理邏輯
3. **可測試性**: 每個 Handler 都可以獨立進行單元測試
4. **向後相容**: 現有程式碼無需修改即可使用新架構
5. **清晰的責任分離**: 每個類別都有明確的職責

## 遷移建議

1. 現有程式碼可以繼續使用原有的 `WebSocket` API
2. 新的功能建議使用 `WebSocketClient` 和 Handler 架構
3. 逐步將現有的邏輯遷移到對應的 Handler 中
4. 在完全遷移後可以移除 `websocket_old.java` 備份檔案
