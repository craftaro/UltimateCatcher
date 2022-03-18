package com.songoda.ultimatecatcher.egg;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.locale.Message;
import com.songoda.core.third_party.de.tr7zw.nbtapi.NBTItem;
import com.songoda.core.utils.TextUtils;
import com.songoda.ultimatecatcher.UltimateCatcher;
import com.songoda.ultimatecatcher.settings.Settings;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        ItemStack item = CompatibleMaterial.GHAST_SPAWN_EGG.getItem();

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(TextUtils.formatText(name));

        String costLine = UltimateCatcher.getInstance().getLocale().getMessage("general.catcher.lorecost")
                .processPlaceholder("cost", cost)
                .getMessage();

        String chanceLine = UltimateCatcher.getInstance().getLocale().getMessage("general.catcher.lorechance")
                .processPlaceholder("chance", chance)
                .getMessage();

        List<String> lore = Settings.CATCHER_LORE_FORMAT.getStringList().stream()
                .map(line -> new Message(line).processPlaceholder("cost", costLine).processPlaceholder("chance", chanceLine).getMessage())
                .collect(Collectors.toList());

        meta.setLore(lore);

        item.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(item);

        nbtItem.setBoolean("UCI", true);
        nbtItem.setString("type", key);

        return nbtItem.getItem();
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

    @Override
    public int hashCode() {
        return 31 * key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CEgg)) return false;

        CEgg other = (CEgg) obj;
        return Objects.equals(key, other.key);
    }

    @Override
    public String toString() {
        return "CEgg:{"
                + "Key:\"" + key + "\","
                + "Name:\"" + name + "\","
                + "Cost:\"" + cost + "\","
                + "Chance:\"" + chance + "\""
                + "}";
    }
}
