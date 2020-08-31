package com.songoda.ultimatecatcher.utils;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.nms.NmsManager;
import com.songoda.core.nms.nbt.NBTItem;
import com.songoda.core.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.UUID;

public class OldEntityUtils {

    @Deprecated
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
            case VILLAGER:
                Villager villager = (Villager) entity;
                villager.setProfession(Villager.
                        Profession.valueOf(nbtItem.getNBTObject("profession").asString()));
                if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14)) {
                    int experience = nbtItem.getNBTObject("experience").asInt();
                    villager.setVillagerExperience(experience == 0 ? 1 : 0);
                }
                break;
            case SLIME:
                Slime slime = (Slime) entity;
                slime.setSize(nbtItem.getNBTObject("size").asInt());
                break;
            case LLAMA:
                Llama llama = (Llama) entity;
                llama.setColor(Llama.Color.valueOf(nbtItem.getNBTObject("color").asString()));

                if (nbtItem.has("decor"))
                    llama.getInventory().setDecor(new ItemStack(CompatibleMaterial
                            .valueOf(nbtItem.getNBTObject("decor").asString()).getMaterial()));
            case DONKEY:
            case MULE:
                ChestedHorse chestedHorse = (ChestedHorse) entity;

                chestedHorse.setCarryingChest(nbtItem.getNBTObject("chest").asBoolean());

                if (nbtItem.has("inventory"))
                    chestedHorse.getInventory().setContents(ItemUtils
                            .itemStackArrayFromBase64(nbtItem.getNBTObject("inventory").asString()));

                chestedHorse.getInventory().setMaxStackSize(nbtItem.getNBTObject("size").asInt());
            case HORSE:
                if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12)) {
                    AbstractHorse abstractHorse = (AbstractHorse) entity;
                    abstractHorse.setJumpStrength(nbtItem.getNBTObject("jump").asDouble());
                    abstractHorse.setDomestication(nbtItem.getNBTObject("domestication").asInt());
                    abstractHorse.setMaxDomestication(nbtItem.getNBTObject("maxDomestication").asInt());

                    if (nbtItem.has("saddle"))
                        abstractHorse.getInventory().setSaddle(new ItemStack(
                                CompatibleMaterial.getItem(nbtItem.getNBTObject("saddle").asString())));

                    if (abstractHorse instanceof Horse) {
                        Horse horse = ((Horse) entity);
                        horse.setColor(Horse.Color.valueOf(nbtItem.getNBTObject("color").asString()));
                        horse.setStyle(Horse.Style.valueOf(nbtItem.getNBTObject("style").asString()));


                        if (nbtItem.has("armor"))
                            horse.getInventory().setArmor(new ItemStack(
                                    CompatibleMaterial.getItem(nbtItem.getNBTObject("armor").asString())));

                    }
                } else {
                    Horse abstractHorse = (Horse) entity;
                    abstractHorse.setJumpStrength(nbtItem.getNBTObject("jump").asDouble());
                    abstractHorse.setDomestication(nbtItem.getNBTObject("domestication").asInt());
                    abstractHorse.setMaxDomestication(nbtItem.getNBTObject("maxDomestication").asInt());

                    if (nbtItem.has("saddle"))
                        abstractHorse.getInventory().setSaddle(new ItemStack(
                                CompatibleMaterial.getItem(nbtItem.getNBTObject("saddle").asString())));

                    Horse horse = ((Horse) entity);
                    horse.setColor(Horse.Color.valueOf(nbtItem.getNBTObject("color").asString()));
                    horse.setStyle(Horse.Style.valueOf(nbtItem.getNBTObject("style").asString()));


                    if (nbtItem.has("armor"))
                        horse.getInventory().setArmor(new ItemStack(
                                CompatibleMaterial.getItem(nbtItem.getNBTObject("armor").asString())));

                }
                break;
            case PANDA:
                Panda panda = (Panda) entity;
                panda.setHiddenGene(Panda.Gene.valueOf(nbtItem.getNBTObject("geneHidden").asString()));
                panda.setMainGene(Panda.Gene.valueOf(nbtItem.getNBTObject("geneMain").asString()));
                break;
            case FOX:
                String owner = nbtItem.getNBTObject("owner").asString();
                if (owner != null && !owner.trim().equals("") && !owner.equals("00000000-0000-0000-0000-000000000000"))
                    ((Fox) entity).setFirstTrustedPlayer(Bukkit.getOfflinePlayer(UUID.fromString(owner)));
                break;
        }

        return entity;
    }


    @Deprecated
    public static LivingEntity spawnEntity(Location location, String json) {
        try {
            json = json.replace("UC-", "");

            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(json);

            LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location,
                    EntityType.valueOf((String) jsonObject.get("type")));

            Object baby = jsonObject.get("baby");
            Object name = jsonObject.get("name");
            Object tamed = jsonObject.get("tamed");

            if (baby != null) {
                if ((boolean) baby)
                    ((Ageable) entity).setBaby();
                else
                    ((Ageable) entity).setAdult();
            }

            if (name != null)
                entity.setCustomName((String) name);

            if (tamed != null) {
                ((Tameable) entity).setTamed((boolean) tamed);
                Object owner = jsonObject.get("owner");
                if (owner != null)
                    ((Tameable) entity).setOwner(Bukkit.getOfflinePlayer(UUID.fromString((String) owner)));
            }

            double health = (double) jsonObject.get("health");
            entity.setHealth(health > entity.getMaxHealth() ? entity.getMaxHealth() : health);

            switch (entity.getType()) {
                case CAT:
                    Cat cat = (Cat) entity;
                    cat.setCollarColor(DyeColor.valueOf((String) jsonObject.get("color")));
                    cat.setCatType(Cat.Type.valueOf((String) jsonObject.get("catType")));
                    break;
                case WOLF:
                    Wolf wolf = (Wolf) entity;
                    wolf.setCollarColor(DyeColor.valueOf((String) jsonObject.get("color")));
                    break;
                case PARROT:
                    Parrot parrot = (Parrot) entity;
                    parrot.setVariant(Parrot.Variant.valueOf((String) jsonObject.get("variant")));
                    break;
                case SHEEP:
                    Sheep sheep = (Sheep) entity;
                    sheep.setColor(DyeColor.valueOf((String) jsonObject.get("color")));

                    Object sheared = jsonObject.get("sheered");
                    if (sheared != null)
                        sheep.setSheared((boolean) sheared);
                    break;
                case LLAMA:
                    Llama llama = (Llama) entity;
                    llama.setColor(Llama.Color.valueOf((String) jsonObject.get("color")));

                    Object decor = jsonObject.get("decor");

                    if (decor != null)
                        llama.getInventory().setDecor(new ItemStack(Material.valueOf((String) decor)));
                    break;
                case VILLAGER:
                    Villager villager = (Villager) entity;
                    villager.setProfession(Villager.Profession.valueOf((String) jsonObject.get("profession")));
                    break;
                case SLIME:
                    Slime slime = (Slime) entity;
                    slime.setSize(Math.toIntExact((long) jsonObject.get("size")));
                    break;
                case HORSE:
                    Horse horse = (Horse) entity;
                    horse.setColor(Horse.Color.valueOf((String) jsonObject.get("color")));
                    horse.setStyle(Horse.Style.valueOf((String) jsonObject.get("style")));
                    horse.setJumpStrength((double) jsonObject.get("jump"));
                    horse.setDomestication(Math.toIntExact((long) jsonObject.get("domestication")));
                    horse.setMaxDomestication(Math.toIntExact((long) jsonObject.get("maxDomestication")));

                    Object armor = jsonObject.get("armor");
                    Object saddle = jsonObject.get("saddle");

                    if (armor != null)
                        horse.getInventory().setArmor(new ItemStack(Material.valueOf((String) armor)));

                    if (saddle != null)
                        horse.getInventory().setSaddle(new ItemStack(Material.valueOf((String) saddle)));

                    break;
                case PANDA:
                    Panda panda = (Panda) entity;
                    panda.setHiddenGene(Panda.Gene.valueOf((String) jsonObject.get("geneHidden")));
                    panda.setMainGene(Panda.Gene.valueOf((String) jsonObject.get("geneMain")));
                    break;
                case FOX:
                    String owner = (String) jsonObject.get("owner");
                    if (owner != null && !owner.trim().equals("") && !owner.equals("00000000-0000-0000-0000-000000000000"))
                        ((Fox)entity).setFirstTrustedPlayer(Bukkit.getOfflinePlayer(UUID.fromString(owner)));
                    break;
            }

            return entity;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
