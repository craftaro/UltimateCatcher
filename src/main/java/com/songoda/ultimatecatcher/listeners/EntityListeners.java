package com.songoda.ultimatecatcher.listeners;

import com.songoda.ultimatecatcher.UltimateCatcher;
import com.songoda.ultimatecatcher.economy.Economy;
import com.songoda.ultimatecatcher.egg.CEgg;
import com.songoda.ultimatecatcher.tasks.EggTrackingTask;
import com.songoda.ultimatecatcher.utils.Methods;
import com.songoda.ultimatecatcher.utils.ServerVersion;
import com.songoda.ultimatecatcher.utils.settings.Setting;
import net.minecraft.server.v1_14_R1.EntityFox;
import net.minecraft.server.v1_14_R1.GameProfileSerializer;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftFox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class EntityListeners implements Listener {

    private final UltimateCatcher plugin;

    private Map<UUID, UUID> eggs = new HashMap<>();
    private Set<UUID> oncePerTick = new HashSet<>();

    public EntityListeners(UltimateCatcher plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntitySmack(PlayerInteractEntityEvent event) {
        boolean isOffHand = false;
        if (plugin.isServerVersionAtLeast(ServerVersion.V1_9)) {
            if (event.getHand() == EquipmentSlot.OFF_HAND) {
                isOffHand = true;
            }
        }
        ItemStack item = event.getPlayer().getItemInHand();
        if (item.getType() == Material.AIR) return;

        if (useEgg(event.getPlayer(), item, isOffHand))
            event.setCancelled(true);
    }

    private boolean useEgg(Player player, ItemStack item, boolean isOffHand) {
        if (item.getItemMeta().hasDisplayName()) {
            String name = item.getItemMeta().getDisplayName().replace(String.valueOf(ChatColor.COLOR_CHAR), "");
            if (!name.startsWith("UCI;") && !name.startsWith("UCI-")) return false;
            if (isOffHand || oncePerTick.contains(player.getUniqueId())) return true;

            String[] split = name.split(";");

            String eggType = split.length == 3 ? split[1] : plugin.getEggManager().getFirstEgg().getKey();

            Location location = player.getEyeLocation();
            Egg egg = location.getWorld().spawn(location, Egg.class);
            egg.setCustomName("UCI;" + eggType);
            egg.setShooter(player);

            oncePerTick.add(player.getUniqueId());
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> oncePerTick.remove(player.getUniqueId()), 1L);

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
        boolean isOffHand = false;
        if (plugin.isServerVersionAtLeast(ServerVersion.V1_9)) {
            if (event.getHand() == EquipmentSlot.OFF_HAND) {
                isOffHand = true;
            }
        }

        if (event.getItem() == null) return;

        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (!item.hasItemMeta()) return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK
                && event.getAction() != Action.LEFT_CLICK_AIR
                && event.getAction() != Action.PHYSICAL
                && useEgg(player, item, isOffHand)) {
            event.setCancelled(true);
        } else if (item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().replace(String.valueOf(ChatColor.COLOR_CHAR), "").startsWith("UC-")) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR) return;
            event.setCancelled(true);
            if (isOffHand) return;

            Location location = player.getEyeLocation().clone();

            ItemStack toThrow = item.clone();
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
                    toThrow.removeEnchantment(Enchantment.ARROW_KNOCKBACK), 50);
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
        if (egg.getCustomName() == null || !egg.getCustomName().startsWith("UCI") || egg.isOnGround()) return;

        String[] split = egg.getCustomName().split(";");

        CEgg catcher = plugin.getEggManager().getEgg(split[1]);

        if (catcher == null) return;

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
                egg.getWorld().getNearbyEntities(egg.getLocation(), 3, 3, 3).stream()
                        .filter(entity -> entity instanceof LivingEntity
                                && entity.getTicksLived() <= 20
                                && entity.getType() != EntityType.PLAYER
                                && entity.getType() == EntityType.CHICKEN).findFirst().ifPresent(Entity::remove), 0L);

        Entity entity = null;

        if (plugin.isServerVersionAtLeast(ServerVersion.V1_11))
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

        if (entity == null || entity.getType() == EntityType.PLAYER) {
            reject(egg, catcher, false);
            return;
        }

        ConfigurationSection configurationSection = plugin.getMobFile().getConfig();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(eggs.get(egg.getUniqueId()));

        String formatted = Methods.getFormattedEntityType(entity.getType());

        if (!offlinePlayer.isOnline() || formatted == null) {
            reject(egg, catcher, true);
            return;
        }

        Economy economy = plugin.getEconomy();
        double cost = catcher.getCost();
        Player player = offlinePlayer.getPlayer();


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
                        || entity instanceof Golem || entity instanceof AbstractVillager))
                || (player.hasPermission("ultimatecatcher.catch.hostile." + entity.getType().name()))
                    && (entity instanceof Monster || entity instanceof Boss
                        || entity instanceof Flying || entity instanceof Slime))) {

            plugin.getLocale().getMessage("event.catch.notenabled")
                    .processPlaceholder("type", Methods.getFormattedEntityType(entity.getType()))
                    .sendPrefixedMessage(player);

            reject(egg, catcher, true);
            return;
        }
        int ch = catcher.getChance();
        double rand = Math.random() * 100;
        if (!(rand - ch < 0 || ch == 100) && !player.hasPermission("ultimatecatcher.bypass.chance")) {

            if (plugin.isServerVersionAtLeast(ServerVersion.V1_9))
                egg.getWorld().playSound(egg.getLocation(), Sound.ENTITY_VILLAGER_NO, 1L, 1L);

            plugin.getLocale().getMessage("event.catch.failed")
                    .processPlaceholder("type", Methods.getFormattedEntityType(entity.getType()))
                    .sendPrefixedMessage(player);

            return;
        }

        if (entity instanceof Tameable
                && Setting.REJECT_TAMED.getBoolean()
                && ((Tameable) entity).isTamed()
                && ((Tameable) entity).getOwner().getUniqueId() != player.getUniqueId()) {
            plugin.getLocale().getMessage("event.catch.notyours").sendPrefixedMessage(player);
            reject(egg, catcher, true);
            return;

        }

        if (plugin.isServerVersionAtLeast(ServerVersion.V1_14)) {
            if (!isPlayerTrusted(entity, player)
                    && !isFoxWild(entity)
                    && Setting.REJECT_TAMED.getBoolean()) {
                plugin.getLocale().getMessage("event.catch.notyours").sendPrefixedMessage(player);
                reject(egg, catcher, true);
                return;
            }
        }

        if (economy != null && cost != 0 && !player.hasPermission("ultimatecatcher.bypass.free")) {
            if (economy.hasBalance(player, cost))
                economy.withdrawBalance(player, cost);
            else {

                plugin.getLocale().getMessage("event.catch.cantafford")
                        .processPlaceholder("amount", cost)
                        .processPlaceholder("type", Methods.getFormattedEntityType(entity.getType()))
                        .sendPrefixedMessage(player);

                reject(egg, catcher, true);
                return;
            }
        }
        egg.remove();


        ItemStack item;

        if (plugin.isServerVersionAtLeast(ServerVersion.V1_13)) {
            Material material = Material.matchMaterial(entity.getType().name().replace("MUSHROOM_COW", "MOOSHROOM") + "_SPAWN_EGG");
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
                + plugin.getLocale().getMessage("general.catcher.spawn")
                .processPlaceholder("type",
                        Methods.formatText(entity.getCustomName() != null
                                && !entity.getCustomName().contains(String.valueOf(ChatColor.COLOR_CHAR))
                                && !(plugin.getStacker() != null && !plugin.getStacker().isStacked(entity)) ? entity.getCustomName()
                                : Methods.getFormattedEntityType(entity.getType()))).getMessage());

        List<String> lore = new ArrayList<>();
        lore.add(plugin.getLocale().getMessage("general.catcherinfo.type")
                .processPlaceholder("value", Methods.getFormattedEntityType(entity.getType()))
                .getMessage());

        double health = Math.round(((LivingEntity) entity).getHealth() * 100.0) / 100.0;
        double max = ((LivingEntity) entity).getMaxHealth();

        lore.add(plugin.getLocale().getMessage("general.catcherinfo.health")
                .processPlaceholder("value", (health == max ? plugin.getLocale().getMessage("general.catcher.max") : health + "/" + max)).getMessage());

        if (entity instanceof Ageable)
            lore.add(plugin.getLocale().getMessage("general.catcherinfo.age").processPlaceholder("value", ((Ageable) entity).isAdult() ? plugin.getLocale().getMessage("general.catcher.adult") : plugin.getLocale().getMessage("general.catcher.baby")).getMessage());

        if (entity instanceof Tameable && ((Tameable) entity).isTamed())
            lore.add(plugin.getLocale().getMessage("general.catcherinfo.tamed").getMessage());

        if (plugin.isServerVersionAtLeast(ServerVersion.V1_14)) {
            if (isPlayerTrusted(entity, player) || !isFoxWild(entity))
                lore.add(plugin.getLocale().getMessage("general.catcherinfo.trusted").getMessage());
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        plugin.getLocale().getMessage("event.catch.success")
                .processPlaceholder("type", Methods.getFormattedEntityType(entity.getType()))
                .sendPrefixedMessage(player);

        entity.getWorld().dropItem(event.getEntity().getLocation(), item);

        if (plugin.isServerVersionAtLeast(ServerVersion.V1_9)) {
            entity.getWorld().spawnParticle(Particle.SMOKE_NORMAL, entity.getLocation(), 100, .5, .5, .5);
            entity.getWorld().playSound(entity.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1L, 1L);
        }
    }

    private void reject(Egg egg, CEgg catcher, boolean sound) {
        if (plugin.isServerVersionAtLeast(ServerVersion.V1_9) && sound)
            egg.getWorld().playSound(egg.getLocation(), Sound.ENTITY_VILLAGER_NO, 1L, 1L);

        egg.getWorld().dropItem(egg.getLocation(), catcher.toItemStack());
        egg.remove();
    }


    public Map<UUID, UUID> getEggs() {
        return eggs;
    }

    private boolean isPlayerTrusted(Entity entity, Player player) {
        // Foxes are only in 1.14, no reflection needed.
        if (!(entity instanceof Fox)) return false;
        Fox fox = (Fox) entity;
        if (!plugin.isServerVersionAtLeast(ServerVersion.V1_14)) return false;
        EntityFox entityFox = ((CraftFox) fox).getHandle();
        NBTTagCompound foxNBT = new NBTTagCompound();
        entityFox.b(foxNBT);
        if (foxNBT.getList("TrustedUUIDs", 10).contains(GameProfileSerializer.a(player.getUniqueId()))) return true;

        return false;
    }

    private boolean isFoxWild(Entity entity) {
        if (!(entity instanceof Fox)) return false;
        Fox fox = (Fox) entity;
        if (!plugin.isServerVersionAtLeast(ServerVersion.V1_14)) return false;
        EntityFox entityFox = ((CraftFox) fox).getHandle();
        NBTTagCompound foxNBT = new NBTTagCompound();
        entityFox.b(foxNBT);
        if (foxNBT.getList("TrustedUUIDs", 10) == null || foxNBT.getList("TrustedUUIDs", 10).isEmpty()) return true;
        return false;
    }
}
