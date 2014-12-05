package com.github.websend;

import org.bukkit.inventory.meta.ItemMeta;
import org.json.JSONObject;

public class BukkitJSONSerializer extends JSONSerializer{
    @Override
    public JSONObject serializeMetaCustom(ItemMeta bannerMeta) {
        return null; //no custom types
    }
}
