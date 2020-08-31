package com.songoda.ultimatecatcher.listeners;

import com.songoda.core.nms.NmsManager;
import com.songoda.core.nms.nbt.NBTItem;
import com.songoda.ultimatecatcher.settings.Settings;
import com.songoda.ultimatecatcher.utils.EntityUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockFace;
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

        if (event.getBlock().getType() != Material.DISPENSER) return;

        NBTItem nbtItem = NmsManager.getNbt().of(item);
        if (nbtItem.has("UCI")) {
            event.setCancelled(true);
            return;
        }
        if (nbtItem.has("UC")) {
            if (Settings.STOP_DISPENSER_IN_WORLD.getBoolean()) {
                event.setCancelled(true);
            } else {
                MaterialData materialData = event.getBlock().getState().getData();
                Dispenser dispenser = (Dispenser) materialData;
                BlockFace face = dispenser.getFacing();
                EntityUtils.spawnEntity(event.getBlock().getRelative(face).getLocation().add(.5, 0, .5), item);
            }
            return;
        }


        // Legacy stuff
        if (item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
            String displayName = item.getItemMeta().getDisplayName()
                    .replace(String.valueOf(ChatColor.COLOR_CHAR), "");
            if (displayName.startsWith("UCI")) { //ToDo: YOU need to figure out what this is.
                event.setCancelled(true);
            } else if (Settings.STOP_DISPENSER_IN_WORLD.getBoolean()) {
                event.setCancelled(true);
            } else if (displayName.startsWith("UC-")) {
                MaterialData materialData = event.getBlock().getState().getData();
                Dispenser dispenser = (Dispenser) materialData;
                BlockFace face = dispenser.getFacing();

                String[] split = item.getItemMeta().getDisplayName().split("~");
                String json = split[0].replace(String.valueOf(ChatColor.COLOR_CHAR), "");

                EntityUtils.spawnEntity(event.getBlock().getRelative(face).getLocation().add(.5, 0, .5), json);
            }
        }
    }
}
