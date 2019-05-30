package com.songoda.ultimatecatcher.listeners;

import com.songoda.ultimatecatcher.UltimateCatcher;
import com.songoda.ultimatecatcher.tasks.EggTrackingTask;
import com.songoda.ultimatecatcher.utils.Methods;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InteractListeners implements Listener {

    private final UltimateCatcher plugin;

    private List<UUID> eggs = new ArrayList<>();

    public InteractListeners(UltimateCatcher plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInt(PlayerInteractEntityEvent event) {
        ItemStack item = event.getPlayer().getItemInHand();
        if (item.getType() == Material.AIR) return;

        if (useEgg(event.getPlayer(), item)) {
            event.setCancelled(true);
        }

    }

    private boolean useEgg(Player player, ItemStack item) {
        if (item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().replace(String.valueOf(ChatColor.COLOR_CHAR), "").startsWith("UCI-")) {
            Location location = player.getEyeLocation();
            Egg egg = location.getWorld().spawn(location, Egg.class);
            egg.setCustomName("UCI");

            eggs.add(egg.getUniqueId());

            location.getWorld().playSound(location, Sound.ENTITY_EGG_THROW, 1L, 1L);

            egg.setVelocity(player.getLocation().getDirection().normalize().multiply(2));

            Methods.takeItem(player, 1);
            return true;
        }
        return false;
    }

    @EventHandler
    public void onToss(PlayerInteractEvent event) {
        if (event.getItem() == null) return;

        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (!item.hasItemMeta()) return;

        if (useEgg(player, item)) {
            event.setCancelled(true);
        } else if (item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().replace(String.valueOf(ChatColor.COLOR_CHAR), "").startsWith("UC-")) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) return;
            event.setCancelled(true);

            Location location = player.getEyeLocation().clone();

            ItemStack toThrow = item.clone();
            toThrow.setAmount(1);
            Methods.setMax(item, 1);

            // When you see it just know it wasn't anyone on our teams idea.
            toThrow.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 69);

            Item egg = location.getWorld().dropItem(location, toThrow);

            egg.setPickupDelay(9999);

            eggs.add(egg.getUniqueId());

            location.getWorld().playSound(location, Sound.ENTITY_EGG_THROW, 1L, 1L);

            egg.setVelocity(player.getLocation().getDirection().normalize().multiply(2));

            EggTrackingTask.addEgg(egg);
            Methods.takeItem(player, 1);
        }
    }

    @EventHandler
    public void onSmack(ProjectileHitEvent event) {
        if (event.getEntity().getType() != EntityType.EGG) return;

        Egg egg = (Egg) event.getEntity();
        if (!egg.getCustomName().equals("UCI")) return;

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            Optional<Entity> found = egg.getWorld().getNearbyEntities(egg.getLocation(), 3, 3, 3).stream().filter(entity -> entity.getTicksLived() <= 20
                    && entity.getType() == EntityType.CHICKEN).findFirst();
            if (found.isPresent()) {
                if (found.get() instanceof LivingEntity) {
                    found.get().remove();
                }
            }
        }, 0L);

        Entity entity = event.getHitEntity();

        if (entity == null) {
            egg.getWorld().dropItem(egg.getLocation(), Methods.createCatcher());
            return;
        }

        ConfigurationSection configurationSection = plugin.getMobFile().getConfig();

        if (!configurationSection.getBoolean("Mobs." + entity.getType().name() + ".Enabled")) {
            egg.getWorld().dropItem(egg.getLocation(), Methods.createCatcher());
            return;
        }

        Material material = Material.matchMaterial(entity.getType() + "_SPAWN_EGG");
        if (material == null) return;

        if (plugin.getStacker().isStacked(entity))
            plugin.getStacker().removeOne(entity);
        else
            entity.remove();

        ItemStack item = new ItemStack(material);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Methods.convertToInvisibleString("UC-" + Methods.serializeEntity((LivingEntity) entity) + "~")
                + plugin.getLocale().getMessage("general.catcher.spawn", Methods.formatText(entity.getCustomName() != null && !plugin.getStacker().isStacked(entity) ? entity.getCustomName() : entity.getType().name().toLowerCase(), true)));
        item.setItemMeta(meta);

        entity.getWorld().dropItem(entity.getLocation(), item);

        entity.getWorld().spawnParticle(Particle.SMOKE_NORMAL, entity.getLocation(), 100, .5, .5, .5);
        entity.getWorld().playSound(entity.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1L, 1L);
        egg.remove();
    }

}
