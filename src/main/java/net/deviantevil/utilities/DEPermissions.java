package net.deviantevil.utilities;

import net.deviantevil.decore.DECore;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Check for various permissions
 * @author Kristian ("kjhf")
 */
public final class DEPermissions {

    /** Permissions instance */
    public static Permission permissionHandle = null;

    private DEPermissions() {}

    /**  
     * Get the DEGroup this sender belongs to. Serverops and console returns ADMIN
     */
    public static DEGroup getGroup(CommandSender sender) {
        if (sender == null) {
            return DEGroup.ERROR;
        }
        if (sender instanceof ConsoleCommandSender) {
            return DEGroup.ADMIN;
        }
        if (sender instanceof Player) {
            return getGroup((Player) sender);
        }
        return DEGroup.ERROR;
    }

    /**  
     * Get the DEGroup this player belongs to. Serverops return ADMIN. Null players return ERROR.
     */
    public static DEGroup getGroup(Player player) {
        if (player == null) {
            return DEGroup.ERROR;
        }
        if (player.isOp()) {
            return DEGroup.ADMIN;
        }
        for (int i = DEGroup.values().length; i >= 0; i--) {
            DEGroup group = DEGroup.getGroup(i); // Count from first rank downwards
            if (group == DEGroup.ERROR)
                continue;

            if (hasPerm(player, group.getPermission())) {
                return group;
            }
        }
        return DEGroup.ERROR;
    }

    /**  
     * Get the DEGroup this player belongs to. Serverops return ADMIN. Null players return ERROR.
     */
    public static DEGroup getGroup(String playername) {
        Player p = DECore.getDECore().getServer().getPlayer(playername);
        if (p != null) {
            return getGroup(p);
        }
        return DEGroup.ERROR;
    }

    /**  
     * Grant a player permission where possible.
     * @param player The player to grant the permission to.
     * @param permission The permission string to grant.
     * @return True if command was sent to the permissions plugin, else false if no permissions installed or an argument was invalid. 
     */
    public static boolean grantPerm(Player player, String permission) { // Convenience. Checks both Permissions and PermissionsBukkit
        if (permission == null || permission.isEmpty() || player == null) {
            return false;
        }

        if (permissionHandle != null) {
            permissionHandle.playerAdd((String) null, player.getName(), permission); // Server has Permissions plugin
            return true;
        }

        return false;
    }

    /**  
     * Test whether a command sender can use this. Senders are converted to Players if appropriate.
     * @param sender The sender issuing the command
     * @param permission The permission to check against that are associated with the command.
     * @return True if sender has permission, else false. 
     */
    public static boolean hasPerm(CommandSender sender, String permission) {
        if (sender instanceof ConsoleCommandSender) {
            return true;
        } else if (!(sender instanceof Player)) {
            return false;
        } else {
            return hasPerm((Player) sender, permission);
        }
    }

    /**  
     * Test whether a command sender can use this. Senders are converted to Players if appropriate.
     * @param sender The sender issuing the command
     * @param permissions A list of permissions to check against that are associated with the command. Will return true if one matches.
     * @return True if sender has permission, else false. 
     */
    public static boolean hasPerm(CommandSender sender, String[] permissions) {
        if (sender instanceof ConsoleCommandSender) {
            return true;
        } else if (!(sender instanceof Player)) {
            return false;
        } else {
            for (String perm : permissions) {
                if (hasPerm((Player) sender, perm)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**  
    * Test whether a player can use this.
    * @param player The player issuing the command
    * @param permission The permission to check against that are associated with the command.
    * @return True if sender has permission, else false. 
    */
    public static boolean hasPerm(Player player, String permission) { // Convenience. Checks both Permissions and PermissionsBukkit
        if (permissionHandle != null) {
            // Server has Permissions plugin
            return permissionHandle.playerHas((String) null, player.getName(), permission);
        }

        return player.isOp(); // Revert to OPS.
    }

    /**  
     * Test whether a player can use this.
     * @param player The player issuing the command
     * @param permissions A list of permissions to check against that are associated with the command. Will return true if one matches.
     * @return True if sender has permission, else false. 
     */
    public static boolean hasPerm(Player player, String[] permissions) {
        for (String perm : permissions) {
            if (hasPerm(player, perm)) {
                return true;
            }
        }
        return false;
    }

    /**  
     * Test to see if the sender is in a group or higher, console and serverops take ADMIN
     */
    public static boolean isAtLeast(CommandSender sender, DEGroup group) {
        DEGroup playergroup = getGroup(sender);
        int playerrank = playergroup.getRank();
        int wantedrank = group.getRank();
        return (playerrank >= wantedrank);
    }

    /**  
     * Test to see if the player is in a group or higher, serverops take ADMIN
     */
    public static boolean isAtLeast(Player player, DEGroup group) {
        DEGroup playergroup = getGroup(player);
        int playerrank = playergroup.getRank();
        int wantedrank = group.getRank();
        return (playerrank >= wantedrank);
    }

    /**  
     * Test to see if the sender is a member of a particular DEGroup, console and serverops take ADMIN
     */
    public static boolean isInGroup(CommandSender sender, DEGroup group) {
        DEGroup group1 = getGroup(sender);
        return group1 == group;
    }

    /**  
     * Test to see if the player is a member of a particular DEGroup
     */
    public static boolean isInGroup(Player player, DEGroup group) {
        DEGroup group1 = getGroup(player);
        return group1 == group;
    }

    /**  
     * Test to see if the player is lower than group. Excludes group specified, hence console will always return false.
     */
    public static boolean isLowerThan(CommandSender sender, DEGroup group) {
        DEGroup playergroup = getGroup(sender);
        int playerrank = playergroup.getRank();
        int wantedrank = group.getRank();
        return (playerrank < wantedrank);
    }

    /**  
     * Test to see if the player is lower than group. Excludes group specified.
     */
    public static boolean isLowerThan(Player player, DEGroup group) {
        DEGroup playergroup = getGroup(player);
        int playerrank = playergroup.getRank();
        int wantedrank = group.getRank();
        return (playerrank < wantedrank);
    }

    /** Set a player's DEgroup 
     * @param player The player to set
     * @param newgroup The group to set the player to
     * @return True if command was sent to the permissions plugin, else false if no permissions installed or an argument was invalid.  
     */
    public static boolean setGroup(Player player, DEGroup newgroup) {
        if (newgroup == null || newgroup == DEGroup.ERROR || player == null) {
            return false;
        }

        if (permissionHandle != null) {
            String[] playerGroups = permissionHandle.getPlayerGroups(player);
            for (String group : playerGroups) {
                permissionHandle.playerRemoveGroup(player, group);
            }
            permissionHandle.playerAddGroup((String) null, player.getName(), newgroup.name().toLowerCase());
            return true;
        }

        return false;
    }

    /**  
     * Ungrant a player permission where possible.
     * @param player The player to ungrant the permission.
     * @param permission The permission string to ungrant.
     * @return True if command was sent to the permissions plugin, else false if no permissions installed or an argument was invalid.  
     */
    public static boolean ungrantPerm(Player player, String permission) {
        if (permission == null || permission.isEmpty() || player == null) {
            return false;
        }

        if (permissionHandle != null) {
            permissionHandle.playerRemove((String) null, player.getName(), permission); // Server has Permissions plugin
            return true;
        }

        return false;
    }

    /** The permissions groups on DE.  <p>
     * Colour codes are available from http://www.minecraftwiki.net/wiki/Classic_server_protocol#Color_Codes */
    public static enum DEGroup {
        /** Player is an admin, op, or sender is from console. Rank is 8 */
        ADMIN(8, ChatColor.RED),
        /** Player is a contributor (VIP). Rank is 5 */
        CONTRIBUTOR(5, ChatColor.DARK_PURPLE),
        /** Player is a donor. Rank is 3 */
        DONOR(3, ChatColor.GREEN),
        /** No permissions found / default group. Rank is -1 */
        ERROR(-1, ChatColor.WHITE),
        /** Player is a guest. Rank is 0 */
        GUEST(0, ChatColor.WHITE),
        /** Player is a junior moderator. Rank is 6 */
        @Deprecated
        JMODERATOR(6, ChatColor.AQUA),
        /** Player is a member. Rank is 1 */
        MEMBER(1, ChatColor.GRAY),
        /** Player is a(n onduty) moderator. Rank is 7 */
        MODERATOR(7, ChatColor.BLUE),
        /** Player is an offduty moderator. Rank is 6 */
        OFFMOD(6, ChatColor.BLUE),
        /** Player is a sponsor. Rank is 4 */
        SPONSOR(4, ChatColor.GOLD),
        /** Player is a veteran. Rank is 2 */
        VETERAN(2, ChatColor.DARK_GRAY); // White

        /** The ChatColor associated with this group. */
        private final ChatColor chatcolour;

        /** The DECore permission associated with this group, in form de.groups.lowerCaseGroupName */
        private final String permission;

        /** The rank as an integer, with Error starting at -1 and Admin at 8. */
        private final int rank;

        /** The permissions groups on DE */
        DEGroup(final int rank, ChatColor chatcolour) {
            this.rank = rank;
            this.chatcolour = chatcolour;
            this.permission = "de.groups." + this.name().toLowerCase();
        }

        /**  
         * Get a DEGroup from a rank number.
         */
        public static DEGroup getGroup(int rank) {
            for (DEGroup group : values()) {
                if (group.getRank() == rank) {
                    return group;
                }
            }
            return DEGroup.ERROR;
        }

        /**  
         * Get a DEGroup from a groupname.
         */
        public static DEGroup getGroup(String groupname) {
            for (DEGroup group : values()) {
                if (group.name().equalsIgnoreCase(groupname)) {
                    return group;
                }
            }
            return DEGroup.ERROR;
        }

        /** Get the ChatColour for this group. */
        public ChatColor getChatColourCode() {
            return this.chatcolour;
        }

        /** Get the colour code for this group in form "\u00A7x". */
        public String getColourCode() {
            return ChatColor.COLOR_CHAR + "" + this.chatcolour.getChar();
        }

        /** Get the DECore permission associated with this group, in form de.groups.LowerCaseGroupName */
        public String getPermission() {
            return this.permission;
        }

        /** Get the rank as an integer, with Error starting at -1 and Admin at 8. */
        public int getRank() {
            return this.rank;
        }

        /** Get the raw colour code for this group. */
        public char getRawColourCode() {
            return this.chatcolour.getChar();
        }

        /** Permission as a string is short for this.chatcolour + this.name() */
        @Override
        public String toString() {
            return this.chatcolour + this.name();
        }
    }
}
