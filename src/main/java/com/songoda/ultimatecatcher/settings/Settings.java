package com.songoda.ultimatecatcher.settings;

import com.songoda.core.configuration.Config;
import com.songoda.core.configuration.ConfigSetting;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.ultimatecatcher.UltimateCatcher;

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


    public static final ConfigSetting ECONOMY_PLUGIN = new ConfigSetting(config, "Main.Economy", EconomyManager.getEconomy() == null ? "Vault" : EconomyManager.getEconomy().getName(),
            "Which economy plugin should be used?",
            "Supported plugins you have installed: \"" + EconomyManager.getManager().getRegisteredPlugins().stream().collect(Collectors.joining("\", \"")) + "\".");

    public static final ConfigSetting LANGUGE_MODE = new ConfigSetting(config, "System.Language Mode", "en_US",
            "The enabled language file.",
            "More language files (if available) can be found in the plugins data folder.");


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