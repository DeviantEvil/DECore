package net.deviantevil.utilities;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import net.deviantevil.decore.DECore;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * A library of Player Inventory functions and some Material functions too.
 * Some bits were lovingly ripped from MobArena (by garbagemule)
 * 
 * @author Kristian ("kjhf")
 */
public final class Inventories
{
    public static final List<Material> ArmoursList = Arrays.asList(
            (Material.DIAMOND_HELMET),
            (Material.GOLD_HELMET),
            (Material.IRON_HELMET),
            (Material.LEATHER_HELMET),
            (Material.CHAINMAIL_HELMET),
            (Material.PUMPKIN),
            (Material.DIAMOND_CHESTPLATE),
            (Material.GOLD_CHESTPLATE),
            (Material.IRON_CHESTPLATE),
            (Material.LEATHER_CHESTPLATE),
            (Material.CHAINMAIL_CHESTPLATE),
            (Material.DIAMOND_LEGGINGS),
            (Material.GOLD_LEGGINGS),
            (Material.IRON_LEGGINGS),
            (Material.LEATHER_LEGGINGS),
            (Material.CHAINMAIL_LEGGINGS),
            (Material.DIAMOND_BOOTS),
            (Material.GOLD_BOOTS),
            (Material.IRON_BOOTS),
            (Material.LEATHER_BOOTS),
            (Material.CHAINMAIL_BOOTS)
            );
    public static final List<Material> AxesList = Arrays.asList(
            (Material.WOOD_AXE),
            (Material.STONE_AXE),
            (Material.IRON_AXE),
            (Material.GOLD_AXE),
            (Material.DIAMOND_AXE)
            );
    public static final List<Material> BootsList = Arrays.asList(
            (Material.DIAMOND_BOOTS),
            (Material.GOLD_BOOTS),
            (Material.IRON_BOOTS),
            (Material.LEATHER_BOOTS),
            (Material.CHAINMAIL_BOOTS)
            );
    public static final List<Material> ChestplateList = Arrays.asList(
            (Material.DIAMOND_CHESTPLATE),
            (Material.GOLD_CHESTPLATE),
            (Material.IRON_CHESTPLATE),
            (Material.LEATHER_CHESTPLATE),
            (Material.CHAINMAIL_CHESTPLATE)
            );
    public static final List<Material> DiamondList = Arrays.asList(
            (Material.DIAMOND_HELMET),
            (Material.DIAMOND_CHESTPLATE),
            (Material.DIAMOND_LEGGINGS),
            (Material.DIAMOND_BOOTS),
            (Material.DIAMOND),
            (Material.DIAMOND_BLOCK),
            (Material.DIAMOND_SWORD),
            (Material.DIAMOND_SPADE),
            (Material.DIAMOND_PICKAXE),
            (Material.DIAMOND_AXE),
            (Material.DIAMOND_HOE)
            );
    public static final List<Material> HelmetList = Arrays.asList(
            (Material.DIAMOND_HELMET),
            (Material.GOLD_HELMET),
            (Material.IRON_HELMET),
            (Material.LEATHER_HELMET),
            (Material.CHAINMAIL_HELMET),
            (Material.PUMPKIN)
            );
    public static final List<Material> HoesList = Arrays.asList(
            (Material.WOOD_HOE),
            (Material.STONE_HOE),
            (Material.IRON_HOE),
            (Material.GOLD_HOE),
            (Material.DIAMOND_HOE)
            );
    public static final List<Material> LeggingsList = Arrays.asList(
            (Material.DIAMOND_LEGGINGS),
            (Material.GOLD_LEGGINGS),
            (Material.IRON_LEGGINGS),
            (Material.LEATHER_LEGGINGS),
            (Material.CHAINMAIL_LEGGINGS)
            );
    public static final List<Material> PickaxesList = Arrays.asList(
            (Material.WOOD_PICKAXE),
            (Material.STONE_PICKAXE),
            (Material.IRON_PICKAXE),
            (Material.GOLD_PICKAXE),
            (Material.DIAMOND_PICKAXE)
            );
    public static final List<Material> ShovelsList = Arrays.asList(
            (Material.WOOD_SPADE),
            (Material.STONE_SPADE),
            (Material.IRON_SPADE),
            (Material.GOLD_SPADE),
            (Material.DIAMOND_SPADE)
            );
    public static final List<Material> SwordsList = Arrays.asList(
            (Material.WOOD_SWORD),
            (Material.STONE_SWORD),
            (Material.IRON_SWORD),
            (Material.GOLD_SWORD),
            (Material.DIAMOND_SWORD)
            );

    /**
     * Create an empty inventory.
     * @param p A Player
     * @return An empty Inventory.
     */
    public static PlayerInventory clearInventory(Player p)
    {
        PlayerInventory inv = p.getInventory();
        inv.clear();
        /*
         * inv.setHelmet(null);
         * inv.setChestplate(null);
         * inv.setLeggings(null);
         * inv.setBoots(null);
         */
        return inv;
    }

    /**
     * Helper method for equipping armour pieces.
     * Will not equip if the item is not an armour type.
     * @param stack The item to equip
     * @param inv The Player's Inventory.
     */
    public static void equipArmorPiece(ItemStack stack, PlayerInventory inv)
    {
        Material type = stack.getType();
        if (HelmetList.contains(type))
        {
            inv.setHelmet(stack);
        }
        else if (ChestplateList.contains(type))
        {
            inv.setChestplate(stack);
        }
        else if (LeggingsList.contains(type))
        {
            inv.setLeggings(stack);
        }
        else if (BootsList.contains(type))
        {
            inv.setBoots(stack);
        }
    }

    /**
     * Gives the player all of the items in the list of ItemStacks.
     * @param p The Player
     * @param stacks List of ItemStack to give
     */
    public static void giveItems(Player p, List<ItemStack> stacks)
    {
        if (stacks == null)
        {
            return;
        }

        PlayerInventory inv = p.getInventory();
        for (ItemStack stack : stacks)
        {
            if (stack == null)
            {
                continue;
            }

            // If this is an armour piece, equip it.
            if (ArmoursList.contains(stack.getType()))
            {
                equipArmorPiece(stack, inv);
                continue;
            }
            inv.addItem(stack);
        }
    }

    /**
     * Checks if all inventory and armour slots are empty.
     * @param p The Player
     * @return True if inventory is empty, else false.
     */
    public static boolean hasEmptyInventory(Player p)
    {
        ItemStack[] inventory = p.getInventory().getContents();
        ItemStack[] armor = p.getInventory().getArmorContents();

        // For inventory, check for null
        for (ItemStack stack : inventory)
        {
            if (stack != null)
            {
                return false;
            }
        }

        // For armour, check for id 0, or AIR
        for (ItemStack stack : armor)
        {
            if (stack.getTypeId() != 0)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets whether the player has a v2 saved inventory.
     * @param p The player
     * @return True if player has a save file <u>in the DECore directory</u>, else false.
     */
    public static boolean hasSavedYMLInventory(Player p)
    {
        return hasSavedYMLInventory(p.getName(), DECore.getDECore().getDataFolder());
    }

    /**
     * Gets whether the player has a v2 saved inventory.
     * @param p The player
     * @param dir The Plugin's save directory
     * @return True if player has a save file, else false.
     */
    public static boolean hasSavedYMLInventory(Player p, File dir)
    {
        return hasSavedYMLInventory(p.getName(), dir);
    }

    /**
     * Gets whether the player has a v2 saved inventory.
     * @param playername The playername [specifically, the name of the file minus the .txt extension].
     * @param dir The Plugin's save directory
     * @return True if player has a save file, else false.
     */
    public static boolean hasSavedYMLInventory(String playername, File dir)
    {
        return (new File(dir + "/" + playername + ".yml")).exists();
    }

    /**
     * Loads armour from inventory backup file.
     * @param p The player whose inventory file you wish to load [specifically, the name of the file minus the .yml extension].
     * @param dir The Directory
     * @return The inventory armour, if it exists.
     */
    public static ItemStack[] loadYMLArmour(Player p, File dir)
    {
        return loadYMLArmour(p.getName(), dir);
    }

    /**
     * Loads armour from inventory backup file.
     * @param playername The player whose inventory file you wish to load [specifically, the name of the file minus the .yml extension].
     * @param dir The Directory
     * @return The inventory armour, if it exists.
     */
    public static ItemStack[] loadYMLArmour(String playername, File dir)
    {
        boolean hasSavedInventory = hasSavedYMLInventory(playername, dir);
        if (!hasSavedInventory)
        {
            return null;
        }

        File playerinvfile = (new File(dir + "/" + playername + ".yml"));
        FileConfiguration yml = YamlConfiguration.loadConfiguration(playerinvfile);

        if (yml != null)
        {
            Set<String> keys;

            ItemStack Helmet = new ItemStack(yml.getInt("Armour.Helmet.Type", 0), 1, (short) yml.getInt("Armour.Helmet.Durability", 0));
            if (yml.getConfigurationSection("Armour.Helmet.Enchantments") != null)
            {
                keys = yml.getConfigurationSection("Armour.Helmet.Enchantments").getKeys(false);
                for (String str : keys)
                {
                    int parseInt = Integer.parseInt(str);
                    Helmet.addEnchantment(Enchantment.getById(parseInt), yml.getInt("Armour.Helmet.Enchantments." + str, 1));
                }
            }

            ItemStack Chestplate = new ItemStack(yml.getInt("Armour.Chestplate.Type", 0), 1, (short) yml.getInt("Armour.Chestplate.Durability", 0));
            if (yml.getConfigurationSection("Armour.Chestplate.Enchantments") != null)
            {
                keys = yml.getConfigurationSection("Armour.Chestplate.Enchantments").getKeys(false);
                for (String str : keys)
                {
                    int parseInt = Integer.parseInt(str);
                    Chestplate.addEnchantment(Enchantment.getById(parseInt), yml.getInt("Armour.Chestplate.Enchantments." + str, 1));
                }
            }

            ItemStack Leggings = new ItemStack(yml.getInt("Armour.Leggings.Type", 0), 1, (short) yml.getInt("Armour.Leggings.Durability", 0));
            if (yml.getConfigurationSection("Armour.Leggings.Enchantments") != null)
            {
                keys = yml.getConfigurationSection("Armour.Leggings.Enchantments").getKeys(false);
                for (String str : keys)
                {
                    int parseInt = Integer.parseInt(str);
                    Leggings.addEnchantment(Enchantment.getById(parseInt), yml.getInt("Armour.Leggings.Enchantments." + str, 1));
                }
            }

            ItemStack Boots = new ItemStack(yml.getInt("Armour.Boots.Type", 0), 1, (short) yml.getInt("Armour.Boots.Durability", 0));
            if (yml.getConfigurationSection("Armour.Boots.Enchantments") != null)
            {
                keys = yml.getConfigurationSection("Armour.Boots.Enchantments").getKeys(false);
                for (String str : keys)
                {
                    int parseInt = Integer.parseInt(str);
                    Boots.addEnchantment(Enchantment.getById(parseInt), yml.getInt("Armour.Boots.Enchantments." + str, 1));
                }
            }
            ItemStack[] armour = new ItemStack[4];
            armour[0] = Helmet;
            armour[1] = Chestplate;
            armour[2] = Leggings;
            armour[3] = Boots;
            return armour;
        }
        return null;
    }

    /**
     * Loads inventory contents from inventory backup file. No armour.
     * @param p The player whose inventory file you wish to load [specifically, the name of the file minus the .ymlextension].
     * @param dir The Directory
     * @return The inventory contents, if it exists.
     */
    public static ItemStack[] loadYMLContents(Player p, File dir)
    {
        return loadYMLContents(p.getName(), dir);
    }

    /**
     * Loads inventory contents from inventory backup file. No armour.
     * @param playername The player whose inventory file you wish to load [specifically, the name of the file minus the .txt extension].
     * @param dir The Directory
     * @return The inventory contents, if it exists.
     */
    public static ItemStack[] loadYMLContents(String playername, File dir)
    {
        if (!hasSavedYMLInventory(playername, dir))
        {
            return null;
        }

        File playerinvfile = (new File(dir + "/" + playername + ".yml"));
        FileConfiguration yml = YamlConfiguration.loadConfiguration(playerinvfile);

        if (yml != null)
        {
            ItemStack[] Contents = new ItemStack[36];
            for (int i = 0; i < 36; i++)
            {
                ItemStack slot = new ItemStack(yml.getInt("Inventory.Slot." + i + ".Type", 0), yml.getInt("Inventory.Slot." + i + ".Amount", 0),
                        (short) yml.getInt("Inventory.Slot." + i + ".Durability", 0));
                if (yml.getConfigurationSection("Inventory.Slot." + i + ".Enchantments") != null)
                {
                    Set<String> keys = yml.getConfigurationSection("Inventory.Slot." + i + ".Enchantments").getKeys(false);
                    for (String str : keys)
                    {
                        int parseInt = Integer.parseInt(str);
                        slot.addEnchantment(Enchantment.getById(parseInt), yml.getInt("Inventory.Slot." + i + ".Enchantments." + str, 1));
                    }
                }
                Contents[i] = slot;
            }
            return Contents;
        }
        return null;
    }

    /**
     * Helper methods for making ItemStacks out of strings and ints
     * Sets byte data to 0.
     * @param name The Material
     * @param amount The amount of material
     * @return A created ItemStack
     */
    public static ItemStack makeItemStack(String name, int amount)
    {
        return makeItemStack(name, amount, "0");
    }

    /**
     * Helper methods for making ItemStacks out of strings and ints
     * @param name The Material
     * @param amount The amount of material
     * @param data Any byte data
     * @return A created ItemStack
     */
    public static ItemStack makeItemStack(String name, int amount, String data)
    {
        try
        {
            byte offset = 0;
            Material material = (name.matches("[0-9]+"))
                    ? Material.getMaterial(Integer.parseInt(name))
                    : Material.valueOf(name.toUpperCase());

            if (material == Material.INK_SACK)
            {
                offset = 15;
            }

            DyeColor dye = (data.matches("[0-9]+"))
                    ? DyeColor.getByDyeData((byte) Math.abs(offset - Integer.parseInt(data)))
                    : DyeColor.valueOf(data.toUpperCase());

            return new ItemStack(material, amount, (byte) Math.abs(offset - dye.getDyeData()));
        }
        catch (Exception e)
        {
            DECore.log.severe("Could not create item \"" + name + "\".");
            return null;
        }
    }

    /**
     * Give an item in the player's hand all the enchantments it can hold
     * @param player The Player that needs the enchantments.
     * @return Success
     */
    public static boolean makeSuper(Player player)
    {
        switch (player.getItemInHand().getType())
        {
            case BOW:
                try
                {
                    player.getItemInHand().addEnchantment(Enchantment.ARROW_DAMAGE, 5);
                    player.getItemInHand().addEnchantment(Enchantment.ARROW_FIRE, 1);
                    player.getItemInHand().addEnchantment(Enchantment.ARROW_INFINITE, 1);
                    player.getItemInHand().addEnchantment(Enchantment.ARROW_KNOCKBACK, 2);
                    return true;
                }
                catch (Exception ex)
                {
                    player.sendMessage(ChatColor.DARK_AQUA + "Oh dear, something went wrong.");
                    player.sendMessage(ChatColor.DARK_AQUA + "" + ex.getLocalizedMessage());
                    return false;
                }
            case WOOD_AXE:
            case STONE_AXE:
            case IRON_AXE:
            case GOLD_AXE:
            case DIAMOND_AXE:
            case WOOD_PICKAXE:
            case STONE_PICKAXE:
            case IRON_PICKAXE:
            case GOLD_PICKAXE:
            case DIAMOND_PICKAXE:
            case WOOD_SPADE:
            case STONE_SPADE:
            case IRON_SPADE:
            case GOLD_SPADE:
            case DIAMOND_SPADE:
                try
                {
                    player.getItemInHand().addEnchantment(Enchantment.DIG_SPEED, 5);
                    player.getItemInHand().addEnchantment(Enchantment.SILK_TOUCH, 1);
                    player.getItemInHand().addEnchantment(Enchantment.DURABILITY, 3);
                    player.getItemInHand().addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 3);
                    return true;
                }
                catch (Exception ex)
                {
                    player.sendMessage(ChatColor.DARK_AQUA + "Oh dear, something went wrong.");
                    player.sendMessage(ChatColor.DARK_AQUA + "" + ex.getLocalizedMessage());
                    return false;
                }
            case WOOD_SWORD:
            case STONE_SWORD:
            case IRON_SWORD:
            case GOLD_SWORD:
            case DIAMOND_SWORD:
                try
                {
                    player.getItemInHand().addEnchantment(Enchantment.DAMAGE_ALL, 5);
                    player.getItemInHand().addEnchantment(Enchantment.DAMAGE_ARTHROPODS, 5);
                    player.getItemInHand().addEnchantment(Enchantment.DAMAGE_UNDEAD, 5);
                    player.getItemInHand().addEnchantment(Enchantment.KNOCKBACK, 2);
                    player.getItemInHand().addEnchantment(Enchantment.FIRE_ASPECT, 2);
                    player.getItemInHand().addEnchantment(Enchantment.LOOT_BONUS_MOBS, 3);
                    return true;
                }
                catch (Exception ex)
                {
                    player.sendMessage(ChatColor.DARK_AQUA + "Oh dear, something went wrong.");
                    player.sendMessage(ChatColor.DARK_AQUA + "" + ex.getLocalizedMessage());
                    return false;
                }
            case CHAINMAIL_HELMET:
            case DIAMOND_HELMET:
            case GOLD_HELMET:
            case IRON_HELMET:
            case LEATHER_HELMET:
                try
                {
                    player.getItemInHand().addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                    player.getItemInHand().addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                    player.getItemInHand().addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                    player.getItemInHand().addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                    player.getItemInHand().addEnchantment(Enchantment.OXYGEN, 3);
                    player.getItemInHand().addEnchantment(Enchantment.WATER_WORKER, 1);
                    return true;
                }
                catch (Exception ex)
                {
                    player.sendMessage(ChatColor.DARK_AQUA + "Oh dear, something went wrong.");
                    player.sendMessage(ChatColor.DARK_AQUA + "" + ex.getLocalizedMessage());
                    return false;
                }
            case CHAINMAIL_BOOTS:
            case DIAMOND_BOOTS:
            case GOLD_BOOTS:
            case IRON_BOOTS:
            case LEATHER_BOOTS:
                try
                {
                    player.getItemInHand().addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                    player.getItemInHand().addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                    player.getItemInHand().addEnchantment(Enchantment.PROTECTION_FALL, 4);
                    player.getItemInHand().addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                    player.getItemInHand().addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                    return true;
                }
                catch (Exception ex)
                {
                    player.sendMessage(ChatColor.DARK_AQUA + "Oh dear, something went wrong.");
                    player.sendMessage(ChatColor.DARK_AQUA + "" + ex.getLocalizedMessage());
                    return false;
                }
            case CHAINMAIL_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case GOLD_CHESTPLATE:
            case IRON_CHESTPLATE:
            case LEATHER_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case GOLD_LEGGINGS:
            case IRON_LEGGINGS:
            case LEATHER_LEGGINGS:
                try
                {
                    player.getItemInHand().addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                    player.getItemInHand().addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 4);
                    player.getItemInHand().addEnchantment(Enchantment.PROTECTION_FIRE, 4);
                    player.getItemInHand().addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
                    return true;
                }
                catch (Exception ex)
                {
                    player.sendMessage(ChatColor.DARK_AQUA + "Oh dear, something went wrong.");
                    player.sendMessage(ChatColor.DARK_AQUA + "" + ex.getLocalizedMessage());
                    return false;
                }

            default:
                player.sendMessage(ChatColor.DARK_AQUA + "The " + player.getItemInHand().getType().name().toLowerCase().replace("_", " ")
                        + " in your hand cannot be enchanted.");
                return false;
        }
    }

    /**
     * Give an item in the player's hand all the enchantments it can hold at level 255.
     * @param player The Player that "needs" the enchantments.
     * @return Success
     */
    public static boolean makeUltimate(Player player)
    {
        switch (player.getItemInHand().getType())
        {
            case BOW:
                try
                {
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 255);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.ARROW_FIRE, 1);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 255);
                    return true;
                }
                catch (Exception ex)
                {
                    player.sendMessage(ChatColor.DARK_AQUA + "Oh dear, something went wrong.");
                    player.sendMessage(ChatColor.DARK_AQUA + "" + ex.getLocalizedMessage());
                    return false;
                }
            case WOOD_AXE:
            case STONE_AXE:
            case IRON_AXE:
            case GOLD_AXE:
            case DIAMOND_AXE:
            case WOOD_PICKAXE:
            case STONE_PICKAXE:
            case IRON_PICKAXE:
            case GOLD_PICKAXE:
            case DIAMOND_PICKAXE:
            case WOOD_SPADE:
            case STONE_SPADE:
            case IRON_SPADE:
            case GOLD_SPADE:
            case DIAMOND_SPADE:
                try
                {
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.DIG_SPEED, 255);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.DURABILITY, 255);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 255);
                    return true;
                }
                catch (Exception ex)
                {
                    player.sendMessage(ChatColor.DARK_AQUA + "Oh dear, something went wrong.");
                    player.sendMessage(ChatColor.DARK_AQUA + "" + ex.getLocalizedMessage());
                    return false;
                }
            case WOOD_SWORD:
            case STONE_SWORD:
            case IRON_SWORD:
            case GOLD_SWORD:
            case DIAMOND_SWORD:
                try
                {
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 255);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.DAMAGE_ARTHROPODS, 255);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.DAMAGE_UNDEAD, 255);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.KNOCKBACK, 255);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 255);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.LOOT_BONUS_MOBS, 255);
                    return true;
                }
                catch (Exception ex)
                {
                    player.sendMessage(ChatColor.DARK_AQUA + "Oh dear, something went wrong.");
                    player.sendMessage(ChatColor.DARK_AQUA + "" + ex.getLocalizedMessage());
                    return false;
                }
            case CHAINMAIL_HELMET:
            case DIAMOND_HELMET:
            case GOLD_HELMET:
            case IRON_HELMET:
            case LEATHER_HELMET:
                try
                {
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 255);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 255);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.PROTECTION_FIRE, 255);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.PROTECTION_PROJECTILE, 255);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.OXYGEN, 255);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.WATER_WORKER, 255);
                    return true;
                }
                catch (Exception ex)
                {
                    player.sendMessage(ChatColor.DARK_AQUA + "Oh dear, something went wrong.");
                    player.sendMessage(ChatColor.DARK_AQUA + "" + ex.getLocalizedMessage());
                    return false;
                }
            case CHAINMAIL_BOOTS:
            case DIAMOND_BOOTS:
            case GOLD_BOOTS:
            case IRON_BOOTS:
            case LEATHER_BOOTS:
                try
                {
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 255);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 255);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.PROTECTION_FALL, 255);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.PROTECTION_FIRE, 255);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.PROTECTION_PROJECTILE, 255);
                    return true;
                }
                catch (Exception ex)
                {
                    player.sendMessage(ChatColor.DARK_AQUA + "Oh dear, something went wrong.");
                    player.sendMessage(ChatColor.DARK_AQUA + "" + ex.getLocalizedMessage());
                    return false;
                }
            case CHAINMAIL_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case GOLD_CHESTPLATE:
            case IRON_CHESTPLATE:
            case LEATHER_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case GOLD_LEGGINGS:
            case IRON_LEGGINGS:
            case LEATHER_LEGGINGS:
                try
                {
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 255);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 255);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.PROTECTION_FIRE, 255);
                    player.getItemInHand().addUnsafeEnchantment(Enchantment.PROTECTION_PROJECTILE, 255);
                    return true;
                }
                catch (Exception ex)
                {
                    player.sendMessage(ChatColor.DARK_AQUA + "Oh dear, something went wrong.");
                    player.sendMessage(ChatColor.DARK_AQUA + "" + ex.getLocalizedMessage());
                    return false;
                }

            default:
                player.sendMessage(ChatColor.DARK_AQUA + "The " + player.getItemInHand().getType().name().toLowerCase().replace("_", " ")
                        + " in your hand cannot be enchanted.");
                return false;
        }
    }

    /**
     * Restore the Player's inventory from file.
     * @param p The Player
     * @param dir The Directory
     * @return True if successful, else false.
     */
    @SuppressWarnings("deprecation")
    public static boolean restoreYMLInventory(final Player p, File dir)
    {
        if (!hasSavedYMLInventory(p, dir))
        {
            return false;
        }

        File playerinvfile = (new File(dir + "/" + p.getName() + ".yml"));
        FileConfiguration yml = YamlConfiguration.loadConfiguration(playerinvfile);

        if (yml != null)
        {
            Set<String> keys;

            final ItemStack Helmet = new ItemStack(yml.getInt("Armour.Helmet.Type", 0), 1, (short) yml.getInt("Armour.Helmet.Durability", 0));
            if (yml.getConfigurationSection("Armour.Helmet.Enchantments") != null)
            {
                keys = yml.getConfigurationSection("Armour.Helmet.Enchantments").getKeys(false);
                for (String str : keys)
                {
                    int parseInt = Integer.parseInt(str);
                    Helmet.addEnchantment(Enchantment.getById(parseInt), yml.getInt("Armour.Helmet.Enchantments." + str, 1));
                }
            }

            final ItemStack Chestplate = new ItemStack(yml.getInt("Armour.Chestplate.Type", 0), 1, (short) yml.getInt("Armour.Chestplate.Durability", 0));
            if (yml.getConfigurationSection("Armour.Chestplate.Enchantments") != null)
            {
                keys = yml.getConfigurationSection("Armour.Chestplate.Enchantments").getKeys(false);
                for (String str : keys)
                {
                    int parseInt = Integer.parseInt(str);
                    Chestplate.addEnchantment(Enchantment.getById(parseInt), yml.getInt("Armour.Chestplate.Enchantments." + str, 1));
                }
            }

            final ItemStack Leggings = new ItemStack(yml.getInt("Armour.Leggings.Type", 0), 1, (short) yml.getInt("Armour.Leggings.Durability", 0));
            if (yml.getConfigurationSection("Armour.Leggings.Enchantments") != null)
            {
                keys = yml.getConfigurationSection("Armour.Leggings.Enchantments").getKeys(false);
                for (String str : keys)
                {
                    int parseInt = Integer.parseInt(str);
                    Leggings.addEnchantment(Enchantment.getById(parseInt), yml.getInt("Armour.Leggings.Enchantments." + str, 1));
                }
            }

            final ItemStack Boots = new ItemStack(yml.getInt("Armour.Boots.Type", 0), 1, (short) yml.getInt("Armour.Boots.Durability", 0));
            if (yml.getConfigurationSection("Armour.Boots.Enchantments") != null)
            {
                keys = yml.getConfigurationSection("Armour.Boots.Enchantments").getKeys(false);
                for (String str : keys)
                {
                    int parseInt = Integer.parseInt(str);
                    Boots.addEnchantment(Enchantment.getById(parseInt), yml.getInt("Armour.Boots.Enchantments." + str, 1));
                }
            }

            final ItemStack[] Contents = new ItemStack[36];
            for (int i = 0; i < 36; i++)
            {
                ItemStack slot = new ItemStack(yml.getInt("Inventory.Slot." + i + ".Type", 0), yml.getInt("Inventory.Slot." + i + ".Amount", 0),
                        (short) yml.getInt("Inventory.Slot." + i + ".Durability", 0));
                if (yml.getConfigurationSection("Inventory.Slot." + i + ".Enchantments") != null)
                {
                    keys = yml.getConfigurationSection("Inventory.Slot." + i + ".Enchantments").getKeys(false);
                    for (String str : keys)
                    {
                        int parseInt = Integer.parseInt(str);
                        slot.addEnchantment(Enchantment.getById(parseInt), yml.getInt("Inventory.Slot." + i + ".Enchantments." + str, 1));
                    }
                }

                Contents[i] = slot;
            }

            DECore.getDECore().getServer().getScheduler().scheduleSyncDelayedTask(DECore.getDECore(),
                    new Runnable()
                    {

                        public void run()
                        {
                            p.getInventory().clear();
                            p.updateInventory();

                            if (Helmet.getTypeId() != 0)
                            {
                                p.getInventory().setHelmet(Helmet);
                            }

                            if (Chestplate.getTypeId() != 0)
                            {
                                p.getInventory().setChestplate(Chestplate);
                            }

                            if (Leggings.getTypeId() != 0)
                            {
                                p.getInventory().setLeggings(Leggings);
                            }
                            if (Boots.getTypeId() != 0)
                            {
                                p.getInventory().setBoots(Boots);
                            }

                            for (int i = 0; i < Contents.length; i++)
                            {
                                if (Contents[i] != null && Contents[i].getTypeId() != 0)
                                {
                                    p.getInventory().setItem(i, Contents[i]);
                                }
                            }
                            p.updateInventory();
                        }
                    }
                    );
            return true;
        }
        return false;
    }

    /**
     * Store the Player's inventory to yml file
     * @param p The Player
     * @param file The Plugin's save directory
     * @return True if successful, else false.
     */
    public static boolean storeYMLInventory(Player p, File file)
    {
        // Grab the contents.
        ItemStack[] armor = new ItemStack[4];
        armor[0] = p.getInventory().getHelmet();
        armor[1] = p.getInventory().getChestplate();
        armor[2] = p.getInventory().getLeggings();
        armor[3] = p.getInventory().getBoots();
        ItemStack[] items = p.getInventory().getContents();

        String invPath = file.getPath();
        File dir = new File(invPath);
        dir.mkdir();
        File backupFile = new File(invPath + "/" + p.getName() + ".yml");
        try
        {
            if (backupFile.exists())
            {
                backupFile.delete();
            }
            backupFile.createNewFile();
        }
        catch (Exception ex)
        {
            DECore.log.severe("storeInventory: Could not create backup file for " + p.getName() + ". " + ex);
            return false;
        }
        FileConfiguration yml = YamlConfiguration.loadConfiguration(backupFile);
        if (armor[0] != null)
        {
            yml.set("Armour.Helmet.Type", armor[0].getTypeId());
            yml.set("Armour.Helmet.Durability", armor[0].getDurability());
            for (Enchantment e : armor[0].getEnchantments().keySet())
            {
                yml.set("Armour.Helmet.Enchantments." + e.getId(), armor[0].getEnchantments().get(e));
            }
        }

        if (armor[1] != null)
        {
            yml.set("Armour.Chestplate.Type", armor[1].getTypeId());
            yml.set("Armour.Chestplate.Durability", armor[1].getDurability());
            for (Enchantment e : armor[1].getEnchantments().keySet())
            {
                yml.set("Armour.Chestplate.Enchantments." + e.getId(), armor[1].getEnchantments().get(e));
            }
        }

        if (armor[2] != null)
        {
            yml.set("Armour.Leggings.Type", armor[2].getTypeId());
            yml.set("Armour.Leggings.Durability", armor[2].getDurability());
            for (Enchantment e : armor[2].getEnchantments().keySet())
            {
                yml.set("Armour.Leggings.Enchantments." + e.getId(), armor[2].getEnchantments().get(e));
            }
        }

        if (armor[3] != null)
        {
            yml.set("Armour.Boots.Type", armor[3].getTypeId());
            yml.set("Armour.Boots.Durability", armor[3].getDurability());
            for (Enchantment e : armor[3].getEnchantments().keySet())
            {
                yml.set("Armour.Boots.Enchantments." + e.getId(), armor[3].getEnchantments().get(e));
            }
        }

        for (int i = 0; i < items.length; i++)
        {
            if (items[i] == null)
            {
                continue;
            }
            yml.set("Inventory.Slot." + i + ".Type", items[i].getTypeId());
            yml.set("Inventory.Slot." + i + ".Amount", items[i].getAmount());
            yml.set("Inventory.Slot." + i + ".Durability", items[i].getDurability());
            for (Enchantment e : items[i].getEnchantments().keySet())
            {
                yml.set("Inventory.Slot." + i + ".Enchantments." + e.getId(), items[i].getEnchantments().get(e));
            }
        }

        try
        {
            yml.save(backupFile);
        }
        catch (Exception ex)
        {
            DECore.log.severe("storeInventoryV2: Could not store inventory for " + p.getName() + ". " + ex);
        }

        return true;
    }

    private Inventories()
    {
    }
}
