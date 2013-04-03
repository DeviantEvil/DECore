package net.deviantevil.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import net.deviantevil.decore.DEConfigOptions;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

/**
 * A library for various functions involving Entities.
 * Map of Entity inheritances: http://jd.bukkit.org/doxygen/de/dd5/interfaceorg_1_1bukkit_1_1entity_1_1Entity.html
 * @author Kristian
 */

public class Entities {    
    /**
     * Returns the friendly bridgeName of an entity
     **/
    public static String getEntityType(Entity entity) {
        if (entity instanceof Player) {
            return ((Player) entity).getName();
        }
        if (entity instanceof TNTPrimed) {
            return "TNT";
        }
        if (entity.getClass().getSimpleName().startsWith("Craft")) {
            return entity.getClass().getSimpleName().substring(5);
        }
        return "Herobrine";
    }
    
/** Kill an Entity. Usually for Players, but compatible with other entities.
     * @param sender The sender issuing the kill
     * @param target The target entity
     * @param behaviour The Behaviour of the kill. Defaults to "Random".
     * @return The String of the Kill Behaviour used (to report back if random was used)
     * @see KillMethod */
    public static String killEntity (CommandSender sender, Entity target, String behaviour) {
        if (target == null) {
            sender.sendMessage(ChatColor.DARK_AQUA + "No target.");
            return "none";
        }
        KillMethod use = null;

        behaviour = (behaviour == null || behaviour.isEmpty()) ? "random" : behaviour.trim().replace("-", "").replace("_", "");
        ArrayList<KillMethod> values = KillMethod.getValues();
        for (KillMethod km : values) {
            if (km.toString().equalsIgnoreCase(behaviour)) {
                use = km;
            }
        }

        if (use == null) {
            sender.sendMessage(ChatColor.DARK_AQUA + "Behaviour not found. /de kill list");
            return "none";
        }
        
        if (use == KillMethod.RANDOM) {
            Random rand;
            do {
                rand = new Random();
                int i = rand.nextInt(KillMethod.values().length);
                use = KillMethod.getMethod(i);
            } while (use == KillMethod.RANDOM);
        }

        Location location = target.getLocation();
        switch (use) {
            case SKYDIVE:
                Player p;
                Location loc;

                if (sender instanceof Player) {
                    p = (Player) sender;
                    loc = p.getLocation();
                } else {
                    loc = target.getWorld().getSpawnLocation();
                }
                loc.setX(loc.getX() + 10);
                loc.setY(300);
                loc.setZ(loc.getZ() + 10);
                target.teleport(loc);
                break;
            case VOID:
                location.setY(-3);
                target.teleport(location);
                break;
            case SUFFOCATE:
                location.setY(2);
                target.teleport(location);
                break;
            case BURN:
                target.setFireTicks(600);
                break;
            case LIGHTNING:
                LightningStrike strikeLightning;
                if (DEConfigOptions.lightningIsReal) {
                     strikeLightning = target.getWorld().strikeLightning(target.getLocation());
                     target.getWorld().createExplosion(target.getLocation(), 2F, true);
                } else {
                    strikeLightning = target.getWorld().strikeLightningEffect(target.getLocation());
                }
                if (target instanceof Player) {
                    target.setLastDamageCause(new EntityDamageByEntityEvent(strikeLightning, target, DamageCause.LIGHTNING, 20));
                    if (!((Player) target).getGameMode().equals(GameMode.CREATIVE)) { 
                        ((Player) target).setHealth(0);
                    }
                } else {
                    target.setLastDamageCause(new EntityDamageByEntityEvent(strikeLightning, target, DamageCause.LIGHTNING, 20));
                    target.remove();                
                }
                break;
            case CREEPER:
                Entities.spawnCreature(EntityType.CREEPER, 6, target.getLocation());
                break;
            case STARVE:
                if (target instanceof Player) {
                    ((Player) target).setFoodLevel(0);
                    ((Player) target).setExhaustion(0);
                } else {
                    target.remove();
                }
                break;
            default:
            case KILL:
                if (target instanceof Player) {
                    try {
                        ((Player) target).setHealth(0);
                    } catch (Exception e) {
                        // Target probably in creative or logged out.
                    } 
                } else {
                    target.remove();
                }
                break;
        }
        return use.toString().toLowerCase();
    }
    
    /**
     * Spawns exp ball on the player equivalent to the amount specified.
     * @param player The player to give experience to.
     * @param amount The exp to give
     */
    public static void spawnExperience(Player player, int amount) {
        player.getWorld().spawn(player.getLocation().add(0, 1, 0), ExperienceOrb.class).setExperience(amount);
    }
    
    /**
     * Gets this Wolf's Owner.
     * If the Entity is not a wolf, "NotAWolf" is returned.
     * If the Wolf is not tamed, or does not have an owner, "NoOwner" is returned.
     * @param entity The Wolf
     * @return The Owner's Name.
     */
    public static String getWolfOwner (Entity entity) {
        if (entity instanceof Wolf) {
            Wolf wolf = (Wolf)entity;
            if (wolf.isTamed()) {
                if (wolf.getOwner() instanceof Player) { 
                    return ((Player)wolf.getOwner()).getName();
                } else if (wolf.getOwner() instanceof OfflinePlayer) {
                    return (OfflinePlayer)wolf.getOwner() != null ? ((OfflinePlayer)wolf.getOwner()).getName() : "NoOwner";
                } else {
                    return "NoOwner";
                }
            }
            return "NoOwner";
        }
        return "NotAWolf";
    }
    
    /**
     * Spawns random aggressive mob(s) somewhere in first world.
     * If multiple mobs, will randomise their locations.
     * @param amount The number of mobs to spawn
     * @param mixtype Mix the type of mob spawned
     */
    public static void spawnAggressiveMob(int amount, boolean mixtype) {
        spawnAggressiveMob(true, null, amount, mixtype);
    }
    
    /**
     * Spawns random aggressive mob(s) at location on the surface.
     * @param location The location to spawn at
     * @param amount The number of mobs to spawn
     * @param mixtype Mix the type of mob spawned
     */
    public static void spawnAggressiveMob(Location location, int amount, boolean mixtype) {
        if (location == null) {
            spawnAggressiveMob(true, null, amount, mixtype);
            return;
        }
        
        Random rand = new Random();
        int mobtype = rand.nextInt(AggressiveMobType.values().length);

        for (int i = 0; i < amount; i++) {
            if (mixtype) {
                rand = new Random();
                mobtype = rand.nextInt(AggressiveMobType.values().length);
            }
            spawnAggressiveMob(mobtype, location);
        }
    }
    
    /**
     * Spawns random aggressive mob(s) at a random location on the surface.
     * @param multipleLocations Should the mobs be spawned in multiple Locations (false for one)
     * @param world The world to spawn in.
     * @param amount The number of mobs to spawn
     * @param mixtype Mix the type of mob spawned
     */
    public static void spawnAggressiveMob(boolean multipleLocations, World world, int amount, boolean mixtype) {
        if (world == null) {
            world = Bukkit.getWorlds().get(0);
        }
        
        Location loc = Locations.makeRandomLocation(world);
        Random rand = new Random();
        int mobtype = rand.nextInt(AggressiveMobType.values().length);

        for (int i = 0; i < amount; i++) {
            if (mixtype) {
                rand = new Random();
                mobtype = rand.nextInt(AggressiveMobType.values().length);
            }
            if (multipleLocations) {
                loc = Locations.makeRandomLocation(world);
            }
            spawnAggressiveMob(mobtype, loc);
        }
    }
    
    /** The resulting spawn code for aggressive mobs. */
    private static void spawnAggressiveMob(int result, Location location) {
        switch (result) {
            case 0: location.getWorld().spawnEntity(location, EntityType.CAVE_SPIDER).setFallDistance(0); break;
            case 1: location.getWorld().spawnEntity(location, EntityType.CREEPER).setFallDistance(0); break;
            case 2: location.getWorld().spawnEntity(location, EntityType.ENDERMAN).setFallDistance(0); break;    
            case 3: location.getWorld().spawnEntity(location, EntityType.GIANT).setFallDistance(0); break; 
            case 4: location.getWorld().spawnEntity(location, EntityType.MAGMA_CUBE).setFallDistance(0); break;
            case 5: location.getWorld().spawnEntity(location, EntityType.SKELETON).setFallDistance(0); break;
            case 6: location.getWorld().spawnEntity(location, EntityType.SPIDER).setFallDistance(0); break;    
            case 7: location.getWorld().spawnEntity(location, EntityType.ZOMBIE).setFallDistance(0); break;  
            default: location.getWorld().spawnEntity(location, EntityType.ZOMBIE).setFallDistance(0); break;     
        }
    }
    
    /**
     * Spawns random Nether mob(s) in first world.
     * @param amount The number of mobs to spawn
     * @param mixtype Mix the type of mob spawned
     */
    public static void spawnNetherMob(int amount, boolean mixtype) {
        spawnNetherMob(true, null, amount, mixtype);
    }
    
    /**
     * Spawns random Nether mob(s) at location on the surface.
     * @param location The location to spawn at
     * @param amount The number of mobs to spawn
     * @param mixtype Mix the type of mob spawned
     */
    public static void spawnNetherMob(Location location, int amount, boolean mixtype) {
        if (location == null) {
            spawnNetherMob(true, null, amount, mixtype);
            return;
        }
        
        Random rand = new Random();
        int mobtype = rand.nextInt(NetherMobType.values().length);

        for (int i = 0; i < amount; i++) {
            if (mixtype) {
                rand = new Random();
                mobtype = rand.nextInt(NetherMobType.values().length);
            }
            spawnNetherMob(mobtype, location);
        }
    }
    
    /**
     * Spawns random Nether mob(s) at a random location on the surface.
     * @param multipleLocations Should the mobs be spawned in multiple Locations (false for one)
     * @param world The world to spawn in.
     * @param amount The number of mobs to spawn
     * @param mixtype Mix the type of mob spawned
     */
    public static void spawnNetherMob(boolean multipleLocations, World world, int amount, boolean mixtype) {
        if (world == null) {
            world = Bukkit.getWorlds().get(0);
        }
        
        Location loc = Locations.makeRandomLocation(world);
        Random rand = new Random();
        int mobtype = rand.nextInt(NetherMobType.values().length);

        for (int i = 0; i < amount; i++) {
            if (mixtype) {
                rand = new Random();
                mobtype = rand.nextInt(NetherMobType.values().length);
            }
            if (multipleLocations) {
                loc = Locations.makeRandomLocation(world);
            }
            spawnNetherMob(mobtype, loc);
        }
    }
    
    /** The resulting spawn code for Nether mobs. */
    private static void spawnNetherMob(int result, Location location) {
        switch (result) {
            case 0: location.getWorld().spawnEntity(location, EntityType.BLAZE).setFallDistance(0); break;
            case 1: location.getWorld().spawnEntity(location, EntityType.GHAST).setFallDistance(0); break;
            case 2: location.getWorld().spawnEntity(location, EntityType.PIG_ZOMBIE).setFallDistance(0); break;   
            default: location.getWorld().spawnEntity(location, EntityType.PIG_ZOMBIE).setFallDistance(0); break;     
        }
    }
    
    /**
     * Spawns random passive mob(s) in first world at a random location on the surface.
     * @param amount The number of mobs to spawn
     * @param mixtype Mix the type of mob spawned
     */
    public static void spawnPassiveMob(int amount, boolean mixtype) {
        spawnPassiveMob(true, null, amount, mixtype);
    }
       
    /**
     * Spawns random passive mob(s) at location on the surface.
     * @param location The location to spawn at
     * @param amount The number of mobs to spawn
     * @param mixtype Mix the type of mob spawned
     */
    public static void spawnPassiveMob(Location location, int amount, boolean mixtype) {
        if (location == null) {
            spawnPassiveMob(true, null, amount, mixtype);
            return;
        }
        
        Random rand = new Random();
        int mobtype = rand.nextInt(PassiveMobType.values().length);

        for (int i = 0; i < amount; i++) {
            if (mixtype) {
                rand = new Random();
                mobtype = rand.nextInt(PassiveMobType.values().length);
            }
            spawnPassiveMob(mobtype, location);
        }
    }
    
    /**
     * Spawns random passive mob(s) at a random location on the surface.
     * @param multipleLocations Should the mobs be spawned in multiple Locations (false for one)
     * @param world The world to spawn in.
     * @param amount The number of mobs to spawn
     * @param mixtype Mix the type of mob spawned
     */
    public static void spawnPassiveMob(boolean multipleLocations, World world, int amount, boolean mixtype) {
        if (world == null) {
            world = Bukkit.getWorlds().get(0);
        }
        
        Location loc = Locations.makeRandomLocation(world);
        Random rand = new Random();
        int mobtype = rand.nextInt(PassiveMobType.values().length);

        for (int i = 0; i < amount; i++) {
            if (mixtype) {
                rand = new Random();
                mobtype = rand.nextInt(PassiveMobType.values().length);
            }
            if (multipleLocations) {
                loc = Locations.makeRandomLocation(world);
            }
            spawnPassiveMob(mobtype, loc);
        }
    }
    
    /** The resulting spawn code for Passive mobs. */
    private static void spawnPassiveMob(int result, Location location) {
        switch (result) {
            case 0: location.getWorld().spawnEntity(location, EntityType.CHICKEN).setFallDistance(0); break;
            case 1: location.getWorld().spawnEntity(location, EntityType.COW).setFallDistance(0); break;
            case 2: location.getWorld().spawnEntity(location, EntityType.PIG).setFallDistance(0); break;    
            case 3: location.getWorld().spawnEntity(location, EntityType.SHEEP).setFallDistance(0); break;  
            case 4: location.getWorld().spawnEntity(location, EntityType.WOLF).setFallDistance(0); break;   
            default: location.getWorld().spawnEntity(location, EntityType.SQUID).setFallDistance(0); break;      
        }
    }
    
    /**
     * Attempts to return the EntityType enum of a name.
     * Returns default value if name not found. 
     */
    public static EntityType stringToEntityType(String name, EntityType Default) {
        name = name.toUpperCase().replace(" ", "_");
        
        if (name.equals("CAVESPIDER") || name.equals("POISONSPIDER") || name.equals("BLUESPIDER")) {
            return EntityType.CAVE_SPIDER; 
        }
        
        if (name.equals("ENDERDRAGON") || name.equals("ENDDRAGON")) {
            return EntityType.ENDER_DRAGON;
        }
        
        if (name.equals("MUSHROOM_COW") || name.equals("MOOSHROOM") || name.equals("MOOSHROOM_COW")) {
            return EntityType.MUSHROOM_COW;
        }
        
        if (name.equals("PIGZOMBIE")) {
            return EntityType.PIG_ZOMBIE;
        }
        
        if (name.equals("SILVER_FISH")) {
            return EntityType.SILVERFISH;
        }

        for (EntityType ct : EntityType.values()) {
            if (ct.toString().equals(name)) {
                return ct;
            }
        }
        return Default;
    }
    
    /**
     * Spawns a creature randomly in the world from its name.
     * Returns success.
     */
    public static boolean spawnCreature(EntityType creature, World world) {
        if (creature == null) {
            return false;
        }
        return world.spawnEntity(Locations.makeRandomLocation(world), creature) != null;
    }
    
    /**
     * Spawns creatures randomly in the world from its name.
     * Returns success.
     */
    public static boolean spawnCreature(EntityType creature, int amount, World world) {
        if (creature == null) {
            return false;
        }
        for (int i = 0; i < amount; i++) {
            world.spawnEntity(Locations.makeRandomLocation(world), creature);
        }
        return true;
    }
    
    /**
     * Spawn a creature at a location.
     * Returns success.
     */
    public static boolean spawnCreature(EntityType creature, Location loc) {
        if (creature == null) {
            return false;
        }
        return loc.getWorld().spawnEntity(loc, creature) != null;
    }
    
    /**
     * Spawns creatures at a location.
     * Returns success.
     */
    public static boolean spawnCreature(EntityType creature, int amount, Location loc) {
        if (creature == null) {
            return false;
        }
        for (int i = 0; i < amount; i++) {
            loc.getWorld().spawnEntity(loc, creature);
        }
        return true;
    }
    
    /**
     * Spawn a creature in a chunk.
     * Returns success.
     */
    public static boolean spawnCreature(EntityType creature, Chunk chunk) {
        if (creature == null) {
            return false;
        }
        return chunk.getWorld().spawnEntity(Locations.makeRandomLocation(chunk), creature) != null;
    }
    
    /**
     * Spawns creatures at a location.
     * Returns success.
     */
    public static boolean spawnCreature(EntityType creature, int amount, Chunk chunk) {
        if (creature == null) {
            return false;
        }
        for (int i = 0; i < amount; i++) {
            chunk.getWorld().spawnEntity(Locations.makeRandomLocation(chunk), creature);
        }
        return true;
    }
    
    /** Passive Mobs:
     * CHICKEN,
     * COW,
     * PIG,
     * SHEEP,
     * WOLF */
    public enum PassiveMobType {
        CHICKEN,
        COW,
        PIG,
        SHEEP,
        WOLF
    }
    
    /** Aggressive Mobs:
     * CAVE SPIDER,
     * CREEPER,
     * ENDERMAN,
     * MONSTER,
     * SKELETON,
     * SPIDER,
     * ZOMBIE */
    public enum AggressiveMobType {
        CAVE_SPIDER,
        CREEPER,
        ENDERMAN,
        MONSTER,
        SKELETON,
        SPIDER,
        ZOMBIE
    }
    
    /** Nether Mobs:
     * BLAZE,
     * GHAST,
     * PIG_ZOMBIE */
    public enum NetherMobType {
        BLAZE,
        GHAST,
        PIG_ZOMBIE
    }
    
    /** Kill Methods used. */
    public enum KillMethod {
        /** Burn the Entity */
        BURN,
        /** Spawn Creeprs around the Entity */
        CREEPER,
        /** Outright kill the Entity */
        KILL,
        /** Lightning the Entity */
        LIGHTNING,
        /** Randomly determine the outcome */
        RANDOM,
        /** Teleport the Entity into the air and let gravity do the work. */
        SKYDIVE,
        /** Set Player's hunger to 0. Entities just die. */
        STARVE,
        /** Teleport the Entity into bedrock so they suffocate. */
        SUFFOCATE,
        /** Teleport the Entity into the void. */
        VOID;

        /** Get a list of the available Kill Methods. */
        public static ArrayList<KillMethod> getValues () {
            return new ArrayList<KillMethod>(Arrays.asList(values()));
        }

        /** Get a Method from its ordinal number */
        public static KillMethod getMethod (int ordinal) {
            return values()[ordinal];
        }
    }

    private Entities() {
    }
}