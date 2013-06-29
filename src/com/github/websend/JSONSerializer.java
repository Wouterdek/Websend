package com.github.websend;

import java.util.Iterator;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONSerializer {

    public static JSONObject serializePlayer(Player ply) throws JSONException {
        JSONObject player = new JSONObject();
        {
            player.put("Name", ply.getName());
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
                if(itemStack != null){
                    JSONObject item = JSONSerializer.serializeItemStack(itemStack);
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
}