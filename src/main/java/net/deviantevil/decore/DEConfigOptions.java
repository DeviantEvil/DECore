package net.deviantevil.decore;

import java.util.*;

import net.deviantevil.decore.DECore.WorldBoarder;
import net.deviantevil.pseudoclasses.DELocation;

/**
 * DECore's config options
 * 
 * @author Kristian
 */
public class DEConfigOptions {

    /** Should helpful throwing potions be banned?
     * Helpful potions: http://www.minecraftwiki.net/wiki/Splash_Potions#Splash_Potions_with_Positive_Effects
     */
    public static boolean banHelpfulThowingPotions = false;
    
    /** Should harmful throwing potions be banned?
     * Harmful potions: http://www.minecraftwiki.net/wiki/Splash_Potions#Splash_Potions_with_Negative_Effects
     */
    public static boolean banHostileThowingPotions = false;
    
    /** Worldname, Boarder */
    public static HashMap<String, WorldBoarder> boarders = new HashMap<String, WorldBoarder>();

    /** Should Creeper world damage be cancelled by DECore? */
    public static boolean creeperprotect = true;

    /** The broadcast range of Death Messages. 0 Turns OFF messages. -1 Broadcasts to the server. -2 Broadcasts to the world. */
    public static int deathMessageBroadcastRange = -2;
    
    /** Is DECore in debugging mode? (Verbose Output) */
    public static boolean debugging = false;
    
    /** Players that can op themselves */
    public static List<String> DECoreOps = Arrays.asList("kjhf", "GordyKnows", "Heifinator");
        
    /** Should DECore block "Can't keep up" console messages? */
    public static boolean disableCantKeepUp = true;

    /** Should Endermen world damage be cancelled by DECore? */
    public static boolean endermenprotect = true;
    
    /** The number of flags until a Guest is banned */
    public static int flagsToBan = 3;
    
    /** Should sheep ALWAYS regrow their wool? */
    public static boolean forceSheepRegrowth = true;
    
    /** Should DECore format chat colours? */
    public static boolean formatChatColours = true;

    /** The guest spawn */
    public static DELocation guestSpawn = null;

    /** Should the lightning sent by the kill commands be "real" lightning (i.e. fire & explosion) */
    public static boolean lightningIsReal = false;

    /** WHOLE SERVER is moderated -- only jmods+ may enter */
    public static boolean moderated = false;

    /** The MOTD to display to players joining (empty for no MOTD, duh.) */
    public static List<String> MOTD = new ArrayList<String>();

    /** The kicked for not being on the whitelist message */
    public static String notWhitelistedMessage = "You are not whitelisted on DE.";
    
    /** Whether we're using the Promote to Veterans system */
    public static boolean promoteToVeterans = true;
    
    /** Time in hours players must play for to be promoted to Veteran */
    public static int promoteToVeteransTime = 72;
    
    /** Tips used by DECore */
    public static ArrayList<String> tips = new ArrayList<String>();

    /** Time between tips in minutes */
    public static int tipsRepetitionTime = 15;
    
    /** Should the check inventory command be interpreted by DECore? */
    public static boolean useCheckInvCommand = true;
    
    /** Should the clear inventory command be interpreted by DECore? */
    public static boolean useClearInvCommand = true;

    /** Should covered double chests be interpreted by DECore? */
    public static boolean useCoveredDoubleChests = true;

    /** Should covered single chests be interpreted by DECore? */
    public static boolean useCoveredSingleChests = true;
    
    /** Use the SQL table? */
    public static boolean useDEPlayersSQL = true;
    
    /** Should DECore control the login/logout messages? */
    public static boolean useLoginLogoutMessages = true;   
    
    /** Should we use the Flags system? */
    public static boolean useFlagsSystem = true;
    
    /** Should we use /adventure [player], /creative [player], /survival [player] to send /gamemode [player] <0/1/2> as if the player typed it? */
    public static boolean useGameModeShortcuts = true;    

    /** Should the resend chunk command be interpreted by DECore? */
    public static boolean useResendChunkCommand = true;

    /** Should the spawn command be interpreted by DECore? */
    public static boolean useSpawnCommand = true;

    /** Should we override Minecraft's /tp to allow for autocompletion of names and omission of player's name. */
    public static boolean useTPCommand = false;

    /** Should the tpall command be interpreted by DECore? */
    public static boolean useTPAllCommand = true;
    
    /** Should the tploc command be interpreted by DECore? */
    public static boolean useTPLocCommand = true;

    /** Should the vanish command be interpreted by DECore? */
    public static boolean useVanishCommand = true;

    /** Should we use the Votifier Listener system? */
    public static boolean useVotifierSystem = true;
    
    /** Should we use the Vouching system? */
    public static boolean useVouchingSystem = true;
    
    /** The chance of receiving DEPoints for voting on the server in percent */
    public static double voteDEPChance = 1;
    
    /** The DEPoints received for voting on the server if voteDEPChance is triggered */
    public static int voteDEPReward = 1;
    
    /** The credits received for voting on the server */
    public static int voteReward = 50;
    
    /** The String for broadcasting when a player has voted. An empty string ("") disables this. Variables are %player, %reward, %times */
    public static String voteBroadcastMessage = "%player has just earned %reward by voting %times times! Have you voted today? http://deviantevil.net";

    /** The String for thanking a player for voting. An empty string ("") disables this. Variables are %player, %reward, %times */
    public static String voteThankyouMessage = "Thank you %player for voting for us %times times since you logged in! You have been rewarded %rewardc!";
    
    /** The String for notifying a player their vote also gave them DEP. An empty string ("") disables this. Variables are %dep, %player, %reward, %times */
    public static String voteThankyouMessageDEPoints = "Thanks %player, your vote earnt you %dep DEP!";
    
    /** The number of vouches until a Guest is promoted to Member */
    public static int vouchesToMember = 3;
    
    /** Static class, should not be initialised */
    private DEConfigOptions() {}
}
