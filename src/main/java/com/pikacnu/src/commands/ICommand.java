package com.pikacnu.src.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

public interface ICommand {
  void register(CommandDispatcher<ServerCommandSource> dispatcher);
}
