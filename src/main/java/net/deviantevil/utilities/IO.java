package net.deviantevil.utilities;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import net.deviantevil.decore.DEConfigOptions;
import net.deviantevil.decore.DECore;

/**
 * Functions involving saving and loading to and from plugins' config and save files.
 * @author Kristian ("kjhf")
 */
public class IO
{

    /** Shortcut for getProperty(new File(directory + File.separator + file), node); */
    public static String getProperty(String directory, String file, String node)
    {
        return getProperty(new File(directory + File.separator + file), node);
    }

    /** Shortcut for getProperty(new File(directory + File.separator + file), node); */
    public static String getProperty(File directory, File file, String node)
    {
        return getProperty(new File(directory + File.separator + file), node);
    }

    /**
     * Get all properties from a .properties file.
     * @param file The full file's name. Expected .properties at the end.
     * @return The properties as a String List, or empty list if the file was not found or empty.
     */
    public static ArrayList<String> getAllProperties(File file)
    {
        Properties properties = new Properties();
        ArrayList<String> list = new ArrayList<String>();
        try
        {
            properties.load(new FileInputStream(file));
            for (Object propertynode : properties.keySet())
            {
                list.add(properties.getProperty((String) propertynode));
            }
            return list;
        }
        catch (Exception ex)
        {
            if (DEConfigOptions.debugging)
                DECore.log.info("getAllProperties: Could not load properties from " + file + "." + ex);
            return null;
        }
    }

    /**
     * Get a property from a .properties file.
     * @param file The full file's name. Expected .properties at the end.
     * @param node The target property to get.
     * @return The property as a String, or null if the property was not found.
     */
    public static String getProperty(File file, String node)
    {
        Properties properties = new Properties();
        try
        {
            properties.load(new FileInputStream(file));
            return properties.getProperty(node);
        }
        catch (Exception ex)
        {
            if (DEConfigOptions.debugging)
                DECore.log.info("getProperty: Could not load property " + node + " from " + file + "." + ex);
            return null;
        }
    }

    /** Short for getText(directory, file.getName()); */
    public static String getText(String directory, File file)
    {
        return getText(new File(directory + File.separator + file));
    }

    /** Short for getText(directory, file.getName()); */
    public static String getText(File directory, File file)
    {
        return getText(new File(directory + File.separator + file));
    }

    /** Short for getText(new File(directory + file)); */
    public static String getText(File directory, String file)
    {
        return getText(new File(directory + File.separator + file));
    }

    /**
     * Get text from a text file.
     * @param file The full filename to load from. Expected .txt at the end.
     * @return The text as a String array.
     */
    public static String getText(File file)
    {
        String in = "";
        try
        {
            // BufferedReader br = new BufferedReader(new FileReader(file.getPath()));
            Scanner scan = new Scanner(file);
            if (!file.canRead())
            {
                DECore.log.severe("getText: Error in reading " + file.getPath() + ". The file cannot be read.");
                scan.close();
                return null;
            }
            while (scan.hasNext())
            {
                in = scan.next();
            }
            scan.close();
            return in;

        }
        catch (Exception ex)
        {
            DECore.log.severe("getText: Error in reading " + file.getPath() + ". " + ex);
            return in;
        }
    }

    /** Shortcut for removeProperty(directory, file, key). */
    public static boolean removeProperty(String directory, String file, String key)
    {
        return removeProperty(new File(directory), new File(file), key);
    }

    /**
     * Remove a property from a .properties file.
     * @param directory The directory of the file.
     * @param file The file to read. <b>Does not include file extension!</b> Expected .properties.
     * @param key The target key to remove.
     * @return Success true/false. Returns false if directory does not exist.
     */
    public static boolean removeProperty(File directory, File file, String key)
    {
        if (!directory.exists())
        {
            return false;
        }
        return removeProperty(new File(directory + File.separator + file), key);
    }

    /**
     * Remove a property from a .properties file.
     * @param file The file to read. <b>Does not include file extension!</b> Expected .properties.
     * @param key The target key to remove.
     * @return Success true/false. Returns false if file does not exist.
     */
    public static boolean removeProperty(File file, String key)
    {
        Properties properties = new Properties();
        try
        {
            if (!file.exists())
            {
                return false;
            }
            properties.load(new FileInputStream(file));
            properties.remove(key);
            properties.store(new FileOutputStream(file), null);
        }
        catch (Exception ex)
        {
            DECore.log.severe("removeProperty: Could not remove property " + key + " in " + file + ". " + ex);
            return false;
        }
        return true;
    }

    /** Shortcut for saveProperty(directory, file, key, property). */
    public static boolean saveProperty(String directory, String file, String key, String property)
    {
        return saveProperty(new File(directory), new File(file), key, property);
    }

    /**
     * Set a property to a .properties file.
     * @param directory The directory to save to.
     * @param file The file to save to. <b>Does not include file extension!</b> Expected .properties.
     * @param key The target key to set to.
     * @param property The target property to set.
     * @return Success true/false
     */
    public static boolean saveProperty(File directory, File file, String key, String property)
    {
        if (!directory.exists())
        {
            directory.mkdirs();
        }
        return saveProperty(new File(directory + File.separator + file), key, property);
    }

    /**
     * Set a property to a .properties file.
     * @param file The full filename to save to. Expected .properties.
     * @param key The target key to set to.
     * @param property The target property to set.
     * @return Success true/false
     */
    public static boolean saveProperty(File file, String key, String property)
    {
        Properties properties = new Properties();
        try
        {
            if (!file.exists())
            {
                file.createNewFile();
            }
            properties.load(new FileInputStream(file));
            properties.setProperty(key, property);
            properties.store(new FileOutputStream(file), null);
        }
        catch (Exception ex)
        {
            DECore.log.severe("saveProperty: Could not save property " + key + " = " + property + " to " + file + ". " + ex);
            return false;
        }
        return true;
    }

    /** Shortcut for saveTextFile(directory, file, out). */
    public static boolean saveTextFile(String directory, String file, String out)
    {
        return saveTextFile(new File(directory), new File(file), out);
    }

    /** Shortcut for saveTextFile(directory, file, new String[] {out}, ""). */
    public static boolean saveTextFile(File directory, File file, String out)
    {
        return saveTextFile(directory, file, new String[] { out }, "");
    }

    /** Shortcut for saveTextFile(directory, file, out, ""). */
    public static boolean saveTextFile(String directory, String file, String[] out)
    {
        return saveTextFile(new File(directory), new File(file), out, "");
    }

    /** Shortcut for saveTextFile(directory, file, out, ""). */
    public static boolean saveTextFile(File directory, File file, String[] out)
    {
        return saveTextFile(directory, file, out, "");
    }

    /**
     * Output text to a file.
     * @param directory The directory to save to.
     * @param file The file to save to. <b>Does not include file extension!</b> Probably want .txt.
     * @param out The text to set.
     * @param separator String appearing after the each String in the out elements. Probably want to use as a separator such as ", " or "|".
     * @return Success true/false
     */
    public static boolean saveTextFile(File directory, File file, String[] out, String separator)
    {
        try
        {
            if (!directory.exists())
            {
                directory.mkdirs();
            }
            File thefile = new File(directory + File.separator + file);
            if (!thefile.exists())
            {
                thefile.createNewFile();
            }
            FileWriter fw = new FileWriter(thefile);
            BufferedWriter bw = new BufferedWriter(fw);
            for (String aString : out)
            {
                bw.write(aString);
                bw.write(separator);
            }
            bw.close();
            return true;
        }
        catch (Exception ex)
        {
            DECore.log.severe("saveTextFile error: " + ex);
            return false;
        }
    }

    private IO()
    {
    }
}
