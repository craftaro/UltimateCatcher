package com.craftaro.ultimatecatcher.utils;

import com.craftaro.core.nms.Nms;
import com.craftaro.ultimatecatcher.UltimateCatcher;
import com.craftaro.core.hooks.EntityStackerManager;
import com.craftaro.core.nms.nbt.NBTEntity;
import com.craftaro.core.third_party.de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class EntityUtils {

    public static String getFormattedEntityType(EntityType type) {
        return UltimateCatcher.getInstance().getMobConfig().getString("Mobs." + type.name() + ".Display Name");
    }

    public static ItemStack serializeEntity(ItemStack item, LivingEntity entity) {
        NBTItem nbtItem = new NBTItem(item);
        NBTEntity nbtEntity = Nms.getImplementations().getNbt().of(entity);
        if (EntityStackerManager.isStacked(entity))
            nbtEntity.set("wasStacked", true);
        nbtItem.setBoolean("UC", true);
        nbtItem.setString("serialized_entity", new String(nbtEntity.serialize(), StandardCharsets.ISO_8859_1));
        return nbtItem.getItem();
    }


    public static LivingEntity spawnEntity(Location location, ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);
        NBTEntity nbtEntity = Nms.getImplementations().getNbt().newEntity();

        byte[] encoded = new byte[0];
        encoded = nbtItem.getString("serialized_entity").getBytes(StandardCharsets.ISO_8859_1);
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
