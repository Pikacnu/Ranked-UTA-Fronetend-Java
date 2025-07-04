package com.pikacnu.src.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import java.util.ArrayList;
import java.util.List;

import com.pikacnu.Config;

public class CommandInit {
	private static final List<ICommand> commands = new ArrayList<>();

	static {
		// Register all commands here
		if (Config.isLobby) {
			commands.add(new QueueCommand());
			commands.add(new PartyCommand());
			commands.add(new ScoreCommand());
		} else {
			commands.add(new PlayerKilledCommand());
			commands.add(new DamagedCommand());
			commands.add(new GameStatusCommand());
			commands.add(new MapChooseCommand());
			commands.add(new SendDataCommand());
			commands.add(new GetDataCommand());
		}
		// commands.add(new SendWsCommand());
		commands.add(new WsStatusCommand());
		commands.add(new GetPlayerInfo());
	}

	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			for (ICommand command : commands)
				command.register(dispatcher);
		});
	}
}
