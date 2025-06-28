package com.pikacnu;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.pikacnu.src.Command;
import com.pikacnu.src.PartyDatabase;
import com.pikacnu.src.WebSocket;
import com.pikacnu.src.PartyDatabase.PartyData;
import com.pikacnu.src.PlayerDatabase;

/**
 * UTA2 主模組，負責初始化與伺服器事件註冊。
 */
public class UTA2 implements ModInitializer {
	/**
	 * 模組 ID
	 */
	public static final String MOD_ID = "uta2";
	/**
	 * 日誌記錄器
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * Minecraft 伺服器實例
	 */
	private static MinecraftServer server;

	/**
	 * 任務排程器
	 */
	private final ScheduledExecutorService executorService = java.util.concurrent.Executors.newScheduledThreadPool(1);

	/**
	 * 初始化方法，於模組載入時呼叫。
	 */
	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");

		// 註冊伺服器生命週期事件
		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
		ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);

		// 註冊玩家連線事件
		ServerPlayConnectionEvents.JOIN.register(this::onPlayerJoin);
		ServerPlayConnectionEvents.DISCONNECT.register(this::onPlayerDisconnect);
		Command.init();

		executorService.scheduleAtFixedRate(() -> {
			PartyDatabase.schedulePartyInvitationCleanup();
		}, 0, 1, java.util.concurrent.TimeUnit.SECONDS);
	}

	private void onServerStarted(MinecraftServer server) {
		UTA2.server = server;
		WebSocket.init(server);
		PartyDatabase.server = server;
		LOGGER.info("Server started, instance acquired");
	}

	private void onServerStopping(MinecraftServer server) {
		UTA2.server = null;
		LOGGER.info("Server stopping");
		if (WebSocket.isConnected()) {
			WebSocket.shutdown();
			LOGGER.info("WebSocket connection closed");
		}
		PlayerDatabase.clear();
		LOGGER.info("Player database cleared");
		PartyDatabase.clear();
		LOGGER.info("Party database cleared");
		executorService.shutdown();
		LOGGER.info("Executor service shutdown");
		LOGGER.info("Server stopped");
		WebSocket.scheduler.shutdown();
	}

	private void onPlayerJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
		LOGGER.info("Player joined: " + handler.getPlayer().getName().getString());
		PlayerDatabase.addPlayerData(
				new PlayerDatabase.PlayerData(handler.getPlayer().getUuid().toString(),
						handler.getPlayer().getName().getString(), 0));
		PlayerDatabase.updatePlayerDataFromServer(handler.getPlayer().getUuid().toString(),
				handler.getPlayer().getName().getString());
	}

	private void onPlayerDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
		LOGGER.info("Player disconnected: " + handler.getPlayer().getName().getString());
		PlayerDatabase.removePlayerData(handler.getPlayer().getUuid().toString());
		PartyData party = PartyDatabase.getPartyData(handler.getPlayer().getUuid().toString());
		if (party != null) {
			party.removePlayer(handler.getPlayer().getUuid().toString());
			if (party.partyMembers.isEmpty()) {
				PartyDatabase.removeParty(party.partyId);
				LOGGER.info("Party removed due to no members left");
			} else {
				LOGGER.info("Player removed from party: " + handler.getPlayer().getName().getString());
			}
		} else {
			LOGGER.info("No party found for player: " + handler.getPlayer().getName().getString());
		}

	}

	public static MinecraftServer getServer() {
		return server;
	}
}