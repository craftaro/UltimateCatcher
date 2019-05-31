package com.songoda.ultimatecatcher.listeners;

import com.songoda.ultimatecatcher.utils.Methods;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dispenser;
import org.bukkit.material.MaterialData;

public class DispenserListeners implements Listener {
    
    @EventHandler
    public void onDispense(BlockDispenseEvent event) {
        ItemStack item = event.getItem();
        if (item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().replace(String.valueOf(ChatColor.COLOR_CHAR), "").startsWith("UC-")) {
            MaterialData materialData = event.getBlock().getState().getData();
            Dispenser dispenser = (Dispenser) materialData;
            BlockFace face = dispenser.getFacing();

            event.setCancelled(true);


            ItemStack toRemove = item.clone();
            toRemove.setAmount(1);

            String[] split = item.getItemMeta().getDisplayName().split("~");
            String json = split[0].replace(String.valueOf(ChatColor.COLOR_CHAR), "");

            Methods.spawnEntity(event.getBlock().getRelative(face).getLocation().add(.5,0,.5), json);

            ((org.bukkit.block.Dispenser)event.getBlock().getState()).getInventory().removeItem(toRemove);

        }
    }
}
