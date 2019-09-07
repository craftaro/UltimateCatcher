package com.songoda.ultimatecatcher.utils;

import com.songoda.core.hooks.EntityStackerManager;
import com.songoda.ultimatecatcher.UltimateCatcher;
import net.minecraft.server.v1_14_R1.EntityFox;
import net.minecraft.server.v1_14_R1.GameProfileSerializer;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftFox;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public class Methods {

    private static Method methodGetItem, methodAsNMSCopy;
    private static Field fieldMaxStackSize;

    public static ItemStack setMax(ItemStack item, int max) {
        try {
            if (methodGetItem == null) {
                String ver = Bukkit.getServer().getClass().getPackage().getName().substring(23);
                Class<?> clazzCraftItemStack = Class.forName("org.bukkit.craftbukkit." + ver + ".inventory.CraftItemStack");
                Class<?> clazzItemStack = Class.forName("net.minecraft.server." + ver + ".ItemStack");
                Class<?> clazzItem = Class.forName("net.minecraft.server." + ver + ".Item");

                methodAsNMSCopy = clazzCraftItemStack.getMethod("asNMSCopy", ItemStack.class);
                methodGetItem = clazzItemStack.getDeclaredMethod("getItem");

                fieldMaxStackSize = clazzItem.getDeclaredField("maxStackSize");
                fieldMaxStackSize.setAccessible(true);
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }

        try {
            Object objItemStack = methodGetItem.invoke(methodAsNMSCopy.invoke(null, item));
            fieldMaxStackSize.set(objItemStack, max);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return item;
    }

    public static String getFormattedEntityType(EntityType type) {
        return UltimateCatcher.getInstance().getMobConfig().getString("Mobs." + type.name() + ".Display Name");
    }

    public static void takeItem(Player player, int amount) {
        if (player.getGameMode() == GameMode.CREATIVE) return;

        ItemStack item = player.getInventory().getItemInHand();

        int result = item.getAmount() - amount;
        item.setAmount(result);

        player.setItemInHand(result > 0 ? item : null);
    }

    public static String serializeEntity(LivingEntity entity) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("type", entity.getType().name());
        if (entity instanceof Ageable)
            jsonObject.put("baby", !((Ageable) entity).isAdult());
        if (entity.getCustomName() != null && !entity.getCustomName().contains(String.valueOf(ChatColor.COLOR_CHAR))
                && !(EntityStackerManager.getStacker() != null && !EntityStackerManager.isStacked(entity)))
            jsonObject.put("name", entity.getCustomName());
        jsonObject.put("health", entity.getHealth());

        if (entity instanceof Tameable && ((Tameable) entity).isTamed()) {
            jsonObject.put("tamed", true);
            AnimalTamer animalTamer = ((Tameable) entity).getOwner();
            if (animalTamer != null)
                jsonObject.put("owner", animalTamer.getUniqueId().toString());
        }

        if (entity instanceof Sheep) {
            Sheep sheep = ((Sheep) entity);
            if (sheep.isSheared())
                jsonObject.put("sheered", true);
            jsonObject.put("color", sheep.getColor().name());
        } else if (UltimateCatcher.getInstance().isServerVersionAtLeast(ServerVersion.V1_14) && entity instanceof Cat) {
            Cat cat = ((Cat) entity);
            jsonObject.put("color", cat.getCollarColor().name());
            jsonObject.put("catType", cat.getCatType().name());
        } else if (entity instanceof Wolf) {
            Wolf wolf = ((Wolf) entity);
            jsonObject.put("color", wolf.getCollarColor().name());
        } else if (UltimateCatcher.getInstance().isServerVersionAtLeast(ServerVersion.V1_11) && entity instanceof Llama) {
            Llama llama = ((Llama) entity);
            jsonObject.put("color", llama.getColor().name());
            if (llama.getInventory().getDecor() != null)
                jsonObject.put("decor", llama.getInventory().getDecor().getType().name());
        } else if (entity instanceof Villager) {
            Villager villager = ((Villager) entity);
            jsonObject.put("profession", villager.getProfession().name());
        } else if (entity instanceof Slime) {
            Slime slime = ((Slime) entity);
            jsonObject.put("size", slime.getSize());
        } else if (UltimateCatcher.getInstance().isServerVersionAtLeast(ServerVersion.V1_12) && entity instanceof Parrot) {
            Parrot parrot = ((Parrot) entity);
            jsonObject.put("variant", parrot.getVariant().name());
        } else if (entity instanceof Horse) {
            Horse horse = ((Horse) entity);
            jsonObject.put("jump", horse.getJumpStrength());
            jsonObject.put("maxDomestication", horse.getMaxDomestication());
            jsonObject.put("domestication", horse.getDomestication());
            jsonObject.put("color", horse.getColor().name());
            jsonObject.put("style", horse.getStyle().name());
            if (horse.getInventory().getArmor() != null)
                jsonObject.put("armor", horse.getInventory().getArmor().getType().name());
            if (horse.getInventory().getSaddle() != null)
                jsonObject.put("saddle", horse.getInventory().getSaddle().getType().name());
        } else if (UltimateCatcher.getInstance().isServerVersionAtLeast(ServerVersion.V1_14) && entity instanceof Panda) {
            Panda panda = ((Panda) entity);
            jsonObject.put("geneHidden", panda.getHiddenGene().name());
            jsonObject.put("geneMain", panda.getMainGene().name());
        } else if (UltimateCatcher.getInstance().isServerVersionAtLeast(ServerVersion.V1_14) && entity instanceof Fox) {
            Fox fox = ((Fox) entity);
            EntityFox entityFox = ((CraftFox) fox).getHandle();
            NBTTagCompound foxNBT = new NBTTagCompound();
            entityFox.b(foxNBT);
            UUID owner = GameProfileSerializer.b(foxNBT.getList("TrustedUUIDs", 10).getCompound(0));
            jsonObject.put("trusted", true);
            jsonObject.put("owner", owner.toString());
        }

        return jsonObject.toJSONString();
    }

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
                    ((Tameable) entity).setOwner(Bukkit.getOfflinePlayer(UUID.fromString((String)owner)));
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
                    Fox fox = (Fox) entity;
                    EntityFox entityFox = ((CraftFox) fox).getHandle();
                    NBTTagCompound foxNBT = new NBTTagCompound();
                    entityFox.b(foxNBT);
                    NBTTagCompound trustedCompound = new NBTTagCompound();
                    Object owner = jsonObject.get("owner");
                    UUID ownerUUID = UUID.fromString((String) owner);
                    trustedCompound.setLong("L", ownerUUID.getLeastSignificantBits());
                    trustedCompound.setLong("M", ownerUUID.getMostSignificantBits());
                    foxNBT.getList("TrustedUUIDs", 10).add(trustedCompound);
                    entityFox.a(foxNBT);
                    break;
            }

            return entity;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String formatText(String text) {
        if (text == null || text.equals(""))
            return "";
        return formatText(text, false);
    }

    public static String formatText(String text, boolean cap) {
        if (text == null || text.equals(""))
            return "";
        if (cap)
            text = text.substring(0, 1).toUpperCase() + text.substring(1);
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
