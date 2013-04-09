package net.deviantevil.moderation;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.deviantevil.core.CoreCommand;
import net.deviantevil.core.CoreModule;
import net.deviantevil.core.exceptions.InvalidTargetException;
import net.deviantevil.moderation.ModerationModule.Action;
import net.deviantevil.moderation.ModerationModule.Message;
import net.deviantevil.moderation.ModerationModule.Permission;

public class Kick extends CoreCommand {

    private final ModerationModule mModule;
    
    public Kick(ModerationModule module) {
        mModule = module;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String label, String[] args) {
        if (!sender.hasPermission(Permission.KICK.toPermission())) {
            // 'Moderator' is not allowed to kick.
            sender.sendMessage(String.format(Message.ERR_SUBCMD.toString(),
                    command));
            return true;
        } else if (args.length < 1) {
            // Moderator has not specified a target.
            sender.sendMessage(Message.ERR_TARGET.toString());
            return true;
        } else if (args.length < 2
                && !sender.hasPermission(Permission.REASON.toPermission())) {
            // Moderator has not specified a mandatory reason.
            sender.sendMessage(Message.ERR_REASON.toString());
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
            sender.sendMessage(Message.ERR_TARGET.toString());
        }
        return true;
    }

    private boolean kick(String target, CommandSender issuer, String reason)
            throws InvalidTargetException {
        Player player_target = Bukkit.getPlayer(target);
        if (player_target == null)
            throw new InvalidTargetException();

        player_target.kickPlayer(reason == null ? "" : reason);

        mModule.log(Action.KICK, issuer.getName(),
                player_target.getName(), reason, null);

        return true;
    }
}
