package net.deviantevil.core;

import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;

public interface CoreModule {
    public static final String PERMISSION_ROOT = "decore";
    public static final String COMMAND_ROOT = "decore";
    
    String getName();

    CommandExecutor getCommandExecutor();
    Listener getEventListener();
    
    void enable();
    void disable();
}
