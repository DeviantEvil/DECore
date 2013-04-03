package net.deviantevil.utilities;

import java.util.*;
import net.deviantevil.decore.DECore;

import org.bukkit.ChatColor;

/**
 * Timestring parsing functions
 * <br>
 * For reference: <br>
 * Minecraft's game loop runs at a fixed rate of 20 cycles per second, so one tick happens every 1/20th of a second. <br>
 * Which means: <br>
 * 1 second = 20L <br>
 * 1 minute = 1200L <br>
 * 20 minutes (one game-day) = 24000L <br>
 * 30 minutes = 36000L <br>
 * 1 hour = 72000L <br>
 * 1 day = 1728000L
 * @author Kristian ("kjhf")
 */
public final class TimeParser {
    /** These values convert from millis (1L = 1ms) and assign to a char. */
    private static Map<Character, Long> timeVars = new LinkedHashMap<Character, Long>();
    private static String timeString = "";
    
    /** Util Class, needs no constructor */
    private TimeParser() { }
    
    static {
        timeVars.put('w', 604800000L);
        timeVars.put('d', 86400000L);
        timeVars.put('h', 3600000L);
        timeVars.put('m', 60000L);
        timeVars.put('s', 1000L);
    }
     
    /**
     * Gets a List of variables (w|d|h|m|s) contained in the timestring without their amounts.
     *
     * @param timestring Timestring argument given
     * @return List of chars found
     */
    public static List<Character> getVars(String timestring) {
        timeString = timestring;
        List<Character> foundvars = new ArrayList<Character>();
        for (Character timeVar : timeVars.keySet()) {
            int index = timeString.indexOf(timeVar); 
            int lastindex = timeString.lastIndexOf(timeVar); 
            if (index != -1) { // If the var is found in the string
                if (lastindex == index) {
                    foundvars.add(timeVar); // Add to the List
                } else {
                    DECore.log.warning(ChatColor.GOLD + "More than one occurrence of variable " + ChatColor.WHITE + timeVar + ChatColor.GOLD + " in " + ChatColor.WHITE + timeString + ChatColor.GOLD + ".");
                    return null;
                }
            }
        }
        //if (foundvars == null || foundvars.isEmpty()) {}
        return foundvars;
    }
    
    /**
     * Gets the number of variables (w|d|h|m|s) found in the timestring.
     *
     * @param timestring Timestring argument given
     * @return Number of usable chars found
     */
    public static int getNumberOfVars(String timestring) {
        timeString = timestring;
        return getVars(timeString).size();
    }   
    
    /**
     * Gets the full name of the time period represented by Character of the variables (w|d|h|m|s).
     *
     * @param var Variable letter
     * @return Full name of variable
     */
    public static String getVarName(Character var) {
        if (var == 'w') {
            return "week(s)";
        } else if (var == 'd') {
            return "day(s)";
        } else if (var == 'h') {
            return "hour(s)";
        } else if (var == 'm') {
            return "minute(s)";
        } else if (var == 's') {
            return "second(s)";
        } else {
            return "";
        }
    }

    /**
     * Shortcut for getVarFromName(name, '0');
     */
    public static char getVarFromName(String name) {
        return getVarFromName(name, '0');
    }
    
    /**
     * Gets the var char associated with a name. <p>
     * e.g. Weeks gives w. <p>
     *
     * @param name The timestring full-name.
     * @param defaultchar The char given if no matches found.
     * @return The char associated.
     */
    public static char getVarFromName(String name, char defaultchar) {
        name = name.replace("(", "").replace(")", "").replace("_", "");
        if (name.equalsIgnoreCase("week") || name.equalsIgnoreCase("weeks")) {
            return 'w';
        } else if (name.equalsIgnoreCase("day") || name.equalsIgnoreCase("days")) {
            return 'd';
        } else if (name.equalsIgnoreCase("hour") || name.equalsIgnoreCase("hours")) {
            return 'h';
        } else if (name.equalsIgnoreCase("minute") || name.equalsIgnoreCase("minutes")) {
            return 'm';
        } else if (name.equalsIgnoreCase("second") || name.equalsIgnoreCase("seconds")) {
            return 's';
        } else {
            return defaultchar;
        }
    }   
    
    /**
     * Gets the raw number before the variable found. <br>
     * e.g. 10d would return 10, 5w would return 5, etc. 
     *
     * @param timestring Timestring argument given
     * @return The number of variable.
     */
    public static int getAmount(String timestring) {
        timeString = timestring;
        int numberOfVars = getNumberOfVars(timeString);
        if (numberOfVars == 0) {
            try {
                int parseInt = Integer.parseInt(timeString);
                return parseInt;
            } catch (Exception e) {
                DECore.log.warning(ChatColor.GOLD + "Unknown variable found in timestring: "+timeString);
                return -1;
            }
        } else if (numberOfVars == 1) {
            Character var = getVars(timeString).get(0);
            try {
                String substring = timeString.substring(0, timeString.indexOf(var));
                return Integer.parseInt(substring);
            } catch (Exception e) {
                DECore.log.warning(ChatColor.GOLD + "Incorrect syntax/could not parse: "+timeString);
                return -1;
            }
        } else {
            DECore.log.warning(ChatColor.GOLD + "Plugin does not support multiple variables.");
            return -1;
        } 
    }
        
    /**
     * Calculates the long of the timestring, assuming: <p>
     * 1w == 604800000L; <br>
     * 1d == 86400000L; <br>
     * 1h == 3600000L; <br>
     * 1m == 60000L; <br>
     * 1s == 1000L;
     *
     * @param timestring Timestring argument given
     * @return The calculated Long.
     */
    public static long getLong(String timestring) {
        timeString = timestring;
        if (getNumberOfVars(timeString) == 0) {
            return getAmount(timeString) * 86400000L; // Assume Days
        }
        
        Character var = getVars(timeString).get(0);
        long get = timeVars.get(var);
        long amount = getAmount(timeString);
        return amount * get;
    }
    /**
     * Calculates the long from an amount and associated time var, assuming: <p>
     * 1w == 604800000L; <br>
     * 1d == 86400000L; <br>
     * 1h == 3600000L; <br>
     * 1m == 60000L; <br>
     * 1s == 1000L;
     *
     * @param amount The number of x
     * @param timevar The associated time var
     * @return The calculated Long.
     */
    public static long getLong(int amount, char timevar) {
        long get = timeVars.get(timevar);
        return amount * get;
    }
    
    /**
     * Calculates a nice timestring from a long, giving: <p>
     * weeks if long is >= 604800000L  else<br>
     * days if long is >= 86400000L  else<br>
     * hours if long is >= 3600000L  else<br>
     * minutes if long is >= 60000L  else<br>
     * seconds if long is >= 1000L  else<br>
     * less than one second;
     *
     * @param Along long argument given
     * @return The nice timestring
     */
    public static String longtoNiceTimeString(long Along) {
        if ((Along / 604800000L) >= 1) {
            Along = (Along / 604800000L);
            return Long.toString(Along)+" week(s)";
        } else if ((Along / 86400000L) >= 1) {
            Along = (Along / 86400000L);
            return Long.toString(Along)+" day(s)";
        } else if ((Along / 3600000L) >= 1) {
            Along = (Along / 3600000L);
            return Long.toString(Along)+" hour(s)";
        } else if ((Along / 60000L) >= 1) {
            Along = (Along / 60000L);
            return Long.toString(Along)+" minute(s)";
        } else if ((Along / 1000L) >= 1) {
            Along = (Along / 1000L);
            return Long.toString(Along)+" second(s)";
        } else {
            return "less than one second";
        }
    }
    /**
     * Calculates a timestring from a long, giving: <p>
     * w if long is >= 604800000L  else<br>
     * d if long is >= 86400000L  else<br>
     * h if long is >= 3600000L  else<br>
     * m if long is >= 60000L  else<br>
     * s
     *
     * @param aLong long argument given
     * @return The timestring
     */
    public static String longtoTimeString(long aLong) {
        if ((aLong / 604800000L) >= 1) {
            aLong = (aLong / 604800000L);
            return Long.toString(aLong)+"w";
        } else if ((aLong / 86400000L) >= 1) {
            aLong = (aLong / 86400000L);
            return Long.toString(aLong)+"d";
        } else if ((aLong / 3600000L) >= 1) {
            aLong = (aLong / 3600000L);
            return Long.toString(aLong)+"h";
        } else if ((aLong / 60000L) >= 1) {
            aLong = (aLong / 60000L);
            return Long.toString(aLong)+"m";
        } else {
            aLong = (aLong / 1000L);
            return Long.toString(aLong)+"s";
        }
    }
    
    /** Return month number. N.B. Jan = 0. */
    public static int getMonth() {
        return Calendar.getInstance().get(Calendar.MONTH);
    }
    
    /** Returns today's date (day) */
    public static int getDay() {
        return getServerCalendar().get(Calendar.DAY_OF_MONTH);
    }
    
    /** Returns current time's hours from the GMT Locale in 24 hour format */
    public static int getGMTHours() {
        final int rawhour = getGMTCalendar().get(Calendar.HOUR);
        
        if (getGMTCalendar().get(Calendar.AM_PM) == Calendar.AM) {
            return rawhour; // For AM
        } 
        return rawhour == 12 ? 0 : rawhour + 12; // For PM
    }
    
    /** Returns current time's hours from the Calendar in 24 hour format */
    public static int getServerHours() {
        final int rawhour = getServerCalendar().get(Calendar.HOUR);
        if (getServerCalendar().get(Calendar.AM_PM) == Calendar.AM) {
            return rawhour; // For AM
        } 
        return rawhour == 12 ? 0 : rawhour + 12; // For PM
    }
    
    /** Returns minutes from current time. */
    public static int getMinutes() {
        return getServerCalendar().get(Calendar.MINUTE);
    }
        
    /** Returns seconds from current time. */
    public static int getSeconds() {
        return getServerCalendar().get(Calendar.SECOND);
    }
    
    /** Returns minutes and seconds in form of decimal number from current time. <br>
     * e.g. 5mins 15s --> 5.25 */
    public static double getMinuteSeconds() {
        return getMinutes() + (getSeconds() / 60.0);
    }
    
    /** Returns hours and minutes in form of decimal number from current time and offset from DECore. <br>
     * e.g. 2h30 -> 2.5 */
    public static double getHourMinutes() {
        return getServerHours() + (getMinutes() / 60.0);
    }
    
    /** Convert ticks to milliseconds. 
     * This assumes one server tick is 0.05s (1/20) == 50ms */
    public static long ticksToMillis(long ticks) {
        return (ticks * 50L);
    }
    
    /** Convert seconds to ticks. 
     * This assumes one server tick is 0.05s (1/20) */
    public static long secondsToTicks(long s) {
        return (s * 20L);
    }
    
    /** Convert milliseconds to ticks. 
     * This assumes one server tick is 0.05s (1/20) == 50ms */
    public static double millisToTicks(long ms) {
        return (ms / 50L);
    }
    
    /** Returns ticks until next real-time half-hour. */
    public static long getTicksToNextHalfHour() {
        double minuteSeconds = TimeParser.getMinuteSeconds(); // Current minute.seconds
        return minuteSeconds > 30 ? TimeParser.secondsToTicks((long)((60 - minuteSeconds) * 60L)) : TimeParser.secondsToTicks((long)((30 - minuteSeconds) * 60L)); // Subtract minute seconds from 60 to give time until next hour, then convert to ticks.
    }
    
    /** Returns ticks until next real-time hour. */
    public static long getTicksToNextHour() {
        double minuteSeconds = TimeParser.getMinuteSeconds(); // Current minute.seconds
        return TimeParser.secondsToTicks((long)((60 - minuteSeconds) * 60L)); // Subtract minute seconds from 60 to give time until next hour, then convert to ticks.
    }
        
    /** Return if today is the first of the month */
    public static boolean isStartOfMonth() {
        return getDay() == 1;
    }  
    
    /** Get the ID of the server time we're running on. */
    public static String getServerTimeTag() {
        return getServerCalendar().getTimeZone().getDisplayName();
    }  
    
    /** Get the current calendar of the server */
    public static Calendar getServerCalendar() {
        return Calendar.getInstance();
    }
    
    /** Get the current date object from GMT */
    public static Calendar getGMTCalendar() {
        final Calendar c = Calendar.getInstance();
        final TimeZone z = c.getTimeZone();
        int offset = z.getRawOffset();
        if (z.inDaylightTime(new Date())){
            offset += z.getDSTSavings();
        }
        int offsetHrs = offset / 1000 / 60 / 60;
        int offsetMins = offset / 1000 / 60 % 60;

        c.add(Calendar.HOUR_OF_DAY, (-offsetHrs));
        c.add(Calendar.MINUTE, (-offsetMins));
        return c;
    }
}
