package net.deviantevil.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.deviantevil.decore.DEConfigOptions;
import net.deviantevil.decore.DECore;
import net.deviantevil.decore.DECore.WorldBoarder;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Various functions using Bukkit's Location and Vector class
 * @author Kristian ("kjhf")
 * @see Location
 * @see Vector
 */
public class Locations
{

    /**
     * Tests distance between two locations are within a limit set. World friendly.
     * @param loc1 The first Location
     * @param loc2 The second Location
     * @param distance Radius in which to check.
     * @return True if distance is less than or equal to the radius specified (and on same world), else false.
     */
    public static boolean checkLocationsAreNear(Location loc1, Location loc2, int distance)
    {
        if (loc1 != null && loc2 != null && loc1.getWorld() == loc2.getWorld())
        {
            if (loc1.distance(loc2) <= distance)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Find a safe Location to teleport a player to.
     * @param location Reference Location
     * @return A new, safe location
     */
    public static Location findSafeLocation(Location location)
    {
        Location clone = location.clone();
        clone.setY(126);
        for (;;)
        {
            for (int offset = 0; clone.getBlock().isEmpty() && clone.getY() != 0; offset++)
            {
                clone.setY(126 - offset);
            }
            if (clone.getY() == 0)
            {
                clone.setY(126);
                changeX(clone, 1);
            }
            else
            {
                break;
            }
        }
        changeY(clone, 2);
        return clone;
    }

    /**
     * Tests if locations are equal in block integers of x, y, z, and worlds are the same.
     * @param loc1 The first Location
     * @param loc2 The second Location
     * @return True if locations are equal, else false.
     */
    public static boolean locationsAreEqual(Location loc1, Location loc2)
    {
        if (loc1 != null && loc2 != null)
        {
            if (loc1.getBlockX() == loc2.getBlockX()
                    && loc1.getBlockY() == loc2.getBlockY()
                    && loc1.getBlockZ() == loc2.getBlockZ()
                    && loc1.getWorld().getName().equals(loc2.getWorld().getName()))
            {
                return true; // Locations' co-ordinates and worlds match
            }
        }
        return false;
    }

    /**
     * Tests if Vectors are equal in block integers of x, y, z.
     * @param v1 The first Vector
     * @param v2 The second Vector
     * @return True if Vectors are equal, else false.
     */
    public static boolean vectorsAreEqual(Vector v1, Vector v2)
    {
        if (v1 != null && v2 != null)
        {
            if (v1.getBlockX() == v2.getBlockX()
                    && v1.getBlockY() == v2.getBlockY()
                    && v1.getBlockZ() == v2.getBlockZ())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests if Location is in a List of Locations. World friendly.
     * @param region The List of Locations
     * @param loc The Location to check
     * @param iff If true, loc must be <u>exactly</u> equal to the one in the region; if false, yaw and pitch is ignored and co-ordinates are block integers.
     * @return True if Location is found in the Region, else false.
     */
    public static boolean checkRegionContainsLocation(List<Location> region, Location loc, boolean iff)
    {
        if (region == null || region.isEmpty() || loc == null)
        {
            return false;
        }
        for (Location location : region)
        {
            if (iff)
            {
                if (location.equals(loc))
                {
                    return true; // Locations match exactly
                }
            }
            else
            { // Not absolutely equal
                if (locationsAreEqual(location, loc))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets distance between two locations. World friendly.
     * @param loc1 The first Location
     * @param loc2 The second Location
     * @return Returns a double of the distance between them. Returns NaN if the Locations are not on the same World or distance is too great.
     */
    public static double getDistance(Location loc1, Location loc2)
    {
        if (loc1 != null && loc2 != null && loc1.getWorld().getName().equals(loc2.getWorld().getName()))
        {
            return loc1.distance(loc2);
        }
        return Double.NaN;
    }

    /**
     * Gets distance between two locations.
     * @param x1 The first x co-ordinate
     * @param y1 The first y co-ordinate
     * @param z1 The first z co-ordinate
     * @param x2 The second x co-ordinate
     * @param y2 The second y co-ordinate
     * @param z2 The second z co-ordinate
     * @return Returns a double of the distance between them.
     */
    public static double getDistance(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2));
    }

    /**
     * Gets the minimum Location in a Region.
     * @param region The List of Locations to search.
     * @return The minimum Location
     */
    public static Location getMinimumLocation(List<Location> region)
    {
        int x1 = Integer.MAX_VALUE, x2, y1 = Integer.MAX_VALUE, y2, z1 = Integer.MAX_VALUE, z2;
        for (Location loc : region)
        {
            x2 = loc.getBlockX();
            y2 = loc.getBlockY();
            z2 = loc.getBlockZ();
            if (x2 < x1)
            { // Finding the Minimum Location (x1,y1,z1) and the Maximum Location (x2,y2,z2) by testing the locations in the region.
                x1 = x2;
            }
            if (y2 < y1)
            {
                y1 = y2;
            }
            if (z2 < z1)
            {
                z1 = z2;
            }
        }
        World world = region.get(0).getWorld();
        return new Location(world, x1, y1, z1);
    }

    /**
     * Gets the maximum Location in a Region.
     * @param region The List of Locations to search.
     * @return The maximum Location
     */
    public static Location getMaximumLocation(List<Location> region)
    {
        int x1 = Integer.MIN_VALUE, x2, y1 = Integer.MIN_VALUE, y2, z1 = Integer.MIN_VALUE, z2;
        for (Location loc : region)
        {
            x2 = loc.getBlockX();
            y2 = loc.getBlockY();
            z2 = loc.getBlockZ();
            if (x2 > x1)
            { // Finding the Minimum Location (x1,y1,z1) and the Maximum Location (x2,y2,z2) by testing the locations in the region.
                x1 = x2;
            }
            if (y2 > y1)
            {
                y1 = y2;
            }
            if (z2 > z1)
            {
                z1 = z2;
            }
        }
        World world = region.get(0).getWorld();
        return new Location(world, x1, y1, z1);
    }

    /**
     * Gets the nearest Player to a Location. Short for getNearestPlayer(loc, null);
     * @param loc The Location to search from
     * @return The nearest Player
     */
    public static Player getNearestPlayer(Location loc)
    {
        return getNearestPlayer(loc, null);
    }

    /**
     * Gets the nearest Player to a Location
     * @param loc The Location to search from
     * @param exclude A Player to exclude from the search (null to disable)
     * @return The nearest Player
     */
    public static Player getNearestPlayer(Location loc, Player exclude)
    {
        Player player = null;
        double distance = Double.MAX_VALUE;

        for (Player ent : loc.getWorld().getPlayers())
        {
            if (exclude != null && ent == exclude)
            {
                continue;
            }
            double distance1 = Locations.getDistance(loc, ent.getLocation());
            if (distance1 != Double.NaN && distance1 < distance)
            {
                distance = distance1;
                player = ent;
            }
        }
        return player;
    }

    /**
     * Gets the nearest Player's distance to a Location
     * @param loc The Location to search from
     * @return The distance away the nearest player is
     */
    public static Double getNearestPlayerDistance(Location loc)
    {
        double distance = Double.MAX_VALUE;

        for (Player ent : loc.getWorld().getPlayers())
        {
            double distance1 = Locations.getDistance(loc, ent.getLocation());
            if (distance1 != Double.NaN && distance1 < distance)
            {
                distance = distance1;
            }
        }
        return distance;
    }

    /**
     * Gets the nearest Entity to a Location. Short for getNearestEntity(loc, null);
     * @param loc The Location to search from
     * @return The nearest Entity
     */
    public static Entity getNearestEntity(Location loc)
    {
        return getNearestEntity(loc, null);
    }

    /**
     * Gets the nearest Entity to a Location (may or may not be a player)
     * @param loc The Location to search from
     * @param exclude An Entity (probably Player) to exclude from the search (null to disable)
     * @return The nearest Entity
     */
    public static Entity getNearestEntity(Location loc, Entity exclude)
    {
        List<Entity> entities = loc.getWorld().getEntities();
        Entity theEntity = null;
        double distance = Double.MAX_VALUE;

        for (Entity ent : entities)
        {
            if ((exclude != null) && (ent == exclude))
            {
                continue;
            }
            double distance1 = Locations.getDistance(loc, ent.getLocation());
            if (distance1 != Double.NaN && distance1 < distance)
            {
                distance = distance1;
                theEntity = ent;
            }
        }
        return theEntity;
    }

    /**
     * Gets the nearest Entity's distance to a Location
     * @param loc The Location to search from
     * @return The distance away the nearest Entity is
     */
    public static Double getNearestEntityDistance(Location loc)
    {
        List<Entity> entities = loc.getWorld().getEntities();
        double distance = Double.MAX_VALUE;

        for (Entity ent : entities)
        {
            double distance1 = Locations.getDistance(loc, ent.getLocation());
            if (distance1 != Double.NaN && distance1 < distance)
            {
                distance = distance1;
            }
        }
        return distance;
    }

    /**
     * Make a String from a Location in form "x, y, z"
     * <p>
     * Lazy method for "location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ()"
     * @param loc The Location to convert
     * @return The String made from the Location
     */
    public static String toStr(Location loc)
    {
        return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }

    /**
     * Make a String from a Location in form "x,y,z,yaw,pitch,worldname"
     * @param loc The Location to convert
     * @return The String made from the Location
     */
    public static String makeCoord(Location loc)
    {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        float yaw = loc.getYaw();
        float pitch = loc.getPitch();
        String worldname = loc.getWorld().getName();
        return x + "," + y + "," + z + "," + yaw + "," + pitch + "," + worldname;
    }

    /**
     * Make a Location from a string in form "x, y, z, yaw, pitch, worldname"
     * @param str The String to convert
     * @return The Location made from the String
     */
    public static Location makeLocation(String str)
    {
        String[] parts = str.split(",");
        double x = Double.parseDouble(parts[0].trim());
        double y = Double.parseDouble(parts[1].trim());
        double z = Double.parseDouble(parts[2].trim());
        float yaw = Float.parseFloat(parts[3].trim());
        float pitch = Float.parseFloat(parts[4].trim());
        World world = null;
        try
        {
            String worldname = (parts[5].trim());
            try
            {
                world = DECore.getDECore().getServer().getWorld(worldname);
            }
            catch (Exception ex)
            {
                DECore.log.warning("Error in making a location. The world called \"" + worldname + "\" was not found.");
            }
        }
        catch (Exception ex)
        {
            DECore.log.warning("Error in making a location. The given String does not contain a Worldname. Assuming first World.");
            List<World> worlds = DECore.getDECore().getServer().getWorlds();
            world = worlds.get(0);
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * Make a floored Location from another Location.
     * @param loc The Location to round
     * @return The new Location.
     */
    public static Location roundLocation(Location loc)
    {
        Location clone = loc.clone();
        clone.setX(loc.getBlockX());
        clone.setY(loc.getBlockY());
        clone.setZ(loc.getBlockZ());
        clone.setPitch(Math.round(loc.getPitch()));
        clone.setYaw(Math.round(loc.getYaw()));
        return clone;
    }

    /**
     * Remove the decimals from the Location. (Effectively rounds all values down)
     * @param loc The Location to round
     * @return The new Location.
     */
    public static Location truncateLocation(Location loc)
    {
        Location clone = loc.clone();
        clone.setX((int) loc.getX());
        clone.setY((int) loc.getY());
        clone.setZ((int) loc.getZ());
        clone.setPitch((int) loc.getPitch());
        clone.setYaw((int) loc.getYaw());
        return clone;
    }

    /**
     * Make a floored Vector from another Vector.
     * @param v The Vector to round
     * @return The new Vector.
     */
    public static Vector roundVector(Vector v)
    {
        Vector clone = v.clone();
        clone.setX(v.getBlockX());
        clone.setY(v.getBlockY());
        clone.setZ(v.getBlockZ());
        return clone;
    }

    /**
     * Remove the decimals from the Vector. (Effectively rounds all values down)
     * @param v The Vector to round
     * @return The new Vector.
     */
    public static Vector truncateVector(Vector v)
    {
        Vector v1 = v.clone();
        v1.setX((int) v.getX());
        v1.setY((int) v.getY());
        v1.setZ((int) v.getZ());
        return v1;
    }

    /**
     * Make a Vector from two Location points. Assumes loc1 is going to loc2.
     * @param loc1 The first Location
     * @param loc2 The second Location
     * @return A Vector for the line between these two Vectors. Returns null if either Location is null or Locations are cross-world.
     */
    public static Vector makeLineVector(Location loc1, Location loc2)
    {
        if (loc1 == null || loc2 == null || (loc1.getWorld() != loc2.getWorld()))
        {
            return null;
        }
        return (new Vector((loc2.getBlockX() - loc1.getBlockX()), (loc2.getBlockY() - loc1.getBlockY()), (loc2.getBlockZ() - loc1.getBlockZ())));
    }

    /**
     * Make a List of Locations covered in a line from one Location to the other.
     * @param loc1 The first Location
     * @param loc2 The second Location
     * @return A List of Locations in the region. Returns null if either Location is null or Locations are cross-world.
     */
    public static List<Location> makeLineRegion(Location loc1, Location loc2)
    {
        if (loc1 == null || loc2 == null || (loc1.getWorld() != loc2.getWorld()))
        {
            return null;
        }
        return makeLineRegion(loc1.toVector(), loc2.toVector(), loc1.getWorld());
    }

    /**
     * Make a List of Locations covered in a direct line between two points.
     * @param v1 The first Vector
     * @param v2 The second Vector
     * @param world The World the Locations should be in
     * @return A List of Locations in the region. Returns null if either Vector is null, or vectors are parallel and won't meet.
     */
    public static List<Location> makeLineRegion(Vector v1, Vector v2, World world)
    {
        if (v1 == null || v2 == null)
        {
            return null;
        }
        v2 = Locations.roundVector(v2);
        v1 = Locations.roundVector(v1);

        Vector test = v1.clone();
        if (test.crossProduct(v2) == new Vector())
        {
            return null; // These vectors will never meet as they are parallel.
        }
        List<Location> locations = new ArrayList<Location>();
        locations.add(v1.toLocation(world));
        double dX = Locations.deltaX(v1, v2);
        double dY = Locations.deltaY(v1, v2);
        double dZ = Locations.deltaZ(v1, v2);
        if (Double.isInfinite(dX) || Double.isNaN(dX))
        {
            dX = 0;
        }
        if (Double.isInfinite(dY) || Double.isNaN(dY))
        {
            dY = 0;
        }
        if (Double.isInfinite(dZ) || Double.isNaN(dZ))
        {
            dZ = 0;
        }
        // Vector change = new Vector (dX, dY, dZ);
        // Log.info("DECore", "Debug: Change: " + change.toString() + ". (" + v1.toString() + " to " + v2.toString() + ").");

        Vector iter = v1.clone();
        Vector increment = new Vector();
        if (dX == 0D && dY == 0D && dZ == 0D)
        {
            locations.add(v1.toLocation(world));
        }
        else
        {
            if (dX == 0D && dY == 0D && dZ != 0D)
            {
                if (dZ < 0D)
                {
                    increment = new Vector(0D, 0D, -1D);
                }
                else
                {
                    increment = new Vector(0D, 0D, 1D);
                }
            }
            else if (dX == 0D && dY != 0D && dZ == 0D)
            {
                if (dY < 0D)
                {
                    increment = new Vector(0D, -1D, 0D);
                }
                else
                {
                    increment = new Vector(0D, 1D, 0D);
                }
            }
            else if (dX != 0D && dY == 0D && dZ == 0D)
            {
                if (dX < 0D)
                {
                    increment = new Vector(-1D, 0D, 0D);
                }
                else
                {
                    increment = new Vector(1D, 0D, 0D);
                }
            }
            else
            {
                // Multiple changes... we need gradients here.
                /*
                 * Log.info("DECore", "Debug: Multiple changes: "
                 * + "XYGrad: " + Locations.findXYGradient(v1, v2) + ", "
                 * + "YXGrad: " + Locations.findYXGradient(v1, v2) + ", "
                 * + "XZGrad: " + Locations.findXZGradient(v1, v2) + ", "
                 * + "ZXGrad: " + Locations.findZXGradient(v1, v2) + ", "
                 * + "YZGrad: " + Locations.findYZGradient(v1, v2) + ", "
                 * + "ZYGrad: " + Locations.findZYGradient(v1, v2) + ". ");
                 */
                if (dX != 0D && dY != 0D && dZ == 0D)
                {
                    double X = dX < 0 ? (-Math.abs(Locations.findYXGradient(v1, v2))) : Math.abs(Locations.findYXGradient(v1, v2));
                    double Y = dY < 0 ? (-Math.abs(Locations.findXYGradient(v1, v2))) : Math.abs(Locations.findXYGradient(v1, v2));
                    increment = new Vector(X, Y, 0D);
                }
                else if (dX != 0D && dY == 0D && dZ != 0D)
                {
                    double X = dX < 0 ? (-Math.abs(Locations.findZXGradient(v1, v2))) : Math.abs(Locations.findZXGradient(v1, v2));
                    double Z = dZ < 0 ? (-Math.abs(Locations.findXZGradient(v1, v2))) : Math.abs(Locations.findXZGradient(v1, v2));
                    increment = new Vector(X, 0D, Z);
                }
                else if (dX == 0D && dY != 0D && dZ != 0D)
                {
                    double Y = dY < 0 ? (-Math.abs(Locations.findZYGradient(v1, v2))) : Math.abs(Locations.findZYGradient(v1, v2));
                    double Z = dZ < 0 ? (-Math.abs(Locations.findYZGradient(v1, v2))) : Math.abs(Locations.findYZGradient(v1, v2));
                    increment = new Vector(0D, Y, Z);
                }
                else
                {
                    double X = dX < 0 ? (-Math.abs(Locations.findYXGradient(v1, v2))) : Math.abs(Locations.findYXGradient(v1, v2));
                    double Y = dY < 0 ? (-Math.abs(Locations.findZYGradient(v1, v2))) : Math.abs(Locations.findZYGradient(v1, v2));
                    double Z = dZ < 0 ? (-Math.abs(Locations.findXZGradient(v1, v2))) : Math.abs(Locations.findXZGradient(v1, v2));
                    increment = new Vector(X, Y, Z);
                }
            }
        }
        // Log.info("DECore", "Debug: Using increment vector: " + increment.toString());

        while (!vectorsAreEqual(iter.add(increment), v2) && locations.size() <= 10)
        {
            Location roundLocation = Locations.truncateLocation(iter.toLocation(world));
            locations.add(roundLocation);
            // Log.info("DECore", "Debug: Adding " + Locations.toStr(roundLocation));
        }
        if (locations.size() == 11)
        {
            locations.clear();
        }
        return locations;
    }

    /**
     * Make a Region using two location strings in form "x, y, z, yaw, pitch, worldname". World friendly.
     * @param loc1 The first Location
     * @param loc2 The second Location
     * @return A List of Locations in the region. Returns null if either Location is null or Locations are cross-world.
     */
    public static List<Location> makeRegion(String loc1, String loc2)
    {
        Location l1 = makeLocation(loc1);
        Location l2 = makeLocation(loc2);
        return makeRegion(l1, l2);
    }

    /**
     * Make a Region using two locations. World friendly.
     * @param loc1 The first Location
     * @param loc2 The second Location
     * @return A List of Locations in the region. Returns null if either Location is null or Locations are cross-world.
     */
    public static List<Location> makeRegion(Location loc1, Location loc2)
    {
        if (loc1 == null || loc2 == null || (loc1.getWorld() != loc2.getWorld()))
        {
            return null;
        }
        World world = loc1.getWorld();
        int x1 = loc1.getBlockX();
        int y1 = loc1.getBlockY();
        int z1 = loc1.getBlockZ();
        int x2 = loc2.getBlockX();
        int y2 = loc2.getBlockY();
        int z2 = loc2.getBlockZ();
        if (x1 > x2)
        { // Assign the largest number to (x/y/z)2
            int temp = x1;
            x1 = x2;
            x2 = temp;
        }
        if (y1 > y2)
        {
            int temp = y1;
            y1 = y2;
            y2 = temp;
        }
        if (z1 > z2)
        {
            int temp = z1;
            z1 = z2;
            z2 = temp;
        }
        List<Location> locations = new ArrayList<Location>();
        int x3 = loc1.getBlockX();
        int y3 = loc1.getBlockY();
        int z3 = loc1.getBlockZ();
        while (y3 <= y2)
        {
            while (z3 <= z2)
            {
                while (x3 <= x2)
                {
                    Location temploc = new Location(world, x3, y3, z3);
                    locations.add(temploc);
                    x3 += 1;
                }
                z3 += 1;
                x3 = x1;
            }
            y3 += 1;
            x3 = x1;
            z3 = z1;
        }
        return locations;
    }

    /**
     * Calculate the change in X. Assumes v1 is going to v2.
     * @param v1 The first Vector
     * @param v2 The second Vector
     * @return The change, or NaN if either Vector is null.
     */
    public static double deltaX(Vector v1, Vector v2)
    {
        try
        {
            return (v2.getX() - v1.getX());
        }
        catch (Exception ex)
        {
            return Double.NaN;
        }
    }

    /**
     * Calculate the change in Y. Assumes v1 is going to v2.
     * @param v1 The first Vector
     * @param v2 The second Vector
     * @return The change, or NaN if either Vector is null.
     */
    public static double deltaY(Vector v1, Vector v2)
    {
        try
        {
            return (v2.getY() - v1.getY());
        }
        catch (Exception ex)
        {
            return Double.NaN;
        }
    }

    /**
     * Calculate the change in Z. Assumes v1 is going to v2.
     * @param v1 The first Vector
     * @param v2 The second Vector
     * @return The change, or NaN if either Vector is null.
     */
    public static double deltaZ(Vector v1, Vector v2)
    {
        try
        {
            return (v2.getZ() - v1.getZ());
        }
        catch (Exception ex)
        {
            return Double.NaN;
        }
    }

    /**
     * Find the Gradient between two points on the 2D-plane, x and y. Assumes v1 is going to v2. <br>
     * ΔY / ΔX
     * @param v1 The first Vector
     * @param v2 The second Vector
     * @return The gradient, or NaN if either Vector is null.
     */
    public static double findXYGradient(Vector v1, Vector v2)
    {
        try
        {
            return (deltaY(v1, v2) / deltaX(v1, v2));
        }
        catch (Exception ex)
        {
            return Double.NaN;
        }
    }

    /**
     * Find the Gradient between two points on the 2D-plane, y and x. Assumes v1 is going to v2.<br>
     * ΔX / ΔY
     * @param v1 The first Vector
     * @param v2 The second Vector
     * @return The gradient, or NaN if either Vector is null.
     */
    public static double findYXGradient(Vector v1, Vector v2)
    {
        try
        {
            return (deltaX(v1, v2) / deltaY(v1, v2));
        }
        catch (Exception ex)
        {
            return Double.NaN;
        }
    }

    /**
     * Find the Gradient between two points on the 2D-plane, x and z. Assumes v1 is going to v2.<br>
     * ΔZ / ΔX
     * @param v1 The first Vector
     * @param v2 The second Vector
     * @return The gradient, or NaN if either Vector is null.
     */
    public static double findXZGradient(Vector v1, Vector v2)
    {
        try
        {
            return (deltaZ(v1, v2) / deltaX(v1, v2));
        }
        catch (Exception ex)
        {
            return Double.NaN;
        }
    }

    /**
     * Find the Gradient between two points on the 2D-plane, z and x. Assumes v1 is going to v2.<br>
     * ΔX / ΔZ
     * @param v1 The first Vector
     * @param v2 The second Vector
     * @return The gradient, or NaN if either Vector is null.
     */
    public static double findZXGradient(Vector v1, Vector v2)
    {
        try
        {
            return (deltaX(v1, v2) / deltaZ(v1, v2));
        }
        catch (Exception ex)
        {
            return Double.NaN;
        }
    }

    /**
     * Find the Gradient between two points on the 2D-plane, y and z. Assumes v1 is going to v2.<br>
     * ΔZ / ΔY
     * @param v1 The first Vector
     * @param v2 The second Vector
     * @return The gradient, or NaN if either Vector is null.
     */
    public static double findYZGradient(Vector v1, Vector v2)
    {
        try
        {
            return (deltaZ(v1, v2) / deltaY(v1, v2));
        }
        catch (Exception ex)
        {
            return Double.NaN;
        }
    }

    /**
     * Find the Gradient between two points on the 2D-plane, z and y. Assumes v1 is going to v2.<br>
     * ΔY / ΔZ
     * @param v1 The first Vector
     * @param v2 The second Vector
     * @return The gradient, or NaN if either Vector is null.
     */
    public static double findZYGradient(Vector v1, Vector v2)
    {
        try
        {
            return (deltaY(v1, v2) / deltaZ(v1, v2));
        }
        catch (Exception ex)
        {
            return Double.NaN;
        }
    }

    /**
     * Change the X value of the Location
     * @param loc The Location to Change
     * @param change How much to change X by
     * @return A clone of the Location
     */
    public static Location changeX(Location loc, double change)
    {
        double x = loc.getX();
        x += change;
        loc.setX(x);
        return loc.clone();
    }

    /**
     * Change the Y value of the Location
     * @param loc The Location to Change
     * @param change How much to change Y by
     * @return A clone of the Location
     */
    public static Location changeY(Location loc, double change)
    {
        double y = loc.getY();
        y += change;
        loc.setY(y);
        return loc.clone();
    }

    /**
     * Change the Z value of the Location
     * @param loc The Location to Change
     * @param change How much to change Z by
     * @return A clone of the Location
     */
    public static Location changeZ(Location loc, double change)
    {
        double z = loc.getZ();
        z += change;
        loc.setZ(z);
        return loc.clone();
    }

    /**
     * Makes a location within the world boarders, specified by the world's worldboarder (if available), else 2500.
     * It generates a y-coordinate of 90, and randomises x and z.
     * @param world The World the location will be in
     * @return The New Location
     */
    public static Location makeRandomLocation(World world)
    {
        Random rand = new Random();
        int x, z;
        if (DEConfigOptions.boarders.containsKey(world.getName()))
        {
            WorldBoarder wb = DEConfigOptions.boarders.get(world.getName());
            x = rand.nextInt(wb.Xmax);
            rand = new Random();
            z = rand.nextInt(wb.Zmax);
        }
        else
        {
            x = rand.nextInt(2500);
            z = rand.nextInt(2500);
        }

        boolean negative = rand.nextBoolean();
        x = negative ? -x : x;
        negative = rand.nextBoolean();
        z = negative ? -z : z;

        return new Location(world, x, 90, z);
    }

    /**
     * Makes a location within this chunk.
     * It generates a y-coordinate of 90, and randomises x and z by adding a number between 0-15 to the origin of the chunk.
     * @param chunk The Chunk the location will be in
     * @return The New Location
     */
    public static Location makeRandomLocation(Chunk chunk)
    {
        Random rand = new Random();
        int x = rand.nextInt(16); // 16x128x16 blocks in one chunk
        x += chunk.getX(); // Change x by the random number.

        rand = new Random();
        int z = rand.nextInt(16);
        z += chunk.getZ();

        return new Location(chunk.getWorld(), x, 90, z);
    }

    /**
     * Makes a location round this location, max of maxchange away (both x and z, can be negative).
     * It generates a y-coordinate of 90, and randomises x and z by adding a number between -maxchange -> +maxchange to the location.
     * @param location The reference point for the new Location
     * @param maxchange The maximum change the x and z can have.
     * @return The New Location
     */
    public static Location makeRandomLocation(Location location, int maxchange)
    {
        Random rand = new Random();
        int x = rand.nextInt(maxchange);

        boolean negative = rand.nextBoolean();
        x = negative ? -x : x;

        int z = rand.nextInt(maxchange);
        negative = rand.nextBoolean();
        z = negative ? -z : z;

        return new Location(location.getWorld(), x, 90, z);
    }

    /**
     * Get the location this player's cross-hair is pointing at.
     * @param player The Player
     * @return The Location the player is pointing at
     */
    public static Location getCursorLocation(Player player)
    {
        return player.getLastTwoTargetBlocks(null, 100).get(1).getLocation();
    }

    /**
     * Get the location this player is pointing at plus 1 Y.
     * @param player The Player
     * @return The Location that is above where the player is pointing at
     */
    public static Location getAboveCursorLocation(Player player)
    {
        return player.getLastTwoTargetBlocks(null, 100).get(1).getLocation().add(0, 1, 0);
    }

    /** Locations Util class - make constructor private */
    private Locations()
    {
    }
}
