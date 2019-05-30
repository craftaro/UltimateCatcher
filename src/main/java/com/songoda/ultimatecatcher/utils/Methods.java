package com.songoda.ultimatecatcher.utils;

import com.songoda.ultimatecatcher.UltimateCatcher;
import com.songoda.update.utils.ServerVersion;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

    public static ItemStack createCatcher() {
        ItemStack item = new ItemStack(Material.GHAST_SPAWN_EGG);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Methods.convertToInvisibleString("UCI-") + UltimateCatcher.getInstance().getLocale().getMessage("general.catcher.name"));
        item.setItemMeta(meta);

        return item;
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
        if (entity instanceof Ageable && !((Ageable) entity).isAdult())
            jsonObject.put("baby", true);
        if (entity.getCustomName() != null)
            jsonObject.put("name", entity.getCustomName());
        jsonObject.put("health", entity.getHealth());

        if (entity instanceof Tameable && ((Tameable) entity).isTamed())
            jsonObject.put("tamed", true);

        if (entity instanceof Sheep) {
            Sheep sheep = ((Sheep) entity);
            if (sheep.isSheared()) {
                jsonObject.put("sheered", true);
            }
            jsonObject.put("color", sheep.getColor().name());
        } else if (UltimateCatcher.getInstance().isServerVersionAtLeast(ServerVersion.V1_11) && entity instanceof Llama) {
            Llama llama = ((Llama) entity);
            jsonObject.put("color", llama.getColor().name());
        } else if (entity instanceof Villager) {
            Villager villager = ((Villager) entity);
            jsonObject.put("profession", villager.getProfession().name());
        } else if (entity instanceof Slime) {
            Slime slime = ((Slime) entity);
            jsonObject.put("size", slime.getSize());
        } else if (entity instanceof Horse) {
            Horse horse = ((Horse) entity);
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

            if (baby != null)
                ((Ageable) entity).setBaby();

            if (name != null)
                entity.setCustomName((String) name);

            if (tamed != null)
                ((Tameable) entity).setTamed((boolean) tamed);

            double health = (double) jsonObject.get("health");
            entity.setHealth(health > entity.getMaxHealth() ? entity.getMaxHealth() : health);

            switch (entity.getType()) {
                case SHEEP:
                    Sheep sheep = (Sheep) entity;
                    sheep.setColor(DyeColor.valueOf((String) jsonObject.get("color")));
                    sheep.setSheared((boolean) jsonObject.get("sheered"));
                    break;
                case LLAMA:
                    Llama llama = (Llama) entity;
                    llama.setColor(Llama.Color.valueOf((String) jsonObject.get("color")));
                    break;
                case VILLAGER:
                    Villager villager = (Villager) entity;
                    villager.setProfession(Villager.Profession.valueOf((String) jsonObject.get("profession")));
                    break;
                case SLIME:
                    Slime slime = (Slime) entity;
                    slime.setSize((int) jsonObject.get("size"));
                    break;
                case HORSE:
                    Horse horse = (Horse) entity;
                    horse.setColor(Horse.Color.valueOf((String) jsonObject.get("color")));
                    horse.setStyle(Horse.Style.valueOf((String) jsonObject.get("style")));

                    Object armor = jsonObject.get("armor");
                    Object saddle = jsonObject.get("saddle");

                    if (armor != null)
                        horse.getInventory().setArmor(new ItemStack(Material.valueOf((String)armor)));

                    if (saddle != null)
                        horse.getInventory().setSaddle(new ItemStack(Material.valueOf((String)saddle)));

                    break;
                case PANDA:
                    Panda panda = (Panda) entity;
                    panda.setHiddenGene(Panda.Gene.valueOf((String) jsonObject.get("geneHidden")));
                    panda.setMainGene(Panda.Gene.valueOf((String) jsonObject.get("geneMain")));
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

    public static String convertToInvisibleString(String s) {
        if (s == null || s.equals(""))
            return "";
        StringBuilder hidden = new StringBuilder();
        for (char c : s.toCharArray()) hidden.append(ChatColor.COLOR_CHAR + "").append(c);
        return hidden.toString();
    }

    public static ItemStack getGlass() {
        UltimateCatcher instance = UltimateCatcher.getInstance();
        return Methods.getGlass(instance.getConfig().getBoolean("Interfaces.Replace Glass Type 1 With Rainbow Glass"), instance.getConfig().getInt("Interfaces.Glass Type 1"));
    }

    public static ItemStack getBackgroundGlass(boolean type) {
        UltimateCatcher instance = UltimateCatcher.getInstance();
        if (type)
            return getGlass(false, instance.getConfig().getInt("Interfaces.Glass Type 2"));
        else
            return getGlass(false, instance.getConfig().getInt("Interfaces.Glass Type 3"));
    }

    private static ItemStack getGlass(Boolean rainbow, int type) {
        int randomNum = 1 + (int) (Math.random() * 6);
        ItemStack glass;
        if (rainbow) {
            glass = new ItemStack(UltimateCatcher.getInstance().isServerVersionAtLeast(ServerVersion.V1_13) ?
                    Material.LEGACY_STAINED_GLASS_PANE : Material.valueOf("STAINED_GLASS_PANE"), 1, (short) randomNum);
        } else {
            glass = new ItemStack(UltimateCatcher.getInstance().isServerVersionAtLeast(ServerVersion.V1_13) ?
                    Material.LEGACY_STAINED_GLASS_PANE : Material.valueOf("STAINED_GLASS_PANE"), 1, (short) type);
        }
        ItemMeta glassmeta = glass.getItemMeta();
        glassmeta.setDisplayName("§l");
        glass.setItemMeta(glassmeta);
        return glass;
    }

}
