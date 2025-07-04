package com.pikacnu.src.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import com.pikacnu.src.websocket.WebSocketClient;
import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.data.Message;
import com.pikacnu.src.json.data.Payload;
import com.pikacnu.src.json.data.MapChoose;

public class MapChooseCommand implements ICommand {
  @Override
  public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(
        CommandManager.literal("map_choose")
            .requires(source -> source.hasPermissionLevel(2)) // Requires operator permission level
            .then(CommandManager.argument("map_index", IntegerArgumentType.integer(0, 18))
                .executes(context -> {
                  try {
                    int mapId = IntegerArgumentType.getInteger(context, "map_index");

                    // Create map choose object
                    MapChoose mapChooseData = new MapChoose(mapId);

                    // Send the map choose data
                    Payload payload = new Payload();
                    payload.data = mapChooseData;
                    Message wsMessage = new Message(Action.MAP_CHOOSE, WebSocketClient.serverSessionId, payload);
                    WebSocketClient.sendMessage(wsMessage);

                    context.getSource().sendMessage(Text
                        .literal("Map choose event sent successfully!").withColor(0x00FF00));
                  } catch (Exception e) {
                    e.printStackTrace();
                    context.getSource().sendError(
                        Text.literal("Failed to send map choose event!").withColor(0xFF0000));
                  }
                  return 1; // Return 1 to indicate success
                })));
  }
}
