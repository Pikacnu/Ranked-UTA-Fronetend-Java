# JSON 重構總結

## 重構內容

已成功將原本的 `json.java` 大型檔案重構為組織良好的包結構：

### 新的包結構

```
src/main/java/com/pikacnu/src/json/
├── Action.java                 # 動作類型枚舉
├── Status.java                 # 狀態枚舉  
├── KillType.java              # 擊殺類型枚舉
├── JsonUtils.java             # JSON 工具類
└── data/                      # 資料類別包
    ├── Kill.java              # 擊殺事件資料
    ├── Damage.java            # 傷害事件資料
    ├── GameStatus.java        # 遊戲狀態資料
    ├── MapChoose.java         # 地圖選擇資料
    ├── StorageData.java       # 儲存資料
    ├── ClientId.java          # 客戶端ID資料
    ├── TeamInfo.java          # 隊伍資訊資料
    ├── Queue.java             # 佇列資料
    ├── PlayerDataRequest.java # 玩家資料請求
    ├── Payload.java           # 有效載荷資料
    └── Message.java           # 訊息資料
```

### 重構優點

1. **更好的組織性**: 將相關功能分組到不同的檔案中
2. **更清楚的職責分離**: 每個類別都有明確的職責
3. **更容易維護**: 小檔案比大檔案更容易理解和修改
4. **更好的重用性**: 可以單獨匯入需要的類別
5. **遵循 Java 命名慣例**: 使用 PascalCase 命名類別

### 修正的檔案

已更新以下檔案的引用：

#### 主要檔案
- `WebSocket.java` - 更新所有 JSON 類別引用
- `PlayerDatabase.java` - 新增 data 包引用
- `PartyDatabase.java` - 新增 data 包引用
- `PlayerOnlineChecker.java` - 更新引用路徑

#### 命令檔案
- `SendWsCommand.java` - 更新 Message, Payload 引用
- `SendDataCommand.java` - 更新 Message, Payload, StorageData 引用
- `QueueCommand.java` - 更新所有相關引用
- `PlayerKilledCommand.java` - 更新 Kill, Payload, Message 引用
- `MapChooseCommand.java` - 更新 MapChoose, Payload, Message 引用
- `GetDataCommand.java` - 更新 Payload 引用
- `GameStatusCommand.java` - 更新 GameStatus, Payload, Message 引用
- `DamagedCommand.java` - 更新 Damage, Payload, Message 引用

### 類別名稱變更

部分類別名稱已從小寫改為 PascalCase：

- `json.kill` → `Kill`
- `json.damage` → `Damage`
- `json.game_status` → `GameStatus`
- `json.map_choose` → `MapChoose`
- `json.storage_data` → `StorageData`
- `json.clientId` → `ClientId`
- `json.team_info` → `TeamInfo`
- `json.teamData` → `TeamData`

## 結果

- ✅ 所有檔案編譯無錯誤
- ✅ 保持原有功能不變
- ✅ 改善代碼組織和可維護性
- ✅ 遵循 Java 最佳實踐

重構完成！整個專案現在擁有更清楚、更容易維護的 JSON 處理架構。
