package com.songoda.ultimatecatcher;

import com.songoda.ultimatecatcher.command.CommandManager;
import com.songoda.ultimatecatcher.economy.Economy;
import com.songoda.ultimatecatcher.economy.PlayerPointsEconomy;
import com.songoda.ultimatecatcher.economy.ReserveEconomy;
import com.songoda.ultimatecatcher.economy.VaultEconomy;
import com.songoda.ultimatecatcher.egg.CEgg;
import com.songoda.ultimatecatcher.egg.EggBuilder;
import com.songoda.ultimatecatcher.egg.EggManager;
import com.songoda.ultimatecatcher.listeners.DispenserListeners;
import com.songoda.ultimatecatcher.listeners.EntityListeners;
import com.songoda.ultimatecatcher.listeners.EntityPickupListeners;
import com.songoda.ultimatecatcher.stacker.Stacker;
import com.songoda.ultimatecatcher.stacker.UltimateStacker;
import com.songoda.ultimatecatcher.tasks.EggTrackingTask;
import com.songoda.ultimatecatcher.utils.ConfigWrapper;
import com.songoda.ultimatecatcher.utils.Methods;
import com.songoda.ultimatecatcher.utils.Metrics;
import com.songoda.ultimatecatcher.utils.ServerVersion;
import com.songoda.ultimatecatcher.utils.locale.Locale;
import com.songoda.ultimatecatcher.utils.settings.Setting;
import com.songoda.ultimatecatcher.utils.settings.SettingsManager;
import com.songoda.ultimatecatcher.utils.updateModules.LocaleModule;
import com.songoda.update.Plugin;
import com.songoda.update.SongodaUpdate;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class UltimateCatcher extends JavaPlugin {

    private static UltimateCatcher INSTANCE;

    private ConfigWrapper mobFile = new ConfigWrapper(this, "", "mobs.yml");
    private ConfigWrapper eggFile = new ConfigWrapper(this, "", "eggs.yml");

    private Stacker stacker;

    private Locale locale;
    private CommandManager commandManager;
    private SettingsManager settingsManager;
    private EggManager eggManager;
    private EntityListeners entityListeners;

    private Economy economy;

    private ServerVersion serverVersion = ServerVersion.fromPackageName(Bukkit.getServer().getClass().getPackage().getName());

    public static UltimateCatcher getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(Methods.formatText("&a============================="));
        console.sendMessage(Methods.formatText("&7UltimateCatcher " + this.getDescription().getVersion() + " by &5Songoda <3&7!"));
        console.sendMessage(Methods.formatText("&7Action: &aEnabling&7..."));

        this.settingsManager = new SettingsManager(this);
        this.settingsManager.setupConfig();

        this.commandManager = new CommandManager(this);
        this.eggManager = new EggManager();

        for (EntityType value : EntityType.values()) {
            if (value.isSpawnable()
                    && value.isAlive()
                    && !value.toString().contains("ARMOR")
                    && value != EntityType.PLAYER
                    && value != EntityType.WITHER
                    && value != EntityType.ENDER_DRAGON
                    && value != EntityType.IRON_GOLEM) {
                mobFile.getConfig().addDefault("Mobs." + value.name() + ".Enabled", true);
                mobFile.getConfig().addDefault("Mobs." + value.name() + ".Display Name", Methods.formatText(value.name().toLowerCase().replace("_", " "), true));
            }
        }
        mobFile.getConfig().options().copyDefaults(true);
        mobFile.saveConfig();

        //Apply default eggs.
        checkEggDefaults();

        /*
         * Register eggs into EggManager from Configuration.
         */
        if (eggFile.getConfig().contains("Eggs")) {
            for (String keyName : eggFile.getConfig().getConfigurationSection("Eggs").getKeys(false)) {
                ConfigurationSection section = eggFile.getConfig().getConfigurationSection("Eggs." + keyName);

                EggBuilder eggBuilder = new EggBuilder(keyName)
                        .setName(section.getString("Name"))
                        .setRecipe(section.getStringList("Recipe"))
                        .setCost(section.getDouble("Cost"))
                        .setChance(Integer.parseInt(section.getString("Chance").replace("%", "")));

                eggManager.addEgg(eggBuilder.build());
            }
        }


        PluginManager pluginManager = Bukkit.getPluginManager();

        if (pluginManager.isPluginEnabled("UltimateStacker"))
            stacker = new UltimateStacker();

        entityListeners = new EntityListeners(this);

        pluginManager.registerEvents(entityListeners, this);
        pluginManager.registerEvents(new DispenserListeners(), this);
        if (isServerVersionAtLeast(ServerVersion.V1_12)) pluginManager.registerEvents(new EntityPickupListeners(), this);

        // Setup language
        new Locale(this, "en_US");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode"));

        //Running Songoda Updater
        Plugin plugin = new Plugin(this, 51);
        plugin.addModule(new LocaleModule());
        SongodaUpdate.load(plugin);

        EggTrackingTask.startTask(this);

        // Setup Economy
        if (Setting.VAULT_ECONOMY.getBoolean() && pluginManager.isPluginEnabled("Vault"))
            this.economy = new VaultEconomy();
        else if (Setting.RESERVE_ECONOMY.getBoolean() && pluginManager.isPluginEnabled("Reserve"))
            this.economy = new ReserveEconomy();
        else if (Setting.PLAYER_POINTS_ECONOMY.getBoolean() && pluginManager.isPluginEnabled("PlayerPoints"))
            this.economy = new PlayerPointsEconomy();

        // Register recipe
        if (Setting.USE_CATCHER_RECIPE.getBoolean()) {
            for (CEgg egg : eggManager.getRegisteredEggs()) {
                ShapelessRecipe shapelessRecipe = isServerVersionAtLeast(ServerVersion.V1_12)
                        ? new ShapelessRecipe(new NamespacedKey(this, egg.getKey()),
                        egg.toItemStack()) : new ShapelessRecipe(egg.toItemStack());
                for (String item : egg.getRecipe()) {
                    String[] split = item.split(":");
                    shapelessRecipe.addIngredient(Integer.valueOf(split[0]), Material.valueOf(split[1]));
                }
                Bukkit.addRecipe(shapelessRecipe);
            }
        }

        // Starting Metrics
        new Metrics(this);

        console.sendMessage(Methods.formatText("&a============================="));
    }

    /*
     * Insert default key list into config.
     */

    private void checkEggDefaults() {
        if (eggFile.getConfig().contains("Eggs")) return;
        eggFile.getConfig().set("Eggs.Regular.Name", "&7Regular Egg");
        eggFile.getConfig().set("Eggs.Regular.Recipe", Arrays.asList("1:EGG", "5:IRON_INGOT"));
        eggFile.getConfig().set("Eggs.Regular.Cost", 0);
        eggFile.getConfig().set("Eggs.Regular.Chance", "25%");
        eggFile.getConfig().set("Eggs.Ultra.Name", "&6Ultra Egg");
        eggFile.getConfig().set("Eggs.Ultra.Recipe", Arrays.asList("1:EGG", "5:DIAMOND"));
        eggFile.getConfig().set("Eggs.Ultra.Cost", 15);
        eggFile.getConfig().set("Eggs.Ultra.Chance", "60%");
        eggFile.getConfig().set("Eggs.Insane.Name", "&5Insane Egg");
        eggFile.getConfig().set("Eggs.Insane.Recipe", Arrays.asList("1:EGG", "5:EMERALD"));
        eggFile.getConfig().set("Eggs.Insane.Cost", 50);
        eggFile.getConfig().set("Eggs.Insane.Chance", "100%");
        eggFile.saveConfig();
    }

    @Override
    public void onDisable() {
        mobFile.saveConfig();
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        console.sendMessage(Methods.formatText("&a============================="));
        console.sendMessage(Methods.formatText("&7UltimateCatcher " + this.getDescription().getVersion() + " by &5Songoda <3!"));
        console.sendMessage(Methods.formatText("&7Action: &cDisabling&7..."));
        console.sendMessage(Methods.formatText("&a============================="));
    }

    public void reload() {
        this.mobFile = new ConfigWrapper(this, "", "mobs.yml");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode"));
        this.locale.reloadMessages();
        this.settingsManager.reloadConfig();
    }

    public ServerVersion getServerVersion() {
        return serverVersion;
    }

    public boolean isServerVersion(ServerVersion version) {
        return serverVersion == version;
    }

    public boolean isServerVersion(ServerVersion... versions) {
        return ArrayUtils.contains(versions, serverVersion);
    }

    public boolean isServerVersionAtLeast(ServerVersion version) {
        return serverVersion.ordinal() >= version.ordinal();
    }

    public Stacker getStacker() {
        return stacker;
    }

    public Locale getLocale() {
        return locale;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public EggManager getEggManager() {
        return eggManager;
    }

    public EntityListeners getEntityListeners() {
        return entityListeners;
    }

    public ConfigWrapper getMobFile() {
        return mobFile;
    }

    public Economy getEconomy() {
        return economy;
    }
}
