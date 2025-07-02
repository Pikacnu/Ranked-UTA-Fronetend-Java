package com.uta2.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.uta2.data.*;
import com.uta2.ws.WebSocket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.literal;

public class QueueCommand implements ICommand {
    private static final Gson gson = new Gson();

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("queue")
            .requires(source -> source.hasPermissionLevel(0))
            .then(literal("solo")
                .executes(context -> executeJoinQueue(context.getSource(), "solo")))
            .then(literal("duo")
                .executes(context -> executeJoinQueue(context.getSource(), "duo")))
            .then(literal("squad")
                .executes(context -> executeJoinQueue(context.getSource(), "squad")))
            .then(literal("siege")
                .executes(context -> executeJoinQueue(context.getSource(), "siege")))
            .then(literal("leave")
                .executes(context -> executeLeaveQueue(context.getSource())))
        );
    }

    private int executeJoinQueue(ServerCommandSource source, String queueType) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendFeedback(() -> Text.literal("只有玩家可以執行此指令").formatted(Formatting.RED), false);
            return 0;
        }

        UUID playerUuid = player.getUuid();
        PlayerData playerData = PlayerDatabase.getPlayer(playerUuid);
        
        if (playerData == null) {
            source.sendFeedback(() -> Text.literal("找不到玩家資料").formatted(Formatting.RED), false);
            return 0;
        }

        if (playerData.isQueuing()) {
            source.sendFeedback(() -> Text.literal("你已經在佇列中了").formatted(Formatting.RED), false);
            return 0;
        }

        PartyData party = PartyDatabase.getPartyByMember(playerUuid);
        
        // If player is in a party, only leader can queue for the party
        if (party != null && !party.getLeader().equals(playerUuid)) {
            source.sendFeedback(() -> Text.literal("只有隊長可以讓整個隊伍加入佇列").formatted(Formatting.RED), false);
            return 0;
        }

        // Validate queue type for party size
        if (party != null) {
            int partySize = party.getMembers().size();
            if (!isValidQueueTypeForPartySize(queueType, partySize)) {
                source.sendFeedback(() -> 
                    Text.literal("隊伍人數 (" + partySize + ") 不符合 " + queueType + " 模式").formatted(Formatting.RED), false);
                return 0;
            }
        }

        // Update queuing status
        if (party != null) {
            // Update all party members
            for (UUID memberUuid : party.getMembers()) {
                PlayerData memberData = PlayerDatabase.getPlayer(memberUuid);
                if (memberData != null) {
                    memberData.setQueuing(true);
                }
            }
        } else {
            // Solo player
            playerData.setQueuing(true);
        }

        // Notify backend
        JsonObject data = new JsonObject();
        data.addProperty("action", "QUEUE_UPDATE");
        data.addProperty("subAction", "JOIN");
        data.addProperty("queueType", queueType);
        data.addProperty("playerUuid", playerUuid.toString());
        
        if (party != null) {
            data.addProperty("partyId", party.getPartyId());
            data.addProperty("partySize", party.getMembers().size());
        }
        
        WebSocket.send(gson.toJson(data));

        // Send feedback
        String message = party != null ? 
            "隊伍已加入 " + queueType + " 佇列" : 
            "你已加入 " + queueType + " 佇列";
        source.sendFeedback(() -> Text.literal(message).formatted(Formatting.GREEN), false);

        // Notify party members if applicable
        if (party != null) {
            for (UUID memberUuid : party.getMembers()) {
                if (!memberUuid.equals(playerUuid)) {
                    ServerPlayerEntity member = source.getServer().getPlayerManager().getPlayer(memberUuid);
                    if (member != null) {
                        member.sendMessage(Text.literal("隊長讓隊伍加入了 " + queueType + " 佇列").formatted(Formatting.GREEN));
                    }
                }
            }
        }

        return 1;
    }

    private int executeLeaveQueue(ServerCommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendFeedback(() -> Text.literal("只有玩家可以執行此指令").formatted(Formatting.RED), false);
            return 0;
        }

        UUID playerUuid = player.getUuid();
        PlayerData playerData = PlayerDatabase.getPlayer(playerUuid);
        
        if (playerData == null) {
            source.sendFeedback(() -> Text.literal("找不到玩家資料").formatted(Formatting.RED), false);
            return 0;
        }

        if (!playerData.isQueuing()) {
            source.sendFeedback(() -> Text.literal("你不在任何佇列中").formatted(Formatting.RED), false);
            return 0;
        }

        PartyData party = PartyDatabase.getPartyByMember(playerUuid);
        
        // If player is in a party, only leader can remove party from queue
        if (party != null && !party.getLeader().equals(playerUuid)) {
            source.sendFeedback(() -> Text.literal("只有隊長可以讓隊伍離開佇列").formatted(Formatting.RED), false);
            return 0;
        }

        // Update queuing status
        if (party != null) {
            // Update all party members
            for (UUID memberUuid : party.getMembers()) {
                PlayerData memberData = PlayerDatabase.getPlayer(memberUuid);
                if (memberData != null) {
                    memberData.setQueuing(false);
                }
            }
        } else {
            // Solo player
            playerData.setQueuing(false);
        }

        // Notify backend
        JsonObject data = new JsonObject();
        data.addProperty("action", "QUEUE_UPDATE");
        data.addProperty("subAction", "LEAVE");
        data.addProperty("playerUuid", playerUuid.toString());
        
        if (party != null) {
            data.addProperty("partyId", party.getPartyId());
        }
        
        WebSocket.send(gson.toJson(data));

        // Send feedback
        String message = party != null ? "隊伍已離開佇列" : "你已離開佇列";
        source.sendFeedback(() -> Text.literal(message).formatted(Formatting.YELLOW), false);

        // Notify party members if applicable
        if (party != null) {
            for (UUID memberUuid : party.getMembers()) {
                if (!memberUuid.equals(playerUuid)) {
                    ServerPlayerEntity member = source.getServer().getPlayerManager().getPlayer(memberUuid);
                    if (member != null) {
                        member.sendMessage(Text.literal("隊長讓隊伍離開了佇列").formatted(Formatting.YELLOW));
                    }
                }
            }
        }

        return 1;
    }

    private boolean isValidQueueTypeForPartySize(String queueType, int partySize) {
        return switch (queueType) {
            case "solo" -> partySize == 1;
            case "duo" -> partySize <= 2;
            case "squad" -> partySize <= 4;
            case "siege" -> partySize <= 8;
            default -> false;
        };
    }
}
