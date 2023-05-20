package com.songoda.ultimatecatcher.egg;

import com.songoda.core.configuration.Config;
import com.songoda.ultimatecatcher.UltimateCatcher;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayDeque;
import java.util.Deque;

public class EggManager {

    private final Deque<CEgg> registeredEggs = new ArrayDeque<>();

    /*
     * Register eggs into EggManager from Configuration.
     */
    public void loadEggs() {
        this.registeredEggs.clear();

        Config eggConfig = UltimateCatcher.getInstance().getEggConfig();

        if (!eggConfig.contains("Eggs"))
            return;

        for (String keyName : eggConfig.getConfigurationSection("Eggs").getKeys(false)) {
            ConfigurationSection section = eggConfig.getConfigurationSection("Eggs." + keyName);

            EggBuilder eggBuilder = new EggBuilder(keyName)
                    .setName(section.getString("Name"))
                    .setMaterial(section.getString("Material"))
                    .setRecipe(section.getStringList("Recipe"))
                    .setCost(section.getDouble("Cost"))
                    .setChance(Integer.parseInt(section.getString("Chance").replace("%", "")))
                    .setCustomModelData(Integer.parseInt(section.getString("CustomModelData")));

            addEgg(eggBuilder.build());
        }
    }

    public CEgg addEgg(CEgg egg) {
        registeredEggs.add(egg);
        return egg;
    }

    public CEgg removeEgg(CEgg egg) {
        registeredEggs.remove(egg);
        return egg;
    }

    public CEgg getEgg(String key) {
        return registeredEggs.stream().filter(egg -> egg.getKey().equalsIgnoreCase(key)).findFirst().orElse(null);
    }

    public CEgg getFirstEgg() {
        return registeredEggs.getFirst();
    }

    public Deque<CEgg> getRegisteredEggs() {
        return new ArrayDeque<>(registeredEggs);
    }
}
