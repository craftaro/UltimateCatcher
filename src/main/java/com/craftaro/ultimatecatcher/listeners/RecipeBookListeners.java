package com.craftaro.ultimatecatcher.listeners;

import com.craftaro.ultimatecatcher.settings.Settings;
import com.craftaro.ultimatecatcher.UltimateCatcher;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class RecipeBookListeners implements Listener {

    private final UltimateCatcher plugin;
    public RecipeBookListeners(UltimateCatcher plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (Settings.USE_RECIPE_BOOK.getBoolean())
            event.getPlayer().discoverRecipes(plugin.getRegisteredRecipes());
    }
}
