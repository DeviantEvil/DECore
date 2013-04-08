package net.deviantevil.core;

import java.util.logging.Logger;

import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;

import com.avaje.ebean.EbeanServer;

public abstract class AbstractCoreModule 
    implements CoreModule, Listener, CommandExecutor {
    
    private final String mName;
    protected final CorePlugin mPlugin;

    protected AbstractCoreModule(String name, CorePlugin plugin) {
        mName = name;
        mPlugin = plugin;
    }
    
    public abstract void enable();
    public abstract void disable();
    public abstract Class<?> getDatabaseClass();
    
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
        return mPlugin.getLogger(getName());
    }
    
    protected final EbeanServer getDatabase() {
        return mPlugin.getDatabase();
    }

    protected final String getPermId(Enum<?> leaf) {
        return (PERMISSION_ROOT+getName()+leaf.name()).toLowerCase();
    }
}
