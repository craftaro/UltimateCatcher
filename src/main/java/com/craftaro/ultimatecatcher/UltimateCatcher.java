package com.craftaro.ultimatecatcher;

import com.craftaro.core.SongodaCore;
import com.craftaro.core.SongodaPlugin;
import com.craftaro.core.dependency.Dependency;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.ultimatecatcher.commands.CommandGive;
import com.craftaro.ultimatecatcher.commands.CommandReload;
import com.craftaro.ultimatecatcher.commands.CommandSettings;
import com.craftaro.ultimatecatcher.egg.CEgg;
import com.craftaro.ultimatecatcher.egg.EggHandler;
import com.craftaro.ultimatecatcher.egg.EggManager;
import com.craftaro.ultimatecatcher.hook.ExternalHookManager;
import com.craftaro.ultimatecatcher.listeners.DispenserListeners;
import com.craftaro.ultimatecatcher.listeners.EntityListeners;
import com.craftaro.ultimatecatcher.listeners.EntityPickupListeners;
import com.craftaro.ultimatecatcher.listeners.RecipeBookListeners;
import com.craftaro.ultimatecatcher.settings.Settings;
import com.craftaro.ultimatecatcher.tasks.EggTrackingTask;
import com.craftaro.core.commands.CommandManager;
import com.craftaro.core.compatibility.ServerVersion;
import com.craftaro.core.configuration.Config;
import com.craftaro.core.gui.GuiManager;
import com.craftaro.core.hooks.EconomyManager;
import com.craftaro.core.hooks.EntityStackerManager;
import com.craftaro.core.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.PluginManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class UltimateCatcher extends SongodaPlugin {

    private static UltimateCatcher INSTANCE;

    private final Config mobConfig = new Config(this, "mobs.yml");
    private final Config eggConfig = new Config(this, "eggs.yml");

    private final GuiManager guiManager = new GuiManager(this);
    private EggManager eggManager;
    private CommandManager commandManager;
    private EntityListeners entityListeners;
    private ExternalHookManager externalHookManager;

    private final Set<NamespacedKey> registeredRecipes = new HashSet<>();
    private EggHandler eggHandler;

    public static UltimateCatcher getInstance() {
        return INSTANCE;
    }

    @Override
    protected Set<Dependency> getDependencies() {
        return new HashSet<>();
    }

    @Override
    public void onPluginLoad() {
        INSTANCE = this;
    }

    @Override
    public void onPluginEnable() {
        // Run craftaro Updater
        SongodaCore.registerPlugin(this, 51, XMaterial.EGG);

        // Load Economy
        EconomyManager.load();

        // Setup Config
        Settings.setupConfig();
        this.setLocale(Settings.LANGUAGE_MODE.getString(), false);

        // Set economy preference
        EconomyManager.getManager().setPreferredHook(Settings.ECONOMY_PLUGIN.getString());

        // Load entity stack manager.
        EntityStackerManager.load();

        // Load hook manager
        externalHookManager = new ExternalHookManager(this);

        // Register commands
        this.commandManager = new CommandManager(this);
        this.commandManager.addMainCommand("uc")
                .addSubCommands(
                        new CommandGive(this),
                        new CommandSettings(guiManager),
                        new CommandReload(this)
                );

        this.eggManager = new EggManager();

        eggHandler = new EggHandler(this);

        // Setup Listeners
        guiManager.init();
        PluginManager pluginManager = Bukkit.getPluginManager();
        entityListeners = new EntityListeners(this, eggHandler);
        pluginManager.registerEvents(entityListeners, this);
        pluginManager.registerEvents(new DispenserListeners(), this);
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12))
            pluginManager.registerEvents(new EntityPickupListeners(this), this);
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14))
            pluginManager.registerEvents(new RecipeBookListeners(this), this);

        EggTrackingTask.startTask(this);

        // Load Mobs and Eggs
        setupMobs();
        setupEgg();

        registerRecipes();
    }

    private void registerRecipes() {

        // Unregister old
        for (NamespacedKey key : registeredRecipes) {
            Bukkit.removeRecipe(key);
        }

        // Register recipes
        if (Settings.USE_CATCHER_RECIPE.getBoolean()) {

            for (CEgg egg : eggManager.getRegisteredEggs()) {
                ShapelessRecipe shapelessRecipe = ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12)
                        ? new ShapelessRecipe(new NamespacedKey(this, egg.getKey()),
                        egg.toItemStack()) : new ShapelessRecipe(egg.toItemStack());

                for (String item : egg.getRecipe()) {
                    String[] split = item.split(":");
                    shapelessRecipe.addIngredient(Integer.parseInt(split[0]), Material.valueOf(split[1]));
                }

                if (Bukkit.addRecipe(shapelessRecipe) && ServerVersion.isServerVersionAtLeast(ServerVersion.V1_16))
                    this.registeredRecipes.add(shapelessRecipe.getKey());
            }
        }
    }

    private void removeLegacyRecipes() {
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_16))
            return;

        Iterator<Recipe> iterator = Bukkit.recipeIterator();
        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();
            if (eggManager.getRegisteredEggs().stream().anyMatch(egg -> egg.toItemStack().isSimilar(recipe.getResult()))) {
                iterator.remove();
            }
        }
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
                        .setDefault("Mobs." + value.name() + ".Display Name", TextUtils.formatText(value.name().toLowerCase().replace("_", " "), true));
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

        eggManager.loadEggs();
    }

    @Override
    public void onPluginDisable() {
    }

    @Override
    public void onDataLoad() {

    }

    @Override
    public void onConfigReload() {
        this.removeLegacyRecipes();

        this.setLocale(Settings.LANGUAGE_MODE.getString(), true);
        this.mobConfig.load();

        this.eggConfig.load();
        this.eggManager.loadEggs();
        this.registerRecipes();
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

    public ExternalHookManager getExternalHookManager() {
        return externalHookManager;
    }

    public Config getMobConfig() {
        return mobConfig;
    }

    public Config getEggConfig() {
        return eggConfig;
    }

    public Set<NamespacedKey> getRegisteredRecipes() {
        return Collections.unmodifiableSet(registeredRecipes);
    }

    public EggHandler getEggHandler() {
        return eggHandler;
    }
}
