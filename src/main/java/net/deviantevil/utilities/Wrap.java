package net.deviantevil.utilities;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Word wrapping methods
 * Each message is allowed to contain 100 characters.
 * A second line starts after 50 characters. 
 * 
 * @author Kristian ("kjhf")
 */
public final class Wrap {
    /** Analyse lines in the message and make sure each word is readable.
     * @param message The message to wrap
     * @return The message lines. [Use an iterator to output these] */
    public static List<String> wrap(String message) {
        List<String> lines = makeIntoLines(message);
        List<String> newmessage = new ArrayList<String>();
        
        String newline = "";
        String biglinecarryover = "";
        String carryover = "";
        for (String aLine : lines) {
            newline = aLine;
            
            if (!carryover.isEmpty() || !biglinecarryover.isEmpty()) { 
                newline = carryover + biglinecarryover + newline; // Append the line onto the carryover of the previous
                biglinecarryover = "";
                carryover = "";
            }
            if (newline.length() > 49) {
                biglinecarryover = newline.substring(49);
                newline = newline.substring(0, 49);
            } 
            if (!newline.contains(" ")) { // How can we split if there are no spaces? o.O
                newmessage.add(newline);
                continue;
            }
            
            if (newline.length() >= 40) { // We would actually gain something by splitting here.
                int lastIndexOf = newline.lastIndexOf(" ");
                if (lastIndexOf == -1) {
                    newmessage.add(newline.substring(0, 49));
                    newmessage.add(newline);
                    carryover = newline.substring(49);
                    continue;
                }
                newmessage.add(newline.substring(0, lastIndexOf));
                carryover = newline.substring(lastIndexOf);
            } else { // Short message. What's the point of splitting?
                newmessage.add(newline);
            }
        }
        
        newline = "";
        if (!carryover.isEmpty() || !biglinecarryover.isEmpty()) { 
            newline = carryover + biglinecarryover; // Create new line from final carryovers
        } else {
            return newmessage;
        }
        
        if (newline.length() <= 50 && !newline.isEmpty()) {  // Carryovers are less than 51. Just add to the end. 
            newmessage.add(newline);
        } else if (!newline.isEmpty()) {
            List<String> wrappedcarryover = wrap(newline);  // Carryovers are greater than 50. We need to wrap it >.<
            newmessage.addAll(wrappedcarryover);
        }
        return newmessage;
    }
    
    /**
     * Wrap the lines and send the result to the sender.
     * @param sender The Sender to output the lines to.
     * @param message The message to wrap
     */
    public static void wrapAndSend(CommandSender sender, String message) {
        List<String> wrap = wrap(message);
        for (String line : wrap) {
            sender.sendMessage(line);
        }
    }
    
    /**
     * Wrap the lines and send the result to the player.
     * @param player The player to output the lines to.
     * @param message The message to wrap
     */
    public static void wrapAndSend(Player player, String message) {
        wrapAndSend((CommandSender)player, message);
    }

    /** Make the message into multiple String lines; each line <= 50 chars
     * @param message The message to wrap
     * @return The message lines. */
    public static List<String> makeIntoLines(String message) {
        List<String> lines = new ArrayList<String>();
        if (message.length() <= 50) {
            lines.add(message);
            // return lines;
        } else {
            int splitter = 1, previous = 0;
            for (splitter = 1; splitter <= message.length(); splitter++) {
                if (splitter % 49 == 0) { // A line of 50 chars
                    lines.add(message.substring(previous, splitter)); // Add line to the new message
                    previous = splitter; // Assign a successful line
                }
            }
            if (splitter != previous) { // The loop finished on a number not divisible by 50 (we're missing some text ;))
                lines.add(message.substring(previous, splitter-1)); // Add line to the new message
            }
        }
        //Log.info("DECore", "Input: "+lines.toString());
        return lines;
    }
}
