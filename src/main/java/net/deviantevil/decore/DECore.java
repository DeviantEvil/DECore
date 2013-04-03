package net.deviantevil.decore;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.deviantevil.deplayer.DEPlayer;
import net.deviantevil.pseudoclasses.DELocation;
import net.deviantevil.utilities.*;
import net.deviantevil.utilities.DEPermissions.DEGroup;
import net.deviantevil.utilities.DEPlayerDatabase.DEPlayerSQLValue;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.vexsoftware.votifier.Votifier;

/**
 * 
 * @author Kristian "Kjhf"
 */
public final class DECore extends JavaPlugin
{
    /** The DECore Instance */
    private static Plugin DECore;

    /** The DEPlayers on the server <Playername, DEPlayer> */
    public static ConcurrentHashMap<String, DEPlayer> DEPlayers = new ConcurrentHashMap<String, DEPlayer>();

    /** The DEPlayer SQL Table for the server */
    public static DEPlayerDatabase DEPlayersSQL = null;

    /** The Economy plugin we're using */
    private static Economy economy = null;

    /** DECore logger */
    public static Logger log;

    /** Returns "ChatColor.AQUA + "[DE]"" */
    public static final String SERVER_PREFIX_STRING = ChatColor.AQUA + "[DE]";

    /** Returns the No Permission message */
    public static final String NO_PERMISSION = SERVER_PREFIX_STRING + ChatColor.DARK_AQUA + " You don't have permission ";

    /** Returns "Deviant Evil" */
    public static final String SERVER_STRING = "Deviant Evil";

    /** The Votifier plugin we're using */
    private static Votifier votifier = null;

    /**
     * DECore's Log filter. Analyses quit reasons and broadcasts. Sends console messages to console-mounted players. Also disables "Can't keep up messages" if
     * configured.
     */
    public final DELogFilter CoreFilter = new DELogFilter();

    /** DECore's Events Listener */
    public final DECoreListeners CoreListener = new DECoreListeners();

    /** DECore's Votifier Listener tracks player votes and rewards creds */
    public DEVotifierListener CoreVotifierListener = null;

    /** Registered Deviant Plugins */
    public HashMap<Plugin, String> DEPlugins = new HashMap<Plugin, String>(); // Plugin, Status

    /** Plugin Manager instance. Required for plugin registration, enabling and disabling. */
    private PluginManager pm;

    /**
     * Get the Core!
     * @return {@link DECore} instance
     */
    public static DECore getDECore()
    {
        return (DECore) DECore;
    }

    /**
     * Get the DEPlayers SQL table! (Can be null)
     * @return {@link DEPlayerDatabase} instance
     */
    public static DEPlayerDatabase getDEPlayersDatabase()
    {
        return DEPlayersSQL;
    }

    /**
     * Get the Economy! (Can be null)
     * @return {@link Economy} instance
     */
    public static Economy getEconomy()
    {
        return economy;
    }

    /**
     * Get the Votifier instance! (Can be null)
     * @return {@link Votifier} instance
     */
    public static Votifier getVotifier()
    {
        return votifier;
    }

    /** Setup MySQL for DECore */
    private static boolean setupMySQL()
    {
        boolean success = false;

        if (DEConfigOptions.debugging)
        {
            log.info("Setting up SQL ...");
        }

        success = DEPlayersSQL != null && DEPlayersSQL.isConnectionEstablished();
        if (DEConfigOptions.debugging)
        {
            log.info("  Connection was " + (success ? "" : "NOT ") + "successful");
        }
        if (success)
        {
            success = DEPlayersSQL.populateColumnInformation();
        }

        if (DEConfigOptions.debugging)
        {
            log.info("  SQLValues population was " + (success ? "" : "NOT ") + "successful");
        }

        return success;
    }

    /** Set up permissions for DECore using Vault. Returns if DECore now has a Permissions Handler */
    private static boolean setupPermissions()
    {
        if (DEPermissions.permissionHandle != null)
        {
            return true;
        }

        RegisteredServiceProvider<Permission> rsp = DECore.getServer().getServicesManager().getRegistration(Permission.class);
        DEPermissions.permissionHandle = rsp.getProvider();
        if (DEPermissions.permissionHandle != null)
        {
            log.info("Found " + rsp.getPlugin().getName() + " sucessfully!");
            return true;
        }

        log.info("Permission system not detected. Attempting to use server permissions and defaulting to OP...");
        return false;
    }

    /** Shortcut for DEConfigOptions.useDEPlayersSQL && DEPlayersSQL != null */
    public static boolean usingMySQL()
    {
        return ((DEConfigOptions.useDEPlayersSQL) && (DEPlayersSQL != null));
    }

    /** Load up the DECore config files and update the tab list. */
    private void loadConfigs()
    {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists())
        {
            saveDefaultConfig();
        }
        reloadConfig();
        FileConfiguration config = getConfig();
        getServer().getScheduler().cancelTasks(this); // If we were running tasks before, stop now. [In case of reload.]

        if (config != null)
        {
            if (!config.getString("Mysql.URL", "").isEmpty())
                DEPlayersSQL = new DEPlayerDatabase(new SQLTable(config.getString("Mysql.URL", ""), config.getString("Mysql.Tablename", ""), config.getString(
                        "Mysql.Username", ""), config.getString("Mysql.Password", "")));

            DEConfigOptions.boarders.clear();

            DEConfigOptions.formatChatColours = config.getBoolean("Chat.FormatChatColourCodes", DEConfigOptions.formatChatColours);
            DEConfigOptions.MOTD = config.getStringList("Chat.MOTD");
            for (int line = 0; line < DEConfigOptions.MOTD.size(); line++)
            {
                DEConfigOptions.MOTD.set(line, Colours.fixColours(DEConfigOptions.MOTD.get(line)));
            }
            DEConfigOptions.useLoginLogoutMessages = config.getBoolean("Chat.UseLoginLogoutMessages", DEConfigOptions.useLoginLogoutMessages);

            DEConfigOptions.useCoveredDoubleChests = config.getBoolean("Chests.UseCoveredDoubleChests", DEConfigOptions.useCoveredDoubleChests);
            DEConfigOptions.useCoveredSingleChests = config.getBoolean("Chests.UseCoveredSingleChests", DEConfigOptions.useCoveredSingleChests);

            DEConfigOptions.useCheckInvCommand = config.getBoolean("Commands.UseCheckInventoryCommand", DEConfigOptions.useCheckInvCommand);
            DEConfigOptions.useGameModeShortcuts = config.getBoolean("Commands.UseGameModeShortcuts", DEConfigOptions.useGameModeShortcuts);
            DEConfigOptions.useResendChunkCommand = config.getBoolean("Commands.UseResendChunkCommand", DEConfigOptions.useResendChunkCommand);
            DEConfigOptions.useSpawnCommand = config.getBoolean("Commands.UseSpawnCommand", DEConfigOptions.useSpawnCommand);
            DEConfigOptions.useTPCommand = config.getBoolean("Commands.UseTPCommand", DEConfigOptions.useTPCommand);
            DEConfigOptions.useTPAllCommand = config.getBoolean("Commands.UseTPAllCommand", DEConfigOptions.useTPAllCommand);
            DEConfigOptions.useTPLocCommand = config.getBoolean("Commands.UseTPLocCommand", DEConfigOptions.useTPLocCommand);
            DEConfigOptions.useVanishCommand = config.getBoolean("Commands.UseVanishCommand", DEConfigOptions.useVanishCommand);

            DEConfigOptions.useFlagsSystem = config.getBoolean("Flags.UseFlagsSystem", DEConfigOptions.useFlagsSystem);
            DEConfigOptions.flagsToBan = config.getInt("Flags.FlagsToBan", DEConfigOptions.flagsToBan);

            DEConfigOptions.debugging = config.getBoolean("Plugin.Debugging", DEConfigOptions.debugging);

            DEConfigOptions.promoteToVeterans = config.getBoolean("Promotions.PromoteToVeterans", DEConfigOptions.promoteToVeterans);
            DEConfigOptions.promoteToVeteransTime = config.getInt("Promotions.PromoteToVeteransTimeInHours", DEConfigOptions.promoteToVeteransTime);

            DEConfigOptions.banHelpfulThowingPotions = config.getBoolean("Protections.BanHelpfulThowingPotions", DEConfigOptions.banHelpfulThowingPotions);
            DEConfigOptions.banHostileThowingPotions = config.getBoolean("Protections.BanHostileThowingPotions", DEConfigOptions.banHostileThowingPotions);
            DEConfigOptions.creeperprotect = config.getBoolean("Protections.CreeperProtect", DEConfigOptions.creeperprotect);
            DEConfigOptions.endermenprotect = config.getBoolean("Protections.EndermenProtect", DEConfigOptions.endermenprotect);
            DEConfigOptions.forceSheepRegrowth = config.getBoolean("Protections.ForceSheepWoolRegrowth", DEConfigOptions.forceSheepRegrowth);
            DEConfigOptions.lightningIsReal = config.getBoolean("Protections.LightningIsReal", DEConfigOptions.lightningIsReal);

            DEConfigOptions.deathMessageBroadcastRange = config.getInt("Server.DeathMessageBroadcastRange", DEConfigOptions.deathMessageBroadcastRange);
            DEConfigOptions.disableCantKeepUp = config.getBoolean("Server.DisableCantKeepUpMessages", DEConfigOptions.disableCantKeepUp);
            DEConfigOptions.moderated = config.getBoolean("Server.Moderated", false);
            DEConfigOptions.notWhitelistedMessage = config.getString("Server.NotWhiteListedMessage", DEConfigOptions.notWhitelistedMessage);
            Set<String> keys = config.getConfigurationSection("Server.WorldBoarders").getKeys(false);
            for (String worldkey : keys)
            {
                if (worldkey.isEmpty())
                    continue;

                if (getServer().getWorld(worldkey) == null)
                {
                    log.warning("The world \"" + worldkey + "\" specified in the WorldBoarders config was not found.");
                    continue;
                }

                WorldBoarder wb = new WorldBoarder();
                wb.Xmax = config.getInt("Server.WorldBoarders" + worldkey + ".XMAX", 2500);
                wb.Ymax = config.getInt("Server.WorldBoarders" + worldkey + ".YMAX", 256);
                wb.Zmax = config.getInt("Server.WorldBoarders" + worldkey + ".ZMAX", 2500);
                wb.Xmin = config.getInt("Server.WorldBoarders" + worldkey + ".XMIN", -2500);
                wb.Ymin = config.getInt("Server.WorldBoarders" + worldkey + ".YMIN", 0);
                wb.Zmin = config.getInt("Server.WorldBoarders" + worldkey + ".ZMIN", -2500);
                DEConfigOptions.boarders.put(worldkey, wb);
            }
            DEConfigOptions.DECoreOps = config.getStringList("Server.DECoreOpsCanOpSelf");

            int x = config.getInt("GuestSpawnLocation.X", 0);
            int y = config.getInt("GuestSpawnLocation.Y", 0);
            int z = config.getInt("GuestSpawnLocation.Z", 0);
            String worldname = config.getString("GuestSpawnLocation.World", "");
            DEConfigOptions.guestSpawn = ((x == 0 && y == 0 && z == 0) || worldname.isEmpty()) ? null : new DELocation(worldname, x, y, z);

            DEConfigOptions.voteBroadcastMessage = config.getString("Vote.BroadcastMessage", DEConfigOptions.voteBroadcastMessage);
            DEConfigOptions.voteDEPChance = config.getDouble("Vote.DEPChance", DEConfigOptions.voteDEPChance);
            DEConfigOptions.voteDEPReward = config.getInt("Vote.DEPReward", DEConfigOptions.voteDEPReward);
            DEConfigOptions.voteReward = config.getInt("Vote.Reward", DEConfigOptions.voteReward);
            DEConfigOptions.voteThankyouMessage = config.getString("Vote.ThankYouMessage", DEConfigOptions.voteThankyouMessage);
            DEConfigOptions.voteThankyouMessageDEPoints = config.getString("Vote.ThankYouMessageDEP", DEConfigOptions.voteThankyouMessageDEPoints);
        }

        final File tabFile = new File(getDataFolder(), "tabnames.yml");
        if (!tabFile.exists())
        {
            saveResource("tabnames.yml", false);
        }

        final File tipsFile = new File(getDataFolder(), "tips.yml");
        if (!tipsFile.exists())
        {
            saveResource("tips.yml", false);
        }

        try
        {
            config = YamlConfiguration.loadConfiguration(tipsFile);
        }
        catch (Exception ex)
        {
            log.warning("Error loading tips: " + ex);
            return;
        }

        if (config != null)
        {
            // Now in tips.yml
            DEConfigOptions.tips = new ArrayList<String>(config.getStringList("Tips"));
            DEConfigOptions.tipsRepetitionTime = config.getInt("RepetitionTimeInMinutes", DEConfigOptions.tipsRepetitionTime);

            if (Strings.isNullOrEmpty(DEConfigOptions.tips) || DEConfigOptions.tipsRepetitionTime <= 0)
            {
                return;
            }

            Runnable broadcastTip = new Runnable()
            {
                public void run()
                {
                    if (Strings.isNullOrEmpty(DEConfigOptions.tips))
                    {
                        return;
                    }

                    Random rand = new Random();
                    int index = rand.nextInt(DEConfigOptions.tips.size());
                    getDECore().getServer().broadcastMessage(Colours.fixColours(DEConfigOptions.tips.get(index)));
                }
            };
            getServer().getScheduler().scheduleSyncRepeatingTask(this, broadcastTip, TimeParser.secondsToTicks(60),
                    TimeParser.secondsToTicks(DEConfigOptions.tipsRepetitionTime * 60));
        }
        getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable()
        {
            public void run()
            {
                for (DEPlayer deplayer : DEPlayers.values())
                {
                    deplayer.setTabName();
                }
            }
        }, TimeParser.secondsToTicks(120), TimeParser.secondsToTicks(120)); // Every 2 mins, the tab names will be updated to reflect permissions.
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        final Player commandIssuer = sender instanceof Player ? (Player) sender : null;
        final DEPlayer deplayer = commandIssuer != null ? DEPlayers.get(commandIssuer.getName()) : null;
        Player target;
        DEPlayer targetdeplayer;
        final String base = command.getName();

        /*
         * ######################
         * # #
         * # OTHER COMMANDS #
         * # #
         * ######################
         */

        switch (base.toLowerCase())
        {

        // DEP Command //
            case "dep":
                if (args.length == 0)
                {
                    if (deplayer == null)
                    {
                        sender.sendMessage("Not online, please use /dep <player>");
                        return true;
                    }
                    sender.sendMessage("You have " + deplayer.getDEP() + " points!");
                }
                else if (args.length == 1)
                {
                    target = getServer().getPlayer(args[0]);
                    if (target == null)
                    {
                        sender.sendMessage("Player " + args[0] + " not found.");
                        return true;
                    }
                    targetdeplayer = DEPlayers.get(target.getName());
                    sender.sendMessage(target.getName() + " has " + targetdeplayer.getDEP() + " points!");
                }
                else if (args.length == 3)
                {
                    if (args[0].equalsIgnoreCase("pay"))
                    {
                        if (commandIssuer == null || deplayer == null)
                        {
                            sender.sendMessage("As server, you can only /give points, not pay.");
                            return true;
                        }

                        int amount = 0;
                        try
                        {
                            amount = Integer.parseInt(args[1]);
                        }
                        catch (Exception ex)
                        {
                            commandIssuer.sendMessage("/dep pay <amount> <player> -- give someone your DEP");
                            return true;
                        }

                        target = getServer().getPlayer(args[2]);
                        if (target == null)
                        {
                            commandIssuer.sendMessage("Player " + args[2] + " not found.");
                            return true;
                        }

                        if (amount <= 0)
                        {
                            commandIssuer.sendMessage("Why?");
                            return true;
                        }

                        if (amount > deplayer.getDEP())
                        {
                            commandIssuer.sendMessage("You can't pay that much: you have " + deplayer.getDEP() + " points.");
                            return true;
                        }

                        targetdeplayer = DEPlayers.get(target.getName());
                        targetdeplayer.changeNumericalProperty(DEPlayerSQLValue.DEPOINTS, amount, usingMySQL());
                        deplayer.changeNumericalProperty(DEPlayerSQLValue.DEPOINTS, -amount, usingMySQL());
                        sender.sendMessage("Paid " + amount + " to " + target.getName() + "!");
                    }
                    else if (args[0].equalsIgnoreCase("give"))
                    {
                        if (DEPermissions.isAtLeast(sender, DEGroup.MODERATOR))
                        {
                            int amount = 0;
                            try
                            {
                                amount = Integer.parseInt(args[1]);
                            }
                            catch (Exception ex)
                            {
                                sender.sendMessage("/dep give <amount> <player> -- give someone DEP");
                                return true;
                            }

                            if (amount <= 0)
                            {
                                sender.sendMessage("Why?");
                                return true;
                            }

                            target = getServer().getPlayer(args[2]);
                            if (target == null)
                            {
                                sender.sendMessage("Player " + args[2] + " not found.");
                                return true;
                            }

                            targetdeplayer = DEPlayers.get(target.getName());
                            targetdeplayer.changeNumericalProperty(DEPlayerSQLValue.DEPOINTS, amount, usingMySQL());
                            sender.sendMessage("Gave " + amount + " to " + target.getName() + "!");
                        }
                        else
                        {
                            sender.sendMessage(NO_PERMISSION + " to give DEP points. Try /dep pay <amount> <player>");
                        }
                    }
                    else if (args[0].equalsIgnoreCase("take"))
                    {
                        if (DEPermissions.isAtLeast(sender, DEGroup.MODERATOR))
                        {
                            int amount = 0;
                            try
                            {
                                amount = Integer.parseInt(args[1]);
                            }
                            catch (Exception ex)
                            {
                                sender.sendMessage("/dep take <amount> <player> -- take someone's DEP");
                                return true;
                            }

                            if (amount <= 0)
                            {
                                sender.sendMessage("Why?");
                                return true;
                            }

                            target = getServer().getPlayer(args[2]);
                            if (target == null)
                            {
                                sender.sendMessage("Player " + args[2] + " not found.");
                                return true;
                            }

                            targetdeplayer = DEPlayers.get(target.getName());
                            if (amount > targetdeplayer.getDEP())
                            {
                                sender.sendMessage("You can't take that much: they have " + targetdeplayer.getDEP() + " points.");
                                return true;
                            }

                            targetdeplayer.changeNumericalProperty(DEPlayerSQLValue.DEPOINTS, -amount, usingMySQL());
                            sender.sendMessage("Took " + amount + " from " + target.getName() + "!");
                        }
                        else
                        {
                            sender.sendMessage(NO_PERMISSION + " to take DEP points.");
                        }
                    }
                }
                else
                {
                    sender.sendMessage("/dep -- display your points");
                    sender.sendMessage("/dep <player> -- display someone else's points");
                    sender.sendMessage("/dep pay <amount> <player> -- give someone your points");
                }
                return true;

                // Creative Command //
            case "adventure":
                if (!DEConfigOptions.useGameModeShortcuts)
                {
                    return false;
                }

                if (!(sender instanceof Player))
                {
                    if (args.length != 1)
                    {
                        sender.sendMessage("/adventure <player>");
                        return true;
                    }

                    target = getServer().getPlayer(args[0]);
                    if (target == null)
                    {
                        sender.sendMessage("Player " + args[0] + " not found.");
                        return true;
                    }

                    target.setGameMode(GameMode.ADVENTURE);
                    sender.sendMessage("Switched " + target.getName() + " to adventure mode.");
                    return true;
                }

                if (commandIssuer != null)
                {
                    if (args.length == 0)
                    {
                        commandIssuer.chat("/gamemode " + GameMode.ADVENTURE.getValue() + " " + commandIssuer.getName());
                    }
                    else
                    {
                        target = getServer().getPlayer(args[0]);
                        if (target == null)
                        {
                            sender.sendMessage("Player " + args[0] + " not found.");
                            return true;
                        }
                        commandIssuer.chat("/gamemode " + GameMode.ADVENTURE.getValue() + " " + target.getName());
                    }
                }
                return true;

                // Inv Command //
            case "checkinventory":
                if (!DEConfigOptions.useCheckInvCommand)
                {
                    return false;
                }

                if (commandIssuer == null)
                {
                    if (args.length != 1)
                    {
                        sender.sendMessage("/inv <player>");
                        return true;
                    }

                    target = getServer().getPlayer(args[0]);
                    if (target == null)
                    {
                        sender.sendMessage("Player " + args[0] + " not found.");
                        return true;
                    }

                    ItemStack[] contents = target.getInventory().getContents();
                    for (int i = 0; i < contents.length; i++)
                    {
                        ItemStack is = contents[i];
                        if (is == null)
                            sender.sendMessage("" + i + ": empty");
                        else
                            sender.sendMessage("" + i + ": " + is.getAmount() + "x " + is.getType().toString().toLowerCase());
                    }
                    return true;
                }

                if (args.length != 1)
                {
                    sender.sendMessage("/inv <player>");
                    return true;
                }

                target = getServer().getPlayer(args[0]);
                if (target == null)
                {
                    sender.sendMessage("Player " + args[0] + " not found.");
                    return true;
                }

                if (DEPermissions.isAtLeast(sender, DEGroup.MODERATOR))
                {
                    sender.sendMessage(ChatColor.DARK_AQUA + "Opening " + target.getName() + "'s inventory.");
                    ((Player) sender).openInventory(target.getInventory());
                }
                else
                {
                    sender.sendMessage(NO_PERMISSION + " to open inventories.");
                }
                return true;

                // Clear Command //
            case "clearinventory":
                if (!DEConfigOptions.useClearInvCommand)
                {
                    return false;
                }

                if (commandIssuer == null)
                {
                    if (args.length != 1)
                    {
                        sender.sendMessage("/clearinv <player>");
                        return true;
                    }

                    target = getServer().getPlayer(args[0]);
                    if (target == null)
                    {
                        sender.sendMessage("Player " + args[0] + " not found.");
                        return true;
                    }

                    target.getInventory().clear();

                    target.updateInventory();
                    sender.sendMessage("Cleared " + target.getName().replace(":", "") + "'s inventory.");
                    return true;
                }

                if (DEPermissions.isAtLeast(commandIssuer, DEGroup.MODERATOR))
                {
                    if (args.length >= 2)
                    {
                        sender.sendMessage(ChatColor.DARK_AQUA + "/clearinv <player>");
                        return true;
                    }

                    if (args.length == 0)
                    {
                        commandIssuer.getInventory().clear();
                        commandIssuer.updateInventory();
                        sender.sendMessage(ChatColor.DARK_AQUA + "Cleared your inventory.");
                    }
                    else
                    {
                        target = getServer().getPlayer(args[0]);
                        if (target == null)
                        {
                            sender.sendMessage(ChatColor.DARK_AQUA + "Player " + args[0] + " not found.");
                            return true;
                        }
                        target.getInventory().clear();
                        target.updateInventory();
                        sender.sendMessage("Cleared " + target.getName().replace(":", "") + "'s inventory.");
                    }
                }
                else
                {
                    sender.sendMessage(NO_PERMISSION + " to clear inventories.");
                }
                return true;

                // Creative Command //
            case "creative":
                if (!DEConfigOptions.useGameModeShortcuts)
                {
                    return false;
                }

                if (!(sender instanceof Player))
                {
                    if (args.length != 1)
                    {
                        sender.sendMessage("/creative <player>");
                        return true;
                    }

                    target = getServer().getPlayer(args[0]);
                    if (target == null)
                    {
                        sender.sendMessage("Player " + args[0] + " not found.");
                        return true;
                    }

                    target.setGameMode(GameMode.CREATIVE);
                    sender.sendMessage("Switched " + target.getName() + " to creative mode.");
                    return true;
                }

                if (commandIssuer != null)
                {
                    if (args.length == 0)
                    {
                        commandIssuer.chat("/gamemode " + GameMode.CREATIVE.getValue() + " " + commandIssuer.getName());
                    }
                    else
                    {
                        target = getServer().getPlayer(args[0]);
                        if (target == null)
                        {
                            sender.sendMessage("Player " + args[0] + " not found.");
                            return true;
                        }
                        commandIssuer.chat("/gamemode " + GameMode.CREATIVE.getValue() + " " + target.getName());
                    }
                }
                return true;

                // Off duty mod Command //
            case "offduty":
                if (DEPermissions.isInGroup(sender, DEGroup.MODERATOR))
                {
                    DEPermissions.setGroup(commandIssuer, DEGroup.OFFMOD); // If sender is a mod, they must be online
                    DEPlayers.get(sender.getName()).setTabName();
                }
                else
                {
                    sender.sendMessage("You aren't an onduty mod. Opped?");
                }
                return true;

                // On duty mod Command //
            case "onduty":
                if (DEPermissions.isInGroup(sender, DEGroup.OFFMOD))
                {
                    DEPermissions.setGroup(commandIssuer, DEGroup.MODERATOR); // If sender is off mod, they must be online
                    DEPlayers.get(sender.getName()).setTabName();
                }
                else
                {
                    sender.sendMessage("You aren't an offduty mod. Opped?");
                }
                return true;

                // MOTD Command //
            case "motd":
                if (!DEConfigOptions.MOTD.isEmpty())
                {
                    for (String line : DEConfigOptions.MOTD)
                    {
                        sender.sendMessage(line);
                    }
                    return true;
                }
                return false;

                // Rank Command //
            case "deviantgetrank":
                sender.sendMessage(ChatColor.DARK_AQUA + "Your Rank is: " + DEPermissions.getGroup(commandIssuer).getChatColourCode()
                        + DEPermissions.getGroup(commandIssuer).toString().toLowerCase());
                return true;

                // Ranks Command //
            case "deviantgetranks":
                for (DEGroup group : DEPermissions.DEGroup.values())
                {
                    if (group == DEGroup.ERROR)
                    {
                        continue;
                    }
                    sender.sendMessage(group.getColourCode() + Strings.correctCase(group.toString()));
                }
                return true;

                // Buckets Command //
            case "deviantbucket":
                if (commandIssuer == null)
                {
                    sender.sendMessage("You must be online to use Buckets.");
                    return true;
                }
                if (DEPermissions.hasPerm(commandIssuer, "de.buckets"))
                {
                    args[0] = args[0].isEmpty() ? "toggle" : args[0];
                    DEPlayer dePlayer = DEPlayers.get(commandIssuer);

                    boolean currentlyOn = (Boolean) dePlayer.getProperty(DEPlayerSQLValue.INFINITE_BUCKET);

                    if (args[0].equalsIgnoreCase("toggle"))
                    {
                        boolean nowOn = !currentlyOn;
                        dePlayer.setProperty(DEPlayerSQLValue.INFINITE_BUCKET, nowOn, DEConfigOptions.useDEPlayersSQL);
                        if (nowOn)
                        {
                            commandIssuer.sendMessage(ChatColor.GREEN + "Infinite Buckets " + ChatColor.GOLD + "on" + ChatColor.GREEN + ".");
                        }
                        else
                        {
                            commandIssuer.sendMessage(ChatColor.GREEN + "Infinite Buckets " + ChatColor.GOLD + "off" + ChatColor.GREEN + ".");
                        }
                    }
                    else if (args[0].equalsIgnoreCase("on"))
                    {
                        if (currentlyOn)
                        {
                            commandIssuer.sendMessage(ChatColor.GREEN + "Infinite Buckets already " + ChatColor.GOLD + "on" + ChatColor.GREEN + ".");
                        }
                        else
                        {
                            dePlayer.setProperty(DEPlayerSQLValue.INFINITE_BUCKET, true, DEConfigOptions.useDEPlayersSQL);
                            commandIssuer.sendMessage(ChatColor.GREEN + "Infinite Buckets now " + ChatColor.GOLD + "on" + ChatColor.GREEN + ".");
                        }
                    }
                    else if (args[0].equalsIgnoreCase("off"))
                    {
                        if (!currentlyOn)
                        {
                            commandIssuer.sendMessage(ChatColor.GREEN + "Infinite Buckets already " + ChatColor.GOLD + "off" + ChatColor.GREEN + ".");
                        }
                        else
                        {
                            dePlayer.setProperty(DEPlayerSQLValue.INFINITE_BUCKET, false, DEConfigOptions.useDEPlayersSQL);
                            commandIssuer.sendMessage(ChatColor.GREEN + "Infinite Buckets now " + ChatColor.GOLD + "off" + ChatColor.GREEN + ".");
                        }
                    }
                    else
                    {
                        commandIssuer.sendMessage(ChatColor.GREEN + "/bucket toggle|on|off");
                    }
                }
                else
                {
                    commandIssuer.sendMessage(NO_PERMISSION + " for Infinite Buckets.");
                }
                return true;

                // Flag commands //
            case "deviantflag":
                if (!DEConfigOptions.useFlagsSystem)
                    return false;

                if (commandIssuer == null || deplayer == null)
                {
                    sender.sendMessage("You need to be online to use this!");
                    return true;
                }

                target = getServer().getPlayer(args[0]);
                if (target == null)
                {
                    sender.sendMessage(ChatColor.DARK_AQUA + "Player " + args[0] + " not found.");
                    return true;
                }

                if (DEPermissions.isAtLeast(target, DEGroup.MEMBER))
                {
                    sender.sendMessage(ChatColor.DARK_AQUA + SERVER_STRING + target.getName() + " is already above Guest.");
                    return true;
                }

                if (DEPermissions.isAtLeast(sender, DEGroup.VETERAN))
                {

                    boolean wasFlagged = deplayer.vouchAndFlagData.flagPlayer(commandIssuer, target);
                    if (wasFlagged)
                    {
                        sender.sendMessage(ChatColor.DARK_AQUA + SERVER_STRING + " You flagged " + target.getName() + " successfully.");

                        targetdeplayer = DEPlayers.get(target.getName());
                        if (targetdeplayer.vouchAndFlagData.getFlagsAmount() >= DEConfigOptions.flagsToBan)
                        {
                            DEPermissions.setGroup(target, DEGroup.ERROR);
                            target.kickPlayer("You have been flagged by the community and therefore kickbanned. \n " +
                                    "If you believe this is in error, please appeal on the forums at \n " +
                                    "www.deviantevil.net");
                            target.setBanned(true);
                        }
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.DARK_AQUA + SERVER_STRING + " You cannot vouch for " + target.getName() + ".");
                    }
                }
                else
                {
                    sender.sendMessage(ChatColor.DARK_AQUA + SERVER_STRING + " Sorry, you cannot vouch for people at your current rank.");
                }

                return true;

                // Time/Ping Command //
            case "deviantgettime":
                int hours = TimeParser.getGMTHours();
                int minutes = TimeParser.getMinutes();
                int seconds = TimeParser.getSeconds();
                String time;

                // GMT display
                if (hours < 10)
                {
                    time = "0" + hours;
                }
                else
                {
                    time = "" + hours;
                }
                if (minutes < 10)
                {
                    time += ":" + "0" + minutes;
                }
                else
                {
                    time += ":" + minutes;
                }
                if (seconds < 10)
                {
                    time += ":" + "0" + seconds;
                }
                else
                {
                    time += ":" + seconds;
                }
                sender.sendMessage(ChatColor.DARK_AQUA + SERVER_STRING + ": The time is " + ChatColor.AQUA + time + ChatColor.DARK_AQUA + " [GMT].");

                if (TimeParser.getServerHours() == TimeParser.getGMTHours())
                    return true;

                time = "";
                hours = TimeParser.getServerHours();
                if (hours < 10)
                {
                    time = "0" + hours;
                }
                else
                {
                    time = "" + hours;
                }
                if (minutes < 10)
                {
                    time += ":" + "0" + minutes;
                }
                else
                {
                    time += ":" + minutes;
                }
                if (seconds < 10)
                {
                    time += ":" + "0" + seconds;
                }
                else
                {
                    time += ":" + seconds;
                }
                sender.sendMessage(ChatColor.DARK_AQUA + SERVER_STRING + ": The time is: " + ChatColor.AQUA + time + ChatColor.DARK_AQUA + " ["
                        + TimeParser.getServerTimeTag() + "].");
                return true;

                // Getpos Command //
            case "deviantgetpos":
                if (commandIssuer == null)
                {
                    sender.sendMessage("You must be in game to get your position.");
                    return true;
                }

                final Location location = commandIssuer.getLocation();
                commandIssuer.sendMessage(ChatColor.GRAY + "Your position: " + ChatColor.WHITE + Locations.toStr(location));

                String direction;
                float pitch = location.getPitch();
                float yaw = location.getYaw();
                if (pitch < -50)
                {
                    direction = "Upwards";
                }
                else if (pitch > 50)
                {
                    direction = "Downwards";
                }
                else if ((yaw >= 22.5 && yaw < 67.5) || (yaw <= -292.5 && yaw > -337.5))
                {
                    direction = "Southwest"; // -X+Z
                }
                else if ((yaw >= 67.5 && yaw < 112.5) || (yaw <= -247.5 && yaw > -292.5))
                {
                    direction = "West"; // -X
                }
                else if ((yaw >= 112.5 && yaw < 157.5) || (yaw <= -202.5 && yaw > -247.5))
                {
                    direction = "Northwest"; // -X-Z
                }
                else if ((yaw >= 157.5 && yaw < 202.5) || (yaw <= -157.5 && yaw > -202.5))
                {
                    direction = "North"; // -Z
                }
                else if ((yaw >= 202.5 && yaw < 247.5) || (yaw <= -112.5 && yaw > -157.5))
                {
                    direction = "Northeast"; // +X-Z
                }
                else if ((yaw >= 247.5 && yaw < 292.5) || (yaw <= -67.5 && yaw > -112.5))
                {
                    direction = "East"; // +X
                }
                else if ((yaw >= 292.5 && yaw < 337.5) || (yaw <= -22.5 && yaw > -67.5))
                {
                    direction = "Southeast"; // +X+Z
                }
                else if ((yaw >= 337.5 || yaw < 22.5) || (yaw <= -337.5 || yaw > -22.5))
                {
                    direction = "South"; // +Z
                }
                else
                {
                    direction = "Unknown";
                }
                commandIssuer.sendMessage(ChatColor.GRAY + "You are facing: " + ChatColor.WHITE + direction + ".");
                return true;

                // Vouch commands //
            case "deviantvouch":
                if (!DEConfigOptions.useVouchingSystem)
                    return false;

                if (commandIssuer == null || deplayer == null)
                {
                    sender.sendMessage("You need to be online to use this!");
                    return true;
                }

                target = getServer().getPlayer(args[0]);
                if (target == null)
                {
                    sender.sendMessage(ChatColor.DARK_AQUA + "Player " + args[0] + " not found.");
                    return true;
                }

                if (DEPermissions.isAtLeast(target, DEGroup.MEMBER))
                {
                    sender.sendMessage(ChatColor.DARK_AQUA + SERVER_STRING + target.getName() + " is already above Guest.");
                    return true;
                }

                if (DEPermissions.isAtLeast(sender, DEGroup.VETERAN))
                {
                    boolean wasVouched = deplayer.vouchAndFlagData.vouchFor(commandIssuer, target);
                    if (wasVouched)
                    {
                        sender.sendMessage(ChatColor.DARK_AQUA + SERVER_STRING + " You vouched for " + target.getName() + " successfully. Thanks!");
                        target.sendMessage(ChatColor.DARK_AQUA + SERVER_STRING + sender.getName() + " vouched for you!");
                        targetdeplayer = DEPlayers.get(target.getName());
                        if (targetdeplayer.vouchAndFlagData.getVouchesAmount() >= DEConfigOptions.vouchesToMember)
                        {
                            DEPermissions.setGroup(target, DEGroup.MEMBER);
                            target.sendMessage(ChatColor.DARK_AQUA + SERVER_STRING + " You were promoted to Member! Congratulations!");
                        }
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.DARK_AQUA + SERVER_STRING + " You cannot vouch for " + target.getName() + ".");
                    }
                }
                else
                {
                    sender.sendMessage(ChatColor.DARK_AQUA + SERVER_STRING + " Sorry, you cannot vouch for people at your current rank.");
                }

                return true;

                // Chunk Command //
            case "resendchunk":
                if (!DEConfigOptions.useResendChunkCommand)
                {
                    return false;
                }
                if (commandIssuer == null)
                {
                    sender.sendMessage("You need to be online to use this!");
                    return true;
                }
                if (args[0].isEmpty())
                {
                    World world = commandIssuer.getWorld();
                    Chunk chunk = world.getChunkAt(commandIssuer.getLocation());
                    int chunkx = chunk.getX();
                    int chunkz = chunk.getZ();
                    world.refreshChunk(chunkx, chunkz);
                    commandIssuer.sendMessage(ChatColor.DARK_AQUA + "Resent your chunk.");
                }
                else
                {
                    int chunksradius;
                    try
                    {
                        chunksradius = Integer.parseInt(args[0]);
                    }
                    catch (Exception ex)
                    {
                        commandIssuer.sendMessage(ChatColor.RED + args[0] + " is not a number.");
                        return true;
                    }

                    switch (DEPermissions.getGroup(commandIssuer))
                    {
                        case ADMIN:
                            if (chunksradius > 20)
                            {
                                commandIssuer.sendMessage(ChatColor.DARK_AQUA + "To limit corruption, you may request up to radius 20.");
                                chunksradius = 20;
                            }
                            break;
                        case MODERATOR:
                        case OFFMOD:
                            if (chunksradius > 10)
                            {
                                commandIssuer.sendMessage(ChatColor.DARK_AQUA + "As moderator, you may request up to radius 10.");
                                chunksradius = 10;
                            }
                            break;
                        case CONTRIBUTOR:
                        case SPONSOR:
                            if (chunksradius > 5)
                            {
                                commandIssuer.sendMessage(ChatColor.DARK_AQUA + "You may request up to radius 5.");
                                chunksradius = 5;
                            }
                            break;
                        case DONOR:
                            if (chunksradius > 4)
                            {
                                commandIssuer.sendMessage(ChatColor.DARK_AQUA + "As a donor, you may request up to radius 4.");
                                chunksradius = 4;
                            }
                            break;
                        case VETERAN:
                            if (chunksradius > 3)
                            {
                                commandIssuer.sendMessage(ChatColor.DARK_AQUA + "As veteran, you may request up to radius 3.");
                                chunksradius = 3;
                            }
                            break;
                        case ERROR:
                        case GUEST:
                        case MEMBER:
                        default:
                            if (chunksradius > 2)
                            {
                                commandIssuer.sendMessage(ChatColor.DARK_AQUA + "As a player, you may request up to radius 2.");
                                chunksradius = 2;
                            }
                            break;
                    }

                    World world = commandIssuer.getWorld();
                    Location l = commandIssuer.getLocation();

                    int loadcount = 0;
                    int refreshcount = 0;

                    int baseX = l.getBlockX();
                    int baseZ = l.getBlockZ();

                    int xoffset = -chunksradius;
                    int zoffset = -chunksradius;

                    while (xoffset <= chunksradius)
                    {
                        while (zoffset <= chunksradius)
                        {
                            Chunk chunkAt = world.getChunkAt(baseX + (16 * xoffset), baseZ + (16 * zoffset));

                            if (!world.isChunkLoaded(chunkAt))
                            {
                                world.loadChunk(chunkAt);
                                loadcount++;
                            }
                            else
                            {
                                commandIssuer.teleport(new Location(world, chunkAt.getX(), 123, chunkAt.getZ()));
                                world.refreshChunk(commandIssuer.getLocation().getBlockX(), commandIssuer.getLocation().getBlockZ());
                                refreshcount++;
                            }
                            zoffset++;
                        }
                        zoffset = -chunksradius;
                        xoffset++;
                    }
                    commandIssuer.sendMessage(ChatColor.DARK_AQUA + "" + refreshcount + " chunks were refreshed (and " + loadcount + " chunks were loaded).");
                    commandIssuer.teleport(l);
                }
                return true;

                // Spawn Command //
            case "spawn":
                if (!DEConfigOptions.useSpawnCommand)
                {
                    return false;
                }
                if (commandIssuer == null)
                {
                    if (args[0].isEmpty())
                    {
                        sender.sendMessage("You must specify a target from console: /spawn <target>");
                        return true;
                    }
                    target = getServer().getPlayer(args[0]);
                    if (target == null)
                    {
                        sender.sendMessage(args[0] + " was not found online.");
                        return true;
                    }
                    target.teleport(target.getWorld().getSpawnLocation());
                    sender.sendMessage("Sent " + target.getName() + " to spawn!");
                    return true;
                }

                if (DEPermissions.hasPerm(commandIssuer, "de.spawn"))
                {
                    commandIssuer.teleport(commandIssuer.getWorld().getSpawnLocation());
                    commandIssuer.sendMessage(ChatColor.DARK_AQUA + "Sending you to the spawn!");
                }
                else
                {
                    commandIssuer.sendMessage(NO_PERMISSION + " to teleport to spawn.");
                }

                return true;

                // Survival Command //
            case "survival":
                if (!DEConfigOptions.useGameModeShortcuts)
                {
                    return false;
                }

                if (commandIssuer == null)
                {
                    if (args.length != 1)
                    {
                        sender.sendMessage("/survival <player>");
                        return true;
                    }
                    target = getServer().getPlayer(args[0]);
                    if (target == null)
                    {
                        sender.sendMessage("Player " + args[0] + " not found.");
                        return true;
                    }
                    target.setGameMode(GameMode.SURVIVAL);
                    sender.sendMessage("Switched " + target.getName() + " to survival mode.");
                    return true;
                }

                if (args.length == 0)
                {
                    commandIssuer.chat("/gamemode " + GameMode.SURVIVAL.getValue() + " " + commandIssuer.getName());
                }
                else
                {
                    target = getServer().getPlayer(args[0]);
                    if (target == null)
                    {
                        sender.sendMessage("Player " + args[0] + " not found.");
                        return true;
                    }
                    commandIssuer.chat("/gamemode " + GameMode.SURVIVAL.getValue() + " " + target.getName());
                }
                return true;

                // tp Command //
            case "tp":
                if (!DEConfigOptions.useTPCommand)
                {
                    return false;
                }
                Player destinationPlayer;
                if (commandIssuer == null)
                {
                    if (args.length != 2)
                    {
                        sender.sendMessage("/tp <player> <destinationPlayer>");
                        return true;
                    }

                    target = getServer().getPlayer(args[0]);
                    if (target == null)
                    {
                        sender.sendMessage("Player " + args[0] + " not found.");
                        return true;
                    }

                    destinationPlayer = getServer().getPlayer(args[1]);
                    if (destinationPlayer == null)
                    {
                        sender.sendMessage("Player " + args[1] + " not found.");
                        return true;
                    }

                    target.teleport(destinationPlayer);
                    target.sendMessage(ChatColor.DARK_AQUA + "Poof! You were sent to " + destinationPlayer.getName() + ".");
                    return true;
                }

                if (DEPermissions.isAtLeast(commandIssuer, DEGroup.MODERATOR))
                {
                    if (args.length != 1 && args.length != 2)
                    {
                        sender.sendMessage(ChatColor.DARK_AQUA + "/tp [player] <destinationPlayer>");
                        return true;
                    }

                    if (args.length == 1)
                    {
                        destinationPlayer = getServer().getPlayer(args[0]);
                        if (destinationPlayer == null)
                        {
                            sender.sendMessage("Player " + args[0] + " not found.");
                            return true;
                        }

                        commandIssuer.teleport(destinationPlayer);
                        commandIssuer.sendMessage(ChatColor.DARK_AQUA + "Poof! You teleported to " + destinationPlayer.getName() + ".");
                    }
                    else
                    {
                        target = getServer().getPlayer(args[0]);
                        if (target == null)
                        {
                            sender.sendMessage("Player " + args[0] + " not found.");
                            return true;
                        }

                        destinationPlayer = getServer().getPlayer(args[1]);
                        if (destinationPlayer == null)
                        {
                            sender.sendMessage("Player " + args[1] + " not found.");
                            return true;
                        }

                        target.teleport(destinationPlayer);
                        target.sendMessage(ChatColor.DARK_AQUA + "Poof! You were sent to " + destinationPlayer.getName() + ".");
                        commandIssuer.sendMessage(ChatColor.DARK_AQUA + "Successfully teleported " + target.getName() + " to " + destinationPlayer.getName()
                                + ".");
                    }
                }
                else
                {
                    commandIssuer.sendMessage(NO_PERMISSION + " to do this.");
                }

                return true;

                // tpall Command //
            case "tpall":
                if (!DEConfigOptions.useTPAllCommand)
                {
                    return false;
                }
                if (commandIssuer == null)
                {
                    sender.sendMessage("You have to be online to teleport players to you.");
                    return true;
                }

                if (DEPermissions.isAtLeast(commandIssuer, DEGroup.MODERATOR))
                {
                    if (args.length != 0 && args.length != 1)
                    {
                        sender.sendMessage(ChatColor.DARK_AQUA + "/tpall [world]");
                        return true;
                    }
                    int teleportCount = 0;

                    if (args.length == 0)
                    {
                        for (Player p : commandIssuer.getWorld().getPlayers())
                        {
                            p.teleport(commandIssuer);
                            p.sendMessage(ChatColor.DARK_AQUA + "Poof! You were teleported to " + commandIssuer.getName() + ".");
                            teleportCount++;
                        }
                    }
                    else
                    {
                        if (args[0].equalsIgnoreCase("all") || args[0].equals("*"))
                        {
                            for (Player p : commandIssuer.getServer().getOnlinePlayers())
                            {
                                p.teleport(commandIssuer);
                                p.sendMessage(ChatColor.DARK_AQUA + "Poof! You were teleported to " + commandIssuer.getName() + ".");
                                teleportCount++;
                            }
                        }
                        else
                        {
                            World world = getServer().getWorld(args[0]);
                            if (world == null)
                            {
                                sender.sendMessage("World " + args[0] + " not found.");
                                return true;
                            }

                            for (Player p : world.getPlayers())
                            {
                                p.teleport(commandIssuer);
                                p.sendMessage(ChatColor.DARK_AQUA + "Poof! You were teleported to " + commandIssuer.getName() + ".");
                                teleportCount++;
                            }
                        }
                    }
                    commandIssuer.sendMessage(ChatColor.DARK_AQUA + "" + teleportCount + " players were teleported to you.");
                }
                else
                {
                    commandIssuer.sendMessage(NO_PERMISSION + " to do this.");
                }

                return true;

                // tploc Command //
            case "tploc":
                if (!DEConfigOptions.useTPLocCommand)
                {
                    return false;
                }

                int x,
                y,
                z;
                if (commandIssuer == null)
                {
                    if (args.length < 4)
                    {
                        sender.sendMessage("/tploc <player> x y z");
                        return true;
                    }
                    target = getServer().getPlayer(args[0]);
                    if (target == null)
                    {
                        sender.sendMessage("Player " + args[0] + " not found.");
                        return true;
                    }

                    try
                    {
                        x = Integer.parseInt(args[1]);
                        y = Integer.parseInt(args[2]);
                        z = Integer.parseInt(args[3]);
                    }
                    catch (Exception ex)
                    {
                        sender.sendMessage("x y z must be numbers. (/tploc <player> x y z)");
                        return true;
                    }
                    final Location loc = new Location(target.getWorld(), x, y, z);
                    target.teleport(loc);
                    target.sendMessage(ChatColor.DARK_AQUA + "Poof! You were sent to " + Locations.toStr(loc));
                    return true;
                }

                if (DEPermissions.isAtLeast(commandIssuer, DEGroup.MODERATOR))
                {
                    if (args.length != 3 && args.length != 4)
                    {
                        sender.sendMessage(ChatColor.DARK_AQUA + "/tploc [player] x y z");
                        return true;
                    }

                    if (args.length == 3)
                    {
                        try
                        {
                            x = Integer.parseInt(args[0]);
                            y = Integer.parseInt(args[1]);
                            z = Integer.parseInt(args[2]);
                        }
                        catch (Exception ex)
                        {
                            sender.sendMessage(ChatColor.DARK_AQUA + "/tploc x y z (Must be numbers!)");
                            return true;
                        }
                        final Location loc = new Location(commandIssuer.getWorld(), x, y, z);
                        commandIssuer.teleport(loc);
                        commandIssuer.sendMessage(ChatColor.DARK_AQUA + "Poof! You were sent to " + Locations.toStr(loc));
                    }
                    else
                    {
                        target = getServer().getPlayer(args[0]);
                        if (target == null)
                        {
                            sender.sendMessage("Player " + args[0] + " not found.");
                            return true;
                        }
                        try
                        {
                            x = Integer.parseInt(args[1]);
                            y = Integer.parseInt(args[2]);
                            z = Integer.parseInt(args[3]);
                        }
                        catch (Exception ex)
                        {
                            sender.sendMessage(ChatColor.DARK_AQUA + "/tploc [player] x y z (Must be numbers!)");
                            return true;
                        }
                        final Location loc = new Location(target.getWorld(), x, y, z);
                        target.teleport(loc);
                        target.sendMessage(ChatColor.DARK_AQUA + "Poof! You were sent to " + Locations.toStr(loc));
                        commandIssuer.sendMessage(ChatColor.DARK_AQUA + "Successfully teleported " + target.getName() + " to " + Locations.toStr(loc));
                    }
                }
                else
                {
                    commandIssuer.sendMessage(NO_PERMISSION + " to do this.");
                }

                return true;

                // Vanish Command //
            case "vanish":
                if (!DEConfigOptions.useVanishCommand)
                {
                    return false;
                }

                if (commandIssuer == null || deplayer == null)
                {
                    sender.sendMessage("You need to be online to vanish.");
                    return true;
                }

                if (DEPermissions.isAtLeast(commandIssuer, DEGroup.MODERATOR))
                {
                    if (deplayer.isProperty(DEPlayerSQLValue.VANISHED))
                    {
                        deplayer.setVanished(false, false);
                    }
                    else
                    {
                        deplayer.setVanished(true, false);
                    }
                }
                else
                {
                    commandIssuer.sendMessage(NO_PERMISSION + " to do this.");
                }
                return true;
        }

        /*
         * ######################
         * # #
         * # DE CORE COMMANDS #
         * # #
         * ######################
         */

        if (!base.equalsIgnoreCase("deviantevilcore"))
        {
            return true;
        }

        if (args.length == 0)
        {
            if (DEPermissions.isLowerThan(sender, DEGroup.MODERATOR))
            {
                sender.sendMessage(ChatColor.DARK_AQUA + "DECore commands are for moderators and admins only.");
                return true;
            }
            sender.sendMessage(ChatColor.AQUA + "/de ? [pagenumber]");
            return true;
        }

        if (DEPermissions.isLowerThan(sender, DEGroup.MODERATOR) && !DEPermissions.hasPerm(sender, "decore.core")
                && !DEConfigOptions.DECoreOps.contains(sender.getName()))
        {
            sender.sendMessage(NO_PERMISSION + " to do this.");
            log.info(sender + " attempted to access a DE command: /de " + Arrays.deepToString(args));
            return true;
        }

        // Help ? //
        if (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help"))
        {
            if (!args[1].equals("2"))
            {
                if (DEPermissions.isInGroup(sender, DEGroup.ADMIN))
                {
                    sender.sendMessage(SERVER_PREFIX_STRING + ": Commands [1/2]: ");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de blaze [amount] -- Spawns Blazes in the Nether.");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de chat -- Change the chat.");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de console -- Mount the console o.o");
                    sender.sendMessage(ChatColor.DARK_AQUA
                            + "/de enable/disable <plugin|all> -- Attempts to enable/disable a registered DE plugin. Note: Case-sensitive, no version.");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de enchant [level] <list|enchantmentName> -- Enchants items.");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de god [player] -- Enable invincibility");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de heal [player] -- Heals you or another player.");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de info [player] -- Gets stored DEPlayer information on a player.");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de kill <list|rage|player> [radius|method] -- Kills entities/players.");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de level -- Sets your level.");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de magic [message] -- Write a magic message");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de message [value] -- appends a message onto everyone's messages for lols.");
                    sender.sendMessage(ChatColor.AQUA + "-----------------------------------------------------");
                }
                else
                {
                    sender.sendMessage(SERVER_PREFIX_STRING + ": Commands [1/2]: ");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de blaze [amount] -- Spawns Blazes in the Nether.");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de chat -- Change the chat.");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de enchant [level] <list|enchantmentName> -- Enchants items.");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de god [player] -- Enable invincibility");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de heal [player] -- Heals you or another player.");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de info [player] -- Gets stored DEPlayer information on a player.");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de kill <player> [method] -- Kills a player.");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de level -- Sets your level.");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de magic [message] -- Write a magic message");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de message [value] -- appends a message onto everyone's messages for lols.");
                    sender.sendMessage(ChatColor.DARK_AQUA + "/de mysql -- DEMysql functions");
                    sender.sendMessage(ChatColor.AQUA + "-----------------------------------------------------");
                }
                return true;
            }

            if (DEPermissions.isInGroup(sender, DEGroup.ADMIN))
            {
                sender.sendMessage(SERVER_PREFIX_STRING + ": Commands [2/2]: ");
                sender.sendMessage(ChatColor.DARK_AQUA + "/de plugins -- Attempt to get a list of all registered DE plugins and their status.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/de rainbow <text> -- Send a message formatted as rainbow text.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/de rainbowmagic <text> -- Send a message formatted as rainbow magic text.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/de random [max] -- Get a random number from 0 to optional max (else 100).");
                sender.sendMessage(ChatColor.DARK_AQUA + "/de reload -- Reloads the DECore configs");
                sender.sendMessage(ChatColor.DARK_AQUA + "/de spawnmob mobtype [amount] -- Spawns mobs.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/de (un)ride <player> -- (Un)Ride another player. /de ride2 rides ANY Entity");
                sender.sendMessage(ChatColor.DARK_AQUA + "/de weather <sun|rain|lightning> -- Set the world's weather.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/de whitelist <add|remove|list> <player|> -- DE whitelist commands.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/de zebra <text> -- Send a message formatted as zebra text.");
                sender.sendMessage(ChatColor.AQUA + "-----------------------------------------------------");
            }
            else
            {
                sender.sendMessage(SERVER_PREFIX_STRING + ": Commands [2/2]: ");
                sender.sendMessage(ChatColor.DARK_AQUA + "/de plugins -- Attempt to get a list of all registered DE plugins and their status.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/de rainbow <text> -- Send a message formatted as rainbow text.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/de rainbowmagic <text> -- Send a message formatted as rainbow magic text.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/de random [max] -- Get a random number from 0 to optional max (else 100).");
                sender.sendMessage(ChatColor.DARK_AQUA + "/de spawnmob mobtype [amount] -- Spawns mobs.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/de (un)ride <player> -- (Un)Ride another player. /de ride2 rides ANY Entity");
                sender.sendMessage(ChatColor.DARK_AQUA + "/de weather <sun|rain|lightning> -- Set the world's weather.");
                sender.sendMessage(ChatColor.DARK_AQUA + "/de zebra <text> -- Send a message formatted as zebra text.");
                sender.sendMessage(ChatColor.AQUA + "-----------------------------------------------------");
            }
            return true;
        }

        // Enable //
        if (args[0].equalsIgnoreCase("enable") && (DEPermissions.isInGroup(sender, DEGroup.ADMIN)))
        {
            if (!args[1].isEmpty())
            {
                if (!args[1].equalsIgnoreCase("all"))
                {
                    Plugin testplugin = this.pm.getPlugin(args[1]);
                    if (testplugin == null)
                    {
                        sender.sendMessage(ChatColor.DARK_AQUA + args[1] + " is not a recognised plugin.");
                    }
                    else
                    {
                        if (this.DEPlugins.containsKey(testplugin))
                        {
                            if (this.DEPlugins.get(testplugin).equalsIgnoreCase("enabled"))
                            {
                                sender.sendMessage(ChatColor.DARK_AQUA + args[1] + " is already enabled.");
                            }
                            else
                            {
                                try
                                {
                                    getPluginLoader().enablePlugin(testplugin);
                                    this.DEPlugins.put(testplugin, "enabled");
                                    sender.sendMessage(ChatColor.DARK_AQUA + "Enabled " + args[1] + " successfully. ");
                                }
                                catch (Exception ex)
                                {
                                    sender.sendMessage(ChatColor.DARK_AQUA + "Could not enable " + args[1] + ". " + ex);
                                }
                            }
                        }
                        else
                        {
                            sender.sendMessage(ChatColor.DARK_AQUA + args[1] + " is not registered with DECore.");
                        }
                    }
                }
                else
                {
                    int numberofplugins = 0;
                    int success = 0;
                    for (Plugin APlugin : this.DEPlugins.keySet())
                    {
                        numberofplugins++;
                        getPluginLoader().enablePlugin(APlugin);
                        this.DEPlugins.put(APlugin, "enabled");
                        success++;
                    }
                    sender.sendMessage(ChatColor.DARK_AQUA + "Enabled " + success + " / " + numberofplugins + ".");
                }
            }
            else
            {
                sender.sendMessage(ChatColor.DARK_AQUA + "/de enable [plugin|all]");
            }
            return true;
        }

        // Disable //
        else if (args[0].equalsIgnoreCase("disable") && (DEPermissions.isInGroup(sender, DEGroup.ADMIN)))
        {
            if (!args[1].isEmpty())
            {
                if (!args[1].equalsIgnoreCase("all"))
                {
                    Plugin testplugin = this.pm.getPlugin(args[1]);
                    if (testplugin == null)
                    {
                        sender.sendMessage(ChatColor.DARK_AQUA + args[1] + " is not a recognised plugin.");
                    }
                    else
                    {
                        if (this.DEPlugins.containsKey(testplugin))
                        {
                            if (this.DEPlugins.get(testplugin).equalsIgnoreCase("disabled"))
                            {
                                sender.sendMessage(ChatColor.DARK_AQUA + args[1] + " is already disabled.");
                            }
                            else
                            {
                                try
                                {
                                    getPluginLoader().disablePlugin(testplugin);
                                    this.DEPlugins.put(testplugin, "disabled");
                                    sender.sendMessage(ChatColor.DARK_AQUA + "Disabled " + args[1] + " successfully. ");
                                }
                                catch (Exception ex)
                                {
                                    sender.sendMessage(ChatColor.DARK_AQUA + "Could not disable " + args[1] + ". " + ex);
                                }
                            }
                        }
                        else
                        {
                            sender.sendMessage(ChatColor.DARK_AQUA + args[1] + " is not registered with DECore.");
                        }
                    }
                }
                else
                {
                    int numberofplugins = 0;
                    int success = 0;
                    for (Plugin APlugin : this.DEPlugins.keySet())
                    {
                        numberofplugins++;
                        getPluginLoader().disablePlugin(APlugin);
                        this.DEPlugins.put(APlugin, "disabled");
                        success++;
                    }
                    sender.sendMessage(ChatColor.DARK_AQUA + "Disabled " + success + " / " + numberofplugins + ".");
                }
            }
            else
            {
                sender.sendMessage(ChatColor.DARK_AQUA + "/de disable [plugin|all]");
            }
            return true;
        }

        // Plugins //
        else if (args[0].equalsIgnoreCase("plugins"))
        {
            List<String> plugins = new ArrayList<String>();
            if (this.DEPlugins.keySet() == null)
            {
                sender.sendMessage(ChatColor.DARK_AQUA + "No plugins registered with DECore.");
                return true;
            } // else

            for (Plugin APlugin : this.DEPlugins.keySet())
            {
                String status = this.DEPlugins.get(APlugin);
                if (status.equalsIgnoreCase("enabled"))
                {
                    plugins.add(ChatColor.GREEN + APlugin.toString());
                }
                else
                {
                    plugins.add(ChatColor.RED + APlugin.toString());
                }
            }
            sender.sendMessage(Strings.stringListToString(plugins, ChatColor.WHITE + ", "));
            return true;
        }

        // Reload //
        else if (args[0].equalsIgnoreCase("reload") && (DEPermissions.isInGroup(sender, DEGroup.ADMIN)))
        {
            loadConfigs();
            DEConfigOptions.useDEPlayersSQL = setupMySQL();
            sender.sendMessage(ChatColor.DARK_AQUA + "Reloaded.");
            return true;
        }

        // Ride //
        else if (args[0].equalsIgnoreCase("ride") || args[0].equalsIgnoreCase("mount"))
        {
            if (commandIssuer == null)
            {
                sender.sendMessage(ChatColor.DARK_AQUA + "You must be online to ride Players ;)");
                return true;
            }

            Entity vehicle = commandIssuer.getVehicle();
            if (vehicle != null)
            {
                if (vehicle instanceof Player)
                {
                    Player ridenplayer = (Player) vehicle;
                    ridenplayer.eject();
                    commandIssuer.sendMessage(ChatColor.DARK_AQUA + "Dismounted from " + ridenplayer.getName() + ".");
                    ridenplayer.sendMessage(ChatColor.DARK_AQUA + "You are free!");
                    return true;
                }

                vehicle.eject();
                commandIssuer.sendMessage(ChatColor.DARK_AQUA + "Dismounted.");
                return true;
            }

            Player nearestPlayer = Locations.getNearestPlayer(commandIssuer.getLocation(), commandIssuer);
            if (nearestPlayer == null)
            {
                sender.sendMessage(ChatColor.DARK_AQUA + "No Players nearby.");
                return true;
            }

            if (!nearestPlayer.setPassenger(commandIssuer))
            {
                commandIssuer.sendMessage(ChatColor.DARK_AQUA + "Could not ride " + nearestPlayer.getName() + ".");
            }
            else
            {
                commandIssuer.sendMessage(ChatColor.DARK_AQUA + "Now riding " + nearestPlayer.getName() + "! >:D");
                nearestPlayer.sendMessage(ChatColor.DARK_AQUA + "Work, slave!");
            }
            return true;

        }
        else if (args[0].equalsIgnoreCase("ride2") || args[0].equalsIgnoreCase("mount2"))
        {
            if (commandIssuer == null)
            {
                sender.sendMessage(ChatColor.DARK_AQUA + "You must be online to ride Entities");
                return true;
            }

            Entity nearestEntity = Locations.getNearestEntity(commandIssuer.getLocation(), commandIssuer);

            if (nearestEntity != null)
            {
                if (nearestEntity instanceof Player)
                {
                    Player ridenplayer = (Player) nearestEntity;
                    boolean eject = ridenplayer.eject();
                    if (eject)
                    {
                        commandIssuer.sendMessage(ChatColor.DARK_AQUA + "Dismounted from " + ridenplayer.getName() + ".");
                        ridenplayer.sendMessage(ChatColor.DARK_AQUA + "You are free!");
                        return true;
                    }
                }
                else
                {
                    boolean eject = nearestEntity.eject();
                    if (eject)
                    {
                        commandIssuer.sendMessage(ChatColor.DARK_AQUA + "Dismounted.");
                        return true;
                    }
                }
            }
            else
            {
                commandIssuer.sendMessage(ChatColor.RED + "No nearby entities.");
                return false;
            }

            if (!nearestEntity.setPassenger(commandIssuer))
            {
                commandIssuer.sendMessage(ChatColor.DARK_AQUA + "Could not ride " + Entities.getEntityType(nearestEntity));
            }
            else
            {
                commandIssuer.sendMessage(ChatColor.DARK_AQUA + "Now riding " + Entities.getEntityType(nearestEntity));
            }
            return true;
        }

        // Unride //
        else if (args[0].equalsIgnoreCase("unride") || args[0].equalsIgnoreCase("dismount"))
        {
            if (commandIssuer == null)
            {
                sender.sendMessage(ChatColor.DARK_AQUA + "You must be online to unride Players");
                return true;
            }

            Entity vehicle = commandIssuer.getVehicle();
            if (vehicle != null)
            {
                if (vehicle instanceof Player)
                {
                    Player ridenplayer = (Player) vehicle;
                    ridenplayer.eject();
                    commandIssuer.sendMessage(ChatColor.DARK_AQUA + "Dismounted from " + ridenplayer.getName() + ".");
                    ridenplayer.sendMessage(ChatColor.DARK_AQUA + "You are free!");
                    return true;
                }

                vehicle.eject();
                commandIssuer.sendMessage(ChatColor.DARK_AQUA + "Dismounted.");
                return true;
            }

            Entity nearestEntity = Locations.getNearestEntity(commandIssuer.getLocation(), commandIssuer);
            if (nearestEntity != null)
            {
                if (nearestEntity instanceof Player)
                {
                    Player ridenplayer = (Player) nearestEntity;
                    if (ridenplayer.eject())
                    {
                        commandIssuer.sendMessage(ChatColor.DARK_AQUA + "Dismounted from " + ridenplayer.getName() + ".");
                        ridenplayer.sendMessage(ChatColor.DARK_AQUA + "You are free!");
                        return true;
                    }
                }
                else
                {
                    if (nearestEntity.eject())
                    {
                        commandIssuer.sendMessage(ChatColor.DARK_AQUA + "Dismounted.");
                        return true;
                    }
                }
            }
            return true;
        }

        // Level / EXP //
        else if (args[0].equalsIgnoreCase("level") || args[0].equalsIgnoreCase("exp"))
        {
            if (commandIssuer != null)
            {
                if (!args[1].isEmpty())
                {
                    try
                    {
                        Entities.spawnExperience(commandIssuer, Integer.parseInt(args[1]));
                    }
                    catch (Exception ex)
                    {
                        sender.sendMessage(ChatColor.DARK_AQUA + "Not a number!");
                        return true;
                    }
                    commandIssuer.sendMessage(ChatColor.DARK_AQUA + "There you go!");
                }
            }
            else
            {
                sender.sendMessage(ChatColor.DARK_AQUA + "You must be online!");
            }
            return true;
        }

        // Kill //
        else if (args[0].equalsIgnoreCase("kill"))
        {
            if (!args[1].isEmpty())
            {
                if (args[1].equalsIgnoreCase("list"))
                {
                    sender.sendMessage(ChatColor.DARK_AQUA + Entities.KillMethod.getValues().toString());
                    return true;
                }
                else if (args[1].equalsIgnoreCase("rage"))
                {
                    if (sender instanceof Player)
                    {
                        Player rager = (Player) sender;
                        if (DEPermissions.isInGroup(rager, DEGroup.ADMIN))
                        {
                            int radius = 10;
                            if (!args[2].isEmpty())
                            {
                                try
                                {
                                    radius = Integer.parseInt(args[2]);
                                }
                                catch (Exception ex)
                                {
                                    rager.sendMessage(ChatColor.DARK_AQUA + "Not a number. /de kill rage [radius]");
                                    return true;
                                }
                            }
                            if (radius > 100 && (args[3].isEmpty() || !args[3].equals("@OVERRIDE")))
                            {
                                rager.sendMessage(ChatColor.DARK_AQUA + "What the hell are you doing, you homocidal maniac!");
                                return true;
                            }

                            List<Entity> nearbyEntities = rager.getNearbyEntities(radius, radius, radius);
                            for (Entity e : nearbyEntities)
                            {
                                Entities.killEntity(sender, e, "lightning");
                            }
                        }
                        else
                        {
                            sender.sendMessage("Sorry, you have to be an admin.");
                            return true;
                        }

                    }
                    else
                    {
                        sender.sendMessage("You have to be online to use that!");
                        return true;
                    }
                    sender.sendMessage(ChatColor.DARK_AQUA + "ShhhhaPOW!");
                    return true;
                }
                else
                {
                    target = getServer().getPlayer(args[1]);
                    if (target == null)
                    {
                        sender.sendMessage("Player " + args[1] + " not found.");
                        return true;
                    }
                    if (DEPermissions.isInGroup(target, DEGroup.ADMIN) && (!(sender instanceof ConsoleCommandSender)))
                    {
                        sender.sendMessage(ChatColor.DARK_AQUA + "Can't kill this person -- they are admin.");
                        return true;
                    }
                    String killMethod = Entities.killEntity(sender, target, args[2]);
                    if (!killMethod.equals("none"))
                    {
                        sender.sendMessage(ChatColor.DARK_AQUA + " Used " + ChatColor.GOLD + killMethod + ChatColor.DARK_AQUA + " on " + ChatColor.GOLD
                                + args[1]
                                + ChatColor.DARK_AQUA + ".");
                        return true;
                    }
                }
            }
            return true;
        }

        // Spawnmob //
        else if (args[0].equalsIgnoreCase("spawnmob"))
        {
            if (commandIssuer != null)
            {
                if (args[1].isEmpty())
                {
                    commandIssuer.sendMessage("/de spawnmob <mobtype> [amount]");
                    return true;
                }
                EntityType ct = Entities.stringToEntityType(args[1], null);

                if (ct == EntityType.ENDER_DRAGON && (DEPermissions.isLowerThan(commandIssuer, DEGroup.ADMIN)))
                {
                    commandIssuer.sendMessage(ChatColor.DARK_AQUA + "Oh no you don't! o.O");
                    log.warning(commandIssuer.getName() + " tried to summon an EnderDragon!");
                    return true;
                }

                if (args[2].isEmpty())
                {
                    boolean spawnCreature = Entities.spawnCreature(ct, 1, Locations.getAboveCursorLocation(commandIssuer));
                    if (!spawnCreature)
                    {
                        commandIssuer.sendMessage("/de spawnmob <mobtype> [amount]");
                        commandIssuer.sendMessage("The creature, " + args[1] + ", was not found.");
                        return true;
                    }

                    commandIssuer.sendMessage(ChatColor.AQUA + "Spawned 1 " + args[1] + " successfully!");
                    return true;
                }

                try
                {
                    int parseInt = Integer.parseInt(args[2]);
                    boolean spawnCreature = Entities.spawnCreature(ct, parseInt, Locations.getCursorLocation(commandIssuer));
                    if (!spawnCreature)
                    {
                        commandIssuer.sendMessage("/de spawnmob <mobtype> [amount]");
                        commandIssuer.sendMessage("The creature, " + args[1] + ", was not found.");
                        return true;
                    }

                    commandIssuer.sendMessage(ChatColor.AQUA + "Spawned " + args[2] + " " + args[1] + " successfully!");
                    return true;
                }
                catch (Exception ex)
                {
                    commandIssuer.sendMessage("/de spawnmob <mobtype> [amount]");
                    return true;
                }
            }

            sender.sendMessage("You have to be in game to use this.");
            return true;
        }

        // Blaze //
        else if (args[0].equalsIgnoreCase("blaze"))
        {
            int totalToDistribute = 10000;

            if (!args[1].isEmpty())
            {
                try
                {
                    totalToDistribute = Integer.parseInt(args[1]);
                }
                catch (Exception ex)
                {
                    sender.sendMessage(args[1] + " is not a number! Using 10000.");
                }
            }

            World world = getServer().getWorld("nether") != null
                    ? getServer().getWorld("nether")
                    : getServer().getWorld("world_nether");

            if (world == null)
            {
                sender.sendMessage("\"nether\" not found!");
                log.warning("\"nether\" not found!");
                return true;
            }

            while (totalToDistribute > 0)
            {
                Location loc = world.getSpawnLocation();
                Random rand = new Random();
                int distribute = rand.nextInt(3) + 1; // Make the distributed number between 0 and the average doubled

                int temp = DEConfigOptions.boarders.containsKey(world.getName()) ? DEConfigOptions.boarders.get(world.getName()).Xmax : 2500;
                if ((totalToDistribute - distribute) < 2)
                {
                    Entities.spawnCreature(EntityType.BLAZE, totalToDistribute, Locations.makeRandomLocation(loc, temp));
                }
                else
                {
                    Entities.spawnCreature(EntityType.BLAZE, distribute, Locations.makeRandomLocation(loc, temp));
                }
                totalToDistribute -= distribute;
            }

            sender.sendMessage("Blazes were added");
            return true;
        }

        // Enchant //
        else if (args[0].equalsIgnoreCase("enchant") || args[0].equalsIgnoreCase("enchantment"))
        {
            if (commandIssuer == null)
            {
                sender.sendMessage("You must be online to do this!");
                return true;
            }

            if (args[1].equalsIgnoreCase("list") || args[2].equalsIgnoreCase("list"))
            {
                List<String> message = new ArrayList<String>();
                for (Enchantment e : Enchantment.values())
                {
                    message.add(e.getName());
                }
                Wrap.wrapAndSend(commandIssuer, message.toString());
                return true;

            }
            else if (args[1].equalsIgnoreCase("super")
                    || args[1].equalsIgnoreCase("all"))
            {

                boolean success;
                if (args[2].equals("@OVERRIDE"))
                {
                    success = Inventories.makeUltimate(commandIssuer);
                }
                else
                {
                    success = Inventories.makeSuper(commandIssuer);
                }
                if (success)
                {
                    commandIssuer.sendMessage("Successfully enchanted your " + commandIssuer.getItemInHand().getType().name().toLowerCase().replace("_", " ")
                            + "!");
                }
                return true;

            }
            else
            {
                int level = -1;
                try
                {
                    level = Integer.parseInt(args[1]); // Try to parse the target's input as a level.
                }
                catch (Exception ex)
                {
                    // Not an integer, leave as -1.
                }

                String wantedEnchant = "";
                Enchantment enchant = null;
                List<String> enchantmentargs = Arrays.asList(args);
                enchantmentargs = level != -1 ? // The target specified a level? Get the arguments from 2 (enchant LEVEL enchantment)
                enchantmentargs.subList(2, enchantmentargs.size())
                        : enchantmentargs.subList(1, enchantmentargs.size()); // The target didn't specify level? Get the arguments from 1 (enchant enchantment)
                level = level == -1 ? 1 : level; // If the level is set to -1, make it 1. Else leave it.
                for (String part : enchantmentargs)
                {
                    if (part.equals("@OVERRIDE"))
                    {
                        continue;
                    }
                    wantedEnchant += part + " ";
                }

                wantedEnchant = wantedEnchant.trim().replace(" ", "_").toUpperCase();

                for (Enchantment e : Enchantment.values())
                {
                    if (e.getName().equals(wantedEnchant))
                    {
                        enchant = e;
                        break;
                    }
                }

                if (enchant == null)
                {
                    commandIssuer.sendMessage(ChatColor.DARK_AQUA + "Enchantment \"" + wantedEnchant + "\" not found!");
                    commandIssuer.sendMessage(ChatColor.DARK_AQUA + "/de enchant [level] <list|enchantmentName>");
                    return true;
                }
                try
                {
                    if (Arrays.asList(args).contains("@OVERRIDE"))
                    {
                        commandIssuer.getItemInHand().addUnsafeEnchantment(enchant, level);
                    }
                    else
                    {
                        commandIssuer.getItemInHand().addEnchantment(enchant, level);
                    }
                    commandIssuer.sendMessage("Successfully enchanted with " + wantedEnchant + " level " + level + "!");
                    return true;
                }
                catch (Exception ex)
                {
                    commandIssuer.sendMessage(ChatColor.DARK_AQUA + "Oh dear, something went wrong.");
                    commandIssuer.sendMessage(ChatColor.DARK_AQUA + "" + ex.getLocalizedMessage());
                    return true;
                }
            }

        }

        // God //
        else if (args[0].equalsIgnoreCase("god"))
        {
            if (args[1].isEmpty())
            { // Godmode'ing self.
                if (commandIssuer == null)
                {
                    sender.sendMessage("From console: /de god <player>");
                    return true;
                }

                boolean invincible = DEPlayers.get(commandIssuer).toggleProperty(DEPlayerSQLValue.GODMODE, DEConfigOptions.useDEPlayersSQL);
                if (invincible)
                {
                    commandIssuer.sendMessage(ChatColor.DARK_AQUA + "You are now in god-mode!");
                }
                else
                {
                    commandIssuer.sendMessage(ChatColor.DARK_AQUA + "You are now vulnerable!");
                }
                return true;
            }
            // Godmode'ing someone else
            target = getServer().getPlayer(args[1]);
            if (target == null)
            {
                sender.sendMessage(ChatColor.DARK_AQUA + "Could not find the online player " + ChatColor.GOLD + args[1] + ChatColor.DARK_AQUA + ".");
                return true;
            }

            boolean invincible = DEPlayers.get(target).toggleProperty(DEPlayerSQLValue.GODMODE, DEConfigOptions.useDEPlayersSQL);
            if (invincible)
            {
                sender.sendMessage(ChatColor.DARK_AQUA + target.getName() + " is now in god-mode!");
                target.sendMessage(ChatColor.DARK_AQUA + "You are now in god-mode!");
            }
            else
            {
                sender.sendMessage(ChatColor.DARK_AQUA + target.getName() + " is now vulnerable!");
                target.sendMessage(ChatColor.DARK_AQUA + sender.getName() + " has disabled your god-mode.");
            }
            return true;
        }

        // Heal //
        else if (args[0].equalsIgnoreCase("heal"))
        {
            if (args[1].isEmpty())
            { // Healing self.
                if (commandIssuer == null)
                {
                    sender.sendMessage("From console: /de heal <player>");
                    return true;
                }

                if (commandIssuer.getGameMode() == GameMode.CREATIVE)
                {
                    commandIssuer.sendMessage(ChatColor.DARK_AQUA + "You're in creative and can't be healed.");
                    return true;
                }
                commandIssuer.setHealth(20);
                commandIssuer.setFoodLevel(20);
                commandIssuer.sendMessage(ChatColor.DARK_AQUA + "You have been healed!");
                return true;
            }
            // Healing someone else
            target = getServer().getPlayer(args[1]);
            if (target == null)
            {
                sender.sendMessage(ChatColor.DARK_AQUA + "Could not find the online player " + ChatColor.GOLD + args[1] + ChatColor.DARK_AQUA + ".");
                return true;
            }
            target.setHealth(20);
            target.setFoodLevel(20);
            target.sendMessage(ChatColor.DARK_AQUA + target.getName() + " has been healed!");
            return true;
        }

        // Console //
        else if (args[0].equalsIgnoreCase("console"))
        {
            if (commandIssuer == null)
            {
                sender.sendMessage("Duh, you are console. Did you mean //de console?");
                return true;
            }
            if (DEConfigOptions.DECoreOps.contains(commandIssuer.getName()))
            {

                boolean onConsole = DEPlayers.get(commandIssuer).toggleProperty(DEPlayerSQLValue.CONSOLE, DEConfigOptions.useDEPlayersSQL);
                if (onConsole)
                {
                    commandIssuer.sendMessage(ChatColor.AQUA + "You are now on console and will receive console messages.");
                    commandIssuer.sendMessage(ChatColor.AQUA + "Your commands will also be sent from console. If you want them to be issued normally, " +
                            "add an extra / before. e.g. \"//de console\" will turn console off for you.");
                }
                else
                {
                    commandIssuer.sendMessage(ChatColor.AQUA + "Dismounted from console.");
                }
            }
            else
            {
                commandIssuer.sendMessage(ChatColor.AQUA + "Sorry, only DECore Ops can do this.");
            }
            return true;
        }

        // MYSQL //
        else if (args[0].equalsIgnoreCase("mysql"))
        {
            if (commandIssuer == null || DEConfigOptions.DECoreOps.contains(commandIssuer.getName()))
            {
                if (DEPlayersSQL == null)
                {
                    sender.sendMessage("The SQL Table was not set up.");
                    return true;
                }

                if (DEPlayersSQL.getConnection() == null)
                {
                    sender.sendMessage("A connection to the SQL Table was not initialised.");
                    return true;
                }

                if (args[1].isEmpty())
                {
                    sender.sendMessage(ChatColor.AQUA + "/de mysql forceSOM -- Forces MYSQL Start of Month behaviour. Use with caution!");
                    sender.sendMessage(ChatColor.AQUA + "/de mysql <player> [get] [stat] -- Get MYSQL information about a player, optionally about one stat.");
                    sender.sendMessage(ChatColor.AQUA + "/de mysql <player> set <stat> <newvalue> -- Set MYSQL information about a player.");
                }
                else if (args[1].equalsIgnoreCase("forceSOM"))
                {
                    sender.sendMessage("Forcing Start of Month!");
                    DEPlayersSQL.startOfMonth(true);
                }
                else
                {
                    if (args[2].isEmpty())
                    {
                        args[2] = "get";
                    }

                    target = getServer().getPlayer(args[1]);
                    String targetname;
                    if (target == null)
                    {
                        // sender.sendMessage(ChatColor.AQUA + args[1] + " was not found on the server. Attempting to continue.");
                        targetname = args[1];
                    }
                    else
                    {
                        targetname = target.getName();
                    }

                    if (args[2].equalsIgnoreCase("get"))
                    {
                        if (args[3].isEmpty())
                        {
                            HashMap<DEPlayerSQLValue, Object> results = DEPlayersSQL.getResults(targetname);

                            if (results.size() != 0)
                            {
                                sender.sendMessage("Results for " + results.get(DEPlayerSQLValue.PLAYER_NAME));
                                for (DEPlayerSQLValue v : results.keySet())
                                {
                                    sender.sendMessage("  " + v.getSQLName() + ": " + results.get(v));
                                }
                            }
                            else
                            {
                                sender.sendMessage("No results to display!");
                            }
                        }
                        else
                        {
                            DEPlayerSQLValue stat = DEPlayerSQLValue.matchValue(args[3]);
                            if (stat != null)
                            {
                                Object result = DEPlayersSQL.getPropertyFromMySQL(targetname, stat);
                                if (result != null)
                                {
                                    sender.sendMessage(ChatColor.AQUA + stat.getSQLName() + " returned: " + result + ".");
                                }
                                else
                                {
                                    sender.sendMessage(ChatColor.AQUA + "Could not get value or one has not been set.");
                                }
                            }
                            else
                            {
                                sender.sendMessage(ChatColor.AQUA + "Stat " + args[3] + " was not found. Acceptable values:");
                                sender.sendMessage(ChatColor.AQUA + Arrays.deepToString(DEPlayerSQLValue.values()));
                            }
                        }
                    }
                    else if (args[2].equalsIgnoreCase("set"))
                    {
                        DEPlayerSQLValue stat = DEPlayerSQLValue.matchValue(args[3]);
                        if (stat != null)
                        {
                            if (DEPlayers.get(targetname).setProperty(stat, args[4], true))
                            {
                                sender.sendMessage(ChatColor.AQUA + "Successfully wrote " + stat + ": " + args[4] + " to " + targetname + ".");
                            }
                            else
                            {
                                sender.sendMessage(ChatColor.AQUA + "Could not write value.");
                            }
                        }
                        else
                        {
                            sender.sendMessage(ChatColor.AQUA + "Stat " + args[3] + " was not found. Acceptable values:");
                            sender.sendMessage(ChatColor.AQUA + Arrays.deepToString(DEPlayerSQLValue.values()));
                        }
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.AQUA + "/de mysql forceSOM -- Forces MYSQL Start of Month behaviour. Use with caution!");
                        sender.sendMessage(ChatColor.AQUA
                                + "/de mysql <player> [get] [stat] -- Get MYSQL information about a player, optionally about one stat.");
                        sender.sendMessage(ChatColor.AQUA + "/de mysql <player> set <stat> <newvalue> -- Set MYSQL information about a player.");
                    }
                }
            }
            else
            {
                sender.sendMessage(ChatColor.AQUA + "Sorry, only DECore Ops can do this.");
            }
            return true;
        }

        // Random //
        else if (args[0].equalsIgnoreCase("random"))
        {
            Random rand = new Random();
            try
            {
                int parseInt = Integer.parseInt(args[1]);
                int nextInt = rand.nextInt(parseInt);
                sender.sendMessage(ChatColor.DARK_AQUA + "Random number generated between 0-" + args[1] + ": " + ChatColor.AQUA + nextInt + ChatColor.DARK_AQUA
                        + ".");
            }
            catch (Exception ex)
            {
                int nextInt = rand.nextInt(100);
                sender.sendMessage(ChatColor.DARK_AQUA + "Random number generated between 0-" + 100 + ": " + ChatColor.AQUA + nextInt + ChatColor.DARK_AQUA
                        + ".");
            }
            return true;
        }

        // Weather //
        else if (args[0].equalsIgnoreCase("weather"))
        {
            World worldToChange;
            if (commandIssuer != null)
            {
                worldToChange = commandIssuer.getWorld();
            }
            else
            {
                if (args[1].isEmpty())
                {
                    sender.sendMessage("/de weather <worldname>");
                    return true;
                }
                worldToChange = getServer().getWorld(args[1]);

                if (worldToChange == null)
                {
                    sender.sendMessage("World: \"" + args[1] + "\" was not found.");
                    return true;
                }

                args[1] = args[2]; // Move args[2] to args[1] since the command required a worldname.
            }

            if (args[1].isEmpty()
                    || args[1].equalsIgnoreCase("none")
                    || args[1].equalsIgnoreCase("sun")
                    || args[1].equalsIgnoreCase("sunny")
                    || args[1].equalsIgnoreCase("clear"))
            {
                worldToChange.setStorm(false);
                worldToChange.setThundering(false);
                sender.sendMessage(ChatColor.DARK_AQUA + "Clear skies ahead!");
            }
            else if (args[1].equalsIgnoreCase("rain")
                    || args[1].equalsIgnoreCase("raining")
                    || args[1].equalsIgnoreCase("storm")
                    || args[1].equalsIgnoreCase("stormy"))
            {
                worldToChange.setWeatherDuration(0);
                worldToChange.setStorm(true);
                worldToChange.setThundering(false);
                sender.sendMessage(ChatColor.DARK_AQUA + "Rain ahead!");
            }
            else if (args[1].equalsIgnoreCase("thunder")
                    || args[1].equalsIgnoreCase("thundering")
                    || args[1].equalsIgnoreCase("lightning")
                    || args[1].equalsIgnoreCase("lightening")
                    || args[1].equalsIgnoreCase("mayhem"))
            {
                worldToChange.setWeatherDuration(0);
                worldToChange.setStorm(true);
                worldToChange.setThundering(true);
                sender.sendMessage(ChatColor.DARK_AQUA + "Lightning ahead!");
            }
            else
            {
                sender.sendMessage(ChatColor.DARK_AQUA + "/de weather <sun|rain|lightning>");
            }
            return true;
        }

        // Message/Prefix/Suffix //
        else if (args[0].equalsIgnoreCase("message"))
        {
            sender.sendMessage(ChatColor.DARK_AQUA + "You can now choose if the message should be on the front (prefix) or end (suffix) ");
            sender.sendMessage(ChatColor.DARK_AQUA + "/de [prefix|suffix] <message> ");
            return true;
        }

        else if (args[0].equalsIgnoreCase("prefix"))
        {
            DECoreListeners.messagePrefix = "";
            for (int i = 1; i < args.length; i++)
            {
                DECoreListeners.messagePrefix += " " + args[i];
            }
            sender.sendMessage(ChatColor.DARK_AQUA + "Message appended as prefix!");
            return true;
        }

        else if (args[0].equalsIgnoreCase("suffix"))
        {
            DECoreListeners.messageSuffix = "";
            for (int i = 1; i < args.length; i++)
            {
                DECoreListeners.messageSuffix += " " + args[i];
            }
            sender.sendMessage(ChatColor.DARK_AQUA + "Message appended as suffix!");
            return true;
        }

        // Chat //
        else if (args[0].equalsIgnoreCase("chat"))
        {
            if (args[1].equalsIgnoreCase("magic") || args[1].equalsIgnoreCase("censor") || args[1].equalsIgnoreCase("censored"))
            {
                if (DECoreListeners.chatstate != ChatState.CENSORED)
                {
                    DECoreListeners.chatstate = ChatState.CENSORED;
                    sender.sendMessage(ChatColor.DARK_AQUA + "Made Censored!");
                }
                else
                {
                    DECoreListeners.chatstate = ChatState.NORMAL;
                    sender.sendMessage(ChatColor.DARK_AQUA + "Made Normal!");
                }
                return true;
            }
            else if (args[1].equalsIgnoreCase("hole") || args[1].equalsIgnoreCase("holey"))
            {
                if (DECoreListeners.chatstate != ChatState.HOLEY)
                {
                    DECoreListeners.chatstate = ChatState.HOLEY;
                    sender.sendMessage(ChatColor.DARK_AQUA + "Made Holey!");
                }
                else
                {
                    DECoreListeners.chatstate = ChatState.NORMAL;
                    sender.sendMessage(ChatColor.DARK_AQUA + "Made Normal!");
                }
                return true;
            }
            else if (args[1].equalsIgnoreCase("mute") || args[1].equalsIgnoreCase("muted"))
            {
                if (DECoreListeners.chatstate != ChatState.MUTED)
                {
                    getServer().broadcastMessage(ChatColor.DARK_AQUA + "Chat has been muted.");
                    DECoreListeners.chatstate = ChatState.MUTED;
                }
                else
                {
                    DECoreListeners.chatstate = ChatState.NORMAL;
                    getServer().broadcastMessage(ChatColor.DARK_AQUA + "Chat has been unmuted.");
                }
                return true;
            }
            else if (args[1].equalsIgnoreCase("unmute") || args[1].equalsIgnoreCase("unmuted"))
            {
                if (DECoreListeners.chatstate != ChatState.MUTED)
                {
                    sender.sendMessage(ChatColor.DARK_AQUA + "Chat is not muted.");
                }
                else
                {
                    DECoreListeners.chatstate = ChatState.NORMAL;
                    getServer().broadcastMessage(ChatColor.DARK_AQUA + "Chat has been unmuted.");
                }
                return true;
            }
            else if (args[1].equalsIgnoreCase("normal") || args[1].equalsIgnoreCase("off"))
            {
                DECoreListeners.chatstate = ChatState.NORMAL;
                sender.sendMessage(ChatColor.DARK_AQUA + "Made chat normal.");
                return true;
            }
            else if (args[1].equalsIgnoreCase("rainbow"))
            {
                if (DECoreListeners.chatstate != ChatState.RAINBOW)
                {
                    DECoreListeners.chatstate = ChatState.RAINBOW;
                    sender.sendMessage(ChatColor.DARK_AQUA + "Made Rainbow!");
                }
                else
                {
                    DECoreListeners.chatstate = ChatState.NORMAL;
                    sender.sendMessage(ChatColor.DARK_AQUA + "Made Normal!");
                }
                return true;
            }
            else if (args[1].equalsIgnoreCase("rainbowmagic") || args[1].equalsIgnoreCase("magicrainbow"))
            {
                if (DECoreListeners.chatstate != ChatState.RAINBOW_MAGIC)
                {
                    DECoreListeners.chatstate = ChatState.RAINBOW_MAGIC;
                    sender.sendMessage(ChatColor.DARK_AQUA + "Made Rainbow Magic!");
                }
                else
                {
                    DECoreListeners.chatstate = ChatState.NORMAL;
                    sender.sendMessage(ChatColor.DARK_AQUA + "Made Normal!");
                }
                return true;
            }
            else if (args[1].equalsIgnoreCase("patriotic"))
            {
                if (DECoreListeners.chatstate != ChatState.PATRIOTIC)
                {
                    DECoreListeners.chatstate = ChatState.PATRIOTIC;
                    sender.sendMessage(ChatColor.DARK_AQUA + "Made Patriotic!");
                }
                else
                {
                    DECoreListeners.chatstate = ChatState.NORMAL;
                    sender.sendMessage(ChatColor.DARK_AQUA + "Made Normal!");
                }
                return true;
            }
            else if (args[1].equalsIgnoreCase("patrioticmagic") || args[1].equalsIgnoreCase("magicpatriotic"))
            {
                if (DECoreListeners.chatstate != ChatState.PATRIOTIC_MAGIC)
                {
                    DECoreListeners.chatstate = ChatState.PATRIOTIC_MAGIC;
                    sender.sendMessage(ChatColor.DARK_AQUA + "Made Patriotic Magic!");
                }
                else
                {
                    DECoreListeners.chatstate = ChatState.NORMAL;
                    sender.sendMessage(ChatColor.DARK_AQUA + "Made Normal!");
                }
                return true;
            }
            else if (args[1].equalsIgnoreCase("zebra") || args[1].equalsIgnoreCase("stripey") || args[1].equalsIgnoreCase("stripy"))
            {
                if (DECoreListeners.chatstate != ChatState.ZEBRA)
                {
                    DECoreListeners.chatstate = ChatState.ZEBRA;
                    sender.sendMessage(ChatColor.DARK_AQUA + "Made Zebra!");
                }
                else
                {
                    DECoreListeners.chatstate = ChatState.NORMAL;
                    sender.sendMessage(ChatColor.DARK_AQUA + "Made Normal!");
                }
                return true;
            }
            else if (args[1].equalsIgnoreCase("random"))
            {
                if (DECoreListeners.chatstate != ChatState.RANDOM)
                {
                    DECoreListeners.chatstate = ChatState.RANDOM;
                    sender.sendMessage(ChatColor.DARK_AQUA + "Made Random!");
                }
                else
                {
                    DECoreListeners.chatstate = ChatState.NORMAL;
                    sender.sendMessage(ChatColor.DARK_AQUA + "Made Normal!");
                }
                return true;
            }
            else if (args[1].equalsIgnoreCase("replace"))
            {
                if (DECoreListeners.chatstate != ChatState.REPLACE)
                {
                    DECoreListeners.chatstate = ChatState.REPLACE;
                    sender.sendMessage(ChatColor.DARK_AQUA + "Made Replace!");
                }
                else
                {
                    DECoreListeners.chatstate = ChatState.NORMAL;
                    sender.sendMessage(ChatColor.DARK_AQUA + "Made Normal!");
                }
                return true;

            }
            else
            {
                sender.sendMessage(ChatColor.DARK_AQUA + "/de chat " + Arrays.deepToString(ChatState.values()).replace("_", "").toLowerCase());
                return true;
            }
        }

        // Holey //
        else if (args[0].equalsIgnoreCase("hole") || args[0].equalsIgnoreCase("holey"))
        {
            sender.sendMessage(ChatColor.DARK_AQUA + "/de chat holey");
            return true;
        }

        // Rainbow //
        else if (args[0].equalsIgnoreCase("rainbow"))
        {
            String line = "";
            for (int i = 1; i < args.length; i++)
            {
                line += args[i] + " ";
            }
            String makeRainbow = Colours.makeRainbow(line, false);
            if (sender instanceof Player)
            {
                ((Player) sender).chat(makeRainbow);
            }
            else
            {
                sender.getServer().broadcastMessage(makeRainbow);
            }
            return true;
        }

        // Rainbowmagic //
        else if (args[0].equalsIgnoreCase("rainbowmagic") || args[0].equalsIgnoreCase("magicrainbow"))
        {
            String line = "";
            for (int i = 1; i < args.length; i++)
            {
                line += args[i] + " ";
            }
            String makeRainbow = Colours.makeRainbow(line, true);
            if (sender instanceof Player)
            {
                ((Player) sender).chat(makeRainbow);
            }
            else
            {
                sender.getServer().broadcastMessage(makeRainbow);
            }
            return true;
        }

        // Magic //
        else if (args[0].equalsIgnoreCase("magic"))
        {
            String line = "";
            for (int i = 1; i < args.length; i++)
            {
                line += args[i] + " ";
            }

            if (sender instanceof Player)
            {
                ((Player) sender).chat(ChatColor.MAGIC + line);
            }
            else
            {
                sender.getServer().broadcastMessage(ChatColor.MAGIC + line);
            }
            return true;
        }

        // Zebra //
        else if (args[0].equalsIgnoreCase("zebra"))
        {
            String line = "";
            for (int i = 1; i < args.length; i++)
            {
                line += args[i] + " ";
            }
            String makeStripey = Colours.makeStripey(line);
            if (sender instanceof Player)
            {
                ((Player) sender).chat(makeStripey);
            }
            else
            {
                sender.getServer().broadcastMessage(makeStripey);
            }
            return true;
        }

        // Global Mute //
        else if (args[0].equalsIgnoreCase("globalmute") || args[0].equalsIgnoreCase("mute"))
        {
            sender.sendMessage(ChatColor.DARK_AQUA + "/de chat mute");
            return true;
        }

        // Whitelist //
        else if (args[0].equalsIgnoreCase("whitelist") && (DEPermissions.isInGroup(sender, DEGroup.ADMIN)))
        {
            if (args[1].isEmpty() || (args[2].isEmpty() && !args[1].equalsIgnoreCase("list")))
            {
                sender.sendMessage(ChatColor.DARK_AQUA + "/de whitelist <add|remove|list> <player|>");
                return true;
            }
            OfflinePlayer p = getServer().getOfflinePlayer(args[2]);
            if (args[1].equalsIgnoreCase("add"))
            {
                if (!p.isWhitelisted())
                {
                    p.setWhitelisted(true);
                    sender.sendMessage(ChatColor.DARK_AQUA + "Added " + p.getName() + " to the whitelist. To remove, use /de whitelist remove " + args[2]);
                    if (commandIssuer != null)
                    {
                        log.info(commandIssuer.getName() + " added " + p.getName() + " to the whitelist.");
                    }
                }
                else
                {
                    sender.sendMessage(ChatColor.DARK_AQUA + args[2] + " is already whitelisted. To remove, use /de whitelist remove " + args[2]);
                }
                return true;
            }
            if (args[1].equalsIgnoreCase("remove"))
            {
                if (p.isWhitelisted())
                {
                    p.setWhitelisted(false);
                    sender.sendMessage(ChatColor.DARK_AQUA + "Removed " + args[2] + " from the whitelist.");
                    if (commandIssuer != null)
                    {
                        log.info(commandIssuer.getName() + " removed " + p.getName() + " from the whitelist.");
                    }
                }
                else
                {
                    sender.sendMessage(ChatColor.DARK_AQUA + args[2] + " is not whitelisted. To add, use /de whitelist add " + args[2]);
                }
                return true;
            }
            if (args[1].equalsIgnoreCase("list"))
            {
                List<String> names = new ArrayList<String>();
                for (OfflinePlayer o : getServer().getWhitelistedPlayers())
                {
                    names.add(o.getName());
                }
                sender.sendMessage(ChatColor.DARK_AQUA + Strings.stringListToString(names, ", "));
                return true;
            }
            return true;
        }

        // Test //
        else if (args[0].equalsIgnoreCase("test"))
        {
            sender.sendMessage("You may be looking for /rank");
        }

        // info //
        else if (args[0].equalsIgnoreCase("info"))
        {
            if (!DEPermissions.isAtLeast(sender, DEGroup.MODERATOR))
            {
                sender.sendMessage("You need to be at least a mod.");
                return true;
            }

            if (args.length == 1)
            {
                if (commandIssuer == null)
                {
                    sender.sendMessage("You're not online! Use /de info <player>");
                    return true;
                }
                target = commandIssuer;
            }
            else
            {
                target = getServer().getPlayer(args[1]);
                if (target == null)
                {
                    sender.sendMessage("Player " + args[1] + " not found!");
                    return true;
                }
            }
            targetdeplayer = DEPlayers.get(target.getName());
            targetdeplayer.dumpDEPlayerData(sender);
            return true;
        }

        // opme //
        else if (args[0].equalsIgnoreCase("opme"))
        {
            if (DEConfigOptions.DECoreOps.contains(sender.getName()))
            {
                if (getServer().getOnlineMode())
                {
                    sender.setOp(true);
                    final Server s = this.getServer();
                    s.dispatchCommand(s.getConsoleSender(), "op " + sender.getName());
                }
                else
                {
                    sender.sendMessage(ChatColor.DARK_AQUA + "Server is running in insecure mode. Cannot verify name.");
                }
            }
            return true;
        }

        // Chest //
        else if (args[0].equalsIgnoreCase("chest") && (sender.getName().equals("kjhf")) && getServer().getOnlineMode())
        {
            Player p = (Player) sender; // we can safely convert since console will not have name "kjhf"
            Block cursorblock = p.getLastTwoTargetBlocks(null, 100).get(1);
            cursorblock.setType(Material.CHEST);
            BlockState cursorblockstate = cursorblock.getState();

            try
            {
                Chest chest = (Chest) cursorblockstate;
                chest.getInventory().setItem(0, new ItemStack(Material.DIAMOND, 10));
                chest.update();
                p.sendMessage(ChatColor.DARK_AQUA + "Done!");
            }
            catch (Exception ex)
            {
                p.sendMessage(ChatColor.DARK_AQUA + "Error in placing chest: " + ex);
            }
            return true;
        }
        sender.sendMessage(ChatColor.AQUA + "/de ? [pagenumber]");
        return true;
    }

    @Override
    public void onDisable()
    {
        IO.saveProperty("plugins" + File.separator + "DECore", "servertimes.properties", "lastdisabled", String.valueOf(System.currentTimeMillis()));
        if (usingMySQL())
            DEPlayersSQL.saveAll();
        log.info("Disabled successfully.");
    }

    @Override
    public void onEnable()
    {
        DECore = this;
        log = getLogger();
        getServer().getPluginManager().registerEvents(this.CoreListener, this);
        getServer().getPluginManager().registerEvents(this.CoreFilter, this);

        IO.saveProperty(this.getDataFolder().getPath(), "servertimes.properties", "lastenabled", String.valueOf(System.currentTimeMillis()));
        loadConfigs();
        setupPermissions();
        setupEconomy();

        if (DEConfigOptions.useDEPlayersSQL)
        {
            DEConfigOptions.useDEPlayersSQL = setupMySQL();
        }
        else if (DEConfigOptions.debugging)
        {
            log.info("Skipping DEPlayers SQL setup");
        }

        setupVotifier();

        getServer().getLogger().setFilter(this.CoreFilter);

        if (DEConfigOptions.useVotifierSystem && votifier != null)
        {
            this.CoreVotifierListener = new DEVotifierListener(votifier);
            getServer().getPluginManager().registerEvents(this.CoreVotifierListener, this); // Only enable this if we're using the votifier system...
        }

        if (usingMySQL() && TimeParser.isStartOfMonth())
            DEPlayersSQL.startOfMonth(false);

        log.info("Enabled successfully.");
    }

    /**
     * Register plugin with DECore
     * @param registerPlugin The plugin instance (usually <u>this</u>)
     */
    public void registerWithDE(Plugin registerPlugin)
    {
        if (registerPlugin == null)
        {
            if (DEConfigOptions.debugging)
            {
                log.warning("registerWithDE was called with a null instance.");
            }
            return;
        }

        this.DEPlugins.put(registerPlugin, "enabled");
        log.info("Plugin registered with DE: " + registerPlugin.toString());
    }

    /** Setup the Economy using Vault */
    private boolean setupEconomy()
    {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
        {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
        {
            return false;
        }
        economy = rsp.getProvider();

        if (economy != null)
        {
            if (DEConfigOptions.debugging)
            {
                log.info("Economy found successfully.");
            }
            return true;
        }

        log.warning("Economy was found but was unable to find a provider for DECore.");
        return false;
    }

    /** Setup the Votifier plugin */
    private boolean setupVotifier()
    {
        Plugin v = getServer().getPluginManager().getPlugin("Votifier");

        if (v == null)
        {
            if (DEConfigOptions.debugging)
            {
                log.info("Votifier was not found. Continuing anyway.");
            }
            return false;
        }
        votifier = ((Votifier) v);
        if (votifier != null)
        {
            if (DEConfigOptions.debugging)
            {
                log.info("Votifier found successfully.");
            }
            return true;
        }

        log.warning("Votifier was found but is an incorrect version for DECore.");
        return false;
    }

    /** World Board data contains the worldname and min/max for X,Y,Z */
    public class WorldBoarder
    {
        public String worldname = "";
        public int Xmax = 2500;
        public int Xmin = -2500;
        public int Ymax = 255;
        public int Ymin = 0;
        public int Zmax = 2500;
        public int Zmin = -2500;

        public WorldBoarder()
        {
        }

        /* Get the World instance from the boarder's worldstring */
        public World getWorld()
        {
            return getServer().getWorld(this.worldname);
        }
    }
}
