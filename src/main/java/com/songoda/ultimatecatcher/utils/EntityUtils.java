package com.songoda.ultimatecatcher.utils;

import com.songoda.core.hooks.EntityStackerManager;
import com.songoda.core.nms.NmsManager;
import com.songoda.core.nms.nbt.NBTEntity;
import com.songoda.core.nms.nbt.NBTItem;
import com.songoda.ultimatecatcher.UltimateCatcher;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class EntityUtils {

    public static String getFormattedEntityType(EntityType type) {
        return UltimateCatcher.getInstance().getMobConfig().getString("Mobs." + type.name() + ".Display Name");
    }

    public static ItemStack serializeEntity(ItemStack item, LivingEntity entity) {
        NBTItem nbtItem = NmsManager.getNbt().of(item);
        NBTEntity nbtEntity = NmsManager.getNbt().of(entity);
        if (EntityStackerManager.isStacked(entity))
            nbtEntity.set("wasStacked", true);
        nbtItem.set("UC", true);
        try {
            nbtItem.set("serialized_entity", new String(nbtEntity.serialize(), "ISO-8859-1"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return nbtItem.finish();
    }


    public static LivingEntity spawnEntity(Location location, ItemStack item) {
        NBTItem nbtItem = NmsManager.getNbt().of(item);
        NBTEntity nbtEntity = NmsManager.getNbt().newEntity();

        byte[] encoded = new byte[0];
        try {
            encoded = nbtItem.getNBTObject("serialized_entity").asString().getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        nbtEntity.deSerialize(encoded);
        nbtEntity.set("UUID", UUID.randomUUID());
        LivingEntity entity = (LivingEntity) nbtEntity.spawn(location);
        if (nbtEntity.has("wasStacked") && nbtEntity.getNBTObject("wasStacked").asBoolean()) {
            entity.setCustomName("");
            entity.setCustomNameVisible(false);
        }
        return entity;
    }
}
