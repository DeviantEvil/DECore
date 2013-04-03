package net.deviantevil.pseudoclasses;
import net.deviantevil.decore.DECore;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

/** Simple Block class. */
public class DEBlock {
    
    /** The type of Block this is from its TypeId */
    private int typeid;
    
    /** The block's meta data */
    private byte data;
    
    /** The location of this Block */
    private DELocation location;

    /** Get the Block's type id */
    public int getTypeid () {
        return this.typeid;
    }

    /** Set the Block's type id */
    public void setTypeid (int typeid) {
        this.typeid = typeid;
    }
    
    /** Get the Block's data value */
    public byte getDurability () {
        return this.data;
    }

    /** Set the Block's data value */
    public void setData (byte data) {
        this.data = data;
    }
    
    /** Get the Block's location */
    public DELocation getLocation () {
        return this.location;
    }

    /** Set the Block's location */
    public void setLocation (DELocation location) {
        this.location = location;
    }
    
    /** Make a DEBlock with the type ID and location. Assumes no extra data. */
    public DEBlock (int typeid, DELocation location) {
        this(typeid, (byte)0, location);
    }
    
    /** Make a DEBlock with the type ID, durability data, and location. */
    public DEBlock (int typeid, byte data, DELocation location) {
        this.typeid = typeid;
        this.data = data;
        this.location = location;
    }
    
    /** Make a DEBlock using a Block */
    public DEBlock (Block block) {
        this.typeid = block.getTypeId();
        this.data = block.getData();
        this.location = new DELocation(block.getLocation());
    }
    
    /** Make a DEBlock using a BlockState */
    public DEBlock (BlockState state) {
        this.typeid = state.getTypeId();
        this.data = state.getData().getData();
        this.location = new DELocation(state.getWorld().getName(), state.getX(), state.getY(), state.getZ());
    }
    
    /** Convert back to a Block. */
    public Block toBlock() {
        return DECore.getDECore().getServer().getWorld(this.location.getWorldname()) == null ? 
                null : 
                DECore.getDECore().getServer().getWorld(this.location.getWorldname()).getBlockAt(this.location.getX(), this.location.getY(), this.location.getZ());
    }
    
    /** Convert back to to a Block. */
    public static Block toBlock(DEBlock block) {
        return DECore.getDECore().getServer().getWorld(block.getLocation().getWorldname()) == null ? 
                null : 
                DECore.getDECore().getServer().getWorld(block.getLocation().getWorldname()).getBlockAt(block.getLocation().getX(), block.getLocation().getY(), block.getLocation().getZ());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        final DEBlock otherBlock = (DEBlock)obj;
        return this.data == otherBlock.data && 
               this.location.equals(otherBlock.location) &&
               this.typeid == otherBlock.typeid;
    }
    
    @Override
    public int hashCode() {
         int hash = 3; 
         hash = 23 * hash + this.location.hashCode();
         hash = 23 * hash + this.data ^ this.data >>> 32;
         hash = 23 * hash + this.typeid ^ this.typeid >>> 32;
         return hash;
     }
}
