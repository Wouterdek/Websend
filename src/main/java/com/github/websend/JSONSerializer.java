package com.github.websend;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONSerializer {

    public static JSONObject serializePlayer(Player ply) throws JSONException {
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
            player.put("CurrentItemIndex", ply.getInventory().getHeldItemSlot());
            player.put("CurrentItemID", ply.getItemInHand().getTypeId());
            JSONObject location = JSONSerializer.serializeLocation(ply.getLocation());
            player.put("Location", location);
            JSONArray inventory = JSONSerializer.serializeInventory(ply.getInventory());
            player.put("Inventory", inventory);
        }
        return player;
    }

    public static JSONObject serializeLocation(Location loc) throws JSONException {
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

    public static JSONArray serializeInventory(Inventory inv) throws JSONException {
        JSONArray inventory = new JSONArray();
        {
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack itemStack = inv.getItem(i);
                if (itemStack != null) {
                    JSONObject item = JSONSerializer.serializeItemStack(itemStack);
                    item.put("Slot", i);
                    inventory.put(item);
                }
            }
        }
        return inventory;
    }

    public static JSONObject serializeItemStack(ItemStack itemStack) throws JSONException {
        JSONObject item = new JSONObject();
        {
            item.put("Type", itemStack.getTypeId());
            item.put("Amount", itemStack.getAmount());
            item.put("Durability", itemStack.getDurability());
            if (itemStack.hasItemMeta()) {
                JSONObject obj = JSONSerializer.serializeMetaData(itemStack.getItemMeta());
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

    public static JSONObject serializeMetaData(ItemMeta meta) throws JSONException {
        if (meta == null) {
            return null;
        }
        JSONObject result = null;
        try {
            if (meta instanceof BookMeta) {
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
                //Is probably an item with enchantments.
                result = new JSONObject();
            }
        } catch (Exception ex) {
            String message = "Exception while serializing item meta data. "
                        + "This may be caused by a mismatch between the Bukkit and "
                        + "Websend versions.";
            if (Main.getSettings().isDebugMode()) {
                Main.logWarning(message, ex);
            } else {
                Main.logWarning(message);
            }
        }
        addNameAndLore(result, meta);
        addEnchantments(result, meta);
        return result;
    }

    private static void addNameAndLore(JSONObject obj, ItemMeta meta) throws JSONException {
        if (meta.hasDisplayName()) {
            obj.put("DisplayName", meta.getDisplayName());
        }
        if (meta.hasLore()) {
            JSONArray lore = new JSONArray(meta.getLore());
            obj.put("Lore", lore);
        }
    }

    private static void addEnchantments(JSONObject obj, ItemMeta meta) throws JSONException {
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

    private static JSONObject serializeMetaBook(BookMeta bookMeta) throws JSONException {
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

    private static JSONObject serializeMetaFireworkEffect(FireworkEffectMeta fireworkEffectMeta) throws JSONException {
        JSONObject metaObj = new JSONObject();
        {
            if (fireworkEffectMeta.hasEffect()) {
                metaObj.put("Effect", new JSONArray(fireworkEffectMeta.getEffect().serialize()));
            }
        }
        return metaObj;
    }

    private static JSONObject serializeMetaFirework(FireworkMeta fireworkMeta) throws JSONException {
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

    private static JSONObject serializeMetaEnchantmentStorage(EnchantmentStorageMeta enchantmentStorageMeta) throws JSONException {
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

    private static JSONObject serializeMetaLeatherArmor(LeatherArmorMeta leatherArmorMeta) throws JSONException {
        JSONObject metaObj = new JSONObject();
        {
            metaObj.put("Color", leatherArmorMeta.getColor().serialize());
        }
        return metaObj;
    }

    private static JSONObject serializeMetaMap(MapMeta mapMeta) throws JSONException {
        JSONObject metaObj = new JSONObject();
        {
            metaObj.put("Scaling", mapMeta.isScaling());
        }
        return metaObj;
    }

    private static JSONObject serializeMetaPotion(PotionMeta potionMeta) throws JSONException {
        JSONObject metaObj = new JSONObject();
        {
            if (potionMeta.hasCustomEffects()) {
                JSONArray arrayOfEffects = new JSONArray();
                for (PotionEffect cur : potionMeta.getCustomEffects()) {
                    arrayOfEffects.put(cur.serialize());
                }
                metaObj.put("CustomEffects", arrayOfEffects);
            }
        }
        return metaObj;
    }

    private static JSONObject serializeMetaRepairable(Repairable repairable) throws JSONException {
        JSONObject metaObj = new JSONObject();
        {
            if (repairable.hasRepairCost()) {
                metaObj.put("RepairCost", repairable.getRepairCost());
            }
        }
        return metaObj;
    }

    private static JSONObject serializeMetaSkull(SkullMeta skullMeta) throws JSONException {
        JSONObject metaObj = new JSONObject();
        {
            if (skullMeta.hasOwner()) {
                metaObj.put("Owner", skullMeta.getOwner());
            }
        }
        return metaObj;
    }
}
