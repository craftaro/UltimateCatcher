package com.songoda.ultimatecatcher.listeners;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.nms.NmsManager;
import com.songoda.core.nms.nbt.NBTItem;
import com.songoda.ultimatecatcher.UltimateCatcher;
import com.songoda.ultimatecatcher.settings.Settings;
import com.songoda.ultimatecatcher.utils.EntityUtils;
import com.songoda.ultimatecatcher.utils.OldEntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
            if (!Settings.STOP_DISPENSER_IN_WORLD.getBoolean()) {
                MaterialData materialData = event.getBlock().getState().getData();
                Dispenser dispenser = (Dispenser) materialData;
                BlockFace face = dispenser.getFacing();
                Location location = event.getBlock().getRelative(face).getLocation().add(.5, 0, .5);
                if (nbtItem.has("serialized_entity"))
                    EntityUtils.spawnEntity(location, item);
                else
                    OldEntityUtils.spawnEntity(location, item);

                Bukkit.getScheduler().runTaskLater(UltimateCatcher.getInstance(), () -> {
                    Entity entity = location.getWorld().getNearbyEntities(location, 1, 1, 1).stream()
                            .filter(e -> e.getCustomName() != null).findFirst().orElse(null);
                    if (entity != null)
                        entity.remove();
                }, 0L);

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

                OldEntityUtils.spawnEntity(event.getBlock().getRelative(face).getLocation().add(.5, 0, .5), json);
            }
        }
    }
}
