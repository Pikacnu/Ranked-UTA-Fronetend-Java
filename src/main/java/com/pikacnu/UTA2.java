package com.pikacnu;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.pikacnu.src.*;
import com.pikacnu.src.websocket.WebSocketClient;

/**
 * UTA2 主模組，負責初始化與伺服器事件註冊。
 */
public class UTA2 implements ModInitializer {
	public static final String MOD_ID = "uta2";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static MinecraftServer server;
	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");

		Config.init();

		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted); // 註冊開始事件
		ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping); // 註冊關閉事件

		ServerPlayConnectionEvents.JOIN.register(this::onPlayerJoin); // 註冊加入
		ServerPlayConnectionEvents.DISCONNECT.register(this::onPlayerDisconnect); // 註冊離開

		ServerPlayerEvents.JOIN.register(this::onPlayerLoad);

		Command.init();
		executorService.scheduleAtFixedRate(PartyDatabase::schedulePartyInvitationCleanup,
				0, 1, TimeUnit.SECONDS); // 每秒執行
	}

	private void onServerStarted(MinecraftServer server) {
		UTA2.server = server;
		WebSocketClient.init(server);
		PartyDatabase.server = server;
		WhiteListManager.server = server;
		LOGGER.info("Server started, instance acquired");
	}

	private void onServerStopping(MinecraftServer server) {
		UTA2.server = null;
		LOGGER.info("Server stopping");
		if (WebSocketClient.isConnected()) {
			WebSocketClient.shutdown();
			LOGGER.info("WebSocket connection closed");
		}
		PlayerDatabase.clear();
		PartyDatabase.clear();
		executorService.shutdown();
		WebSocketClient.scheduler.shutdownNow();
		PlayerOnlineChecker.scheduler.shutdownNow();
		Config.saveConfig();
	}

	private void onPlayerJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
		PlayerEntity player = handler.getPlayer();
		String playerName = player.getName().getString();
		String playerUUID = player.getUuidAsString();
		LOGGER.info("Player joined: {}", playerName);

		if (!Config.isLobby) {
			PlayerOnlineChecker.addPlayer(playerUUID);
			return;
		}

		PlayerDatabase.addPlayerData(new PlayerDatabase.PlayerData(playerUUID, playerName, 0));
		PlayerDatabase.updatePlayerDataFromServer(playerUUID, playerName);
	}

	private void onPlayerLoad(ServerPlayerEntity player) {
		LOGGER.info("Player loaded: {}", player.getName().getString());
		PlayerOnlineChecker.addPlayer(player.getUuid().toString());
	}

	private void onPlayerDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
		PlayerEntity player = handler.getPlayer();
		String playerUUID = player.getUuidAsString();
		String playerName = player.getName().getString();
		LOGGER.info("Player disconnected: {}", playerName);

		if (!Config.isLobby) // 遊戲伺服器
		{
			PlayerOnlineChecker.removePlayer(playerUUID);
			return;
		}

		// 大廳伺服器
		PlayerDatabase.removePlayerData(playerUUID);
		PartyDatabase.PartyData party = PartyDatabase.getPartyData(playerUUID);
		if (party == null) {
			LOGGER.info("No party found for player: {}", playerName);
			return;
		}

		party.removePlayer(playerUUID);
		LOGGER.info("Player removed from party: {}", party.partyId);
		if (party.partyMembers.isEmpty()) {
			PartyDatabase.removeParty(party.partyId);
			LOGGER.info("Party removed due to no members left");
		}
	}

	public static MinecraftServer getServer() {
		return server;
	}
}