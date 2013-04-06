package net.deviantevil.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.deviantevil.logging.SQLLogger;
import net.deviantevil.moderation.ModerationModule.Permission;

import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginLogger;

public abstract class AbstractCoreModule 
    implements CoreModule, Listener, CommandExecutor {
    
    private final String mName;
    private final CorePlugin mPlugin;

    protected AbstractCoreModule(String name, CorePlugin plugin) {
        mName = name;
        mPlugin = plugin;
    }
    
    public abstract void enable();
    public abstract void disable();
    
    public String getName() {
        return mName;
    }
    
    public CommandExecutor getCommandExecutor() {
        return this;
    }

    public Listener getEventListener() {
        return this;
    }

    protected final Logger getLogger() {
        return SQLLogger.getLogger(getName());
    }
    
    protected final boolean hasPermission(String user, String permission) {
        return false;
    }
    
    protected final String getPermId(Enum<?> leaf) {
        return (PERMISSION_ROOT+getName()+leaf.name()).toLowerCase();
    }
}
