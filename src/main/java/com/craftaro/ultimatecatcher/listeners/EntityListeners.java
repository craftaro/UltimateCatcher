package com.craftaro.ultimatecatcher.listeners;

import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.ultimatecatcher.egg.CEgg;
import com.craftaro.ultimatecatcher.settings.Settings;
import com.craftaro.core.compatibility.CompatibleHand;
import com.craftaro.core.compatibility.CompatibleMaterial;
import com.craftaro.core.compatibility.CompatibleParticleHandler;
import com.craftaro.core.compatibility.CompatibleSound;
import com.craftaro.core.compatibility.ServerVersion;
import com.craftaro.core.hooks.EconomyManager;
import com.craftaro.core.hooks.EntityStackerManager;
import com.craftaro.core.locale.Message;
import com.craftaro.core.third_party.de.tr7zw.nbtapi.NBTItem;
import com.craftaro.core.utils.ItemUtils;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.ultimatecatcher.UltimateCatcher;
import com.craftaro.ultimatecatcher.tasks.EggTrackingTask;
import com.craftaro.ultimatecatcher.utils.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.WaterMob;
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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class EntityListeners implements Listener {

    private final UltimateCatcher plugin;

    private final Map<UUID, UUID> eggs = new HashMap<>();
    private final Set<UUID> oncePerTick = new HashSet<>();

    public EntityListeners(UltimateCatcher plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySmack(PlayerInteractEntityEvent event) {
        ItemStack item = event.getPlayer().getItemInHand();
        if (item.getType() == Material.AIR) return;

        if (useEgg(event.getPlayer(), item, CompatibleHand.getHand(event)) || new NBTItem(item).hasKey("UC"))
            event.setCancelled(true);
    }

    private boolean useEgg(Player player, ItemStack item, CompatibleHand hand) {
        if (item.getItemMeta().hasDisplayName()) {
            String name = item.getItemMeta().getDisplayName().replace(String.valueOf(ChatColor.COLOR_CHAR), "");

            if ((item.getType() != Material.AIR
                    && !new NBTItem(item).hasKey("UCI"))
                    // Legacy Crap
                    && !name.startsWith("UCI;") && !name.startsWith("UCI-")) return false;

            if (oncePerTick.contains(player.getUniqueId())) return true;

            String eggType;
            if (new NBTItem(item).hasKey("UCI")) {
                eggType = new NBTItem(item).getString("type");
            } else {
                // More legacy crap.
                String[] split = name.split(";");
                eggType = split.length == 3 ? split[1] : plugin.getEggManager().getFirstEgg().getKey();
            }

            Location location = player.getEyeLocation();
            Egg egg = location.getWorld().spawn(location, Egg.class);
            egg.setCustomName("UCI;" + eggType);
            egg.setShooter(player);

            oncePerTick.add(player.getUniqueId());
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> oncePerTick.remove(player.getUniqueId()), 1L);

            eggs.put(egg.getUniqueId(), player.getUniqueId());

            location.getWorld().playSound(location, CompatibleSound.ENTITY_EGG_THROW.getSound(), 1L, 1L);

            egg.setVelocity(player.getLocation().getDirection().normalize().multiply(2));

            if (player.getGameMode() != GameMode.CREATIVE)
                ItemUtils.takeActiveItem(player, hand);
            return true;
        }

        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void InventorySnotch(InventoryPickupItemEvent event) {
        if (eggs.containsKey(event.getItem().getUniqueId())) event.setCancelled(true);
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
                && useEgg(player, item, hand)) {
            event.setCancelled(true);
        } else if (item.getItemMeta().hasDisplayName()
                && (item.getItemMeta().getDisplayName().replace(String.valueOf(ChatColor.COLOR_CHAR), "").startsWith("UC-") || new NBTItem(item).hasKey("UC"))) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) return;
            event.setCancelled(true);

            if (Settings.BLOCKED_SPAWNING_WORLDS.getStringList().contains(player.getEyeLocation().getWorld().getName()) && !player.hasPermission("ultimatecatcher.bypass.blockedspawningworld")) {
                plugin.getLocale().getMessage("event.catch.blockedspawningworld").processPlaceholder("world", player.getWorld().getName()).sendPrefixedMessage(player);
                return;
            }

            Location location = player.getEyeLocation().clone();

            NBTItem nbtItem = new NBTItem(item);
            nbtItem.setBoolean("UCI", true);
            ItemStack toThrow = nbtItem.getItem();

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
                    toThrow.removeEnchantment(Enchantment.ARROW_KNOCKBACK), 50);
            toThrow.setAmount(1);

            // When you see it just know it wasn't anyone on our teams idea.
            toThrow.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 69);

            Item egg = location.getWorld().dropItem(location, toThrow);

            egg.setPickupDelay(9999);

            eggs.put(egg.getUniqueId(), player.getUniqueId());

            location.getWorld().playSound(location, CompatibleSound.ENTITY_EGG_THROW.getSound(), 1L, 1L);

            egg.setVelocity(player.getLocation().getDirection().normalize().multiply(2));

            EggTrackingTask.addEgg(egg);
            if (player.getGameMode() != GameMode.CREATIVE)
                ItemUtils.takeActiveItem(player, hand);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSmack(ProjectileHitEvent event) {
        if (event.getEntity().getType() != EntityType.EGG) return;

        Egg egg = (Egg) event.getEntity();
        if (egg.getCustomName() == null || !egg.getCustomName().startsWith("UCI") || egg.isOnGround()) return;

        String[] split = egg.getCustomName().split(";");

        if (split.length < 2) return;

        CEgg catcher = plugin.getEggManager().getEgg(split[1]);

        if (catcher == null) return;

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
                egg.getWorld().getNearbyEntities(egg.getLocation(), 3, 3, 3).stream()
                        .filter(entity -> entity instanceof LivingEntity
                                && entity.getTicksLived() <= 20
                                && entity.getType() != EntityType.PLAYER
                                && entity.getType() == EntityType.CHICKEN).findFirst().ifPresent(Entity::remove), 0L);

        Entity entity = null;

        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11))
            entity = event.getHitEntity();
        else {
            Optional<Entity> found = egg.getWorld().getNearbyEntities(egg.getLocation(), 2, 2, 2).stream()
                    .filter(e -> e instanceof LivingEntity
                            && e.getType() != EntityType.PLAYER
                            && e.getTicksLived() > 20)
                    .sorted(Comparator.comparingDouble(e -> e.getLocation().distance(egg.getLocation()))).findFirst();
            if (found.isPresent()) {
                entity = found.get();
            }
        }

        if (!(entity instanceof LivingEntity) || entity.getType() == EntityType.PLAYER) {
            reject(egg, catcher, false);
            return;
        }

        LivingEntity livingEntity = (LivingEntity) entity;

        ConfigurationSection configurationSection = plugin.getMobConfig();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(eggs.get(egg.getUniqueId()));

        String formatted = EntityUtils.getFormattedEntityType(entity.getType());

        if (!offlinePlayer.isOnline() || formatted == null) {
            reject(egg, catcher, true);
            return;
        }

        double cost = catcher.getCost();
        Player player = offlinePlayer.getPlayer();

        if (Settings.BLOCKED_CATCHING_WORLDS.getStringList().contains(player.getWorld().getName()) && !player.hasPermission("ultimatecatcher.bypass.blockedcatchingworld")) {
            plugin.getLocale().getMessage("event.catch.blockedcatchingworld").processPlaceholder("world", player.getWorld().getName()).sendPrefixedMessage(player);
            reject(egg, catcher, true);
            return;
        }

        if (Bukkit.getPluginManager().isPluginEnabled("Citizens")) {
            if (net.citizensnpcs.api.CitizensAPI.getNPCRegistry().isNPC(entity)) {
                reject(egg, catcher, true);
                return;
            }
        }

        if (plugin.getExternalHookManager().shouldStopCapture(player, entity)) {
            reject(egg, catcher, true);
            return;
        }

        String val = "Mobs." + entity.getType().name() + ".Enabled";
        if (!configurationSection.contains(val)) {
            reject(egg, catcher, true);
            return;
        }
        if (!configurationSection.getBoolean(val) && !player.hasPermission("ultimatecatcher.bypass.disabled")) {
            plugin.getLocale().getMessage("event.catch.notenabled")
                    .processPlaceholder("type", formatted).getMessage();
            reject(egg, catcher, true);
            return;
        }

        if (!(player.hasPermission("ultimatecatcher.catch.*")
                || (player.hasPermission("ultimatecatcher.catch.peaceful." + entity.getType().name())
                && (entity instanceof Animals || entity instanceof Ambient || entity instanceof WaterMob
                || entity instanceof Golem && !entity.getType().name().equals("SHULKER") || entity instanceof AbstractVillager))
                || (player.hasPermission("ultimatecatcher.catch.hostile." + entity.getType().name()))
                && (entity instanceof Monster || entity instanceof Boss
                || entity instanceof Flying || entity instanceof Slime || entity.getType().name().equals("SHULKER")))) {

            plugin.getLocale().getMessage("event.catch.notenabled")
                    .processPlaceholder("type", EntityUtils.getFormattedEntityType(entity.getType()))
                    .sendPrefixedMessage(player);

            reject(egg, catcher, true);
            return;
        }
        int ch = catcher.getChance();
        double rand = Math.random() * 100;
        if (!(rand - ch < 0 || ch == 100) && !player.hasPermission("ultimatecatcher.bypass.chance")) {

            egg.getWorld().playSound(egg.getLocation(), CompatibleSound.ENTITY_VILLAGER_NO.getSound(), 1L, 1L);

            plugin.getLocale().getMessage("event.catch.failed")
                    .processPlaceholder("type", EntityUtils.getFormattedEntityType(entity.getType()))
                    .sendPrefixedMessage(player);
            return;
        }

        if (entity instanceof Tameable
                && Settings.REJECT_TAMED.getBoolean()
                && ((Tameable) entity).isTamed()
                && ((Tameable) entity).getOwner().getUniqueId() != player.getUniqueId()) {
            plugin.getLocale().getMessage("event.catch.notyours").sendPrefixedMessage(player);
            reject(egg, catcher, true);
            return;
        }

        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14)) {
            if (entity instanceof Fox) {
                AnimalTamer tamer = ((Fox) entity).getFirstTrustedPlayer();
                if (tamer != null && !tamer.getUniqueId().equals(player.getUniqueId())
                        && Settings.REJECT_TAMED.getBoolean()) {
                    plugin.getLocale().getMessage("event.catch.notyours").sendPrefixedMessage(player);
                    reject(egg, catcher, true);
                    return;
                }
            }
        }

        if (EconomyManager.isEnabled() && cost != 0 && !player.hasPermission("ultimatecatcher.bypass.free")) {
            if (EconomyManager.hasBalance(player, cost))
                EconomyManager.withdrawBalance(player, cost);
            else {

                plugin.getLocale().getMessage("event.catch.cantafford")
                        .processPlaceholder("amount", cost)
                        .processPlaceholder("type", EntityUtils.getFormattedEntityType(entity.getType()))
                        .sendPrefixedMessage(player);

                reject(egg, catcher, true);
                return;
            }
        }

        PlayerInteractEvent playerInteractEvent = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, null, entity.getLocation().getBlock(), BlockFace.UP);
        Bukkit.getPluginManager().callEvent(playerInteractEvent);
        if (playerInteractEvent.isCancelled()) {
            reject(egg, catcher, true);
            return;
        }

        egg.remove();

        Optional<XMaterial> spawnEgg = CompatibleMaterial.getSpawnEgg(entity.getType());
        if (spawnEgg == null) {
            return;
        }
        ItemStack item = spawnEgg.get().parseItem();

        if (EntityStackerManager.getStacker() != null && EntityStackerManager.isStacked(livingEntity))
            EntityStackerManager.getStacker().removeOne(livingEntity);
        else
            entity.remove();

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.getLocale().getMessage("general.catcher.spawn")
                .processPlaceholder("type",
                        TextUtils.formatText(entity.getCustomName() != null
                                && !entity.getCustomName().contains(String.valueOf(ChatColor.COLOR_CHAR))
                                && !(EntityStackerManager.getStacker() != null && !EntityStackerManager.isStacked(livingEntity)) ? entity.getCustomName()
                                : EntityUtils.getFormattedEntityType(entity.getType()))).getMessage());

        String typeLine = plugin.getLocale().getMessage("general.catcherinfo.type")
                .processPlaceholder("value", EntityUtils.getFormattedEntityType(entity.getType()))
                .getMessage();

        double health = Math.round(livingEntity.getHealth() * 100.0) / 100.0;
        double max = livingEntity.getMaxHealth();

        String healthLine = plugin.getLocale().getMessage("general.catcherinfo.health")
                .processPlaceholder("value", (health == max ? plugin.getLocale().getMessage("general.catcher.max").getMessage() : health + "/" + max)).getMessage();

        List<String> lore = new ArrayList<>();

        // Parse lore according to config format.
        for (String line : Settings.CATCHER_CAUGHT_LORE_FORMAT.getStringList()) {

            Message messageLine = new Message(line);

            if (line.toLowerCase().contains("%age%")) {
                if (entity instanceof Ageable) {
                    lore.add(plugin.getLocale().getMessage("general.catcherinfo.age").processPlaceholder("value", ((Ageable) entity).isAdult() ? plugin.getLocale().getMessage("general.catcher.adult").getMessage() : plugin.getLocale().getMessage("general.catcher.baby").getMessage()).getMessage());
                }
                continue;
            }

            if (line.toLowerCase().contains("%tamed%")) {
                if (entity instanceof Tameable && ((Tameable) entity).isTamed()) {
                    lore.add(plugin.getLocale().getMessage("general.catcherinfo.tamed").getMessage());
                }
                continue;
            }

            if (line.toLowerCase().contains("%trusted%")) {
                if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14) && entity instanceof Fox) {
                    AnimalTamer tamer = ((Fox) entity).getFirstTrustedPlayer();
                    if (tamer != null && !tamer.getUniqueId().equals(player.getUniqueId())
                            && Settings.REJECT_TAMED.getBoolean()) {
                        lore.add(plugin.getLocale().getMessage("general.catcherinfo.trusted").getMessage());
                    }
                }
                continue;
            }

            lore.add(messageLine.processPlaceholder("health", healthLine)
                    .processPlaceholder("type", typeLine).getMessage());
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        plugin.getLocale().getMessage("event.catch.success")
                .processPlaceholder("type", EntityUtils.getFormattedEntityType(entity.getType()))
                .sendPrefixedMessage(player);

        entity.getWorld().dropItem(event.getEntity().getLocation(), EntityUtils.serializeEntity(item, livingEntity));

        CompatibleParticleHandler.spawnParticles(CompatibleParticleHandler.ParticleType.SMOKE_NORMAL, entity.getLocation(), 100, .5, .5, .5);
        entity.getWorld().playSound(entity.getLocation(), CompatibleSound.ITEM_FIRECHARGE_USE.getSound(), 1L, 1L);
    }

    private void reject(Egg egg, CEgg catcher, boolean sound) {
        if (sound)
            egg.getWorld().playSound(egg.getLocation(), CompatibleSound.ENTITY_VILLAGER_NO.getSound(), 1L, 1L);

        egg.getWorld().dropItem(egg.getLocation(), catcher.toItemStack());
        egg.remove();
    }


    public Map<UUID, UUID> getEggs() {
        return eggs;
    }
}
