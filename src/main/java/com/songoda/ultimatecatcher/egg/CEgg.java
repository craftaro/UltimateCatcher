package com.songoda.ultimatecatcher.egg;

import com.songoda.core.utils.TextUtils;
import com.songoda.ultimatecatcher.UltimateCatcher;
import com.songoda.ultimatecatcher.utils.Methods;
import com.songoda.ultimatecatcher.utils.ServerVersion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CEgg {

    private final String key;
    private String name;
    private List<String> recipe;
    private double cost;
    private int chance;

    CEgg(String key) {
        this.key = key;
    }

    public ItemStack toItemStack() {
        ItemStack item = new ItemStack(UltimateCatcher.getInstance().isServerVersionAtLeast(ServerVersion.V1_13)
                ? Material.GHAST_SPAWN_EGG : Material.valueOf("MONSTER_EGG"), 1, (byte)56);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(TextUtils.convertToInvisibleString("UCI;" + key + ";") + Methods.formatText(name));

        // ToDo: Translate this.
        List<String> lore = new ArrayList<>();
        lore.add(UltimateCatcher.getInstance().getLocale().getMessage("general.catcher.lorecost")
                .processPlaceholder("cost", cost).getMessage());

        lore.add(UltimateCatcher.getInstance().getLocale().getMessage("general.catcher.lorechance")
                .processPlaceholder("chance", chance).getMessage());
        meta.setLore(lore);

        item.setItemMeta(meta);

        return item;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRecipe() {
        return recipe;
    }

    public void setRecipe(List<String> recipe) {
        this.recipe = recipe;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public int getChance() {
        return chance;
    }

    public void setChance(int chance) {
        if (chance > 100 || chance < 0) {
            this.chance = 0;
            return;
        }
        this.chance = chance;
    }

    public int hashCode() {
        return 31 * key.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CEgg)) return false;

        CEgg other = (CEgg) obj;
        return Objects.equals(key, other.key);
    }

    public String toString() {
        return "CEgg:{"
                + "Key:\"" + key + "\","
                + "Name:\"" + name + "\","
                + "Cost:\"" + cost + "\","
                + "Chance:\"" + chance + "\""
                + "}";
    }
}
