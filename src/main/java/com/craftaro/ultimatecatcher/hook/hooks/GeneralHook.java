package com.craftaro.ultimatecatcher.hook.hooks;

import com.craftaro.ultimatecatcher.hook.ExternalHook;
import com.craftaro.core.hooks.ProtectionManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class GeneralHook implements ExternalHook {

    /* Should allow compatibility with the
    craftaroCore supported protection hooks.

    As of writing this it supports:
        BentoBox
        GriefPrevention
        Lands
        Redprotect
        UltimateClaims
    */

    @Override
    public boolean shouldStopCapture(Player attemptingPlayer, Entity entityToCapture) {
        return !ProtectionManager.canPlace(attemptingPlayer, entityToCapture.getLocation());
    }

}
