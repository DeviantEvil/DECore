package net.deviantevil.core;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public abstract class CoreCommand implements CommandExecutor {
    
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public abstract boolean onCommand(CommandSender sender, Command command,
            String label, String[] args);
    

}
