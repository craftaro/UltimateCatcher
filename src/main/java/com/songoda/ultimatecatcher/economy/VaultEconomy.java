package com.songoda.ultimatecatcher.economy;

import com.songoda.ultimatecatcher.UltimateCatcher;
import org.bukkit.entity.Player;

public class VaultEconomy implements Economy {

    private final UltimateCatcher plugin;

    private final net.milkbowl.vault.economy.Economy vault;

    public VaultEconomy(UltimateCatcher plugin) {
        this.plugin = plugin;

        this.vault = plugin.getServer().getServicesManager().
                getRegistration(net.milkbowl.vault.economy.Economy.class).getProvider();
    }

    @Override
    public boolean hasBalance(Player player, double cost) {
        return vault.has(player, cost);
    }

    @Override
    public boolean withdrawBalance(Player player, double cost) {
        return vault.withdrawPlayer(player, cost).transactionSuccess();
    }
}
