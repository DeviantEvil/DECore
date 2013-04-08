package net.deviantevil.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import net.deviantevil.moderation.ModerationModule;

import org.bukkit.plugin.java.JavaPlugin;

public class CorePlugin extends JavaPlugin {
    
    private static Map<String, Logger> mLoggers = new HashMap<String, Logger>();
    
    private CoreModule[] mModules = {
            new ModerationModule(this)
    };
    
    @Override
    public void onEnable() {
        for(CoreModule c : mModules) {
            c.enable();
        }
        setupDatabase();
    }

    void setupDatabase() {
        try {
            getDatabase().find(CoreRecord.class).findRowCount();
        } catch (PersistenceException ex) {
            installDDL();
        }
    }
    
    @Override
    public void onDisable() {
        for(CoreModule c : mModules) {
            c.disable();
        }
    }
    
    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        for(CoreModule m : mModules) {
            list.add(m.getDatabaseClass());
        }
        return list;
    }
    
    Logger getLogger(String name) {
        Logger logger = mLoggers.get(name);
        if(logger == null) {
            logger = Logger.getLogger(name, null);
            logger.addHandler(new SQLHandler(this));
        }
        return logger;
    }
}
