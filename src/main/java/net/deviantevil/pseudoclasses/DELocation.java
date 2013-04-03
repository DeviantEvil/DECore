package net.deviantevil.pseudoclasses;
import net.deviantevil.decore.DECore;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Simplistic Location class.
 * @author Kristian
 * @see Location
 */
public class DELocation {
    /** The Worldname this Location is in */
    private String worldname;
    
    /** The X block co-ordinate */
    private int x;
    
    /** The Y block co-ordinate */
    private int y;
    
    /** The Z block co-ordinate */
    private int z;

    /** Get this Location's worldname */
    public String getWorldname () {
        return this.worldname;
    }

    /** Set this Location's worldname */
    public void setWorldname (String worldname) {
        this.worldname = worldname;
    }

    /** Get the X co-ordinate of this Location */
    public int getX () {
        return this.x;
    }

    /** Set the X co-ordinate of this Location */
    public void setX (int x) {
        this.x = x;
    }

    /** Get the Y co-ordinate of this Location */
    public int getY () {
        return this.y;
    }
    
    /** Set the Y co-ordinate of this Location */
    public void setY (int y) {
        this.y = y;
    }

    /** Get the Z co-ordinate of this Location */
    public int getZ () {
        return this.z;
    }

    /** Set the Z co-ordinate of this Location */
    public void setZ (int z) {
        this.z = z;
    }

    /** Make a DELocation using its fields */
    public DELocation (String worldname, int x, int y, int z) {
        this.worldname = worldname;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /** Make a DELocation using a Location */
    public DELocation (Location loc) {
        this.worldname = loc.getWorld().getName();
        this.x = loc.getBlockX();
        this.y = loc.getBlockY();
        this.z = loc.getBlockZ();
    }
    
    /** Fetch a world from the server using the DELocation's worldname. Returns null if not found. */
    public World getWorld() {
        try {
            return DECore.getDECore().getServer().getWorld(this.worldname); 
        } catch (Exception ex) {
            return null;
        }
    }
    
    /** Convert back to a Location. Returns null if world wasn't found. */
    public Location toLocation() {
        try {
            return new Location (DECore.getDECore().getServer().getWorld(this.worldname), this.x, this.y, this.z); 
        } catch (Exception ex) {
            return null;
        }
    }
    
    /** Convert a DELocation back to a Location. Returns null if world wasn't found. */
    public static Location toLocation(DELocation pseudoLocation) {
        try {
            return new Location (DECore.getDECore().getServer().getWorld(pseudoLocation.getWorldname()), pseudoLocation.getX(), pseudoLocation.getY(), pseudoLocation.getZ()); 
        } catch (Exception ex) {
            return null;
        }
    }
    
    @Override
    public String toString() {
        return "[ " + this.x + " | " + this.y + " | " + this.z + " ] (" + this.worldname + ")"; 
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        final DELocation otherLoc = (DELocation)obj;
        return this.x == otherLoc.x && 
               this.y == otherLoc.y &&
               this.z == otherLoc.z && 
               this.worldname.equals(otherLoc.worldname);
    }
    
    @Override
    public int hashCode() {
         int hash = 5; 
         hash = 17 * hash + (this.getWorld() != null ? this.getWorld().hashCode() : 0);
         hash = 17 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
         hash = 17 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
         hash = 17 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
         return hash;
     }
}
