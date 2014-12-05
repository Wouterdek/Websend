package com.github.websend.spigot;

import com.github.websend.JSONSerializer;
import com.github.websend.Main;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.bukkit.DyeColor;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SpigotJSONSerializer extends JSONSerializer{
    @Override
    public JSONObject serializeMetaCustom(ItemMeta meta) throws JSONException {
        if(meta.getClass().getSimpleName().equals("CraftMetaBanner")){
            return serializeMetaBanner(meta);
        }
        return null;
    }
    
    public JSONObject serializeMetaBanner(ItemMeta bannerMeta) throws JSONException {
        DyeColor baseColor = (DyeColor)invokeMethod(bannerMeta, "getBaseColor", null);
        List<Object> patterns = (List)invokeMethod(bannerMeta, "getPatterns", null);
        if(baseColor != null && patterns != null){
            JSONObject banner = new JSONObject();
            {
                banner.put("BaseColor", baseColor.name());
                
                JSONArray jsonPatterns = new JSONArray();
                for(Object curPattern : patterns){
                    DyeColor color = (DyeColor)invokeMethod(curPattern, "getColor", null);
                    String patternType = invokeMethod(curPattern, "getPattern", null).toString();
                    JSONObject curJSONPattern = new JSONObject();
                    {
                        curJSONPattern.put("Color", color.name());
                        curJSONPattern.put("PatternType", patternType);
                    }
                    jsonPatterns.put(curJSONPattern);
                }
                banner.put("Patterns", jsonPatterns);
            }
            return banner;
        }else{
            return null;
        }
    }
    
    private Object invokeMethod(Object targetObject, String methodName, Class[] argTypes, Object... args){
        if(argTypes == null){
            argTypes = new Class[]{};
        }
        try {
            return targetObject.getClass().getMethod(methodName, argTypes).invoke(targetObject, (Object[])args);
        } catch (NoSuchMethodException ex) {
            Main.logError("Cannot retrieve Spigot object data: Spigot was detected, but class definition does not match", ex);
        } catch (SecurityException ex) {
            Main.logError("Cannot retrieve Spigot object data due to security settings preventing reflection", ex);
        } catch (IllegalAccessException ex) {
            Main.logError("Cannot retrieve Spigot object data: no access", ex);
        } catch (IllegalArgumentException ex) {
            Main.logError("Cannot retrieve Spigot object data: Spigot was detected, but class definition does not match", ex);
        } catch (InvocationTargetException ex) {
            Main.logError("An exception occured while retrieving Spigot object data", ex);
        }
        return null;
    }
}
