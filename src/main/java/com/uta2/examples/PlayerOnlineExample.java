package com.uta2.examples;

import com.uta2.helpers.PlayerOnlineChecker;
import java.util.Arrays;
import java.util.List;

/**
 * 示範如何使用更新後的 PlayerOnlineChecker
 * 現在支援 pikacnu JSON 格式
 */
public class PlayerOnlineExample {
    
    public static void main(String[] args) {
        // 設定伺服器 Session ID
        PlayerOnlineChecker.setServerSessionId("my-server-id");
        
        // 發送單個玩家連線狀態
        String playerUuid = "550e8400-e29b-41d4-a716-446655440000";
        
        // 通知玩家加入
        PlayerOnlineChecker.notifyPlayerConnected(playerUuid);
        
        // 通知玩家離開
        PlayerOnlineChecker.notifyPlayerDisconnected(playerUuid);
        
        // 發送多個玩家狀態
        List<String> playerUuids = Arrays.asList(
            "550e8400-e29b-41d4-a716-446655440000",
            "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
            "6ba7b811-9dad-11d1-80b4-00c04fd430c8"
        );
        
        PlayerOnlineChecker.sendPlayerOnlineStatus(playerUuids, "CONNECTED");
        
        // 使用舊方法（已棄用但仍然可用）
        PlayerOnlineChecker.queryPlayerOnlineStatus("PlayerName");
    }
    
    /**
     * 範例：在玩家加入事件中使用
     */
    public void onPlayerJoin(String playerUuid) {
        PlayerOnlineChecker.notifyPlayerConnected(playerUuid);
    }
    
    /**
     * 範例：在玩家離開事件中使用
     */
    public void onPlayerLeave(String playerUuid) {
        PlayerOnlineChecker.notifyPlayerDisconnected(playerUuid);
    }
}
