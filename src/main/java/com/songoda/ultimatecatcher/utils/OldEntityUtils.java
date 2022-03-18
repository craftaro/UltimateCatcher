package com.songoda.ultimatecatcher.utils;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.third_party.de.tr7zw.nbtapi.NBTItem;
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
        NBTItem nbtItem = new NBTItem(item);

        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location,
                EntityType.valueOf(nbtItem.getString("type")));

        if (nbtItem.hasKey("baby")) {
            if (nbtItem.getBoolean("baby"))
                ((Ageable) entity).setBaby();
            else
                ((Ageable) entity).setAdult();
        }

        if (nbtItem.hasKey("name"))
            entity.setCustomName(nbtItem.getString("name"));

        if (nbtItem.hasKey("tamed")) {
            ((Tameable) entity).setTamed(nbtItem.getBoolean("tamed"));
            String owner = nbtItem.getString("owner");
            if (owner != null)
                ((Tameable) entity).setOwner(Bukkit.getOfflinePlayer(UUID.fromString(owner)));
        }

        double health = nbtItem.getDouble("health");
        entity.setHealth(health > entity.getMaxHealth() ? entity.getMaxHealth() : health);

        switch (entity.getType()) {
            case CAT:
                Cat cat = (Cat) entity;
                cat.setCollarColor(DyeColor.valueOf(nbtItem.getString("color")));
                cat.setCatType(Cat.Type.valueOf(nbtItem.getString("catType")));
                break;
            case WOLF:
                Wolf wolf = (Wolf) entity;
                wolf.setCollarColor(DyeColor.valueOf(nbtItem.getString("color")));
                break;
            case PARROT:
                Parrot parrot = (Parrot) entity;
                parrot.setVariant(Parrot.Variant.valueOf(nbtItem.getString("variant")));
                break;
            case SHEEP:
                Sheep sheep = (Sheep) entity;
                sheep.setColor(DyeColor.valueOf(nbtItem.getString("color")));

                if (nbtItem.hasKey("sheered"))
                    sheep.setSheared(nbtItem.getBoolean("sheered"));
                break;
            case VILLAGER:
                Villager villager = (Villager) entity;
                villager.setProfession(Villager.
                        Profession.valueOf(nbtItem.getString("profession")));
                if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14)) {
                    int experience = nbtItem.getInteger("experience");
                    villager.setVillagerExperience(experience == 0 ? 1 : 0);
                }
                break;
            case SLIME:
                Slime slime = (Slime) entity;
                slime.setSize(nbtItem.getInteger("size"));
                break;
            case LLAMA:
                Llama llama = (Llama) entity;
                llama.setColor(Llama.Color.valueOf(nbtItem.getString("color")));

                if (nbtItem.hasKey("decor"))
                    llama.getInventory().setDecor(new ItemStack(CompatibleMaterial
                            .valueOf(nbtItem.getString("decor")).getMaterial()));
            case DONKEY:
            case MULE:
                ChestedHorse chestedHorse = (ChestedHorse) entity;

                chestedHorse.setCarryingChest(nbtItem.getBoolean("chest"));

                if (nbtItem.hasKey("inventory"))
                    chestedHorse.getInventory().setContents(ItemUtils
                            .itemStackArrayFromBase64(nbtItem.getString("inventory")));

                chestedHorse.getInventory().setMaxStackSize(nbtItem.getInteger("size"));
            case HORSE:
                if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12)) {
                    AbstractHorse abstractHorse = (AbstractHorse) entity;
                    abstractHorse.setJumpStrength(nbtItem.getDouble("jump"));
                    abstractHorse.setDomestication(nbtItem.getInteger("domestication"));
                    abstractHorse.setMaxDomestication(nbtItem.getInteger("maxDomestication"));

                    if (nbtItem.hasKey("saddle"))
                        abstractHorse.getInventory().setSaddle(new ItemStack(
                                CompatibleMaterial.getItem(nbtItem.getString("saddle"))));

                    if (abstractHorse instanceof Horse) {
                        Horse horse = ((Horse) entity);
                        horse.setColor(Horse.Color.valueOf(nbtItem.getString("color")));
                        horse.setStyle(Horse.Style.valueOf(nbtItem.getString("style")));


                        if (nbtItem.hasKey("armor"))
                            horse.getInventory().setArmor(new ItemStack(
                                    CompatibleMaterial.getItem(nbtItem.getString("armor"))));

                    }
                } else {
                    Horse abstractHorse = (Horse) entity;
                    abstractHorse.setJumpStrength(nbtItem.getDouble("jump"));
                    abstractHorse.setDomestication(nbtItem.getInteger("domestication"));
                    abstractHorse.setMaxDomestication(nbtItem.getInteger("maxDomestication"));

                    if (nbtItem.hasKey("saddle"))
                        abstractHorse.getInventory().setSaddle(new ItemStack(
                                CompatibleMaterial.getItem(nbtItem.getString("saddle"))));

                    Horse horse = ((Horse) entity);
                    horse.setColor(Horse.Color.valueOf(nbtItem.getString("color")));
                    horse.setStyle(Horse.Style.valueOf(nbtItem.getString("style")));


                    if (nbtItem.hasKey("armor"))
                        horse.getInventory().setArmor(new ItemStack(
                                CompatibleMaterial.getItem(nbtItem.getString("armor"))));

                }
                break;
            case PANDA:
                Panda panda = (Panda) entity;
                panda.setHiddenGene(Panda.Gene.valueOf(nbtItem.getString("geneHidden")));
                panda.setMainGene(Panda.Gene.valueOf(nbtItem.getString("geneMain")));
                break;
            case FOX:
                String owner = nbtItem.getString("owner");
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
