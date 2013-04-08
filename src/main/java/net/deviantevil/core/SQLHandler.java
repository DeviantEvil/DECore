package net.deviantevil.core;

import java.util.logging.Handler;
import java.util.logging.LogRecord;


public class SQLHandler extends Handler {
    
    private final CorePlugin mPlugin;
    
    public SQLHandler(CorePlugin plugin) {
        mPlugin = plugin;
    }
    
    @Override
    public void close() throws SecurityException {
        return;
    }

    @Override
    public void flush() {
        return;
    }

    @Override
    public void publish(LogRecord record) {
        if(record == null) return;
        CoreRecord log = new CoreRecord();
        log.setLevel(record.getLevel().getName());
        log.setName(record.getLoggerName());
        log.setMessage(record.getMessage());
        mPlugin.getDatabase().save(log);
    }

}

