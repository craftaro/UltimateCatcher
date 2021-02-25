package com.songoda.ultimatecatcher.hook;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.songoda.ultimatecatcher.UltimateCatcher;

public class ExternalHooks
{
	protected IExternalHook WORLDGUARD;
	
	public ExternalHooks(UltimateCatcher plugin) {
		
		IExternalHook disabled = new DisabledHook();
		
		// Load external hooks. If a plugin is not enabled, return a disabled hook which will present default behaviour wherever required
		WORLDGUARD = (plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard") ? new WorldGuardHook() : disabled);
	}
	
	/* Called when an entity attempts to be caught, will return true if the event should be cancelled */
	public boolean shouldStopCapture(Player attemptingPlayer, Entity entityToCapture) {
		if(WORLDGUARD.shouldStopCapture(attemptingPlayer, entityToCapture)) { return true; } 
		return false;
	}
}
