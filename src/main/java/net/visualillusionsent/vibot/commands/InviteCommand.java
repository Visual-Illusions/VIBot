package net.visualillusionsent.vibot.commands;

import net.visualillusionsent.vibot.VIBot;
import net.visualillusionsent.vibot.api.commands.BaseCommand;
import net.visualillusionsent.vibot.api.commands.BotCommand;
import net.visualillusionsent.vibot.api.plugin.BotPlugin;
import net.visualillusionsent.vibot.io.irc.Channel;
import net.visualillusionsent.vibot.io.irc.User;

/**
 * Invite Command<br>
 * Invites a user into the channel<br>
 * <b>Usage:</b> !invite {@literal <user>}<br>
 * <b>Minimum Params:</b> 2<br>
 * <b>Maximum Params:</b> 2<br>
 * <b>Requires:</b> Op Channel<br>
 * 
 * @since 1.0
 * @version 1.0
 * @author Jason (darkdiplomat)
 */
@BotCommand(main = "invite", usage = "!invite <user>", desc = "Invites a user into the channel", minParam = 2, maxParam = 2, op = true, chanOnly = true)
public final class InviteCommand extends BaseCommand {

    /**
     * Constructs a new {@code InviteComamnd}
     */
    public InviteCommand(BotPlugin fake) {
        super(fake);
    }

    @Override
    public boolean execute(Channel channel, User user, String[] args) {
        if (!channel.isBotOp()) {
            user.sendNotice("VIBot needs to be Op in the channel for this command");
            return false;
        }
        VIBot.inviteUser(channel, args[1]);
        return true;
    }
}
