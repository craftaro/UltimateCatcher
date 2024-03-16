package com.craftaro.ultimatecatcher.listeners;

import com.craftaro.core.compatibility.CompatibleHand;
import com.craftaro.core.third_party.de.tr7zw.nbtapi.NBTItem;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.ultimatecatcher.UltimateCatcher;
import com.craftaro.ultimatecatcher.egg.EggHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class EntityListeners implements Listener {

    private final UltimateCatcher plugin;
    private final EggHandler eggHandler;

    public EntityListeners(UltimateCatcher plugin, EggHandler eggHandler) {
        this.plugin = plugin;
        this.eggHandler = eggHandler;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySmack(PlayerInteractEntityEvent event) {
        ItemStack item = event.getPlayer().getItemInHand();
        if (item.getType() == Material.AIR) return;

        if (eggHandler.useEgg(event.getPlayer(), item, CompatibleHand.getHand(event)) || new NBTItem(item).hasKey("UC"))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void InventorySnotch(InventoryPickupItemEvent event) {
        if (eggHandler.isEgg(event.getItem()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onStartExist(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
                && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.DISPENSE_EGG) return;

        Entity entity = event.getEntity();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (entity.getCustomName() != null && entity.getCustomName().replace(String.valueOf(ChatColor.COLOR_CHAR), "").startsWith("UC-"))
                entity.remove();
        }, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onToss(PlayerInteractEvent event) {
        CompatibleHand hand = CompatibleHand.getHand(event);

        if (event.getItem() == null
                || event.getClickedBlock() != null
                && event.getClickedBlock().getType() == XMaterial.SPAWNER.parseMaterial()) return;

        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (!item.hasItemMeta()) return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK
                && event.getAction() != Action.LEFT_CLICK_AIR
                && event.getAction() != Action.PHYSICAL
                && eggHandler.useEgg(player, item, hand)) {
            event.setCancelled(true);
        } else if (item.getItemMeta().hasDisplayName()
                && (item.getItemMeta().getDisplayName().replace(String.valueOf(ChatColor.COLOR_CHAR), "").startsWith("UC-") || new NBTItem(item).hasKey("UC"))) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) return;
            event.setCancelled(true);

            eggHandler.handleEggToss(player, item, hand);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSmack(ProjectileHitEvent event) {
        if (event.getEntity().getType() != EntityType.EGG) return;

        Egg egg = (Egg) event.getEntity();
        if (egg.getCustomName() == null || !egg.getCustomName().startsWith("UCI") || egg.isOnGround()) return;

        eggHandler.handleEggHit(egg, event);
    }
}