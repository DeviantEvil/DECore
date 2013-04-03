package net.deviantevil.decore;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

import net.deviantevil.deplayer.DEPlayer;
import net.deviantevil.utilities.Colours;
import net.deviantevil.utilities.DEPlayerDatabase.DEPlayerSQLValue;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class DELogFilter implements Filter, Listener {    

    public boolean isLoggable (LogRecord log) {            
        final String message = log.getMessage().toLowerCase();
        
        if (message.contains("can't keep up!") && DEConfigOptions.disableCantKeepUp) {
            return false;
        }
        
        if (DEConfigOptions.useLoginLogoutMessages && (message.contains("lost connection: disconnect") || message.contains("moved too quickly") || message.contains("floating too long"))) {
            if (DEConfigOptions.debugging) {
                DECore.getDECore().getLogger().info("Processing disconnect message");
            }
            final String playername = log.getMessage().split("\\s+")[0]; // split by whitespace
            
            if (message.contains("banned")) {
                DECore.getDECore().getServer().broadcastMessage(ChatColor.YELLOW + playername + ChatColor.DARK_GRAY + " was kickbanned.");
            } else if (message.contains("endofstream") || message.contains("genericreason")) {
                DECore.getDECore().getServer().broadcastMessage(ChatColor.YELLOW + playername + ChatColor.DARK_GRAY + " lost connection.");
            } else if (message.contains("floating too long")) {
                if (! DECore.getDECore().getServer().getAllowFlight()) {
                    DECore.getDECore().getServer().broadcastMessage(ChatColor.YELLOW + playername + ChatColor.DARK_GRAY + " was autokicked by Minecraft for flying.");
                }
            } else if (message.contains("overflow")) {
                DECore.getDECore().getServer().broadcastMessage(ChatColor.YELLOW + playername + ChatColor.DARK_GRAY + " disconnected due to overload.");
            } else if (message.contains("quitting")) {
                DECore.getDECore().getServer().broadcastMessage(ChatColor.YELLOW + playername + ChatColor.DARK_GRAY + " logged out.");
            } else if (message.contains("moved too quickly")) {
                Player p = DECore.getDECore().getServer().getPlayer(playername);
                if (! DECore.getDECore().getServer().getAllowFlight() && p != null && p.getGameMode() != GameMode.CREATIVE) {
                    DECore.getDECore().getServer().broadcastMessage(ChatColor.YELLOW + playername + ChatColor.DARK_GRAY + " was autokicked by Minecraft for speedhacking.");
                }
            } else if (message.contains("timeout")) {
                DECore.getDECore().getServer().broadcastMessage(ChatColor.YELLOW + playername + ChatColor.DARK_GRAY + " timed out.");
            }
        }
        
        for (DEPlayer p : DECore.DEPlayers.values()) {
            if (! (Boolean)p.getProperty(DEPlayerSQLValue.CONSOLE)) continue;
            
            String[] messages = log.getMessage().split(System.lineSeparator());
            for (String m : messages)  
                p.getPlayer().sendMessage(ChatColor.GRAY + "> " + Colours.fixColours(m));
        }
        return true;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit (PlayerQuitEvent event) {
        if (DEConfigOptions.useLoginLogoutMessages)
            event.setQuitMessage(null);
    }
}
