# Party 系統邏輯修正建議

## 1. 玩家狀態同步問題
- 確保所有涉及 `isInParty` 與 `partyId` 狀態的操作（如加入、離開、解散隊伍）都能正確同步 `PlayerDatabase` 與 `PartyData`。
- 建議所有隊伍成員異動後，強制呼叫 `PlayerDatabase.updatePlayerData`，並考慮加鎖或同步處理避免多執行緒下狀態不一致。

## 2. 隊長轉移與隊伍解散
- 當隊長離開時，若 `partyMembers` 為空，應避免執行 `partyLeaderUUID = partyMembers.get(0).uuid;`，需先檢查成員數量。
- 建議在 `removePlayer` 內，隊長離開且剩餘成員為 0 時，直接呼叫 `disbandParty` 並 return，避免後續重複操作。

## 3. 邀請與接受邀請流程
- `createInvitation` 建議先檢查邀請者是否在隊伍且為隊長，並檢查目標玩家是否已在隊伍。
- `AcceptInvitation` 處理多重邀請時，建議加鎖或同步，避免 race condition 導致重複加入或資料遺漏。

## 4. 分數範圍檢查
- 建議將分數檢查條件改為：新成員分數需同時滿足 `minimumScore() - 300 <= player.score <= maximumScore() + 300`，避免兩端繞過。

## 5. 多重邀請與過期邀請
- 多重邀請移除時，建議根據 `partyId` 與 `targetUuid` 一起過濾，確保所有相關邀請都能正確移除。
- 過期邀請清理時，建議先判斷玩家是否在線，避免 NullPointerException。

## 6. 其他建議
- `createParty` 建議使用更嚴謹的唯一 ID 產生方式（如 UUID），避免同時建立多隊伍時發生 ID 衝突。
- `removePlayer` 及 `disbandParty` 內部呼叫順序需檢查，避免重複移除或多次通知伺服器。
- 建議所有 public static 操作都加上同步鎖，避免多執行緒下資料競爭。

---
如需範例程式碼或進一步細節，請再告知！
