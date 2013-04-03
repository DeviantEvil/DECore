package net.deviantevil.decore;

import java.util.Random;

import net.deviantevil.decore.DECore.WorldBoarder;
import net.deviantevil.deplayer.DEPlayer;
import net.deviantevil.pseudoclasses.DELocation;
import net.deviantevil.utilities.*;
import net.deviantevil.utilities.DEPlayerDatabase.DEPlayerSQLValue;

import org.bukkit.*;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

/**
 * The current state of the global chat channel
 */
enum ChatState
{
    /** Chat messages are converted to Magic text */
    CENSORED,
    /** Chat messages have 'holes' in them. */
    HOLEY,
    /** Chat messages are muted */
    MUTED,
    /** Chat messages appear as normal */
    NORMAL,
    /** Chat messages are converted to Red, White and Blue text */
    PATRIOTIC,
    /** Chat messages are converted to magic text and then made Red, White and Blue */
    PATRIOTIC_MAGIC,
    /** Chat messages are converted to rainbow text */
    RAINBOW,
    /** Chat messages are converted to magic text and then made rainbow */
    RAINBOW_MAGIC,
    /** Chat messages are random colours and formats */
    RANDOM,
    /** Key words are replaced to annoy =p */
    REPLACE,
    /** Chat messages are converted to zebra text */
    ZEBRA
}

/**
 * 
 * @author Kristian
 */
public class DECoreListeners implements Listener
{
    /** Special formatting for the chat/muted */
    static ChatState chatstate = ChatState.NORMAL;

    /** Appending prefix on the message */
    public static String messagePrefix = "";

    /** Appending suffix on the message */
    public static String messageSuffix = "";

    /**
     * Change a chat message dependent on the current chat settings.
     * Returns the altered message or null if event should be cancelled and no message should be sent.
     */
    public static String changeChatMessage(String message)
    {
        String newmessage = messagePrefix + message.trim() + messageSuffix;

        switch (chatstate)
        {
            case CENSORED:
                newmessage = ChatColor.MAGIC + newmessage;
                break;
            case HOLEY:
                newmessage = Colours.makeHoley(newmessage, new Random().nextDouble());
                break;
            case MUTED:
                return null;
            case PATRIOTIC:
                newmessage = Colours.makePatriotic(newmessage, false);
                break;
            case PATRIOTIC_MAGIC:
                newmessage = Colours.makePatriotic(newmessage, true);
                break;
            case RAINBOW:
                newmessage = Colours.makeRainbow(newmessage, false);
                break;
            case RAINBOW_MAGIC:
                newmessage = Colours.makeRainbow(newmessage, true);
                break;
            case RANDOM:
                newmessage = Colours.makeRandom(newmessage);
                break;
            case REPLACE:
                newmessage = Colours.doAnnoyingReplace(newmessage);
                break;
            case ZEBRA:
                newmessage = Colours.makeStripey(newmessage);
                break;
            default:
                break;
        }

        if (DEConfigOptions.formatChatColours)
        {
            newmessage = Colours.fixColours(newmessage);
        }

        return newmessage;
    }

    /**
     * Checks for an illegal Potion
     * Info on Potions: http://forums.bukkit.org/threads/info-on-potions.47846/#post-832108
     * @param player The player being checked
     * @param itemInHand The Item stack to check
     * @return True if potion confiscated, else false
     */
    private static boolean checkForPotion(Player player, ItemStack itemInHand)
    {
        if (itemInHand != null && itemInHand.getType() == Material.POTION)
        {
            Potion pot = Potion.fromItemStack(itemInHand);
            if (pot.isSplash())
            {
                PotionType t = pot.getType();
                boolean isHelpful =
                        (t == PotionType.FIRE_RESISTANCE) ||
                                (t == PotionType.INSTANT_HEAL) ||
                                (t == PotionType.REGEN) ||
                                (t == PotionType.SPEED) ||
                                (t == PotionType.STRENGTH) ||
                                (t == PotionType.WATER);
                boolean isHostile =
                        (t == PotionType.INSTANT_DAMAGE) ||
                                (t == PotionType.POISON) ||
                                (t == PotionType.SLOWNESS) ||
                                (t == PotionType.WEAKNESS);
                if (DEConfigOptions.banHelpfulThowingPotions && isHelpful)
                {
                    player.updateInventory();
                    player.sendMessage(ChatColor.RED + "That throwing potion is currently banned.");
                    return true;
                }

                if (DEConfigOptions.banHostileThowingPotions && isHostile)
                {
                    player.updateInventory();
                    player.sendMessage(ChatColor.RED + "That throwing potion is currently banned.");
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketUse(PlayerBucketEmptyEvent event)
    {
        DEPlayer dePlayer = DEPlayer.getDEPlayer(event.getPlayer());
        if (dePlayer == null || dePlayer.getProperty(DEPlayerSQLValue.INFINITE_BUCKET) == null
                || !((Boolean) dePlayer.getProperty(DEPlayerSQLValue.INFINITE_BUCKET)))
            return;

        switch (event.getBucket())
        {
            case LAVA_BUCKET:
                event.setItemStack(new ItemStack(Material.LAVA_BUCKET, 1));
                break;
            case MILK_BUCKET:
                event.setItemStack(new ItemStack(Material.MILK_BUCKET, 1));
                break;
            case WATER_BUCKET:
                event.setItemStack(new ItemStack(Material.WATER_BUCKET, 1));
                break;
            default:
                break;
        }
        event.getPlayer().updateInventory();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event)
    {
        if (!DEConfigOptions.boarders.containsKey(event.getLocation().getWorld().getName()))
        {
            return;
        }
        WorldBoarder wb = DEConfigOptions.boarders.get(event.getLocation().getWorld().getName());
        if (event.getLocation().getBlockX() > wb.Xmax
                || event.getLocation().getBlockY() > wb.Ymax
                || event.getLocation().getBlockZ() > wb.Zmax
                || event.getLocation().getBlockX() < wb.Xmin
                || event.getLocation().getBlockY() < wb.Ymin
                || event.getLocation().getBlockZ() < wb.Zmin)
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEndermanGrief(EntityChangeBlockEvent event)
    {
        if (DEConfigOptions.endermenprotect)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity ent = event.getEntity();
        if (ent instanceof Player)
        {
            DEPlayer deplayer = DEPlayer.getDEPlayer((Player) ent);
            if (deplayer == null)
                return;
            if ((Boolean) deplayer.getProperty(DEPlayerSQLValue.GODMODE))
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event)
    {
        if (!DEConfigOptions.creeperprotect)
        {
            return;
        }
        Entity ent = event.getEntity();
        if (ent instanceof Creeper)
        {
            event.blockList().clear();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGuestSpawn(PlayerRespawnEvent event)
    {
        if (DEConfigOptions.guestSpawn != null)
        {
            Player player = event.getPlayer();
            if (DEPermissions.isLowerThan(player, DEPermissions.DEGroup.MEMBER))
            {
                event.setRespawnLocation(DEConfigOptions.guestSpawn.toLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChatASync(AsyncPlayerChatEvent event)
    {
        if (event.isCancelled())
            return;
        String newmessage = changeChatMessage(event.getMessage());

        if (newmessage == null)
        {
            event.setCancelled(true);
        }
        else
        {
            event.setMessage(newmessage);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        DEPlayer p = DEPlayer.getDEPlayer(event.getPlayer());
        if (p != null && p.isProperty(DEPlayerSQLValue.CONSOLE))
        {
            if (!event.getMessage().startsWith("//"))
            {
                event.getPlayer().sendMessage(ChatColor.GRAY + "> Issuing command as console.");
                event.setCancelled(true);
                final Server s = DECore.getDECore().getServer();
                s.dispatchCommand(s.getConsoleSender(), event.getMessage().substring(1)); // remove first /
                return;
            }
            event.setMessage(event.getMessage().substring(1)); // Message started with a //, remove one /.
        }

        if (!event.getMessage().contains("/me") || event.isCancelled())
        {
            return;
        }

        String changeChatMessage = changeChatMessage(event.getMessage());
        changeChatMessage = changeChatMessage.replace("/me", ""); // In case of prefix
        changeChatMessage = "/me" + changeChatMessage;
        event.setMessage(changeChatMessage);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        Player p = event.getEntity();
        if (p != null)
        {
            DEPlayer deplayer = DEPlayer.getDEPlayer(p);
            deplayer.setLastDeathLocation(new DELocation(p.getLocation()));
            String messageToSend = event.getDeathMessage();

            if (DEConfigOptions.deathMessageBroadcastRange != -1)
            {
                event.setDeathMessage(null);
            }

            switch (DEConfigOptions.deathMessageBroadcastRange)
            {
                case -2: // Send message to players in the world.
                    for (Player pl : p.getWorld().getPlayers())
                    {
                        pl.sendMessage(messageToSend);
                    }
                    break;
                case -1: // Send message to players on the server (default, no action needs to be taken).
                case 0: // Don't send message (message already nulled).
                    break;
                default: // Another option: must be the radius.
                    for (Entity e : p.getNearbyEntities(DEConfigOptions.deathMessageBroadcastRange, DEConfigOptions.deathMessageBroadcastRange,
                            DEConfigOptions.deathMessageBroadcastRange))
                    {
                        if (e instanceof Player)
                            ((Player) e).sendMessage(messageToSend); // Send message to nearby player.
                    }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerGameModeSwitch(PlayerGameModeChangeEvent event)
    {
        DEPlayer p = DEPlayer.getDEPlayer(event.getPlayer());

        final long currentTime = System.currentTimeMillis();
        final GameMode newGameMode = event.getNewGameMode();
        final GameMode oldGameMode = event.getPlayer().getGameMode();

        if (newGameMode == GameMode.CREATIVE)
        {
            p.resetCreativeTime(currentTime);
        }
        else if (oldGameMode == GameMode.CREATIVE)
        {
            final long creativeTime = p.getCreativeTime(currentTime);
            if (creativeTime > 0)
            {
                p.changeNumericalProperty(DEPlayerSQLValue.CREATIVE_TIME_MONTH, creativeTime, DECore.usingMySQL());
                p.changeNumericalProperty(DEPlayerSQLValue.CREATIVE_TIME_TOTAL, creativeTime, DECore.usingMySQL());
            }
        }

        p.setProperty(DEPlayerSQLValue.CREATIVE_CURRENT, newGameMode == GameMode.CREATIVE, DECore.usingMySQL());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (DEConfigOptions.banHelpfulThowingPotions || DEConfigOptions.banHostileThowingPotions)
        {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
            {
                boolean hadPotion = checkForPotion(event.getPlayer(), event.getItem());
                if (hadPotion)
                {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        if (DEConfigOptions.banHelpfulThowingPotions || DEConfigOptions.banHostileThowingPotions)
        {
            boolean hadPotion = checkForPotion(event.getPlayer(), event.getPlayer().getItemInHand());
            if (hadPotion)
            {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();
        DEPlayer deplayer;
        if (DECore.DEPlayers.contains(player.getName()))
        {
            deplayer = DECore.DEPlayers.get(player.getName());
        }
        else
        {
            deplayer = new DEPlayer(player);
            DECore.DEPlayers.put(player.getName(), deplayer);
            if (DECore.usingMySQL())
                DECore.getDEPlayersDatabase().loadPlayer(deplayer);
        }
        deplayer.resetPlayTime(System.currentTimeMillis());
        final boolean promoted = deplayer.checkForVeteran();

        if (promoted)
        {
            DECore.getDECore().getServer().broadcastMessage(
                    DEPermissions.getGroup(player).getChatColourCode() + player.getName() + ChatColor.LIGHT_PURPLE +
                            " was promoted to Veteran for " + DEConfigOptions.promoteToVeteransTime + " hours play time!");
        }

        if (DEConfigOptions.useLoginLogoutMessages)
            event.setJoinMessage(DEPermissions.getGroup(player).getChatColourCode() + player.getName() + ChatColor.DARK_GRAY + " joined the game.");

        deplayer.setTabName();
        if (!DEConfigOptions.MOTD.isEmpty())
        {
            for (String line : DEConfigOptions.MOTD)
            {
                line = Colours.fixColours(line);
                player.sendMessage(line);
            }
        }

        if (DEConfigOptions.moderated)
        {
            if (DEPermissions.isLowerThan(player, DEPermissions.DEGroup.OFFMOD))
            {
                if (DEConfigOptions.DECoreOps.contains(player.getName()))
                {
                    player.sendMessage(ChatColor.GREEN + "Server is currently in moderated mode. "
                            + "Please note your permissions is less than JMod -- however I did not kick you since you are a DECore op :) ");
                }
            }
            else
            {
                player.sendMessage(ChatColor.GREEN + "Welcome. Please note that the Server is currently in moderated mode.");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event)
    {
        if (event.getResult() == Result.KICK_WHITELIST)
        {
            event.setKickMessage(DEConfigOptions.notWhitelistedMessage);
            Player[] onlinePlayers = DECore.getDECore().getServer().getOnlinePlayers();
            for (Player p : onlinePlayers)
            {
                if (p.isOp())
                {
                    p.sendMessage(ChatColor.BLUE + event.getPlayer().getName() + " tried to connect but was not whitelisted. ");
                    p.sendMessage(ChatColor.DARK_AQUA + "To whitelist, type /de whitelist add " + event.getPlayer().getName());
                }
            }
        }
        if (DEConfigOptions.moderated)
        {
            if (DEPermissions.isLowerThan(event.getPlayer(), DEPermissions.DEGroup.OFFMOD)
                    && (!DEConfigOptions.DECoreOps.contains(event.getPlayer().getName())))
            {
                event.setKickMessage("Sorry -- the server is currently in moderated mode. You must be Mod+ to enter.");
                event.setResult(Result.KICK_OTHER);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMoveOutsideWorldBoarder(PlayerMoveEvent event)
    {
        if ((event.getTo() == null) || (event.getPlayer() == null) || (!DEConfigOptions.boarders.containsKey(event.getTo().getWorld().getName())))
        {
            return;
        }

        final Player player = event.getPlayer();
        final Location loc = player.getLocation();
        final Location to = event.getTo();
        final Location from = event.getFrom();
        final WorldBoarder wb = DEConfigOptions.boarders.get(event.getTo().getWorld().getName());

        if (to.getX() > wb.Xmax)
        {
            if (DEPermissions.hasPerm(player, "de.worldboarder.allow.x") || DEPermissions.hasPerm(player, "de.worldboarder.allow.*"))
            {
                if (from.getX() <= wb.Xmax)
                { // otherwise spam occurs...
                    player.sendMessage(ChatColor.GREEN + "You are leaving the world boarder. [X -> " + wb.Xmax + "].");
                }
            }
            else
            {
                player.sendMessage(ChatColor.RED + "You may not move outside the world boarder. [X -> " + wb.Xmax + "].");
                player.teleport(new Location(loc.getWorld(), loc.getX() - 3, loc.getY(), loc.getZ()));
            }
        }
        else if (to.getX() < wb.Xmin)
        {
            if (DEPermissions.hasPerm(player, "de.worldboarder.allow.x") || DEPermissions.hasPerm(player, "de.worldboarder.allow.*"))
            {
                if (from.getX() >= wb.Xmin)
                {
                    player.sendMessage(ChatColor.GREEN + "You are leaving the world boarder. [X -> " + wb.Xmin + "].");
                }
            }
            else
            {
                player.sendMessage(ChatColor.RED + "You may not move outside the world boarder. [X -> " + wb.Xmin + "].");
                player.teleport(new Location(loc.getWorld(), loc.getX() + 3, loc.getY(), loc.getZ()));
            }
        }
        else if (to.getZ() > wb.Zmax)
        {
            if (DEPermissions.hasPerm(player, "de.worldboarder.allow.z") || DEPermissions.hasPerm(player, "de.worldboarder.allow.*"))
            {
                if (from.getZ() <= wb.Zmax)
                {
                    player.sendMessage(ChatColor.GREEN + "You are leaving the world boarder. [Z -> " + wb.Zmax + "].");
                }
            }
            else
            {
                player.sendMessage(ChatColor.RED + "You may not move outside the world boarder. [Z -> " + wb.Zmax + "].");
                player.teleport(new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ() - 3));
            }
        }
        else if (to.getZ() < wb.Zmin)
        {
            if (DEPermissions.hasPerm(player, "de.worldboarder.allow.z") || DEPermissions.hasPerm(player, "de.worldboarder.allow.*"))
            {
                if (from.getZ() >= wb.Zmin)
                {
                    player.sendMessage(ChatColor.GREEN + "You are leaving the world boarder. [Z -> " + wb.Zmin + "].");
                }
            }
            else
            {
                player.sendMessage(ChatColor.RED + "You may not move outside the world boarder. [Z -> " + wb.Zmin + "].");
                player.teleport(new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ() + 3));
            }
        }
        else if (to.getY() > wb.Ymax)
        {
            if (DEPermissions.hasPerm(player, "de.worldboarder.allow.y") || DEPermissions.hasPerm(player, "de.worldboarder.allow.*"))
            {
                if (from.getY() <= wb.Ymax)
                {
                    player.sendMessage(ChatColor.GREEN + "You are leaving the world boarder. [Y -> " + wb.Ymax + "].");
                }
            }
            else
            {
                player.sendMessage(ChatColor.RED + "You may not move outside the world boarder. [Y -> " + wb.Ymax + "].");
                player.teleport(Locations.findSafeLocation(loc));
            }
        }
        else if (to.getY() < wb.Ymin)
        {
            if (DEPermissions.hasPerm(player, "de.worldboarder.allow.y") || DEPermissions.hasPerm(player, "de.worldboarder.allow.*"))
            {
                if (from.getY() >= wb.Ymin)
                {
                    player.sendMessage(ChatColor.GREEN + "You are leaving the world boarder. [Y -> " + wb.Ymin + "].");
                }
            }
            else
            {
                player.sendMessage(ChatColor.RED + "You may not move outside the world boarder. [Y -> " + wb.Ymin + "].");
                player.teleport(Locations.findSafeLocation(loc));
            }
        } // else return;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        if (DECore.usingMySQL())
        {
            DECore.getDEPlayersDatabase().save(DEPlayer.getDEPlayer(event.getPlayer()));
        }
        DECore.DEPlayers.remove(event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerTeleportOutsideWorldBoarder(PlayerTeleportEvent event)
    {
        if ((event.getTo() == null) || (event.getPlayer() == null) || (!DEConfigOptions.boarders.containsKey(event.getTo().getWorld().getName())))
        {
            return;
        }

        final Player player = event.getPlayer();
        WorldBoarder wb = DEConfigOptions.boarders.get(event.getTo().getWorld().getName());
        if (event.getTo().getBlockX() > wb.Xmax)
        {
            if (DEPermissions.hasPerm(player, "de.worldboarder.allow.x") || DEPermissions.hasPerm(event.getPlayer(), "de.worldboarder.allow.*"))
            {
                player.sendMessage(ChatColor.GREEN + "You are leaving the world boarder. [X -> " + wb.Xmax + "].");
            }
            else
            {
                player.sendMessage(ChatColor.RED + "You may not teleport outside the world boarder. [X -> " + wb.Xmax + "].");
                event.setCancelled(true);
            }
        }
        else if (event.getTo().getBlockX() < wb.Xmin)
        {
            if (DEPermissions.hasPerm(player, "de.worldboarder.allow.x") || DEPermissions.hasPerm(event.getPlayer(), "de.worldboarder.allow.*"))
            {
                player.sendMessage(ChatColor.GREEN + "You are leaving the world boarder. [X -> " + wb.Xmin + "].");
            }
            else
            {
                player.sendMessage(ChatColor.RED + "You may not teleport outside the world boarder. [X -> " + wb.Xmin + "].");
                event.setCancelled(true);
            }
        }
        else if (event.getTo().getBlockZ() > wb.Zmax)
        {
            if (DEPermissions.hasPerm(player, "de.worldboarder.allow.z") || DEPermissions.hasPerm(event.getPlayer(), "de.worldboarder.allow.*"))
            {
                player.sendMessage(ChatColor.GREEN + "You are leaving the world boarder. [Z -> " + wb.Zmax + "].");
            }
            else
            {
                player.sendMessage(ChatColor.RED + "You may not teleport outside the world boarder. [Z -> " + wb.Zmax + "].");
                event.setCancelled(true);
            }
        }
        else if (event.getTo().getBlockZ() < wb.Zmin)
        {
            if (DEPermissions.hasPerm(player, "de.worldboarder.allow.z") || DEPermissions.hasPerm(event.getPlayer(), "de.worldboarder.allow.*"))
            {
                player.sendMessage(ChatColor.GREEN + "You are leaving the world boarder. [Z -> " + wb.Zmin + "].");
            }
            else
            {
                player.sendMessage(ChatColor.RED + "You may not teleport outside the world boarder. [Z -> " + wb.Zmin + "].");
                event.setCancelled(true);
            }
        }
        else if (event.getTo().getBlockY() > wb.Ymax)
        {
            if (DEPermissions.hasPerm(player, "de.worldboarder.allow.y") || DEPermissions.hasPerm(event.getPlayer(), "de.worldboarder.allow.*"))
            {
                player.sendMessage(ChatColor.GREEN + "You are leaving the world boarder. [Y -> " + wb.Ymax + "].");
            }
            else
            {
                player.sendMessage(ChatColor.RED + "You may not teleport outside the world boarder. [Y -> " + wb.Ymax + "].");
                event.setCancelled(true);
            }
        }
        else if (event.getTo().getBlockY() < wb.Ymin)
        {
            if (DEPermissions.hasPerm(player, "de.worldboarder.allow.y") || DEPermissions.hasPerm(event.getPlayer(), "de.worldboarder.allow.*"))
            {
                player.sendMessage(ChatColor.GREEN + "You are leaving the world boarder. [Y -> " + wb.Ymin + "].");
            }
            else
            {
                player.sendMessage(ChatColor.RED + "You may not teleport outside the world boarder. [Y -> " + wb.Ymin + "].");
                event.setCancelled(true);
            }
        } // else return;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSheepRegrowA(SheepRegrowWoolEvent event)
    {
        if (DEConfigOptions.forceSheepRegrowth)
        {
            event.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSheepRegrowB(SheepRegrowWoolEvent event)
    {
        if (DEConfigOptions.forceSheepRegrowth)
        {
            event.setCancelled(false);
        }
    }
}
