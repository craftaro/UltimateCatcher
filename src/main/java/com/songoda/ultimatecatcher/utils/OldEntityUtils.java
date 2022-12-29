package com.songoda.ultimatecatcher.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.third_party.de.tr7zw.nbtapi.NBTItem;
import com.songoda.core.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Cat;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;

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

        json = json.replace("UC-", "");

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = (JsonObject) parser.parse(json);

        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location,
                EntityType.valueOf(jsonObject.get("type").getAsString()));

        Object baby = jsonObject.get("baby");
        String name = jsonObject.get("name").getAsString();
        Object tamed = jsonObject.get("tamed");

        if (baby != null) {
            if ((boolean) baby)
                ((Ageable) entity).setBaby();
            else
                ((Ageable) entity).setAdult();
        }

        if (name != null)
            entity.setCustomName(name);

        if (tamed != null) {
            ((Tameable) entity).setTamed((boolean) tamed);
            String owner = jsonObject.get("owner").getAsString();
            if (owner != null)
                ((Tameable) entity).setOwner(Bukkit.getOfflinePlayer(UUID.fromString(owner)));
        }

        double health = jsonObject.get("health").getAsDouble();
        entity.setHealth(health > entity.getMaxHealth() ? entity.getMaxHealth() : health);

        switch (entity.getType()) {
            case CAT:
                Cat cat = (Cat) entity;
                cat.setCollarColor(DyeColor.valueOf(jsonObject.get("color").getAsString()));
                cat.setCatType(Cat.Type.valueOf(jsonObject.get("catType").getAsString()));
                break;
            case WOLF:
                Wolf wolf = (Wolf) entity;
                wolf.setCollarColor(DyeColor.valueOf(jsonObject.get("color").getAsString()));
                break;
            case PARROT:
                Parrot parrot = (Parrot) entity;
                parrot.setVariant(Parrot.Variant.valueOf(jsonObject.get("variant").getAsString()));
                break;
            case SHEEP:
                Sheep sheep = (Sheep) entity;
                sheep.setColor(DyeColor.valueOf(jsonObject.get("color").getAsString()));

                Object sheared = jsonObject.get("sheered");
                if (sheared != null)
                    sheep.setSheared((boolean) sheared);
                break;
            case LLAMA:
                Llama llama = (Llama) entity;
                llama.setColor(Llama.Color.valueOf(jsonObject.get("color").getAsString()));

                Object decor = jsonObject.get("decor");

                if (decor != null)
                    llama.getInventory().setDecor(new ItemStack(Material.valueOf((String) decor)));
                break;
            case VILLAGER:
                Villager villager = (Villager) entity;
                villager.setProfession(Villager.Profession.valueOf(jsonObject.get("profession").getAsString()));
                break;
            case SLIME:
                Slime slime = (Slime) entity;
                slime.setSize(Math.toIntExact(jsonObject.get("size").getAsLong()));
                break;
            case HORSE:
                Horse horse = (Horse) entity;
                horse.setColor(Horse.Color.valueOf(jsonObject.get("color").getAsString()));
                horse.setStyle(Horse.Style.valueOf(jsonObject.get("style").getAsString()));
                horse.setJumpStrength(jsonObject.get("jump").getAsDouble());
                horse.setDomestication(Math.toIntExact(jsonObject.get("domestication").getAsLong()));
                horse.setMaxDomestication(Math.toIntExact(jsonObject.get("maxDomestication").getAsLong()));

                String armor = jsonObject.get("armor").getAsString();
                String saddle = jsonObject.get("saddle").getAsString();

                if (armor != null)
                    horse.getInventory().setArmor(new ItemStack(Material.valueOf(armor)));

                if (saddle != null)
                    horse.getInventory().setSaddle(new ItemStack(Material.valueOf(saddle)));

                break;
            case PANDA:
                Panda panda = (Panda) entity;
                panda.setHiddenGene(Panda.Gene.valueOf(jsonObject.get("geneHidden").getAsString()));
                panda.setMainGene(Panda.Gene.valueOf(jsonObject.get("geneMain").getAsString()));
                break;
            case FOX:
                String owner = jsonObject.get("owner").getAsString();
                if (owner != null && !owner.trim().equals("") && !owner.equals("00000000-0000-0000-0000-000000000000"))
                    ((Fox) entity).setFirstTrustedPlayer(Bukkit.getOfflinePlayer(UUID.fromString(owner)));
                break;
        }
        return entity;
    }
}
