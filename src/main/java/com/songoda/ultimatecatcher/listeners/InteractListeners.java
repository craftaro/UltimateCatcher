package com.songoda.ultimatecatcher.listeners;

import com.songoda.ultimatecatcher.UltimateCatcher;
import com.songoda.ultimatecatcher.economy.Economy;
import com.songoda.ultimatecatcher.tasks.EggTrackingTask;
import com.songoda.ultimatecatcher.utils.Methods;
import com.songoda.ultimatecatcher.utils.ServerVersion;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class InteractListeners implements Listener {

    private final UltimateCatcher plugin;

    private Map<UUID, UUID> eggs = new HashMap<>();

    public InteractListeners(UltimateCatcher plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySmack(PlayerInteractEntityEvent event) {
        ItemStack item = event.getPlayer().getItemInHand();
        if (item.getType() == Material.AIR) return;

        if (item.getItemMeta().hasDisplayName()
                && item.getItemMeta().getDisplayName().replace(String.valueOf(ChatColor.COLOR_CHAR), "")
                .startsWith("UCI-"))
            event.setCancelled(true);
    }

    private boolean useEgg(Player player, ItemStack item) {
        if (item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().replace(String.valueOf(ChatColor.COLOR_CHAR), "").startsWith("UCI-")) {
            Location location = player.getEyeLocation();
            Egg egg = location.getWorld().spawn(location, Egg.class);
            egg.setCustomName("UCI");
            egg.setShooter(player);

            eggs.put(egg.getUniqueId(), player.getUniqueId());

            if (plugin.isServerVersionAtLeast(ServerVersion.V1_9))
                location.getWorld().playSound(location, Sound.ENTITY_EGG_THROW, 1L, 1L);

            egg.setVelocity(player.getLocation().getDirection().normalize().multiply(2));

            Methods.takeItem(player, 1);
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onToss(PlayerInteractEvent event) {
        if (plugin.isServerVersionAtLeast(ServerVersion.V1_9)) {
            if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        }
        if (event.getItem() == null) return;

        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (!item.hasItemMeta()) return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR && useEgg(player, item)) {
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

            eggs.put(egg.getUniqueId(), player.getUniqueId());

            if (plugin.isServerVersionAtLeast(ServerVersion.V1_9))
                location.getWorld().playSound(location, Sound.ENTITY_EGG_THROW, 1L, 1L);

            egg.setVelocity(player.getLocation().getDirection().normalize().multiply(2));

            EggTrackingTask.addEgg(egg);
            Methods.takeItem(player, 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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

        Entity entity = null;

        if (plugin.isServerVersionAtLeast(ServerVersion.V1_11))
            entity = event.getHitEntity();
        else {
            Optional<Entity> found = egg.getWorld().getNearbyEntities(egg.getLocation(), 3, 3, 3).stream()
                    .filter(e -> e instanceof LivingEntity && e.getType() != EntityType.EGG)
                    .sorted(Comparator.comparingDouble(e -> e.getLocation().distance(egg.getLocation()))).findFirst();
            if (found.isPresent()) {
                entity = found.get();
            }
        }

        if (entity == null) {
            reject(egg, false);
            return;
        }

        String formatedType = Methods.formatText(entity.getType().name().toLowerCase(), true);
        ConfigurationSection configurationSection = plugin.getMobFile().getConfig();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(eggs.get(egg.getUniqueId()));

        if (!offlinePlayer.isOnline()) {
            reject(egg, true);
            return;
        }

        Economy economy = plugin.getEconomy();
        double cost = configurationSection.getDouble("Mobs." + entity.getType().name() + ".Cost");
        Player player = offlinePlayer.getPlayer();

        if (!configurationSection.getBoolean("Mobs." + entity.getType().name() + ".Enabled") && !player.hasPermission("ultimatecatcher.bypass.disabled")) {
            player.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.catch.notenabled", formatedType));
            reject(egg, true);
            return;
        }

        if (!player.hasPermission("ultimatecatcher.catch.*")
                || !player.hasPermission("ultimatecatcher.catch.peaceful." + entity.getType().name()) && entity instanceof Animals
                || !player.hasPermission("ultimatecatcher.catch.hostile." + entity.getType().name()) && entity instanceof Monster) {
            player.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.catch.notenabled", formatedType));
            reject(egg, true);
            return;
        }

        int ch = Integer.parseInt(configurationSection.getString("Mobs." + entity.getType().name() + ".Chance")
                .replace("%", ""));
        double rand = Math.random() * 100;
        if (!(rand - ch < 0 || ch == 100) && !player.hasPermission("ultimatecatcher.bypass.chance")) {

            if (plugin.isServerVersionAtLeast(ServerVersion.V1_9))
            egg.getWorld().playSound(egg.getLocation(), Sound.ENTITY_VILLAGER_NO, 1L, 1L);
            player.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.catch.failed", formatedType));
            return;
        }

        if (economy != null && cost != 0 && !player.hasPermission("ultimatecatcher.bypass.free")) {
            if (economy.hasBalance(player, cost))
                economy.withdrawBalance(player, cost);
            else {
                player.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.catch.cantafford",cost, formatedType));
                reject(egg, true);
                return;
            }
        }
        egg.remove();


        ItemStack item;

        if (plugin.isServerVersionAtLeast(ServerVersion.V1_13)) {
            Material material = Material.matchMaterial(entity.getType() + "_SPAWN_EGG");
            if (material == null) return;
            item = new ItemStack(material);
        } else {
            item = new ItemStack(Material.valueOf("MONSTER_EGG"), 1, entity.getType().getTypeId());
        }

        if (plugin.getStacker() != null && plugin.getStacker().isStacked(entity))
            plugin.getStacker().removeOne(entity);
        else
            entity.remove();

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Methods.convertToInvisibleString("UC-" + Methods.serializeEntity((LivingEntity) entity) + "~")
                + plugin.getLocale().getMessage("general.catcher.spawn",
                Methods.formatText(entity.getCustomName() != null
                        && !entity.getCustomName().contains(String.valueOf(ChatColor.COLOR_CHAR))
                        && (plugin.getStacker() != null && !plugin.getStacker().isStacked(entity)) ? entity.getCustomName()
                        : formatedType)));

        List<String> lore = new ArrayList<>();
        lore.add(plugin.getLocale().getMessage("general.catcherinfo.type", formatedType));

        double health = ((LivingEntity) entity).getHealth();
        double max = ((LivingEntity) entity).getMaxHealth();

        lore.add(plugin.getLocale().getMessage("general.catcherinfo.health", (health == max ? "Max" : health + "/" + max)));

        if (entity instanceof Ageable)
            lore.add(plugin.getLocale().getMessage("general.catcherinfo.age", (((Ageable) entity).isAdult() ? "Adult" : "Baby")));

        if (entity instanceof Tameable && ((Tameable) entity).isTamed())
            lore.add(plugin.getLocale().getMessage("general.catcherinfo.tamed"));

        meta.setLore(lore);
        item.setItemMeta(meta);

        player.sendMessage(plugin.getReferences().getPrefix() + plugin.getLocale().getMessage("event.catch.success", formatedType));

        entity.getWorld().dropItem(entity.getLocation(), item);

        if (plugin.isServerVersionAtLeast(ServerVersion.V1_9)) {
            entity.getWorld().spawnParticle(Particle.SMOKE_NORMAL, entity.getLocation(), 100, .5, .5, .5);
            entity.getWorld().playSound(entity.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1L, 1L);
        }
    }

    private void reject(Egg egg, boolean sound) {
        if (plugin.isServerVersionAtLeast(ServerVersion.V1_9) && sound)
            egg.getWorld().playSound(egg.getLocation(), Sound.ENTITY_VILLAGER_NO, 1L, 1L);
        egg.getWorld().dropItem(egg.getLocation(), Methods.createCatcher());
        egg.remove();
    }

}
