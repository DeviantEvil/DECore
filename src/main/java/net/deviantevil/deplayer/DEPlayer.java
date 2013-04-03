package net.deviantevil.deplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import net.deviantevil.decore.DEConfigOptions;
import net.deviantevil.decore.DECore;
import net.deviantevil.pseudoclasses.DELocation;
import net.deviantevil.utilities.*;
import net.deviantevil.utilities.DEPermissions.DEGroup;
import net.deviantevil.utilities.DEPlayerDatabase.DEPlayerSQLValue;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * Class for players playing on the server.
 * @author Kristian "kjhf"
 * @see Player
 */
public final class DEPlayer {

    /** The time the player switched to creative */
    private long creativeTimeStart = -1;

    /** The Player's last death location */
    private DELocation lastDeathLocation = null;

    /** The time the player logged in */
    private long loginTime = -1;

    /** The properties associated with this DEPlayer */
    private HashMap<DEPlayerSQLValue, Object> properties = new HashMap<DEPlayerSQLValue, Object>();

    /** The player's vouches and flags data */
    public VouchAndFlagData vouchAndFlagData = null;

    // If adding a field, please update dumpDEPlayerData() //

    /** 
     * Make a DEPlayer using a Player instance
     */
    public DEPlayer(Player player) {
        this(player.getName());
    }

    /**
     * Make a DEPlayer using their name
     */
    public DEPlayer(String playername) {
        for (DEPlayerSQLValue v : DEPlayerSQLValue.values()) {
            if (v == DEPlayerSQLValue.PLAYER_NAME) {
                this.properties.put(DEPlayerSQLValue.PLAYER_NAME, playername);
                continue;
            }
            if (v == DEPlayerSQLValue.RANK) {
                this.properties.put(DEPlayerSQLValue.RANK, getRank().name());
                continue;
            }
            
            if (v.getValueType() == Boolean.class) this.properties.put(v, false);
            else if (v.getValueType() == Byte.class) this.properties.put(v, 0);
            else if (v.getValueType() == Integer.class) this.properties.put(v, 0);
            else if (v.getValueType() == Long.class) this.properties.put(v, 0L);
            else if (v.getValueType() == Short.class) this.properties.put(v, 0);
            else if (v.getValueType() == String.class) this.properties.put(v, "");
            else this.properties.put(v, null);
        }
    }

    /**
     * Get the DEPlayer from their Player instance. Can be null.
     */
    public static DEPlayer getDEPlayer(Player player) {
        return DECore.DEPlayers.get(player.getName());
    }

    /**
     * Get the DEPlayer from their playername. Can be null.
     */
    public static DEPlayer getDEPlayer(String playername) {
        return DECore.DEPlayers.get(playername);
    }

    /** Changes a numerical property. Returns new value. If the property is not a Number, -1 is returned and no action taken. The result may be floored if a double is added to an int. */
    public Number changeNumericalProperty(DEPlayerSQLValue property, double changeAmount, boolean saveToSQL) {
        if (property.getValueType() == Byte.class) {
            byte previous;

            try {
                previous = (Byte) this.getProperty(property);
            } catch (Exception ex) {
                return -1;
            }
            byte now = (byte) (previous + changeAmount);
            this.setProperty(property, now, saveToSQL);
            return now;
        } else if (property.getValueType() == Double.class) {
            double previous;

            try {
                previous = (Double) this.getProperty(property);
            } catch (Exception ex) {
                return -1;
            }
            double now = previous + changeAmount;
            this.setProperty(property, now, saveToSQL);
            return now;
        } else if (property.getValueType() == Integer.class) {
            int previous;

            try {
                previous = (Integer) this.getProperty(property);
            } catch (Exception ex) {
                return -1;
            }
            int now = (int) (previous + changeAmount);
            this.setProperty(property, now, saveToSQL);
            return now;
        } else if (property.getValueType() == Long.class) {
            long previous;

            try {
                previous = (Long) this.getProperty(property);
            } catch (Exception ex) {
                return -1;
            }
            long now = previous + (long) changeAmount;
            this.setProperty(property, now, saveToSQL);
            return now;
        } else if (property.getValueType() == Short.class) {
            short previous;

            try {
                previous = (Short) this.getProperty(property);
            } catch (Exception ex) {
                return -1;
            }
            short now = (short) (previous + changeAmount);
            this.setProperty(property, now, saveToSQL);
            return now;
        } else {
            try {
                int previous = (Integer) this.getProperty(property);
                int now = (int) (previous + changeAmount);
                this.setProperty(property, now, saveToSQL);
                return now;
            } catch (Exception ex) {
                return -1;
            }
        }
    }

    /**
     * Check the player's play time to see if they need promoting to veteran.
     * Returns if they were promoted.
     */
    public boolean checkForVeteran() {
        if ((!DEConfigOptions.promoteToVeterans) || (DEPermissions.isAtLeast(getPlayer(), DEGroup.VETERAN)))
            return false; // We aren't promoting or Player is already above Veteran
        final long totalPlayTime = getTotalPlayTime();
        if (totalPlayTime >= (3600 * DEConfigOptions.promoteToVeteransTime)) {
            DEPermissions.setGroup(getPlayer(), DEGroup.VETERAN);
            return true;
        }
        return false;
    }

    /**
     * Send info about this DEPlayer to the commandsender
     */
    public void dumpDEPlayerData(org.bukkit.command.CommandSender commandsender) {
        commandsender.sendMessage("DEPlayer info for " + getRank().getColourCode() + getPlayername());
        commandsender.sendMessage(ChatColor.GRAY + "Creative Time Start: " + ChatColor.GOLD + (this.creativeTimeStart));
        commandsender.sendMessage(ChatColor.GRAY + "Login Time: " + ChatColor.GOLD + (this.loginTime));
        commandsender.sendMessage(ChatColor.GRAY + "TabName: " + getPlayer().getPlayerListName());

        commandsender.sendMessage(ChatColor.GRAY + "Properties: " + ChatColor.GOLD);
        for (DEPlayerSQLValue val : this.properties.keySet()) {
            commandsender.sendMessage("  " + ChatColor.GRAY + val.getDisplayName() + ": " + ChatColor.GOLD + this.properties.get(val));
        }

        commandsender.sendMessage(ChatColor.GRAY + "Vouch and Flag Data: " + ChatColor.GOLD + (this.vouchAndFlagData));
        if (this.vouchAndFlagData != null) {
            commandsender.sendMessage(ChatColor.GRAY + "  Flags:");
            for (String flag : this.vouchAndFlagData.getFlags()) {
                commandsender.sendMessage("    " + ChatColor.GOLD + flag);
            }
            commandsender.sendMessage(ChatColor.GRAY + "  Vouches:");
            for (String vouch : this.vouchAndFlagData.getVouches()) {
                commandsender.sendMessage("    " + ChatColor.GOLD + vouch);
            }
        }
        //        commandsender.sendMessage(ChatColor.GRAY + "Vouches left to give: " + ChatColor.GOLD + (this.vouchAndFlagData.getVouchesToGiveAmount()));
    }

    /**
     * Get the time the player has been in creative for (seconds).
     */
    public long getCreativeTime(long currentTime) {
        if (this.creativeTimeStart == -1) {
            return 0;
        }

        return ((currentTime - (this.creativeTimeStart == 0 ? currentTime : this.creativeTimeStart)) / 1000);
    }

    /** Get this DEPlayer's DEP Points. Convenience function for getProperty(DEPlayerSQLValue.DEPOINTS)  */
    public long getDEP() {
        return this.getProperty(DEPlayerSQLValue.DEPOINTS) instanceof Long ? 
                (Long) this.getProperty(DEPlayerSQLValue.DEPOINTS) : (Integer) this.getProperty(DEPlayerSQLValue.DEPOINTS);
    }

    /**
     * Get the player's last death Location, or null if they have not died since startup.
     */
    public DELocation getLastDeathLocation() {
        return this.lastDeathLocation;
    }

    /**
     * Get this DEPlayer's Player instance from their Playername
     */
    public Player getPlayer() {
        return DECore.getDECore().getServer().getPlayer(getPlayername());
    }

    /**
     * Get the name of this DEPlayer
     */
    public String getPlayername() {
        return (String) this.properties.get(DEPlayerSQLValue.PLAYER_NAME);
    }

    /**
     * Get the time the player has been logged in to the server (seconds). <p />
     * N.B. this may have been reset so may not be the *precise* time. <br />
     * A better description may be the time since reset (sent to MySQL?).
     */
    public long getPlayTime(long currentTime) {
        if (this.loginTime == -1) {
            return 0;
        }

        return ((currentTime - (this.loginTime == 0 ? currentTime : this.loginTime)) / 1000);
    }

    /** Get a player's property. If player does not have the property, a default one is inserted and returned. <p>
     * Boolean default is false <br />
     * Double, Integer, Long, Short default is 0 <br />
     * String default is ""  <br />
     * All others return null 
     */
    public Object getProperty(DEPlayerSQLValue property) {
        if (this.properties.containsKey(property))
            return this.properties.get(property);

        // Player does not have said property
        if (property.getValueType() == Boolean.class) {
            this.properties.put(property, false);
            return false;
        } else if (property.getValueType() == Double.class ||
                property.getValueType() == Integer.class ||
                property.getValueType() == Long.class ||
                property.getValueType() == Short.class) {
            this.properties.put(property, 0);
            return 0;
        } else if (property.getValueType() == String.class) {
            this.properties.put(property, "");
            return "";
        } else {
            this.properties.put(property, null);
            return null;
        }
    }

    /**
     * Get this Player's rank. Returns DEGroup.ERROR if the player was not found
     */
    public DEGroup getRank() {
        return DEPermissions.getGroup(this.getPlayername());
    }

    /**
     * Get the player's tabname, or null if they aren't online
     */
    public String getTabname() {
        return getPlayer() != null ? getPlayer().getPlayerListName() : null;
    }

    /**
     * Get the total play time this player has been playing for,
     * including their time online this session.
     * 
     */
    public long getTotalPlayTime() {
        final long currentTime = System.currentTimeMillis();
        return (this.loginTime == -1) ? currentTime : currentTime + getPlayTime(currentTime);
    }

    /** Increments a property. Returns new value. If the property is not a Number, -1 is returned and no action taken. */
    public Number incrementProperty(DEPlayerSQLValue property, boolean saveToSQL) {
        return changeNumericalProperty(property, 1, saveToSQL);
    }

    /** Get a player's property in boolean form. If player does not have the property, a default one is created and returns false. <p>
     * If the object is not of boolean form then false is returned.
     */
    public boolean isProperty(DEPlayerSQLValue property) {
        if (this.properties.containsKey(property)) {
            Object result = this.properties.get(property);
            return result instanceof Boolean ? (Boolean) this.properties.get(property) : false;
        }

        // Player does not have said property
        if (property.getValueType() == Boolean.class) {
            this.properties.put(property, false);
        } else if (property.getValueType() == Double.class ||
                property.getValueType() == Integer.class ||
                property.getValueType() == Long.class ||
                property.getValueType() == Short.class) {
            this.properties.put(property, 0);
        } else if (property.getValueType() == String.class) {
            this.properties.put(property, "");
        } else {
            this.properties.put(property, null);
        }
        return false;
    }

    /**
     * Reset the creative start time.
     */
    public void resetCreativeTime(long currentTime) {
        this.creativeTimeStart = currentTime;
    }

    /**
     * Reset the login start time.
     */
    public void resetPlayTime(long currentTime) {
        this.loginTime = currentTime;
    }

    /**
     * Set the player's last death Location
     */
    public void setLastDeathLocation(DELocation lastDeathLocation) {
        this.lastDeathLocation = lastDeathLocation;
    }

    /** Copy a results table into the player's properties. This does not report back to MySQL. */
    public void setProperties(HashMap<DEPlayerSQLValue, Object> results) {
        this.properties.putAll(results);
    }

    /**
     * Set a player's property.  The method checks types before setting.
     * @param property The property to set
     * @param newValue The property's new value
     * @param saveToSQL Should the property be saved to MySQL? [Pro tip: use DECore.usingMySQL()]
     * @return If the value was set.
     */
    public boolean setProperty(DEPlayerSQLValue property, Object newValue, boolean saveToSQL) {
        if (newValue.getClass() == String.class) {
            String stringValue = ((String) newValue);
            if (property.getValueType() != String.class) {
                // Try to convert it.

                if (property.getValueType() == Boolean.class) {
                    try {
                        newValue = Short.parseShort(stringValue) != 0;
                    } catch (Exception ex) {
                        newValue = Boolean.parseBoolean(stringValue);
                    }
                } else if (property.getValueType() == Byte.class) {
                    try {
                        newValue = Byte.parseByte(stringValue);
                    } catch (Exception ex) {
                        DECore.getDECore().getServer().getLogger()
                                .warning("Illegal cast! " + property.getSQLName() + " is a Byte but was given an invalid String: " + stringValue);
                        return false;
                    }
                } else if (property.getValueType() == Double.class) {
                    try {
                        newValue = Double.parseDouble(stringValue);
                    } catch (Exception ex) {
                        DECore.getDECore().getServer().getLogger()
                                .warning("Illegal cast! " + property.getSQLName() + " is a Double but was given an invalid String: " + stringValue);
                        return false;
                    }
                } else if (property.getValueType() == Integer.class) {
                    try {
                        newValue = Integer.parseInt(stringValue);
                    } catch (Exception ex) {
                        DECore.getDECore().getServer().getLogger()
                                .warning("Illegal cast! " + property.getSQLName() + " is an Integer but was given an invalid String: " + stringValue);
                        return false;
                    }
                } else if (property.getValueType() == Long.class) {
                    try {
                        newValue = Long.parseLong(stringValue);
                    } catch (Exception ex) {
                        DECore.getDECore().getServer().getLogger()
                                .warning("Illegal cast! " + property.getSQLName() + " is an Long but was given an invalid String: " + stringValue);
                        return false;
                    }
                } else if (property.getValueType() == Short.class) {
                    try {
                        newValue = Short.parseShort(stringValue);
                    } catch (Exception ex) {
                        DECore.getDECore().getServer().getLogger()
                                .warning("Illegal cast! " + property.getSQLName() + " is an Short but was given an invalid String: " + stringValue);
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }

        try {
            if ((property.getValueType().cast(newValue)) != null) {
                this.properties.put(property, newValue);
                if (saveToSQL) {
                    DECore.getDEPlayersDatabase().writeValue(getPlayername(), property.getSQLName(), newValue);
                }
                return true;
            }
        } catch (Exception ex) {
            // Illegal cast or SQL exception, let return false
        }
        return false;
    }

    /** 
     * Safely set the Player's tabname from tabnames.yml, or their rank colour if one is not defined.
     */
    public void setTabName() {
        YamlConfiguration tabnames = null;
        final String playername = getPlayername();
        
        final File tabFile = new File(DECore.getDECore().getDataFolder(),"tabnames.yml");
        if (!tabFile.exists()) {
            DECore.getDECore().saveResource("tabnames.yml", false);
        }
        tabnames = YamlConfiguration.loadConfiguration(tabFile);
        
        String tabname;
        if (tabnames != null) {           
            boolean vanished = isProperty(DEPlayerSQLValue.VANISHED);
            if (!vanished) {
                String s = tabnames.getString("Players." + playername, null);    
                if (s == null) {
                    // No custom name
                    tabname = getRank().getChatColourCode() + playername;
                } else {
                    // Custom name
                    tabname = Colours.fixColours(s);
                }
            } else {
                // Vanished
                tabname = "";
            }
        } else {
            // tabname yml problem, default to permission group colour
            tabname = getRank().getChatColourCode() + playername;
            DECore.log.severe("Error loading tabnames.yml");
        }
        
        if (tabname.length() >= 16) {
            tabname = tabname.substring(0, 15);
        }
        
        String append = "";
        for (int tries = 0; tries < 16; tries++) {
            try {
                getPlayer().setPlayerListName(tabname + append);
                break;
            } catch (IllegalArgumentException ex) {
                // try again by adding a space
                append += " ";
            }
        }
    }

    /** 
     * Set the player's vanish status
     * @param vanished Set vanished status
     * @param silent Should the player be alerted?
     */
    public void setVanished(boolean vanished, boolean silent) {
        setProperty(DEPlayerSQLValue.VANISHED, vanished, DECore.usingMySQL());

        if (vanished) {
            for (Player p : DECore.getDECore().getServer().getOnlinePlayers()) {
                if (DEPermissions.isLowerThan(p, getRank())) {
                    p.hidePlayer(getPlayer());
                }
            }

            if (!silent) {
                getPlayer().sendMessage(ChatColor.BLUE + "You have vanished from the server!");
                getPlayer().sendMessage(ChatColor.BLUE + "Those of equal or higher rank than you can still see you though.");
            }
        } else {
            for (Player p : DECore.getDECore().getServer().getOnlinePlayers()) {
                p.showPlayer(getPlayer());
            }

            if (!silent) {
                getPlayer().sendMessage(ChatColor.BLUE + "You have unvanished.");
            }
        }
    }

    /** Toggles a property. Returns new value. If the property is not a boolean, false is returned and no action taken. */
    public boolean toggleProperty(DEPlayerSQLValue property, boolean saveToSQL) {
        if (property.getValueType() != Boolean.class) {
            return false;
        }

        boolean previous;
        try {
            previous = (Boolean) this.getProperty(property);
        } catch (Exception ex) {
            return false;
        }

        boolean now = !previous;
        this.setProperty(property, now, saveToSQL);
        return now;
    }

    public final class VouchAndFlagData {
        /** The players this player has flagged, or received flags from. Veterans+ give flags away, other players are flagged (reported). */
        private ArrayList<String> flags;

        /** The players this player has vouched for, or received vouches from. Veterans+ give vouches away, other players require these vouches. */
        private ArrayList<String> vouches;

        /** How many vouches this player can give */
        //private int vouchesToGive = 0;

        /** New Vouch and Flag data */
        public VouchAndFlagData() {
            this.flags = new ArrayList<String>();
            this.vouches = new ArrayList<String>();
        }

        /** Data to load */
        public VouchAndFlagData(ArrayList<String> flags, ArrayList<String> vouches) {
            this.flags = flags;
            this.vouches = vouches;
        }

        /** Data to load */
        public VouchAndFlagData(String flags, String vouches) {
            this.flags = Strings.stringToStringList(flags);
            this.vouches = Strings.stringToStringList(vouches);
        }

        //        /** Data to load */
        //        public VouchAndFlagData(int vouchesToGive, ArrayList<String> flags, ArrayList<String> vouches) {
        //            //this.vouchesToGive = vouchesToGive;
        //            this.flags = flags;
        //            this.vouches = vouches;
        //        }
        //
        //        /** Data to load */
        //        public VouchAndFlagData(int vouchesToGive, String flags, String vouches) {
        //            //this.vouchesToGive = vouchesToGive;
        //            this.flags = Strings.stringToStringList(flags);
        //            this.vouches = Strings.stringToStringList(vouches);
        //        }

        /**
         * Flag a player.
         * @param playerFlagging The player flagging (that this DEPlayer belongs to!)
         * @param playerToFlag The player who is being flagged
         * Returns success on conditions of: <br />
         *   - The flagger is not flagging themself <br />
         *   - This flagger has not already flagged the player <br />
         *   - The flagged player is able to take flags <br />
         * This does <b>NOT</b> check permissions or number of flags.
         */
        public boolean flagPlayer(Player playerFlagging, Player playerToFlag) {
            if (this.flags.contains(playerToFlag.getName()))
                return false;
            if (playerToFlag == playerFlagging)
                return false;

            DEPlayer flagged = DEPlayer.getDEPlayer(playerToFlag);
            if (flagged == null)
                return false;
            if (flagged.vouchAndFlagData.flags.contains(playerFlagging.getName()))
                return false;

            if (!flagged.vouchAndFlagData.flags.add(playerFlagging.getName()))
                return false;

            this.flags.add(playerToFlag.getName());
            return true;
        }

        /**
         * Get the players this player has flagged, or has been flagged by
         */
        public String[] getFlags() {
            return this.flags.toArray(new String[0]);
        }

        /**
         * Get the number of the flags this player has, or number of players the player has flagged.
         */
        public int getFlagsAmount() {
            return this.flags.size();
        }

        /**
         * Get the players this player has vouched for, or has been vouched by
         */
        public String[] getVouches() {
            return this.vouches.toArray(new String[0]);
        }

        /**
         * Get the number of players this player has vouched for, or received vouches from. 
         * Veterans+ give vouches away, other players require these vouches.
         */
        public int getVouchesAmount() {
            return this.vouches.size();
        }

        /**
         * Get the number of the vouches this player has to give.
         */
        //        public int getVouchesToGiveAmount() {
        //            return this.vouchesToGive;
        //        }

        //        @Override
        //        public String toString() {
        //            return "VouchesToGive: " + this.vouchesToGive + ", VouchedPlayers: " + this.vouches.toString() + ", FlaggedPlayers: " + this.flags.toString() + ".";
        //        }
        @Override
        public String toString() {
            return "VouchedPlayers: " + this.vouches.toString() + ", FlaggedPlayers: " + this.flags.toString() + ".";
        }

        /**
         * Vouch for a player. Automatically decrements VouchesToGive.
         * @param playerVouching The player vouching (that this DEPlayer belongs to!)
         * @param playerToVouchFor The player who is being vouched for
         * @return Returns success on conditions of: <br />
         *   - The voucher is not vouching for themself <br />
         *   - The voucher has enough vouches <br />
         *   - This voucher has not already vouched for the player <br />
         *   - The vouched player is able to accept vouches <br />
         * This does <b>NOT</b> check permissions.
         */
        public boolean vouchFor(Player playerVouching, Player playerToVouchFor) {
            //            if (this.vouchesToGive < 1)
            //                return false;

            if (this.vouches.contains(playerToVouchFor.getName()))
                return false;

            if (playerToVouchFor == playerVouching)
                return false;

            DEPlayer vouched = DEPlayer.getDEPlayer(playerToVouchFor);

            if (vouched == null)
                return false;

            if (vouched.vouchAndFlagData.vouches.contains(playerVouching.getName()))
                return false;

            if (!vouched.vouchAndFlagData.vouches.add(playerVouching.getName()))
                return false;

            this.vouches.add(playerToVouchFor.getName());
            //            this.vouchesToGive--;
            return true;
        }
    }
}
