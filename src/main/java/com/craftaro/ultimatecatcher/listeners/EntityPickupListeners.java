package com.craftaro.ultimatecatcher.listeners;

import com.craftaro.ultimatecatcher.UltimateCatcher;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class EntityPickupListeners implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEggPoke(EntityPickupItemEvent event) {
        if (UltimateCatcher.getInstance().getEntityListeners().getEggs().containsKey(event.getItem().getUniqueId())) event.setCancelled(true);
    }
}
