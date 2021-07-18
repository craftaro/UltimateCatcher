package com.songoda.ultimatecatcher.listeners;

import com.songoda.ultimatecatcher.UltimateCatcher;
import com.songoda.ultimatecatcher.settings.Settings;
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
