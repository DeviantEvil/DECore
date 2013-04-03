package net.deviantevil.worldeditapi;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.RegionSelector;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
/**
 *
 * @author Cole Erickson
 */
public class WESelection extends CuboidSelection {
    
    public WESelection(World world, Location pt1, Location pt2) {
        super(world, pt1, pt2);
    }
    
    public WESelection(World world, RegionSelector sel, CuboidRegion region) {
        super(world, sel, region);
    }
    
    public WESelection(World world, Vector pt1, Vector pt2) {
        super(world, pt1, pt2);
    }
    
    public Location getCorner(Corner corner) {
        switch (corner) {
            case UPPER_NORTHEASTERN:
                return getMinimumPoint().add(0, getHeight() - 1, 0);
            case UPPER_NORTHWESTERN:
                return getMaximumPoint().subtract(getWidth() - 1, 0, 0);
            case UPPER_SOUTHEASTERN:
                return getMaximumPoint().subtract(0, 0, getLength() - 1);
            case UPPER_SOUTHWESTERN:
                return getMaximumPoint();
                
            case LOWER_NORTHEASTERN:
                return getMinimumPoint();
            case LOWER_NORTHWESTERN:
                return getMinimumPoint().add(0, 0, getLength() - 1);
            case LOWER_SOUTHEASTERN:
                return getMinimumPoint().add(getWidth() - 1, 0, 0);
            case LOWER_SOUTHWESTERN:
                return getMaximumPoint().subtract(0, getHeight() - 1, 0);
                
            default:
                return null;
        }
    }
    
    public Block getCornerBlock(Corner corner) {
        switch (corner) {
            case UPPER_NORTHEASTERN:
                return getMinimumPoint().add(0, getHeight() - 1, 0).getBlock();
            case UPPER_NORTHWESTERN:
                return getMaximumPoint().subtract(getWidth() - 1, 0, 0).getBlock();
            case UPPER_SOUTHEASTERN:
                return getMaximumPoint().subtract(0, 0, getLength() - 1).getBlock();
            case UPPER_SOUTHWESTERN:
                return getMaximumPoint().getBlock();
                
            case LOWER_NORTHEASTERN:
                return getMinimumPoint().getBlock();
            case LOWER_NORTHWESTERN:
                return getMinimumPoint().add(0, 0, getLength() - 1).getBlock();
            case LOWER_SOUTHEASTERN:
                return getMinimumPoint().add(getWidth() - 1, 0, 0).getBlock();
            case LOWER_SOUTHWESTERN:
                return getMaximumPoint().subtract(0, getHeight() - 1, 0).getBlock();
                
            default:
                return null;
        }
    }
    
    public Selection getFace(BlockFace face) {
        switch (face) {
            case UP:
                return new CuboidSelection(getWorld(), getCorner(Corner.UPPER_NORTHEASTERN), getCorner(Corner.UPPER_SOUTHWESTERN));
            case DOWN:
                return new CuboidSelection(getWorld(), getCorner(Corner.LOWER_NORTHEASTERN), getCorner(Corner.LOWER_SOUTHWESTERN));
            case NORTH:
                return new CuboidSelection(getWorld(), getCorner(Corner.UPPER_NORTHEASTERN), getCorner(Corner.LOWER_NORTHWESTERN));
            case EAST:
                return new CuboidSelection(getWorld(), getCorner(Corner.UPPER_NORTHEASTERN), getCorner(Corner.LOWER_SOUTHEASTERN));
            case SOUTH:
                return new CuboidSelection(getWorld(), getCorner(Corner.UPPER_SOUTHEASTERN), getCorner(Corner.LOWER_SOUTHWESTERN));
            case WEST:
                return new CuboidSelection(getWorld(), getCorner(Corner.UPPER_NORTHWESTERN), getCorner(Corner.LOWER_SOUTHWESTERN));
            default:
                return null;           
        }
    }
    
    public Location getCenterOfFace(BlockFace face) {
         switch (face) {
            case UP:
                return getCorner(Corner.UPPER_NORTHEASTERN).add(getCorner(Corner.UPPER_SOUTHWESTERN)).multiply(0.5).add(0, 1, 0);
            case DOWN:
                return getCorner(Corner.LOWER_NORTHEASTERN).add(getCorner(Corner.LOWER_SOUTHWESTERN)).multiply(0.5).add(0, -1, 0);
            case NORTH:
                return getCorner(Corner.UPPER_NORTHEASTERN).add(getCorner(Corner.LOWER_NORTHWESTERN)).multiply(0.5).add(1, 0, 0);
            case EAST:
                return getCorner(Corner.UPPER_NORTHEASTERN).add(getCorner(Corner.LOWER_SOUTHEASTERN)).multiply(0.5).add(0, 0, 1);
            case SOUTH:
                return getCorner(Corner.UPPER_SOUTHEASTERN).add(getCorner(Corner.LOWER_SOUTHWESTERN)).multiply(0.5).add(-1, 0, 0);
            case WEST:
                return getCorner(Corner.UPPER_NORTHWESTERN).add(getCorner(Corner.LOWER_SOUTHWESTERN)).multiply(0.5).add(0, 0, -1);
            default:
                return null;           
        }
    }
}
