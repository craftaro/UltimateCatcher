package com.songoda.ultimatecatcher.hook.hooks;

import com.songoda.core.hooks.ProtectionManager;
import com.songoda.ultimatecatcher.hook.ExternalHook;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class GeneralHook implements ExternalHook {

    /* Should allow compatibility with the
    SongodaCore supported protection hooks.

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
