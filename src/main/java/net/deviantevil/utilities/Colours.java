package net.deviantevil.utilities;
import java.util.Random;

import net.deviantevil.decore.DECore;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Fix Chat Colours class
 * @author Kristian ("kjhf")
 */
public final class Colours {

    /** Replace the line with annoying things. */
    public static String doAnnoyingReplace(String line) {
        for (Player p : DECore.getDECore().getServer().getOnlinePlayers()) {
            line = line.replace(p.getName(), "derp");
        }
        
        line = line.replace(" I ", " you ")
                   .replace(" i ", " you ")
                   .replace(" me ", " you ")
                   .replace("help", "****")
                   .replace(" :(", " ;)")
                   .replace(" lol ", " I love you! ")
                   .replace("ha", "u")
                   .replace("he", "eh");    
        
        return line;
    }
    
    /** Return if the symbol can represent a colour if it was preceded by a colour sign, \u00A7.
     * Specifically, if the char is 0-9, A-F, or a-f. */
    public static boolean isColourable(char c){
    	c = Character.toLowerCase(c);
    	for (ChatColor cc : ChatColor.values()) {
    	    if (cc.getChar() == c) return true;
    	}
    	return false;
    }
    
    /** Fix the colours in a line. This assumes colours start with a $ or &
     * @param line The line to fix 
     * @return The resulting line */
    public static String fixColours(String line) {
        char[] chararray = line.toCharArray();
        
        for (int i = 0; i < chararray.length; i++) {
            if (chararray[i] == '$' || chararray[i] == '&') {
                if (i+1 < chararray.length && isColourable(chararray[i+1])) {
                    chararray[i] = ChatColor.COLOR_CHAR; //'\u00A7';
                }
            }
        }
        
        return String.valueOf(chararray);
    }
    
    /** Punch holes in the text (letters punched appear black)
     * @param line The line to 'punch'
     * @param force Chance of a text change [0-1]
     * @return The resulting line */
    public static String makeHoley(String line, double force) {
        if (force == 1) {
            return "";
        }
        
        String newline = "";
        Random random;
        boolean skipnext = false;
        
        for (char c : line.toCharArray()) {
            if (skipnext) {
                newline += c;
                skipnext = false;
                continue;
            }
            if (c == ' ') {
                newline += c; // No point colouring in a space
                continue;
            }
            if (c == ChatColor.COLOR_CHAR) {
                newline += c; // No point colouring in a colour symbol
                skipnext = true; // Skip the next character, which is the colour type
                continue;
            }
            random = new Random();
            double nextDouble = random.nextDouble();
            if (force > nextDouble) {
                newline += ChatColor.BLACK.toString() + c + ChatColor.WHITE.toString(); // The letter was punched away!
                continue;
            }
            
            newline += c; // The letter won!
            continue;         
        }
        return newline;
    }
    
    /** Make the letters in line Red, White and Blue ChatColors
     * @param line The line to change
     * @return The resulting line */
    public static String makePatriotic(String line, boolean makeMagic) {
        String newline = "";
        int counter = 0;
        boolean skipnext = false;
        
        for (char c : line.toCharArray()) {
            if (skipnext) {
                newline += c;
                skipnext = false;
                continue;
            }
            if (c == ' ') {
                newline += c; // No point colouring in a space
                continue;
            }
            if (c == ChatColor.COLOR_CHAR) {
                newline += c; // No point colouring in a colour symbol
                skipnext = true; // Skip the next character, which is the colour type
                continue;
            }
            switch (counter) {
                case 0: 
                    newline += makeMagic ? 
                        ChatColor.RED.toString() + ChatColor.MAGIC.toString() + c :
                        ChatColor.RED.toString() + c; 
                    break;
                case 1: 
                    newline += makeMagic ? 
                        ChatColor.WHITE.toString() + ChatColor.MAGIC.toString() + c :
                        ChatColor.WHITE.toString() + c; 
                    break;
                case 2: 
                    newline += makeMagic ? 
                        ChatColor.BLUE.toString() + ChatColor.MAGIC.toString() + c :
                        ChatColor.BLUE.toString() + c; 
                    break;
            }
            counter++;
            if (counter == 3) {
                counter = 0;
            }            
        }
        return newline;
    }
    
    /** Make the letters in line rainbow ChatColors
     * @param line The line to make a rainbow with 
     * @return The resulting line */
    public static String makeRainbow(String line, boolean makeMagic) {
        String newline = "";
        int counter = 0;
        boolean skipnext = false;
        
        for (char c : line.toCharArray()) {
            if (skipnext) {
                newline += c;
                skipnext = false;
                continue;
            }
            if (c == ' ') {
                newline += c; // No point colouring in a space
                continue;
            }
            if (c == ChatColor.COLOR_CHAR) {
                newline += c; // No point colouring in a colour symbol
                skipnext = true; // Skip the next character, which is the colour type
                continue;
            }
            switch (counter) {
                case 0: newline += makeMagic ? 
                        ChatColor.RED.toString() + ChatColor.MAGIC.toString() + c :
                        ChatColor.RED.toString() + c; break;
                case 1: newline += makeMagic ? 
                        ChatColor.GOLD.toString() + ChatColor.MAGIC.toString() + c :
                        ChatColor.GOLD.toString() + c; break;
                case 2: newline += makeMagic ? 
                        ChatColor.YELLOW.toString() + ChatColor.MAGIC.toString() + c :
                        ChatColor.YELLOW.toString() + c; break;
                case 3: newline += makeMagic ? 
                        ChatColor.GREEN.toString() + ChatColor.MAGIC.toString() + c :
                        ChatColor.GREEN.toString() + c; break;  
                case 4: newline += makeMagic ? 
                        ChatColor.BLUE.toString() + ChatColor.MAGIC.toString() + c :
                        ChatColor.BLUE.toString() + c; break; 
                case 5: newline += makeMagic ? 
                        ChatColor.DARK_PURPLE.toString() + ChatColor.MAGIC.toString() + c :
                        ChatColor.DARK_PURPLE.toString() + c; break; 
                case 6: newline += makeMagic ? 
                        ChatColor.LIGHT_PURPLE.toString() + ChatColor.MAGIC.toString() + c :
                        ChatColor.LIGHT_PURPLE.toString() + c; break; 
                    // &C &6 &E &A &9 &5 &D
            }
            counter++;
            if (counter == 7) {
                counter = 0;
            }            
        }
        return newline;
    }
    
    /** Make the letters in line alternate dark grey and white ChatColors
     * This assumes Colours start with a $ or &
     * @param line The line to make striped 
     * @return The resulting line */
    public static String makeStripey(String line) {
        String newline = "";
        int counter = 0;
        boolean skipnext = false;
        
        for (char c : line.toCharArray()) {
            if (skipnext) {
                newline += c;
                skipnext = false;
                continue;
            }
            if (c == ' ') {
                newline += c; // No point colouring in a space
                continue;
            }
            if (c == ChatColor.COLOR_CHAR) {
                newline += c; // No point colouring in a colour symbol
                skipnext = true; // Skip the next character, which is the colour type
                continue;
            }
            switch (counter) {
                case 0: newline += ChatColor.DARK_GRAY.toString() + c; break;
                case 1: newline += ChatColor.WHITE.toString() + c; break; 
            }
            counter++;
            if (counter == 2) {
                counter = 0;
            }            
        }
        return newline;
    }
    
    
    /** Make the letters in line random colours
     * @return The resulting line */
    public static String makeRandom(String line) {
        String newline = "";
        boolean skipnext = false;
        
        for (char c : line.toCharArray()) {
            if (skipnext) {
                newline += c;
                skipnext = false;
                continue;
            }
            if (c == ' ') {
                newline += c; // No point colouring in a space
                continue;
            }
            if (c == ChatColor.COLOR_CHAR) {
                newline += c; // No point colouring in a colour symbol
                skipnext = true; // Skip the next character, which is the colour type
                continue;
            }
            
            newline += ChatColor.values()[new Random().nextInt(ChatColor.values().length)].toString() + c;          
        }
        return newline;
    }

    private Colours () {
    }
}
