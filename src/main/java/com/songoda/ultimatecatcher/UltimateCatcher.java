package com.songoda.ultimatecatcher;

import com.songoda.core.SongodaCore;
import com.songoda.core.SongodaPlugin;
import com.songoda.core.commands.CommandManager;
import com.songoda.core.compatibility.LegacyMaterials;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.configuration.Config;
import com.songoda.core.gui.GuiManager;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.core.hooks.EntityStackerManager;
import com.songoda.ultimatecatcher.commands.CommandGive;
import com.songoda.ultimatecatcher.commands.CommandReload;
import com.songoda.ultimatecatcher.commands.CommandSettings;
import com.songoda.ultimatecatcher.commands.CommandUltimateCatcher;
import com.songoda.ultimatecatcher.egg.CEgg;
import com.songoda.ultimatecatcher.egg.EggBuilder;
import com.songoda.ultimatecatcher.egg.EggManager;
import com.songoda.ultimatecatcher.listeners.DispenserListeners;
import com.songoda.ultimatecatcher.listeners.EntityListeners;
import com.songoda.ultimatecatcher.listeners.EntityPickupListeners;
import com.songoda.ultimatecatcher.settings.Settings;
import com.songoda.ultimatecatcher.tasks.EggTrackingTask;
import com.songoda.ultimatecatcher.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.PluginManager;

import java.util.Arrays;
import java.util.List;

public class UltimateCatcher extends SongodaPlugin {

    private static UltimateCatcher INSTANCE;

    private final Config mobConfig = new Config(this, "mobs.yml");
    private final Config eggConfig = new Config(this, "eggs.yml");

    private final GuiManager guiManager = new GuiManager(this);
    private EggManager eggManager;
    private CommandManager commandManager;
    private EntityListeners entityListeners;

    public static UltimateCatcher getInstance() {
        return INSTANCE;
    }

    @Override
    public void onPluginLoad() {
        INSTANCE = this;
    }

    @Override
    public void onPluginEnable() {
        // Run Songoda Updater
        SongodaCore.registerPlugin(this, 51, LegacyMaterials.EGG);

        // Load Economy
        EconomyManager.load();

        // Setup Config
        Settings.setupConfig();
        this.setLocale(Settings.LANGUGE_MODE.getString(), false);

        // Set economy preference
        EconomyManager.getManager().setPreferredHook(Settings.ECONOMY_PLUGIN.getString());

        // Load entity stack manager.
        EntityStackerManager.load();

        // Register commands
        this.commandManager = new CommandManager(this);
        this.commandManager.addCommand(new CommandUltimateCatcher(this))
                .addSubCommands(
                        new CommandGive(this),
                        new CommandSettings(guiManager),
                        new CommandReload(this)
                );

        this.eggManager = new EggManager();

        // Setup Listeners
        guiManager.init();
        PluginManager pluginManager = Bukkit.getPluginManager();
        entityListeners = new EntityListeners(this);
        pluginManager.registerEvents(entityListeners, this);
        pluginManager.registerEvents(new DispenserListeners(), this);
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12))
            pluginManager.registerEvents(new EntityPickupListeners(), this);

        EggTrackingTask.startTask(this);

        // Set economy preference
        EconomyManager.getManager().setPreferredHook(Settings.ECONOMY_PLUGIN.getString());

        // Register recipe
        if (Settings.USE_CATCHER_RECIPE.getBoolean()) {
            for (CEgg egg : eggManager.getRegisteredEggs()) {
                ShapelessRecipe shapelessRecipe = ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12)
                        ? new ShapelessRecipe(new NamespacedKey(this, egg.getKey()),
                        egg.toItemStack()) : new ShapelessRecipe(egg.toItemStack());
                for (String item : egg.getRecipe()) {
                    String[] split = item.split(":");
                    shapelessRecipe.addIngredient(Integer.valueOf(split[0]), Material.valueOf(split[1]));
                }
                Bukkit.addRecipe(shapelessRecipe);
            }
        }

        // Load Mobs and Eggs
        setupMobs();
        setupEgg();
    }

    private void setupMobs() {
        for (EntityType value : EntityType.values()) {
            if (value.isSpawnable()
                    && value.isAlive()
                    && !value.toString().contains("ARMOR")
                    && value != EntityType.PLAYER
                    && value != EntityType.WITHER
                    && value != EntityType.ENDER_DRAGON
                    && value != EntityType.IRON_GOLEM) {
                mobConfig.setDefault("Mobs." + value.name() + ".Enabled", true)
                        .setDefault("Mobs." + value.name() + ".Display Name", Methods.formatText(value.name().toLowerCase().replace("_", " "), true));
            }
        }
        mobConfig.setRootNodeSpacing(1).setCommentSpacing(0);
        mobConfig.load();
        mobConfig.saveChanges();
    }

    /*
     * Insert default key list into config.
     */
    private void setupEgg() {
        eggConfig.createDefaultSection("Eggs")
                .setDefault("Regular.Name", "&7Regular Egg")
                .setDefault("Regular.Recipe", Arrays.asList("1:EGG", "5:IRON_INGOT"))
                .setDefault("Regular.Cost", 0)
                .setDefault("Regular.Chance", "25%")
                .setDefault("Ultra.Name", "&6Ultra Egg")
                .setDefault("Ultra.Recipe", Arrays.asList("1:EGG", "5:DIAMOND"))
                .setDefault("Ultra.Cost", 15)
                .setDefault("Ultra.Chance", "60%")
                .setDefault("Insane.Name", "&5Insane Egg")
                .setDefault("Insane.Recipe", Arrays.asList("1:EGG", "5:EMERALD"))
                .setDefault("Insane.Cost", 50)
                .setDefault("Insane.Chance", "100%");
        eggConfig.setRootNodeSpacing(1).setCommentSpacing(0);
        eggConfig.load();
        eggConfig.saveChanges();

        /*
         * Register eggs into EggManager from Configuration.
         */
        if (eggConfig.contains("Eggs")) {
            for (String keyName : eggConfig.getConfigurationSection("Eggs").getKeys(false)) {
                ConfigurationSection section = eggConfig.getConfigurationSection("Eggs." + keyName);

                EggBuilder eggBuilder = new EggBuilder(keyName)
                        .setName(section.getString("Name"))
                        .setRecipe(section.getStringList("Recipe"))
                        .setCost(section.getDouble("Cost"))
                        .setChance(Integer.parseInt(section.getString("Chance").replace("%", "")));

                eggManager.addEgg(eggBuilder.build());
            }
        }
    }

    @Override
    public void onPluginDisable() {
    }

    @Override
    public void onConfigReload() {
        this.setLocale(Settings.LANGUGE_MODE.getString(), true);
    }

    @Override
    public List<Config> getExtraConfig() {
        return Arrays.asList(mobConfig, eggConfig);
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public EggManager getEggManager() {
        return eggManager;
    }

    public EntityListeners getEntityListeners() {
        return entityListeners;
    }

    public Config getMobConfig() {
        return mobConfig;
    }
}
