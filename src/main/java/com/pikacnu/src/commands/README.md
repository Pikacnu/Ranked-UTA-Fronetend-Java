# Commands Structure

This project has been restructured to separate individual commands into their own files for better maintainability and organization.

## Directory Structure

```
src/main/java/com/pikacnu/src/commands/
├── ICommand.java                # Base interface for all commands
├── CommandInit.java             # Command initialization and registration
├── SendWsCommand.java          # WebSocket message sending command
├── WsStatusCommand.java        # WebSocket status checking command
├── PlayerKilledCommand.java    # Player killed event command
├── DamagedCommand.java         # Player damage event command
├── GameStatusCommand.java      # Game status command
├── MapChooseCommand.java       # Map selection command
├── SendDataCommand.java        # Storage data sending command
├── GetDataCommand.java         # Data retrieval command
├── QueueCommand.java           # Queue management command
└── PartyCommand.java           # Party management command
```

## Command List

### WebSocket Commands
- **send_ws** - Send WebSocket messages
- **ws_status** - Check WebSocket connection status

### Game Event Commands
- **player_killed** - Send player killed events
- **damaged** - Send player damage events
- **game_status** - Update game status
- **map_choose** - Select game map

### Data Commands
- **send_data** - Send storage data
- **get_data** - Retrieve data with function execution

### Player Management Commands
- **queue** - Join/leave game queues with modes (solo, duo, squad, stige)
- **party** - Manage party invitations and membership

## Adding New Commands

1. Create a new command class implementing `ICommand` interface
2. Implement the `register(CommandDispatcher<ServerCommandSource> dispatcher)` method
3. Add the command instance to the `commands` list in `CommandInit.java`

Example:
```java
public class NewCommand implements ICommand {
    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("new_command")
                .executes(context -> {
                    // Command logic here
                    return 1;
                })
        );
    }
}
```

Then add it to CommandInit.java:
```java
commands.add(new NewCommand());
```

## Initialization

All commands are automatically registered through `CommandInit.init()` which is called from the main `Command.init()` method.
