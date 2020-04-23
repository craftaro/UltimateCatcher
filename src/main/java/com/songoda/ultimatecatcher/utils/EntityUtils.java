package com.songoda.ultimatecatcher.utils;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.hooks.EntityStackerManager;
import com.songoda.core.nms.NmsManager;
import com.songoda.core.nms.nbt.NBTItem;
import com.songoda.ultimatecatcher.UltimateCatcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class EntityUtils {

    public static String getFormattedEntityType(EntityType type) {
        return UltimateCatcher.getInstance().getMobConfig().getString("Mobs." + type.name() + ".Display Name");
    }

    public static ItemStack serializeEntity(ItemStack item, LivingEntity entity) {
        NBTItem nbtItem = NmsManager.getNbt().of(item);

        nbtItem.set("UC", true);
        nbtItem.set("type", entity.getType().name());
        if (entity instanceof Ageable)
            nbtItem.set("baby", !((Ageable) entity).isAdult());
        if (entity.getCustomName() != null && !entity.getCustomName().contains(String.valueOf(ChatColor.COLOR_CHAR))
                && !(EntityStackerManager.getStacker() != null && !EntityStackerManager.isStacked(entity)))
            nbtItem.set("name", entity.getCustomName());
        nbtItem.set("health", entity.getHealth());

        if (entity instanceof Tameable && ((Tameable) entity).isTamed()) {
            nbtItem.set("tamed", true);
            AnimalTamer animalTamer = ((Tameable) entity).getOwner();
            if (animalTamer != null)
                nbtItem.set("owner", animalTamer.getUniqueId().toString());
        }

        if (entity instanceof Sheep) {
            Sheep sheep = ((Sheep) entity);
            if (sheep.isSheared())
                nbtItem.set("sheered", true);
            nbtItem.set("color", sheep.getColor().name());
        } else if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14) && entity instanceof Cat) {
            Cat cat = ((Cat) entity);
            nbtItem.set("color", cat.getCollarColor().name());
            nbtItem.set("catType", cat.getCatType().name());
        } else if (entity instanceof Wolf) {
            Wolf wolf = ((Wolf) entity);
            nbtItem.set("color", wolf.getCollarColor().name());
        } else if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11) && entity instanceof Llama) {
            Llama llama = ((Llama) entity);
            nbtItem.set("color", llama.getColor().name());
            if (llama.getInventory().getDecor() != null)
                nbtItem.set("decor", llama.getInventory().getDecor().getType().name());
        } else if (entity instanceof Villager) {
            Villager villager = ((Villager) entity);
            nbtItem.set("profession", villager.getProfession().name());
        } else if (entity instanceof Slime) {
            Slime slime = ((Slime) entity);
            nbtItem.set("size", slime.getSize());
        } else if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12) && entity instanceof Parrot) {
            Parrot parrot = ((Parrot) entity);
            nbtItem.set("variant", parrot.getVariant().name());
        } else if (entity instanceof Horse) {
            Horse horse = ((Horse) entity);
            nbtItem.set("jump", horse.getJumpStrength());
            nbtItem.set("maxDomestication", horse.getMaxDomestication());
            nbtItem.set("domestication", horse.getDomestication());
            nbtItem.set("color", horse.getColor().name());
            nbtItem.set("style", horse.getStyle().name());
            if (horse.getInventory().getArmor() != null)
                nbtItem.set("armor", horse.getInventory().getArmor().getType().name());
            if (horse.getInventory().getSaddle() != null)
                nbtItem.set("saddle", horse.getInventory().getSaddle().getType().name());
        } else if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14) && entity instanceof Panda) {
            Panda panda = ((Panda) entity);
            nbtItem.set("geneHidden", panda.getHiddenGene().name());
            nbtItem.set("geneMain", panda.getMainGene().name());
        } else if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14) && entity instanceof Fox) {
            UUID ownerUUID = FoxNMS.getOwner(entity);
            if (ownerUUID != null) {
                nbtItem.set("trusted", true);
                nbtItem.set("owner", ownerUUID.toString());
            }
        }

        return nbtItem.finish();
    }


    public static LivingEntity spawnEntity(Location location, ItemStack item) {
        NBTItem nbtItem = NmsManager.getNbt().of(item);

        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location,
                EntityType.valueOf(nbtItem.getNBTObject("type").asString()));

        if (nbtItem.has("baby")) {
            if (nbtItem.getNBTObject("baby").asBoolean())
                ((Ageable) entity).setBaby();
            else
                ((Ageable) entity).setAdult();
        }

        if (nbtItem.has("name"))
            entity.setCustomName(nbtItem.getNBTObject("name").asString());

        if (nbtItem.has("tamed")) {
            ((Tameable) entity).setTamed(nbtItem.getNBTObject("tamed").asBoolean());
            String owner = nbtItem.getNBTObject("owner").asString();
            if (owner != null)
                ((Tameable) entity).setOwner(Bukkit.getOfflinePlayer(UUID.fromString(owner)));
        }

        double health = nbtItem.getNBTObject("health").asDouble();
        entity.setHealth(health > entity.getMaxHealth() ? entity.getMaxHealth() : health);

        switch (entity.getType()) {
            case CAT:
                Cat cat = (Cat) entity;
                cat.setCollarColor(DyeColor.valueOf(nbtItem.getNBTObject("color").asString()));
                cat.setCatType(Cat.Type.valueOf(nbtItem.getNBTObject("catType").asString()));
                break;
            case WOLF:
                Wolf wolf = (Wolf) entity;
                wolf.setCollarColor(DyeColor.valueOf(nbtItem.getNBTObject("color").asString()));
                break;
            case PARROT:
                Parrot parrot = (Parrot) entity;
                parrot.setVariant(Parrot.Variant.valueOf(nbtItem.getNBTObject("variant").asString()));
                break;
            case SHEEP:
                Sheep sheep = (Sheep) entity;
                sheep.setColor(DyeColor.valueOf(nbtItem.getNBTObject("color").asString()));

                if (nbtItem.has("sheered"))
                    sheep.setSheared(nbtItem.getNBTObject("sheered").asBoolean());
                break;
            case LLAMA:
                Llama llama = (Llama) entity;
                llama.setColor(Llama.Color.valueOf(nbtItem.getNBTObject("color").asString()));

                if (nbtItem.has("decor"))
                    llama.getInventory().setDecor(new ItemStack(CompatibleMaterial
                            .valueOf(nbtItem.getNBTObject("decor").asString()).getMaterial()));
                break;
            case VILLAGER:
                Villager villager = (Villager) entity;
                villager.setProfession(Villager.
                        Profession.valueOf(nbtItem.getNBTObject("profession").asString()));
                break;
            case SLIME:
                Slime slime = (Slime) entity;
                slime.setSize(nbtItem.getNBTObject("size").asInt());
                break;
            case HORSE:
                Horse horse = (Horse) entity;
                horse.setColor(Horse.Color.valueOf(nbtItem.getNBTObject("color").asString()));
                horse.setStyle(Horse.Style.valueOf(nbtItem.getNBTObject("style").asString()));
                horse.setJumpStrength(nbtItem.getNBTObject("jump").asDouble());
                horse.setDomestication(nbtItem.getNBTObject("domestication").asInt());
                horse.setMaxDomestication(nbtItem.getNBTObject("maxDomestication").asInt());

                if (nbtItem.has("armor"))
                    horse.getInventory().setArmor(new ItemStack(
                            CompatibleMaterial.getItem(nbtItem.getNBTObject("armor").asString())));

                if (nbtItem.has("saddle"))
                    horse.getInventory().setSaddle(new ItemStack(
                            CompatibleMaterial.getItem(nbtItem.getNBTObject("saddle").asString())));

                break;
            case PANDA:
                Panda panda = (Panda) entity;
                panda.setHiddenGene(Panda.Gene.valueOf(nbtItem.getNBTObject("geneHidden").asString()));
                panda.setMainGene(Panda.Gene.valueOf(nbtItem.getNBTObject("geneMain").asString()));
                break;
            case FOX:
                String owner = nbtItem.getNBTObject("owner").asString();
                if (owner != null && !owner.equals("00000000-0000-0000-0000-000000000000"))
                    FoxNMS.applyOwner(entity, UUID.fromString(owner));
                break;
        }

        return entity;
    }

    @Deprecated
    public static LivingEntity spawnEntity(Location location, String json) {
        return OldEntityUtils.spawnEntity(location, json);
    }
}
