package com.songoda.ultimatecatcher.economy;

import com.songoda.ultimatecatcher.UltimateCatcher;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.entity.Player;

public class PlayerPointsEconomy implements Economy {

    private final UltimateCatcher plugin;

    private final PlayerPoints playerPoints;

    public PlayerPointsEconomy(UltimateCatcher plugin) {
        this.plugin = plugin;

        this.playerPoints = (PlayerPoints) plugin.getServer().getPluginManager().getPlugin("PlayerPoints");
    }

    private int convertAmount(double amount) {
        return (int) Math.ceil(amount);
    }

    @Override
    public boolean hasBalance(Player player, double cost) {
        int amount = convertAmount(cost);
        return playerPoints.getAPI().look(player.getUniqueId()) >= amount;

    }

    @Override
    public boolean withdrawBalance(Player player, double cost) {
        int amount = convertAmount(cost);
        return playerPoints.getAPI().take(player.getUniqueId(), amount);

    }
}
