package com.songoda.ultimatecatcher.hook;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;

public class WorldGuardHook implements IExternalHook
{
	@Override
	public boolean shouldStopCapture(Player attemptingPlayer, Entity entityToCapture) {
		LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(attemptingPlayer);
		Location loc = BukkitAdapter.adapt(entityToCapture.getLocation());
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		return !(container.createQuery().testBuild(loc, localPlayer) || WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localPlayer, localPlayer.getWorld()));
	}

}
