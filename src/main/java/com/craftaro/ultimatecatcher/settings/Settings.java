package com.craftaro.ultimatecatcher.settings;

import com.craftaro.core.configuration.Config;
import com.craftaro.core.configuration.ConfigSetting;
import com.craftaro.core.hooks.EconomyManager;
import com.craftaro.ultimatecatcher.UltimateCatcher;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Settings {

    static final Config config = UltimateCatcher.getInstance().getCoreConfig();

    public static final ConfigSetting USE_CATCHER_RECIPE = new ConfigSetting(config, "Main.Use Catcher Recipe", true,
            "Should egg recipes be enabled.");

    public static final ConfigSetting REJECT_TAMED = new ConfigSetting(config, "Main.Reject Tamed Entities That Are Not Yours", true,
            "Should players attempting to tame another players tamed entity",
            "be rejected?");

    public static final ConfigSetting STOP_DISPENSER_IN_WORLD = new ConfigSetting(config, "Main.Block Dispenser In Blocked Spawning Worlds", false,
            "Should dispensers still spawn caught mobs even though it's in a blocked spawning world?");

    public static final ConfigSetting BLOCKED_CATCHING_WORLDS = new ConfigSetting(config, "Main.Blocked Catching Worlds", Arrays.asList("world_nether"),
            "Which worlds should players not be allowed to catch mobs?");

    public static final ConfigSetting BLOCKED_SPAWNING_WORLDS = new ConfigSetting(config, "Main.Blocked Spawning Worlds", Arrays.asList("world_nether"),
            "Which worlds should players not be able to spawn caught mobs?");

    public static final ConfigSetting USE_RECIPE_BOOK = new ConfigSetting(config, "Main.Use Recipe Book", false,
            "Should we add the recipes to the recipe book available in 1.12 and above?",
            "Note: This option won't do anything in servers below 1.14.");

    public static final ConfigSetting ECONOMY_PLUGIN = new ConfigSetting(config, "Main.Economy", EconomyManager.getEconomy() == null ? "Vault" : EconomyManager.getEconomy().getName(),
            "Which economy plugin should be used?",
            "Supported plugins you have installed: \"" + EconomyManager.getManager().getRegisteredPlugins().stream().collect(Collectors.joining("\", \"")) + "\".");

    public static final ConfigSetting LANGUAGE_MODE = new ConfigSetting(config, "System.Language Mode", "en_US",
            "The enabled language file.",
            "More language files (if available) can be found in the plugins data folder.");

    public static final ConfigSetting CATCHER_LORE_FORMAT = new ConfigSetting(config, "Lore Formats.Catcher.Lore", Arrays.asList("%chance%", "%cost%"),
            "Configure the order of lines in the catcher lore.",
            "Placeholders will get replaced with lines from the language file.");

    public static final ConfigSetting CATCHER_CAUGHT_LORE_FORMAT = new ConfigSetting(config, "Lore Formats.Catcher Caught.Lore", Arrays.asList("%type%", "%age%", "%health%", "%tamed%", "%trusted%"),
            "Configure the order of lines in a spawn egg.",
            "Placeholders will get replaced with lines from the language file.");

    /**
     * In order to set dynamic economy comment correctly, this needs to be
     * called after EconomyManager load
     */
    public static void setupConfig() {
        config.load();
        config.setAutoremove(true).setAutosave(true);

        // convert economy settings
        if (config.getBoolean("Economy.Use Vault Economy") && EconomyManager.getManager().isEnabled("Vault")) {
            config.set("Main.Economy", "Vault");
        } else if (config.getBoolean("Economy.Use Reserve Economy") && EconomyManager.getManager().isEnabled("Reserve")) {
            config.set("Main.Economy", "Reserve");
        } else if (config.getBoolean("Economy.Use Player Points Economy") && EconomyManager.getManager().isEnabled("PlayerPoints")) {
            config.set("Main.Economy", "PlayerPoints");
        }

        config.saveChanges();
    }
}