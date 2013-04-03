package net.deviantevil.utilities;

import java.util.Arrays;
import java.util.List;
import net.deviantevil.worldeditapi.WESelection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

/** 
 * Various relative faces functions
 * @author Kristian ("kjhf")
 * @author Cole Erickson
 * @see BlockFace
 */
public class BlockFaces {
    /** List of BlockFaces */
    private static final List<BlockFace> basicFaces = Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);

    /** Gets the face with an adjacent wall sign on it. 
     * @param block The Block to search from.
     * @return The BlockFace with the wallsign. If none found, null. */
    public static BlockFace getAdjacentSignFace(Block block) {
        BlockFace direction = null;
        for (BlockFace each : basicFaces) {
            if (block.getRelative(each).getType() != Material.WALL_SIGN) {
                continue;
            }
            direction = each;
        }
        return direction;
    }

    /** Gets the BlockFace of the block specified closest to the Location specified. e.g. make loc the player's location.
     * @param block The Block to search from.
     * @param loc The location to search from.
     * @return The found BlockFace. */
    public static BlockFace getBlockFaceInFront(Block block, Location loc) {
        loc.setY(block.getY()); // Make sure the relative is on the same level for basic faces.
        double distValue = Double.POSITIVE_INFINITY;
        BlockFace direction = null;
        for (BlockFace each : basicFaces) {
            double holder = block.getRelative(each).getLocation().distanceSquared(loc);
            if (holder < distValue) {
                distValue = holder;
                direction = each;
            }
        }
        return direction;
    }

    /** Gets the BlockFace of the blocks in the WorldEdit selection specified closest to the Location specified. e.g. make loc the player's location.
     * @param srSelection The WorldEdit selection to search from.
     * @param loc The location to search from.
     * @return The found BlockFace. */
    public static BlockFace getBlockFaceInFront(WESelection srSelection, Location loc) {
        double distValue = Double.POSITIVE_INFINITY;
        BlockFace direction = null;
        for (BlockFace each : basicFaces) {
            double holder = srSelection.getCenterOfFace(each).distanceSquared(loc);
            if (holder < distValue) {
                distValue = holder;
                direction = each;
            }
        }
        return direction;
    }

    /** Gets the BlockFace of the block specified furthest from the Location specified. e.g. make loc the player's location.
     * @param block The Block to search from.
     * @param loc The location to search from.
     * @return The found BlockFace. */
    public static BlockFace getBlockFaceBehind(Block block, Location loc) {
        loc.setY(block.getY()); // Make sure the relative is on the same level for basic faces.
        double distValue = Double.NEGATIVE_INFINITY;
        BlockFace direction = null;
        for (BlockFace each : basicFaces) {
            double holder = block.getRelative(each).getLocation().distanceSquared(loc);
            if (holder > distValue) {
                distValue = holder;
                direction = each;
            }
        }
        return direction;
    }

    /** Gets the BlockFace of the blocks in the WorldEdit selection specified furthest from the Location specified. e.g. make loc the player's location.
     * @param srSelection The WorldEdit selection to search from.
     * @param loc The location to search from.
     * @return The found BlockFace. */
    public static BlockFace getBlockFaceBehind(WESelection srSelection, Location loc) {
        double distValue = Double.NEGATIVE_INFINITY;
        BlockFace direction = null;
        for (BlockFace each : basicFaces) {
            double holder = srSelection.getCenterOfFace(each).distanceSquared(loc);
            if (holder > distValue) {
                distValue = holder;
                direction = each;
            }
        }
        return direction;
    }

    /** Get the player's facing direction as a BlockFace */
    public static BlockFace getPlayerFacingDirection(Player player) {
        BlockFace dir = null;
        float y = player.getLocation().getYaw();

        if (y < 0) {
            y += 360;
        }

        y %= 360;

        int i = (int) ((y + 8) / 22.5);

        switch (i) {
            case 0: 
                dir = BlockFace.WEST;
                break;
            case 1: 
                dir = BlockFace.WEST_NORTH_WEST;
                break;
            case 2: 
                dir = BlockFace.NORTH_WEST;
                break;
            case 3: 
                dir = BlockFace.NORTH_NORTH_WEST;
                break;
            case 4: 
                dir = BlockFace.NORTH;
                break;
            case 5: 
                dir = BlockFace.NORTH_NORTH_EAST;
                break;
            case 6: 
                dir = BlockFace.NORTH_EAST;
                break;
            case 7: 
                dir = BlockFace.EAST_NORTH_EAST;
                break;
            case 8: 
                dir = BlockFace.EAST;
                break;
            case 9: 
                dir = BlockFace.EAST_SOUTH_EAST;
                break;
            case 10: 
                dir = BlockFace.SOUTH_EAST;
                break;
            case 11: 
                dir = BlockFace.SOUTH_SOUTH_EAST;
                break;
            case 12: 
                dir = BlockFace.SOUTH;
                break;
            case 13: 
                dir = BlockFace.SOUTH_SOUTH_WEST;
                break;
            case 14: 
                dir = BlockFace.SOUTH_WEST;
                break;
            case 15: 
                dir = BlockFace.WEST_SOUTH_WEST;
                break;
            default:
                dir = BlockFace.WEST;
                break;
        }
        
        return dir;
    }
    
    /** Get the player's facing direction as a BlockFace, limiting options to NORTH, WEST, SOUTH and EAST */
    public static BlockFace getPlayerFacingDirectionSimple(Player player) {
        BlockFace dir = null;
        float y = player.getLocation().getYaw();

        if (y < 0) {
            y += 360;
        }

        y %= 360;

        int i = (int) ((y + 2) / 90);

        switch (i) {
            case 0: 
                dir = BlockFace.WEST;
                break;
            case 1: 
                dir = BlockFace.NORTH;
                break;
            case 2: 
                dir = BlockFace.EAST;
                break;
            case 3: 
                dir = BlockFace.SOUTH;
                break;
        }
        
        return dir;
    }
}
