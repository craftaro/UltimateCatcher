package com.songoda.ultimatecatcher.hook.hooks;

import com.songoda.ultimatecatcher.hook.ExternalHook;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class WorldGuardHook implements ExternalHook {

    @Override
    public boolean shouldStopCapture(Player attemptingPlayer, Entity entityToCapture) {
        return !com.songoda.core.hooks.WorldGuardHook.isBuildAllowed(entityToCapture.getLocation());
    }

}
