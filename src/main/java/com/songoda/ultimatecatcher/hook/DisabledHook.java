package com.songoda.ultimatecatcher.hook;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class DisabledHook implements IExternalHook
{

	@Override
	public boolean shouldStopCapture(Player attemptingPlayer, Entity entityToCapture) {
		return false;
	}

}
