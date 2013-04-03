package net.deviantevil.utilities;

import java.util.ArrayList;
import java.util.Collection;

public final class Strings {
     
    /** Forces first char in the string to uppercase, the rest lowercase. Returns the resulting string. */
    public static String correctCase(String s) {
        if (isNullOrEmpty(s)) return s;
        return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
    }
    
    /** Check if a string is empty or null */
    public static boolean isNullOrEmpty(String s) {
        return (s == null || s.isEmpty());
    }
    
    /** Check if a list is empty or null */
    public static boolean isNullOrEmpty(Collection<String> s) {
        return (s == null || s.isEmpty());
    }
    
    /** Check if a list is empty or null */
    public static boolean isNullOrEmpty(String[] s) {
        return (s == null || s.length == 0);
    }
    
    /** Convert a String[] to a string using ";" as a delimiter. Returns empty if null or empty array */
    public static String stringArrayToString(String[] theArray) {
        return stringArrayToString(theArray, ";");
    }
    
    /** Convert a String[] to a string using a custom delimiter. Returns empty if null or empty array */
    public static String stringArrayToString(String[] theArray, String delimiter) {
        String ret = "";
        if (isNullOrEmpty(theArray)) return ret;
        
        for (String s : theArray) {
            ret += s + delimiter;
        }
        return ret.substring(0, ret.length() - delimiter.length()); // Remove the delimiter at the end
    }

    /** Convert an ArrayList to a String using ";" as a delimiter. Returns empty if null or empty list */
    public static String stringListToString(Collection<String> theList) {
        return stringListToString(theList, ";");
    }
    
    /** Convert an ArrayList to a String using a custom delimiter. Returns empty if null or empty list */
    public static String stringListToString(Collection<String> theList, String delimiter) {
        String ret = "";
        if (isNullOrEmpty(theList)) return ret;
        
        for (String s : theList) {
            ret += s + delimiter;
        }
        return ret.substring(0, ret.length() - delimiter.length()); // Remove the delimiter at the end
    }

    /** Convert a string to a ArrayList of Strings using ";" as a delimiter. Returns empty list if null or empty String */
    public static ArrayList<String> stringToStringList(String theString) {
        return stringToStringList(theString, ";");
    }
    
    /** Convert a string to a ArrayList of Strings using a custom delimiter. Returns empty list if null or empty String */
    public static ArrayList<String> stringToStringList(String theString, String delimiter) {
        ArrayList<String> ret = new ArrayList<String>();
        if (isNullOrEmpty(theString)) return ret;
        
        String[] saved = theString.split(delimiter);
        for (String s : saved) {
            ret.add(s);
        }
        return ret;
    }
    
    private Strings() { 
        // Util Class, no constructor
    }
}
