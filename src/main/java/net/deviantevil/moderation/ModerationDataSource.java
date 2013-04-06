package net.deviantevil.moderation;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.deviantevil.moderation.ModerationModule.Action;
import net.deviantevil.core.DataSource;

public class ModerationDataSource extends DataSource {

    public ModerationDataSource(String url, Logger logger) {
        super(url, logger);
    }

    public void log(Level severity, String message) {
        
    }
    
    public void insertRecord(String player, Action action,
                                                  String reason,
                                                  Date duration) {

    }
    
    public void insertRecord(String player, Action action,
                                                  String reason) {
        insertRecord(player, action, reason, null);
    }

    public boolean removeRecord(String player, Action action, Date timestamp) {
        return false;
    }
    
    public boolean pardon(String player, Action action) {
        return false;
    }
}
