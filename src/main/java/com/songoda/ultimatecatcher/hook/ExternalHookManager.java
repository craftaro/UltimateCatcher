package com.songoda.ultimatecatcher.hook;

import com.songoda.ultimatecatcher.UltimateCatcher;
import com.songoda.ultimatecatcher.hook.hooks.WorldGuardHook;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class ExternalHookManager {

    private final Set<ExternalHook> registeredHooks = new HashSet<>();

    public ExternalHookManager(UltimateCatcher plugin) {
        if (plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard"))
            registerEternalHook(new WorldGuardHook());
    }

    public void registerEternalHook(ExternalHook hook) {
        this.registeredHooks.add(hook);
    }

    /* Called when an entity attempts to be caught, will return true if the event should be cancelled */
    public boolean shouldStopCapture(Player attemptingPlayer, Entity entityToCapture) {
        for (ExternalHook hook : registeredHooks)
            if (hook.shouldStopCapture(attemptingPlayer, entityToCapture))
                return true;
        return false;
    }
}
