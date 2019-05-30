package com.songoda.ultimatecatcher;

import com.songoda.ultimatecatcher.command.CommandManager;
import com.songoda.ultimatecatcher.listeners.InteractListeners;
import com.songoda.ultimatecatcher.stacker.Stacker;
import com.songoda.ultimatecatcher.stacker.UltimateStacker;
import com.songoda.ultimatecatcher.tasks.EggTrackingTask;
import com.songoda.ultimatecatcher.utils.Methods;
import com.songoda.ultimatecatcher.utils.Metrics;
import com.songoda.ultimatecatcher.utils.settings.SettingsManager;
import com.songoda.ultimatestacker.utils.ConfigWrapper;
import com.songoda.update.utils.ServerVersion;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class UltimateCatcher extends JavaPlugin {

    private static UltimateCatcher INSTANCE;
    private References references;

    private ConfigWrapper mobFile = new ConfigWrapper(this, "", "mobs.yml");

    private Stacker stacker;

    private Locale locale;
    private CommandManager commandManager;
    private SettingsManager settingsManager;

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

        for (EntityType value : EntityType.values()) {
            if (value.isSpawnable() && value.isAlive() && !value.toString().contains("ARMOR")) {
                mobFile.getConfig().addDefault("Mobs." + value.name() + ".Enabled", true);
            }
        }
        mobFile.getConfig().options().copyDefaults(true);
        mobFile.saveConfig();

        PluginManager pluginManager = Bukkit.getPluginManager();

        if (pluginManager.isPluginEnabled("UltimateStacker"))
            stacker = new UltimateStacker();

        Bukkit.getPluginManager().registerEvents(new InteractListeners(this), this);

        String langMode = getConfig().getString("System.Language Mode");
        Locale.init(this);
        Locale.saveDefaultLocale("en_US");

        this.references = new References();

        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode", langMode));

        EggTrackingTask.startTask(this);

        // Starting Metrics
        new Metrics(this);

        console.sendMessage(Methods.formatText("&a============================="));
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
        String langMode = getConfig().getString("System.Language Mode");
        this.mobFile = new ConfigWrapper(this, "", "mobs.yml");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode", langMode));
        this.locale.reloadMessages();
        this.references = new References();
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

    public References getReferences() {
        return references;
    }

    public ConfigWrapper getMobFile() {
        return mobFile;
    }
}
