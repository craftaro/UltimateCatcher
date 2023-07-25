package com.craftaro.ultimatecatcher.hook.hooks;

import com.craftaro.ultimatecatcher.hook.ExternalHook;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class WorldGuardHook implements ExternalHook {

    @Override
    public boolean shouldStopCapture(Player attemptingPlayer, Entity entityToCapture) {
        return !com.craftaro.core.hooks.WorldGuardHook.isBuildAllowed(attemptingPlayer, entityToCapture.getLocation());
    }

}
