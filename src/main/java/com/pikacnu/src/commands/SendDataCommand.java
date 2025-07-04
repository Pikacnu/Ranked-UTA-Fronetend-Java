package com.pikacnu.src.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import com.pikacnu.src.StorageNbtHelper;
import com.pikacnu.src.WebSocket;
import com.pikacnu.src.json.Action;
import com.pikacnu.src.json.data.Message;
import com.pikacnu.src.json.data.Payload;
import com.pikacnu.src.json.data.StorageData;

public class SendDataCommand implements ICommand {
  @Override
  public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(
        CommandManager.literal("send_data")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.argument("type", StringArgumentType.string())
                .then(CommandManager.argument("storage", NbtPathArgumentType.nbtPath())
                    .then(CommandManager.argument("nbt", NbtPathArgumentType.nbtPath())
                        .executes(context -> {
                          try {
                            String storage = NbtPathArgumentType.getNbtPath(context, "storage").getString();
                            String nbtData = NbtPathArgumentType.getNbtPath(context, "nbt").getString();
                            String type = StringArgumentType.getString(context, "type");

                            Object[] nbtResult = StorageNbtHelper
                                .getStorageNbtByPath(context.getSource().getServer(), storage, nbtData).stream()
                                .map(nbtElement -> nbtElement.toString()).toArray(Object[]::new);

                            // Create storage data object
                            StorageData storageData = new StorageData(storage, "data", nbtResult);

                            // Send the storage data
                            Payload payload = new Payload();
                            payload.data = storageData;
                            Message wsMessage = new Message(Action.fromString(type),
                                WebSocket.serverSessionId, payload);
                            WebSocket.sendMessage(wsMessage);

                            context.getSource().sendMessage(Text
                                .literal("Storage data event sent successfully!").withColor(0x00FF00));
                          } catch (Exception e) {
                            e.printStackTrace();
                            context.getSource().sendError(
                                Text.literal("Failed to send storage data event!").withColor(0xFF0000));
                          }
                          return 1; // Return 1 to indicate success
                        })))));
  }
}
