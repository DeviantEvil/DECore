package net.deviantevil.moderation;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.deviantevil.core.CoreCommand;
import net.deviantevil.core.CoreEvent;
import net.deviantevil.core.CoreModule;
import net.deviantevil.core.CorePlugin;
import net.deviantevil.core.exceptions.InvalidTargetException;

public final class ModerationModule extends CoreModule {

    public ModerationModule(CorePlugin plugin) {
        super(plugin);
    }

    private final List<CoreCommand> mCommands = Arrays.asList(
            new Kick(this), new Ban(this)
    );
    private final List<CoreEvent> mEvents = Arrays.asList();
    private final List<Class<?>> mRecords = Arrays.asList();

    public static enum Action {
        MUTE, KICK, BAN;
    }

    public static enum Permission {
        MUTE, KICK, BAN, PERMANENT, REASON;
        
        public String toPermission() {
            return (CoreModule.PERMISSION_ROOT+".moderation."+this.name())
                    .toLowerCase();
        }
    }
    
    public static enum Message {
        ERR_SUBCMD("&cYou are not allowed to use this subcommand '%s'.)"),
        ERR_TARGET("&cYou must enter a valid player name."),
        ERR_REASON("&cYou must include a reason."),
        ERR_PERMA("&cYour punishment duration may not exceed %s");
        
        private final String mText;
        
        private Message(String text) {
            mText = text;
        }
        
        public String toString() {
            return mText;
        }
    }

    void log(Action action, String issuer, String target, String reason,
            Date end) {
        LogRecord record = new LogRecord();
        record.setTimestamp(new Date());
        record.setEnd(end);
        record.setAction(action.name());
        record.setIssuer(issuer);
        record.setTarget(target);
        record.setReason(reason);
        getDatabase().save(record);
    }
    
    public List<Class<?>> getDatabaseClasses() {
        return mRecords;
    }
    
    public List<CoreCommand> getCommands() {
        return mCommands;
    }
    
    public List<CoreEvent> getEvents() {
        return mEvents;
    }
}
