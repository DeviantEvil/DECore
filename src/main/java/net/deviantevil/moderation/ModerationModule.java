package net.deviantevil.moderation;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import net.deviantevil.core.AbstractCoreModule;
import net.deviantevil.core.CorePlugin;

public final class ModerationModule extends AbstractCoreModule {
    
    public static final String NAME = "Moderation";
    public static enum Action {
        MUTE, KICK, BAN,
    }
    public static enum Permission {
        MUTE, KICK, BAN, PERMANENT, REASON,
    }
    public static final String ERR_SUBCMD = "&cYou are not allowed to use subcommand '%s'.";
    public static final String ERR_TARGET = "&cYou must enter a valid player name.";
    public static final String ERR_REASON = "&cYou must include a reason.";
    public static final String ERR_PERMA = "&cYour punishment duration may not exceed %s";

    ModerationModule(String name, CorePlugin plugin) {
        super(NAME, plugin);
    }
    
    @Override
    public void enable() {
        return;
    }

    @Override
    public void disable() {
        return;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String label, String[] args) {
        if (!command.getName().equalsIgnoreCase(COMMAND_ROOT)) {
            return false;
        }
        if (args.length == 0) return false;
        String subcmd = args[0].toLowerCase();
        if(subcmd.equals("kick")) {
            if(!sender.hasPermission(getPermId(Permission.KICK))) {
                // 'Moderator' is not allowed to kick. Command does not exist.
                sender.sendMessage(String.format(ERR_SUBCMD, subcmd));
                return false;
            } else if (args.length < 2) {
                // Moderator has not specified a target. TODO
                sender.sendMessage(ERR_TARGET);
                return true;
            } else if (args.length < 3 &&
                    !sender.hasPermission(getPermId(Permission.REASON))) {
                // Moderator has not specified a mandatory reason. TODO
                sender.sendMessage(ERR_REASON);
                return true;
            }
            
            StringBuilder reason = new StringBuilder();
            for(int i = 2; i < args.length; ++i) {
                reason.append(args[i]).append(' ');
            }
            
            try {
                Moderation.kick(args[1], sender, reason.toString());
            } catch (InvalidPlayerException e) {
                return true;
            }
        }

        return true;
    }
}
