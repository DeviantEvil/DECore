package net.deviantevil.moderation;

import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.deviantevil.core.AbstractCoreModule;
import net.deviantevil.core.CorePlugin;
import net.deviantevil.core.exceptions.InvalidTargetException;

public final class ModerationModule extends AbstractCoreModule {

    public static final String NAME = "Moderation";

    public static enum Action {
        MUTE, KICK, BAN,
    }

    public static enum Permission {
        MUTE, KICK, BAN, PERMANENT, REASON,
    }

    public static final String ERR_SUBCMD
        = "&cYou are not allowed to use this subcommand '%s'.";
    public static final String ERR_TARGET
        = "&cYou must enter a valid player name.";
    public static final String ERR_REASON
        = "&cYou must include a reason.";
    public static final String ERR_PERMA
        = "&cYour punishment duration may not exceed %s";

    public ModerationModule(CorePlugin plugin) {
        super(NAME, plugin);
    }

    @Override
    public void enable() {
        mPlugin.getCommand("kick").setExecutor(this);
        mPlugin.getCommand("ban").setExecutor(this);
    }

    @Override
    public void disable() {
        return;
    }
    
    public Class<?> getDatabaseClass() {
        return ModerationRecord.class;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("kick")) {
            // KICK -----------------------------------------------------------
            if (!sender.hasPermission(getPermId(Permission.KICK))) {
                // 'Moderator' is not allowed to kick.
                sender.sendMessage(String.format(ERR_SUBCMD, cmd));
                return true;
            } else if (args.length < 1) {
                // Moderator has not specified a target.
                sender.sendMessage(ERR_TARGET);
                return true;
            } else if (args.length < 2
                    && !sender.hasPermission(getPermId(Permission.REASON))) {
                // Moderator has not specified a mandatory reason.
                sender.sendMessage(ERR_REASON);
                return true;
            }

            // Form a reason out of the remaining arguments.
            StringBuilder reason = new StringBuilder();
            for (int i = 1; i < args.length; ++i) {
                reason.append(args[i]);
                if (i != args.length) {
                    reason.append(' ');
                }
            }

            // Kick target.
            try {
                kick(args[0], sender, reason.toString());
            } catch (InvalidTargetException e) {
                sender.sendMessage(ERR_TARGET);
            }
            return true;
        } else if (cmd.equals("ban")) {
            // BAN ------------------------------------------------------------
            if (!sender.hasPermission(getPermId(Permission.BAN))) {
                // 'Moderator' is not allowed to kick.
                sender.sendMessage(String.format(ERR_SUBCMD, cmd));
                return true;
            } else if (args.length < 1) {
                // Moderator has not specified a target.
                sender.sendMessage(ERR_TARGET);
                return true;
            } else if (args.length < 2
                    && !sender.hasPermission(getPermId(Permission.REASON))) {
                // Moderator has not specified a mandatory reason.
                sender.sendMessage(ERR_REASON);
                return true;
            }

            // Form a reason out of the remaining arguments.
            StringBuilder reason = new StringBuilder();
            for (int i = 1; i < args.length; ++i) {
                reason.append(args[i]);
                if (i != args.length) {
                    reason.append(' ');
                }
            }

            // Ban target.
            try {
                ban(args[0], sender, reason.toString(), new Date());
            } catch (InvalidTargetException e) {
                sender.sendMessage(ERR_TARGET);
            }
            return true;
        }
        return false;
    }

    private boolean kick(String target, CommandSender issuer, String reason)
            throws InvalidTargetException {
        Player player_target = Bukkit.getPlayer(target);
        if (player_target == null)
            throw new InvalidTargetException();

        player_target.kickPlayer(reason == null ? "" : reason);

        log(Action.KICK, issuer.getName(), player_target.getName(), reason,
                null);

        return true;
    }

    private boolean ban(String target, CommandSender issuer, String reason,
            Date end) throws InvalidTargetException {
        Player player_target = Bukkit.getPlayer(target);
        if (player_target == null)
            throw new InvalidTargetException();

        player_target.kickPlayer(reason == null ? "" : reason);

        log(Action.BAN, issuer.getName(), player_target.getName(), reason, end);

        return true;
    }

    void log(Action action, String issuer, String target, String reason,
            Date end) {
        ModerationRecord record = new ModerationRecord();
        record.setTimestamp(new Date());
        record.setEnd(end);
        record.setAction(action.name());
        record.setIssuer(issuer);
        record.setTarget(target);
        record.setReason(reason);
        mPlugin.getDatabase().save(record);
    }
}
