package com.songoda.ultimatecatcher.hook;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface IExternalHook
{
	public boolean shouldStopCapture(Player attemptingPlayer, Entity entityToCapture);
}
