package net.deviantevil.core;

import java.util.List;
import java.util.logging.Logger;

import com.avaje.ebean.EbeanServer;

public abstract class CoreModule {
    public static final String PERMISSION_ROOT = "decore";

    private final CorePlugin mPlugin;

    public CoreModule(CorePlugin plugin) {
        mPlugin = plugin;
    }

    public void enable() {
        for(CoreCommand c : getCommands()) {
            getPlugin().getCommand(c.getName()).setExecutor(c);
        }
        for(CoreEvent c : getEvents()) {
            getPlugin().getServer().getPluginManager().registerEvents(c, getPlugin());
        }
    }

    public void disable() {
        
    }

    public abstract List<Class<?>> getDatabaseClasses();
    public abstract List<CoreCommand> getCommands();
    public abstract List<CoreEvent> getEvents();

    public String getName() {
        return this.getClass().getSimpleName();
    }
    
    public CorePlugin getPlugin() {
        return mPlugin;
    }

    protected final Logger getLogger() {
        return mPlugin.getLogger(getName());
    }

    protected final EbeanServer getDatabase() {
        return mPlugin.getDatabase();
    }

}
