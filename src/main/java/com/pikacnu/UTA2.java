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
import com.pikacnu.src.websocket.WebSocketClient;
import com.pikacnu.src.WhiteListManager;
import com.pikacnu.src.PartyDatabase.PartyData;
import com.pikacnu.src.PlayerDatabase;
import com.pikacnu.src.PlayerOnlineChecker;

/**
 * UTA2 主模組，負責初始化與伺服器事件註冊。
 */
public class UTA2 implements ModInitializer {
	public static final String MOD_ID = "uta2";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static MinecraftServer server;
	private final ScheduledExecutorService executorService = java.util.concurrent.Executors.newScheduledThreadPool(1);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");

		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
		ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);

		ServerPlayConnectionEvents.JOIN.register(this::onPlayerJoin);
		ServerPlayConnectionEvents.DISCONNECT.register(this::onPlayerDisconnect);

		Command.init();
		executorService.scheduleAtFixedRate(() -> {
			PartyDatabase.schedulePartyInvitationCleanup();
		}, 0, 1, java.util.concurrent.TimeUnit.SECONDS);
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
	}

	private void onPlayerJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
		LOGGER.info("Player joined: " + handler.getPlayer().getName().getString());
		if (Config.isLobby) {
			PlayerDatabase.addPlayerData(
					new PlayerDatabase.PlayerData(handler.getPlayer().getUuid().toString(),
							handler.getPlayer().getName().getString(), 0));
			PlayerDatabase.updatePlayerDataFromServer(handler.getPlayer().getUuid().toString(),
					handler.getPlayer().getName().getString());
		} else {
			PlayerOnlineChecker.addPlayer(handler.getPlayer().getUuid().toString());
		}
	}

	private void onPlayerDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
		LOGGER.info("Player disconnected: " + handler.getPlayer().getName().getString());
		if (Config.isLobby) {
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
		} else {
			PlayerOnlineChecker.removePlayer(handler.getPlayer().getUuid().toString());
		}

	}

	public static MinecraftServer getServer() {
		return server;
	}
}