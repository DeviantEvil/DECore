package net.deviantevil.pseudoclasses;
import org.bukkit.inventory.ItemStack;

/** Simple ItemStack class. Does not hold additional data such as Enchantments, damage shorts, or inventories.  */
public class DEItem {
    
    /** The type of Item this is from its TypeId */
    private int typeid;
    
    /** The amount of this Item */
    private int amount;
    
    /** The item's durability data */
    private short durability;

    /** Get the number of Items in this stack */
    public int getAmount () {
        return this.amount;
    }

    /** Set the number of Items in this stack */
    public void setAmount (int amount) {
        this.amount = amount;
    }

    /** Get the Item's data value */
    public short getDurability () {
        return this.durability;
    }

    /** Set the Item's data value */
    public void setData (short durability) {
        this.durability = durability;
    }

    /** Get the Item's type id */
    public int getTypeid () {
        return this.typeid;
    }

    /** Set the Item's type id */
    public void setTypeid (int typeid) {
        this.typeid = typeid;
    }

    /** Make a DEItem with only the type ID. Assumes one Item with no extra data. */
    public DEItem (int typeid) {
        this(typeid, 1, (short)0);
    }
    
    /** Make a DEItem with the type ID and durability. Assumes one Item. */
    public DEItem (int typeid, short durability) {
        this(typeid, 1, durability);
    }
    
    /** Make a DEItem with the type ID and amount with no extra data. */
    public DEItem (int typeid, int amount) {
        this(typeid, amount, (short)0);
    }
    
    /** Make a DEItem with type ID, amount, and a durability value. */
    public DEItem (int typeid, int amount, short durability) {
        this.typeid = typeid;
        this.amount = amount;
        this.durability = durability;
    }
    
    /** Make a DEItem using an ItemStack */
    public DEItem (ItemStack item) {
        this.typeid = item.getTypeId();
        this.amount = item.getAmount();
        this.durability = item.getDurability();
    }
    
    /** Convert back to an ItemStack. */
    public ItemStack toItemStack() {
        return new ItemStack(this.typeid, this.amount, this.durability);
    }
    
    /** Convert back to an ItemStack. */
    public static ItemStack toItemStack(DEItem item) {
        return new ItemStack(item.getTypeid(), item.getAmount(), item.getDurability());
    }
    
}
