package com.github.websend;

import com.github.websend.spigot.SpigotJSONSerializer;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Bukkit is not updated to 1.8 and the server mods based of bukkit are implementing their own patches to support 1.8
 * This is a compatibility problem since the central Bukkit API is now splitting up in several small APIs that all require seperate code to support.
 * This class provides several abstract serialization methods for subclasses to implement according to each API.
 */
public abstract class JSONSerializer {
    private static JSONSerializer instance = null;
    
    public static JSONSerializer getInstance(){
        if(instance == null){
            if(Bukkit.getServer().getVersion().contains("Spigot")){
                instance = new SpigotJSONSerializer();
            }else{
                instance = new BukkitJSONSerializer();
            }
        }
        return instance;
    }
    
    public JSONObject serializePlayer(Player ply, boolean serializeAllData) throws JSONException {
        JSONObject player = new JSONObject();
        {
            player.put("Name", ply.getName());
            player.put("UUID", ply.getUniqueId());
            player.put("UUIDVersion", ply.getUniqueId().version());
            player.put("XP", ply.getExp());
            player.put("XPLevel", ply.getLevel());
            player.put("Exhaustion", ply.getExhaustion());
            player.put("FoodLevel", ply.getFoodLevel());
            player.put("GameMode", ply.getGameMode());
            player.put("Health", ply.getHealth());
            player.put("IP", ply.getAddress().toString());
            player.put("IsOP", ply.isOp());
            if(serializeAllData){
                player.put("CurrentItemIndex", ply.getInventory().getHeldItemSlot());
                player.put("MainHandItemID", ply.getInventory().getItemInMainHand().getType().name());
                player.put("OffHandItemID", ply.getInventory().getItemInOffHand().getType().name());
                JSONObject location = serializeLocation(ply.getLocation());
                player.put("Location", location);
                JSONArray inventory = serializeInventory(ply.getInventory());
                player.put("Inventory", inventory);
            }
        }
        return player;
    }

    public JSONObject serializeLocation(Location loc) throws JSONException {
        JSONObject location = new JSONObject();
        {
            location.put("X", loc.getX());
            location.put("Y", loc.getY());
            location.put("Z", loc.getZ());
            location.put("Yaw", loc.getYaw());
            location.put("Pitch", loc.getPitch());
            location.put("World", loc.getWorld().getName());
        }
        return location;
    }

    public JSONArray serializeInventory(Inventory inv) throws JSONException {
        JSONArray inventory = new JSONArray();
        {
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack itemStack = inv.getItem(i);
                if (itemStack != null) {
                    JSONObject item = serializeItemStack(itemStack);
                    item.put("Slot", i);
                    inventory.put(item);
                }
            }
        }
        return inventory;
    }

    public JSONObject serializeItemStack(ItemStack itemStack) throws JSONException {
        JSONObject item = new JSONObject();
        {
            item.put("TypeName", itemStack.getType().name());
            item.put("Amount", itemStack.getAmount());
            item.put("Durability", itemStack.getDurability());
            if (itemStack.hasItemMeta()) {
                JSONObject obj = serializeMetaData(itemStack.getItemMeta());
                if (obj != null) {
                    item.put("Meta", obj);
                }
            }
            if (itemStack.getData().getData() != 0) {
                item.put("Data", itemStack.getData().getData());
            }
            
            JSONArray enchantments = new JSONArray();
            {
                Iterator<Enchantment> enchIter = itemStack.getEnchantments().keySet().iterator();
                while (enchIter.hasNext()) {
                    Enchantment cur = enchIter.next();
                    JSONObject enchantment = new JSONObject();
                    {
                        enchantment.put("Name", cur.getName());
                        enchantment.put("Level", itemStack.getEnchantmentLevel(cur));
                    }
                    enchantments.put(enchantment);
                }
            }
        }
        return item;
    }

    public JSONObject serializeMetaData(ItemMeta meta) throws JSONException {
        if (meta == null) {
            return null;
        }
        JSONObject result = null;
        try {
            result = serializeMetaCustom(meta);
            if(result != null){
                //Is custom item implemented by subclass
            }else if (meta instanceof BookMeta) {
                result = serializeMetaBook((BookMeta) meta);
            } else if (meta instanceof FireworkEffectMeta) {
                result = serializeMetaFireworkEffect((FireworkEffectMeta) meta);
            } else if (meta instanceof FireworkMeta) {
                result = serializeMetaFirework((FireworkMeta) meta);
            } else if (meta instanceof EnchantmentStorageMeta) {
                result = serializeMetaEnchantmentStorage((EnchantmentStorageMeta) meta);
            } else if (meta instanceof LeatherArmorMeta) {
                result = serializeMetaLeatherArmor((LeatherArmorMeta) meta);
            } else if (meta instanceof MapMeta) {
                result = serializeMetaMap((MapMeta) meta);
            } else if (meta instanceof PotionMeta) {
                result = serializeMetaPotion((PotionMeta) meta);
            } else if (meta instanceof Repairable) {
                result = serializeMetaRepairable((Repairable) meta);
            } else if (meta instanceof SkullMeta) {
                result = serializeMetaSkull((SkullMeta) meta);
            } else {
                result = new JSONObject();
            }
        } catch (Exception ex) {
            if (Main.getSettings().isDebugMode()) {
                Main.getMainLogger().log(
                        Level.WARNING,
                        "Exception while serializing item meta data. "
                        + "This may be caused by a mismatch between the Bukkit and "
                        + "Websend versions.",
                        ex);
            } else {
                Main.getMainLogger().log(
                        Level.WARNING,
                        "Exception while serializing item meta data. "
                        + "This may be caused by a mismatch between the Bukkit and "
                        + "Websend versions. Enable debug mode for stack trace.");
            }
        }
        addNameAndLore(result, meta);
        addEnchantments(result, meta);
        return result;
    }

    public void addNameAndLore(JSONObject obj, ItemMeta meta) throws JSONException {
        if (meta.hasDisplayName()) {
            obj.put("DisplayName", meta.getDisplayName());
        }
        if (meta.hasLore()) {
            JSONArray lore = new JSONArray(meta.getLore());
            obj.put("Lore", lore);
        }
    }

    public void addEnchantments(JSONObject obj, ItemMeta meta) throws JSONException {
        if (!meta.hasEnchants()) {
            return;
        }
        JSONArray enchantArray = new JSONArray();
        {
            for (Entry<Enchantment, Integer> set : meta.getEnchants().entrySet()) {
                Enchantment enchantment = set.getKey();
                JSONObject enchantmentObj = new JSONObject();
                {
                    enchantmentObj.put("Type", enchantment.getId());
                    enchantmentObj.put("Name", enchantment.getName());
                    enchantmentObj.put("MaxLevel", enchantment.getMaxLevel());
                    enchantmentObj.put("StartLevel", enchantment.getStartLevel());
                    enchantmentObj.put("Level", meta.getEnchantLevel(enchantment));
                }
                enchantArray.put(enchantmentObj);
            }
        }
        obj.put("Enchantments", enchantArray);
    }

    public JSONObject serializeMetaBook(BookMeta bookMeta) throws JSONException {
        JSONObject metaObj = new JSONObject();
        {
            if (bookMeta.hasAuthor()) {
                metaObj.put("Author", bookMeta.getAuthor());
            }
            if (bookMeta.hasTitle()) {
                metaObj.put("Title", bookMeta.getTitle());
            }
            if (bookMeta.hasPages()) {
                metaObj.put("Pages", new JSONArray(bookMeta.getPages()));
            }
        }
        return metaObj;
    }

    public JSONObject serializeMetaFireworkEffect(FireworkEffectMeta fireworkEffectMeta) throws JSONException {
        JSONObject metaObj = new JSONObject();
        {
            if (fireworkEffectMeta.hasEffect()) {
                metaObj.put("Effect", new JSONArray(fireworkEffectMeta.getEffect().serialize()));
            }
        }
        return metaObj;
    }

    public JSONObject serializeMetaFirework(FireworkMeta fireworkMeta) throws JSONException {
        JSONObject metaObj = new JSONObject();
        {
            if (fireworkMeta.hasEffects()) {
                JSONArray arrayOfEffects = new JSONArray();
                for (FireworkEffect cur : fireworkMeta.getEffects()) {
                    arrayOfEffects.put(cur.serialize());
                }
                metaObj.put("Effects", arrayOfEffects);
            }
            metaObj.put("Power", fireworkMeta.getPower());
        }
        return metaObj;
    }

    public JSONObject serializeMetaEnchantmentStorage(EnchantmentStorageMeta enchantmentStorageMeta) throws JSONException {
        JSONObject metaObj = new JSONObject();
        {
            JSONArray enchantArray = new JSONArray();
            {
                for (Entry<Enchantment, Integer> set : enchantmentStorageMeta.getStoredEnchants().entrySet()) {
                    Enchantment enchantment = set.getKey();
                    JSONObject enchantmentObj = new JSONObject();
                    {
                        enchantmentObj.put("Type", enchantment.getId());
                        enchantmentObj.put("Name", enchantment.getName());
                        enchantmentObj.put("MaxLevel", enchantment.getMaxLevel());
                        enchantmentObj.put("StartLevel", enchantment.getStartLevel());
                        enchantmentObj.put("Level", set.getValue());
                    }
                    enchantArray.put(enchantmentObj);
                }
            }
            metaObj.put("EnchantmentStorage", enchantArray);
        }
        return metaObj;
    }

    public JSONObject serializeMetaLeatherArmor(LeatherArmorMeta leatherArmorMeta) throws JSONException {
        JSONObject metaObj = new JSONObject();
        {
            metaObj.put("Color", leatherArmorMeta.getColor().serialize());
        }
        return metaObj;
    }

    public JSONObject serializeMetaMap(MapMeta mapMeta) throws JSONException {
        JSONObject metaObj = new JSONObject();
        {
            metaObj.put("Scaling", mapMeta.isScaling());
        }
        return metaObj;
    }

    public JSONObject serializeMetaPotion(PotionMeta potionMeta) throws JSONException {
        JSONObject metaObj = new JSONObject();
        {
            if (potionMeta.hasCustomEffects()) {
                JSONArray arrayOfEffects = new JSONArray();
                for (PotionEffect cur : potionMeta.getCustomEffects()) {
                    arrayOfEffects.put(cur.serialize());
                }
                metaObj.put("CustomEffects", arrayOfEffects);
            }
            
            PotionData data = potionMeta.getBasePotionData();
            if(data != null){
                JSONObject potionTypeObj = new JSONObject();
                {
                    potionTypeObj.put("Name", data.getType().name());
                    potionTypeObj.put("MaxLevel", data.getType().getMaxLevel());
                    potionTypeObj.put("Extendable", data.getType().isExtendable());
                    potionTypeObj.put("Instant", data.getType().isInstant());
                    potionTypeObj.put("Upgradable", data.getType().isUpgradeable());
                    potionTypeObj.put("EffectType", data.getType().getEffectType().getName());
                    potionTypeObj.put("EffectDurationMod", data.getType().getEffectType().getDurationModifier());
                    potionTypeObj.put("EffectInstant", data.getType().getEffectType().isInstant());
                }
                metaObj.put("Type", potionTypeObj);
                metaObj.put("Extended", data.isExtended());
                metaObj.put("Upgraded", data.isUpgraded());
            }
        }
        return metaObj;
    }

    public JSONObject serializeMetaRepairable(Repairable repairable) throws JSONException {
        JSONObject metaObj = new JSONObject();
        {
            if (repairable.hasRepairCost()) {
                metaObj.put("RepairCost", repairable.getRepairCost());
            }
        }
        return metaObj;
    }

    public JSONObject serializeMetaSkull(SkullMeta skullMeta) throws JSONException {
        JSONObject metaObj = new JSONObject();
        {
            if (skullMeta.hasOwner()) {
                metaObj.put("Owner", skullMeta.getOwner());
            }
        }
        return metaObj;
    }
    
    public abstract JSONObject serializeMetaCustom(ItemMeta bannerMeta) throws JSONException;
}
