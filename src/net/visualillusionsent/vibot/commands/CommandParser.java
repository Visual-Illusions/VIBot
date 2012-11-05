package net.visualillusionsent.vibot.commands;

import java.util.LinkedHashMap;

import net.visualillusionsent.vibot.Channel;
import net.visualillusionsent.vibot.User;
import net.visualillusionsent.vibot.VIBotMain;

public class CommandParser {
    private static CommandParser instance;
    private final LinkedHashMap<String, BaseCommand> commands = new LinkedHashMap<String, BaseCommand>();

    private CommandParser() {
        add(new DisablePluginCommand());
        add(new DisconnectCommand());
        add(new EchoCommand());
        add(new EnablePluginCommand());
        add(new HelpCommand());
        add(new IgnoreUserCommand());
        add(new InformationCommand());
        add(new JoinChannelCommand());
        add(new IdentifyCommand());
        add(new ListIgnoresCommand());
        add(new ListPluginsCommand());
        add(new NickChangeCommand());
        add(new OkThanksCommand());
        add(new PartChannelCommand());
        add(new PingCommand());
        add(new ReloadPluginCommand());
        add(new ShutTheFuckUpCommand());
        add(new TopicCommand());
        add(new UnignoreUserCommand());
    }

    /**
     * Add a command to the server list.
     * 
     * @param name
     * @param cmd
     */
    public void add(BaseCommand cmd) {
        if (cmd != null) {
            BotCommand botcmd = cmd.getClass().getAnnotation(BotCommand.class);
            for (String alias : botcmd.aliases()) {
                if (!commands.containsValue(alias)) {
                    commands.put(alias, cmd);
                } 
                else {
                    VIBotMain.logger.warning("Command: '" + alias + "' is already registered!");
                }
            }
        }
    }

    /**
     * Remove a command from the server list. (No / prefix)
     * 
     * @param name
     */
    public void remove(String name) {
        if (name != null) {
            commands.remove(name);
        }
    }

    public void remove(BaseCommand cmd) {
        if (cmd != null) {
            BotCommand botcmd = cmd.getClass().getAnnotation(BotCommand.class);
            for (String alias : botcmd.aliases()) {
                if (commands.containsValue(alias)) {
                    commands.remove(alias);
                }
            }
        }
    }

    public static CommandParser getInstance() {
        checkInstance();
        return instance;
    }

    /**
     * Performs a lookup for a command of the given name and executes it if
     * found. Returns false if command not found.
     * 
     * @param command
     * @param caller
     * @param args
     * @return
     */
    public static boolean parseBotCommand(Channel channel, User user, String[] args) {
        checkInstance();
        BaseCommand cmd = instance.getCommand(args[0]);
        if (cmd != null) {
            BotCommand botcmd = cmd.getClass().getAnnotation(BotCommand.class);
            if (botcmd.adminonly() && !(user.isAdmin() || user.isConsole())) {
                user.sendNotice("You do not have permission to use that command!");
                return false;
            }
            if (botcmd.oponly() && !(user.isAdmin() || user.isOp() || user.isConsole())) {
                user.sendNotice("You do not have permission to use that command!");
                return false;
            }
            return cmd.execute(channel, user, args);
        }
        user.sendNotice("Unknown Command. Use !help to see availible commands.");
        return false;
    }

    public BaseCommand getCommand(String command) {
        return commands.get(command);
    }

    BaseCommand[] getCommands() {
        return commands.values().toArray(new BaseCommand[] {});
    }

    private static void checkInstance() {
        if (instance == null) {
            instance = new CommandParser();
        }
    }
}
