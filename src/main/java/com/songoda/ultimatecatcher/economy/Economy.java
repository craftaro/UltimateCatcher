package com.songoda.ultimatecatcher.economy;

import org.bukkit.entity.Player;

public interface Economy {

    boolean hasBalance(Player player, double cost);

    boolean withdrawBalance(Player player, double cost);
}
