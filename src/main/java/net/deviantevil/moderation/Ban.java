package net.deviantevil.moderation;

import java.util.Date;

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

public class Ban extends CoreCommand {

    private final ModerationModule mModule;
    
    public Ban(ModerationModule module) {
        mModule = module;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String label, String[] args) {
        if (!sender.hasPermission(Permission.BAN.toPermission())) {
            // 'Moderator' is not allowed to ban.
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

        // Ban target.
        try {
            ban(args[0], sender, reason.toString(), new Date());
        } catch (InvalidTargetException e) {
            sender.sendMessage(Message.ERR_TARGET.toString());
        }
        return true;
    }

    private boolean ban(String target, CommandSender issuer, String reason,
            Date end) throws InvalidTargetException {
        Player player_target = Bukkit.getPlayer(target);
        if (player_target == null)
            throw new InvalidTargetException();

        player_target.kickPlayer(reason == null ? "" : reason);

        mModule.log(Action.BAN, issuer.getName(), player_target.getName(),
                reason, end);

        return true;
    }
}
