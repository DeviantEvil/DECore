package net.deviantevil.decore;

import com.vexsoftware.votifier.Votifier;
import com.vexsoftware.votifier.model.VotifierEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import net.deviantevil.deplayer.DEPlayer;
import net.deviantevil.utilities.*;
import net.deviantevil.utilities.DEPlayerDatabase.DEPlayerSQLValue;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * 
 * @author Kristian ("kjhf")
 *
 */

public class DEVotifierListener implements Listener {
    private static ArrayList<String> pendingPlayers;
    private static File pendingPlayersFile;
    private static File voteRecordFile;

    public DEVotifierListener(Votifier v) {
        if (v != null) {
            pendingPlayersFile = new File(v.getDataFolder(), "DEVotersDelayed.properties");
            voteRecordFile = new File(v.getDataFolder(), "DEVoters.properties");
            if (!pendingPlayersFile.exists()) {
                IO.saveProperty(pendingPlayersFile, "", "");
            }
            if (!voteRecordFile.exists()) {
                IO.saveProperty(voteRecordFile, "", "");
            }
            
            // Load pending players from Properties file
            pendingPlayers = IO.getAllProperties(pendingPlayersFile);
            v.getServer().getPluginManager().registerEvents(this, v); // Register a player listener to do things on player log in
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (pendingPlayers.isEmpty()) {
            return;
        }

        String player = e.getPlayer().getName();
        for (String pendingplayer : pendingPlayers) {
            if (player.equalsIgnoreCase(pendingplayer)) {
                int currentVotesPendingForPlayer;
                try {
                    currentVotesPendingForPlayer = Integer.parseInt(IO.getProperty(pendingPlayersFile, player));
                } catch (Exception ex) {
                    DECore.log.warning("Couldn't retrieve " + player + "'s vote count: " + ex);
                    return;
                }

                if (DEConfigOptions.debugging) DECore.log.info("Found " + player + " in pending list. Paying !");

                rewardPlayer(e.getPlayer(), currentVotesPendingForPlayer);
                pendingPlayers.remove(player);
                IO.removeProperty(pendingPlayersFile, player);
            }
        }
    }

    @EventHandler(priority=EventPriority.NORMAL)
    public void onVotifierEvent(VotifierEvent event) {
        // Record the vote //
        
        String player = event.getVote().getUsername();
        
        int currentVotesPendingForPlayer;
        int totalVotesForPlayer;
        
        try {
            totalVotesForPlayer = Integer.parseInt(IO.getProperty(voteRecordFile, player));
            currentVotesPendingForPlayer = Integer.parseInt(IO.getProperty(pendingPlayersFile, player));
        } catch (Exception ex) {
            // Assume no votes
            currentVotesPendingForPlayer = 0;
            totalVotesForPlayer = 0;
        }
           
        IO.saveProperty(voteRecordFile, player, (totalVotesForPlayer + 1) + "");
        
        // Reward player //
        
        // Check if they are on server for instant payment, otherwise put on waiting list if not already on it
        Player[] players = DECore.getDECore().getServer().getOnlinePlayers();
        for (int i = 0; i < players.length; i++) {
            String playerName = players[i].getName();
            if (player.equalsIgnoreCase(playerName)) {
                rewardPlayer(players[i], currentVotesPendingForPlayer + 1);
                pendingPlayers.remove(player);
                IO.removeProperty(pendingPlayersFile, player);
                return;
            }
        }

        // If they got this far they are not on server so check if they are already on pending list
        for (String pendingPlayer : pendingPlayers) {
            if (pendingPlayer.equals(player)) {
                try {
                    currentVotesPendingForPlayer = Integer.parseInt(IO.getProperty(pendingPlayersFile, pendingPlayer));
                } catch (Exception ex) {
                    DECore.log.warning("Couldn't retrieve " + player + "'s vote count. Aborting the vote. " + ex);
                    return;
                }

                if (!DEConfigOptions.voteBroadcastMessage.isEmpty()) {
                    for (Player p : DECore.getDECore().getServer().getOnlinePlayers()) {                    
                        p.sendMessage(player + " has just voted! Have you done so today? http://deviantevil.net");
                    }
                }
                
                if (DEConfigOptions.debugging) DECore.log.info(player + " is not on the server. Saving to file.");
                break;
            }
        }

        if (!pendingPlayers.contains(player)) {
            pendingPlayers.add(player);
        }
        IO.saveProperty(pendingPlayersFile, player, (currentVotesPendingForPlayer + 1) + "");
    }

    /**
     * Reward the player
     * @param player The player
     * @param times Times the player voted
     * @return Success of payment for Economy. N.B. DEP Points may have been awarded.
     */
    private static boolean rewardPlayer(Player player, int times) {
        if (player == null) return false;
        DEPlayer deplayer = DEPlayer.getDEPlayer(player);
                
        int rewarddep = 0;
        for (int i = 0; i < times; i++) {
            if (new Random().nextInt(100) + 1 <= DEConfigOptions.voteDEPChance) rewarddep++;
        }
        
        deplayer.changeNumericalProperty(DEPlayerSQLValue.VOTES_MONTH, times, DECore.usingMySQL());
        deplayer.changeNumericalProperty(DEPlayerSQLValue.VOTES_TOTAL, times, DECore.usingMySQL());

        if (rewarddep != 0) {
            deplayer.changeNumericalProperty(DEPlayerSQLValue.DEPOINTS, DEConfigOptions.voteDEPReward * rewarddep, DECore.usingMySQL());
            
            if (!DEConfigOptions.voteThankyouMessageDEPoints.isEmpty()) {
            player.sendMessage(
                    ChatColor.DARK_AQUA + 
                    Colours.fixColours(DEConfigOptions.voteBroadcastMessage
                        .replace("%times", times+"")
                        .replace("%player", player.getName())
                        .replace("%reward", (times * DEConfigOptions.voteReward + ""))
                        .replace("%dep", (rewarddep * DEConfigOptions.voteDEPReward) + ""))
                        );
            }
        }
        
        return pay(player, times, rewarddep * DEConfigOptions.voteDEPReward);
    }
    
    /**
     * Pay the player
     * @param player The player
     * @param times Times player needs paying
     * @param depreward Number of dep points awarded. [For information -- this has already been awarded to the player]
     * @return Success -- false if no economy.
     */
    private static boolean pay(Player player, int times, int depreward) {
        if (times == 0) return true;
        if (DECore.getEconomy() == null) return false;
        
        //Transaction through vault
        EconomyResponse r = DECore.getEconomy().depositPlayer(player.getName(), (times * DEConfigOptions.voteReward));

        if (r.transactionSuccess()) {

            if (!DEConfigOptions.voteThankyouMessage.isEmpty()) {
                player.sendMessage(
                        ChatColor.DARK_AQUA + 
                        Colours.fixColours(DEConfigOptions.voteBroadcastMessage
                                .replace("%times", times+"")
                                .replace("%player", player.getName())
                                .replace("%reward", (times * DEConfigOptions.voteReward + ""))
                                .replace("%dep", (depreward +"")))
                                );
            }

            if (!DEConfigOptions.voteBroadcastMessage.isEmpty()) {
                for (Player p : DECore.getDECore().getServer().getOnlinePlayers()) {
                    if (p == player) continue;
                
                    p.sendMessage(
                            ChatColor.DARK_AQUA + 
                            Colours.fixColours(DEConfigOptions.voteBroadcastMessage
                                    .replace("%times", times+"")
                                    .replace("%player", player.getName())
                                    .replace("%reward", (times * DEConfigOptions.voteReward + "")))
                                    );
                }
            }
            return true;
        }

        //Message to player
        player.sendMessage(ChatColor.RED + "Error giving money for vote:" + r.errorMessage);
        //Message to console
        DECore.log.warning(player.getName() + " could not be given money for voting: " + r.errorMessage);
        return false;
    }
}
