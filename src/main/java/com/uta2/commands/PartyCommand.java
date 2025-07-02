package com.uta2.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.uta2.data.*;
import com.uta2.ws.WebSocket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PartyCommand implements ICommand {
    private static final Gson gson = new Gson();

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("party")
            .requires(source -> source.hasPermissionLevel(0))
            .then(literal("create")
                .executes(context -> executeCreate(context.getSource())))
            .then(literal("invite")
                .then(argument("player", StringArgumentType.string())
                    .executes(context -> executeInvite(context.getSource(), 
                        StringArgumentType.getString(context, "player")))))
            .then(literal("accept")
                .then(argument("player", StringArgumentType.string())
                    .executes(context -> executeAccept(context.getSource(), 
                        StringArgumentType.getString(context, "player")))))
            .then(literal("decline")
                .then(argument("player", StringArgumentType.string())
                    .executes(context -> executeDecline(context.getSource(), 
                        StringArgumentType.getString(context, "player")))))
            .then(literal("leave")
                .executes(context -> executeLeave(context.getSource())))
            .then(literal("kick")
                .then(argument("player", StringArgumentType.string())
                    .executes(context -> executeKick(context.getSource(), 
                        StringArgumentType.getString(context, "player")))))
            .then(literal("disband")
                .executes(context -> executeDisband(context.getSource())))
            .then(literal("list")
                .executes(context -> executeList(context.getSource())))
        );
    }

    private int executeCreate(ServerCommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendFeedback(() -> Text.literal("只有玩家可以執行此指令").formatted(Formatting.RED), false);
            return 0;
        }

        UUID playerUuid = player.getUuid();
        
        // Check if player is already in a party
        if (PartyDatabase.getPartyByMember(playerUuid) != null) {
            source.sendFeedback(() -> Text.literal(PartyResultMessage.ALREADY_IN_PARTY.getMessage()).formatted(Formatting.RED), false);
            return 0;
        }

        // Create new party
        String partyId = UUID.randomUUID().toString();
        ArrayList<UUID> members = new ArrayList<>();
        members.add(playerUuid);
        
        PartyData party = new PartyData(partyId, playerUuid, members);
        PartyDatabase.addParty(party);

        // Update player data
        PlayerData playerData = PlayerDatabase.getPlayer(playerUuid);
        if (playerData != null) {
            playerData.setPartyId(partyId);
        }

        // Notify backend
        notifyBackend("PARTY_CREATE", partyId, playerUuid, null);

        source.sendFeedback(() -> Text.literal(PartyResultMessage.PARTY_CREATED.getMessage()).formatted(Formatting.GREEN), false);
        return 1;
    }

    private int executeInvite(ServerCommandSource source, String targetPlayerName) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendFeedback(() -> Text.literal("只有玩家可以執行此指令").formatted(Formatting.RED), false);
            return 0;
        }

        UUID playerUuid = player.getUuid();
        PartyData party = PartyDatabase.getPartyByMember(playerUuid);

        if (party == null) {
            source.sendFeedback(() -> Text.literal(PartyResultMessage.NOT_IN_PARTY.getMessage()).formatted(Formatting.RED), false);
            return 0;
        }

        if (!party.getLeader().equals(playerUuid)) {
            source.sendFeedback(() -> Text.literal(PartyResultMessage.NOT_LEADER.getMessage()).formatted(Formatting.RED), false);
            return 0;
        }

        if (player.getName().getString().equals(targetPlayerName)) {
            source.sendFeedback(() -> Text.literal(PartyResultMessage.CANNOT_INVITE_SELF.getMessage()).formatted(Formatting.RED), false);
            return 0;
        }

        // Find target player
        ServerPlayerEntity targetPlayer = source.getServer().getPlayerManager().getPlayer(targetPlayerName);
        if (targetPlayer == null) {
            source.sendFeedback(() -> Text.literal(PartyResultMessage.PLAYER_NOT_FOUND.getMessage()).formatted(Formatting.RED), false);
            return 0;
        }

        UUID targetUuid = targetPlayer.getUuid();

        // Check if target is already in a party
        if (PartyDatabase.getPartyByMember(targetUuid) != null) {
            source.sendFeedback(() -> Text.literal(PartyResultMessage.PLAYER_ALREADY_IN_PARTY.getMessage()).formatted(Formatting.RED), false);
            return 0;
        }

        // Create invitation
        PartyInvitation invitation = new PartyInvitation(playerUuid, targetUuid);
        PartyDatabase.addInvitation(invitation);

        // Notify both players
        source.sendFeedback(() -> Text.literal(PartyResultMessage.INVITATION_SENT.getMessage()).formatted(Formatting.GREEN), false);
        targetPlayer.sendMessage(Text.literal("你收到來自 " + player.getName().getString() + " 的隊伍邀請！").formatted(Formatting.YELLOW));
        targetPlayer.sendMessage(Text.literal("使用 /party accept " + player.getName().getString() + " 來接受邀請").formatted(Formatting.GRAY));

        // Notify backend
        notifyBackend("PARTY_INVITE", party.getPartyId(), playerUuid, targetUuid);

        return 1;
    }

    private int executeAccept(ServerCommandSource source, String inviterName) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendFeedback(() -> Text.literal("只有玩家可以執行此指令").formatted(Formatting.RED), false);
            return 0;
        }

        UUID playerUuid = player.getUuid();
        PartyInvitation invitation = PartyDatabase.getInvitation(playerUuid);

        if (invitation == null) {
            source.sendFeedback(() -> Text.literal(PartyResultMessage.NO_INVITATION.getMessage()).formatted(Formatting.RED), false);
            return 0;
        }

        if (invitation.isExpired()) {
            PartyDatabase.removeInvitation(playerUuid);
            source.sendFeedback(() -> Text.literal(PartyResultMessage.INVITATION_EXPIRED.getMessage()).formatted(Formatting.RED), false);
            return 0;
        }

        // Find inviter
        ServerPlayerEntity inviter = source.getServer().getPlayerManager().getPlayer(invitation.getInviter());
        if (inviter == null || !inviter.getName().getString().equals(inviterName)) {
            source.sendFeedback(() -> Text.literal(PartyResultMessage.NO_INVITATION.getMessage()).formatted(Formatting.RED), false);
            return 0;
        }

        PartyData party = PartyDatabase.getPartyByMember(invitation.getInviter());
        if (party == null) {
            PartyDatabase.removeInvitation(playerUuid);
            source.sendFeedback(() -> Text.literal("邀請已無效").formatted(Formatting.RED), false);
            return 0;
        }

        // Add player to party
        party.getMembers().add(playerUuid);
        
        // Update player data
        PlayerData playerData = PlayerDatabase.getPlayer(playerUuid);
        if (playerData != null) {
            playerData.setPartyId(party.getPartyId());
        }

        // Remove invitation
        PartyDatabase.removeInvitation(playerUuid);

        // Notify players
        source.sendFeedback(() -> Text.literal(PartyResultMessage.PLAYER_JOINED.getMessage()).formatted(Formatting.GREEN), false);
        inviter.sendMessage(Text.literal(player.getName().getString() + " 已加入隊伍！").formatted(Formatting.GREEN));

        // Notify backend
        notifyBackend("PARTY_JOIN", party.getPartyId(), invitation.getInviter(), playerUuid);

        return 1;
    }

    private int executeDecline(ServerCommandSource source, String inviterName) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendFeedback(() -> Text.literal("只有玩家可以執行此指令").formatted(Formatting.RED), false);
            return 0;
        }

        UUID playerUuid = player.getUuid();
        PartyInvitation invitation = PartyDatabase.getInvitation(playerUuid);

        if (invitation == null) {
            source.sendFeedback(() -> Text.literal(PartyResultMessage.NO_INVITATION.getMessage()).formatted(Formatting.RED), false);
            return 0;
        }

        // Remove invitation
        PartyDatabase.removeInvitation(playerUuid);

        // Notify both players
        source.sendFeedback(() -> Text.literal("你已拒絕邀請").formatted(Formatting.YELLOW), false);
        
        ServerPlayerEntity inviter = source.getServer().getPlayerManager().getPlayer(invitation.getInviter());
        if (inviter != null) {
            inviter.sendMessage(Text.literal(player.getName().getString() + " 已拒絕你的邀請").formatted(Formatting.RED));
        }

        return 1;
    }

    private int executeLeave(ServerCommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendFeedback(() -> Text.literal("只有玩家可以執行此指令").formatted(Formatting.RED), false);
            return 0;
        }

        UUID playerUuid = player.getUuid();
        PartyData party = PartyDatabase.getPartyByMember(playerUuid);

        if (party == null) {
            source.sendFeedback(() -> Text.literal(PartyResultMessage.NOT_IN_PARTY.getMessage()).formatted(Formatting.RED), false);
            return 0;
        }

        // Remove player from party
        party.getMembers().remove(playerUuid);

        // Update player data
        PlayerData playerData = PlayerDatabase.getPlayer(playerUuid);
        if (playerData != null) {
            playerData.setPartyId(null);
        }

        // If leader left, transfer leadership or disband
        if (party.getLeader().equals(playerUuid)) {
            if (party.getMembers().isEmpty()) {
                // Disband party
                PartyDatabase.removeParty(party.getPartyId());
            } else {
                // Transfer leadership to next member
                UUID newLeader = party.getMembers().get(0);
                party.setLeader(newLeader);
                
                ServerPlayerEntity newLeaderPlayer = source.getServer().getPlayerManager().getPlayer(newLeader);
                if (newLeaderPlayer != null) {
                    newLeaderPlayer.sendMessage(Text.literal(PartyResultMessage.LEADER_CHANGED.getMessage() + "你").formatted(Formatting.YELLOW));
                }
            }
        }

        source.sendFeedback(() -> Text.literal(PartyResultMessage.PLAYER_LEFT.getMessage()).formatted(Formatting.YELLOW), false);

        // Notify backend
        notifyBackend("PARTY_LEAVE", party.getPartyId(), playerUuid, null);

        return 1;
    }

    private int executeKick(ServerCommandSource source, String targetPlayerName) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendFeedback(() -> Text.literal("只有玩家可以執行此指令").formatted(Formatting.RED), false);
            return 0;
        }

        UUID playerUuid = player.getUuid();
        PartyData party = PartyDatabase.getPartyByMember(playerUuid);

        if (party == null) {
            source.sendFeedback(() -> Text.literal(PartyResultMessage.NOT_IN_PARTY.getMessage()).formatted(Formatting.RED), false);
            return 0;
        }

        if (!party.getLeader().equals(playerUuid)) {
            source.sendFeedback(() -> Text.literal(PartyResultMessage.NOT_LEADER.getMessage()).formatted(Formatting.RED), false);
            return 0;
        }

        if (player.getName().getString().equals(targetPlayerName)) {
            source.sendFeedback(() -> Text.literal(PartyResultMessage.CANNOT_KICK_SELF.getMessage()).formatted(Formatting.RED), false);
            return 0;
        }

        // Find target player
        ServerPlayerEntity targetPlayer = source.getServer().getPlayerManager().getPlayer(targetPlayerName);
        if (targetPlayer == null) {
            source.sendFeedback(() -> Text.literal(PartyResultMessage.PLAYER_NOT_FOUND.getMessage()).formatted(Formatting.RED), false);
            return 0;
        }

        UUID targetUuid = targetPlayer.getUuid();

        // Check if target is in the same party
        if (!party.getMembers().contains(targetUuid)) {
            source.sendFeedback(() -> Text.literal("該玩家不在你的隊伍中").formatted(Formatting.RED), false);
            return 0;
        }

        // Remove player from party
        party.getMembers().remove(targetUuid);

        // Update player data
        PlayerData targetPlayerData = PlayerDatabase.getPlayer(targetUuid);
        if (targetPlayerData != null) {
            targetPlayerData.setPartyId(null);
        }

        // Notify players
        source.sendFeedback(() -> Text.literal(PartyResultMessage.PLAYER_KICKED.getMessage()).formatted(Formatting.YELLOW), false);
        targetPlayer.sendMessage(Text.literal("你已被踢出隊伍").formatted(Formatting.RED));

        // Notify other party members
        for (UUID memberUuid : party.getMembers()) {
            if (!memberUuid.equals(playerUuid)) {
                ServerPlayerEntity member = source.getServer().getPlayerManager().getPlayer(memberUuid);
                if (member != null) {
                    member.sendMessage(Text.literal(targetPlayerName + " 被踢出隊伍").formatted(Formatting.YELLOW));
                }
            }
        }

        // Notify backend
        notifyBackend("PARTY_KICK", party.getPartyId(), playerUuid, targetUuid);

        return 1;
    }

    private int executeDisband(ServerCommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendFeedback(() -> Text.literal("只有玩家可以執行此指令").formatted(Formatting.RED), false);
            return 0;
        }

        UUID playerUuid = player.getUuid();
        PartyData party = PartyDatabase.getPartyByMember(playerUuid);

        if (party == null) {
            source.sendFeedback(() -> Text.literal(PartyResultMessage.NOT_IN_PARTY.getMessage()).formatted(Formatting.RED), false);
            return 0;
        }

        if (!party.getLeader().equals(playerUuid)) {
            source.sendFeedback(() -> Text.literal(PartyResultMessage.NOT_LEADER.getMessage()).formatted(Formatting.RED), false);
            return 0;
        }

        // Notify all party members
        for (UUID memberUuid : party.getMembers()) {
            ServerPlayerEntity member = source.getServer().getPlayerManager().getPlayer(memberUuid);
            if (member != null) {
                member.sendMessage(Text.literal(PartyResultMessage.PARTY_DISBANDED.getMessage()).formatted(Formatting.RED));
            }

            // Update player data
            PlayerData memberData = PlayerDatabase.getPlayer(memberUuid);
            if (memberData != null) {
                memberData.setPartyId(null);
                memberData.setQueuing(false); // Also remove from queue
            }
        }

        // Remove party
        PartyDatabase.removeParty(party.getPartyId());

        // Notify backend
        notifyBackend("PARTY_DISBAND", party.getPartyId(), playerUuid, null);

        return 1;
    }

    private int executeList(ServerCommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendFeedback(() -> Text.literal("只有玩家可以執行此指令").formatted(Formatting.RED), false);
            return 0;
        }

        UUID playerUuid = player.getUuid();
        PartyData party = PartyDatabase.getPartyByMember(playerUuid);

        if (party == null) {
            source.sendFeedback(() -> Text.literal(PartyResultMessage.NOT_IN_PARTY.getMessage()).formatted(Formatting.RED), false);
            return 0;
        }

        source.sendFeedback(() -> Text.literal("隊伍成員:").formatted(Formatting.GOLD), false);
        
        for (UUID memberUuid : party.getMembers()) {
            ServerPlayerEntity member = source.getServer().getPlayerManager().getPlayer(memberUuid);
            if (member != null) {
                String prefix = memberUuid.equals(party.getLeader()) ? "[隊長] " : "";
                source.sendFeedback(() -> Text.literal("- " + prefix + member.getName().getString()).formatted(Formatting.WHITE), false);
            }
        }

        return 1;
    }

    private void notifyBackend(String action, String partyId, UUID initiator, UUID target) {
        JsonObject data = new JsonObject();
        data.addProperty("action", "PARTY_UPDATE");
        data.addProperty("subAction", action);
        data.addProperty("partyId", partyId);
        data.addProperty("initiator", initiator.toString());
        if (target != null) {
            data.addProperty("target", target.toString());
        }
        
        WebSocket.send(gson.toJson(data));
    }
}
