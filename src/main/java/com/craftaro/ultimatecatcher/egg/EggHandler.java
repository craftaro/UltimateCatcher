package com.craftaro.ultimatecatcher.egg;

import com.craftaro.core.compatibility.CompatibleHand;
import com.craftaro.core.compatibility.CompatibleMaterial;
import com.craftaro.core.compatibility.CompatibleParticleHandler;
import com.craftaro.core.compatibility.ServerVersion;
import com.craftaro.core.hooks.EconomyManager;
import com.craftaro.core.hooks.EntityStackerManager;
import com.craftaro.core.locale.Message;
import com.craftaro.core.third_party.de.tr7zw.nbtapi.NBTItem;
import com.craftaro.core.utils.ItemUtils;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.third_party.com.cryptomorin.xseries.XSound;
import com.craftaro.ultimatecatcher.UltimateCatcher;
import com.craftaro.ultimatecatcher.settings.Settings;
import com.craftaro.ultimatecatcher.tasks.EggTrackingTask;
import com.craftaro.ultimatecatcher.utils.EntityUtils;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;

import com.craftaro.core.compatibility.*;
import com.craftaro.core.hooks.*;
import com.craftaro.core.locale.Message;
import com.craftaro.core.third_party.de.tr7zw.nbtapi.NBTItem;
import com.craftaro.core.utils.*;
import com.craftaro.third_party.com.cryptomorin.xseries.*;
import com.craftaro.ultimatecatcher.*;
import com.craftaro.ultimatecatcher.settings.Settings;
import com.craftaro.ultimatecatcher.tasks.EggTrackingTask;
import com.craftaro.ultimatecatcher.utils.*;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class EggHandler {

    private static final String UCI_KEY = "UCI";
    private static final String TYPE_KEY = "type";
    private static final String MOBS_PATH = "Mobs";

    private final UltimateCatcher plugin;
    private final Map<UUID, UUID> eggs = Collections.synchronizedMap(new HashMap<>());
    private final Set<UUID> oncePerTick = Collections.synchronizedSet(new HashSet<>());

    public EggHandler(UltimateCatcher plugin) {
        this.plugin = plugin;
    }

    public boolean useEgg(Player player, ItemStack item, CompatibleHand hand) {
        if (!isUltimateCatcherEgg(item)) return false;
        if (oncePerTick.contains(player.getUniqueId())) return true;

        String eggType = getEggType(item);

        spawnEgg(player, eggType);
        takeEggFromPlayer(player, hand);

        return true;
    }

    private String getEggType(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);
        return nbtItem.hasKey(UCI_KEY) ? nbtItem.getString(TYPE_KEY) : plugin.getEggManager().getFirstEgg().getKey();
    }

    private boolean isUltimateCatcherEgg(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return item.getType() != Material.AIR
                && (!meta.hasDisplayName()
                || (!meta.getDisplayName().startsWith(UCI_KEY + ";")
                && !meta.getDisplayName().startsWith(UCI_KEY + "-")))
                && new NBTItem(item).hasKey(UCI_KEY);
    }

    private void spawnEgg(Player player, String eggType) {
        Location location = player.getEyeLocation();
        Egg egg = location.getWorld().spawn(location, Egg.class);
        egg.setCustomName(UCI_KEY + ";" + eggType);
        egg.setShooter(player);

        oncePerTick.add(player.getUniqueId());
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> oncePerTick.remove(player.getUniqueId()), 1L);

        eggs.put(egg.getUniqueId(), player.getUniqueId());

        XSound.ENTITY_EGG_THROW.play(location, 1L, 1L);
        egg.setVelocity(player.getLocation().getDirection().normalize().multiply(2));
    }

    private void takeEggFromPlayer(Player player, CompatibleHand hand) {
        if (player.getGameMode() != GameMode.CREATIVE) ItemUtils.takeActiveItem(player, hand);
    }

    public boolean isEgg(Item item) {
        return eggs.containsKey(item.getUniqueId());
    }

    public void handleEggToss(Player player, ItemStack item, CompatibleHand hand) {
        if (isSpawningBlocked(player)) {
            plugin.getLocale().getMessage("event.catch.blockedspawningworld")
                    .processPlaceholder("world", player.getWorld().getName())
                    .sendPrefixedMessage(player);
            return;
        }

        Location location = player.getEyeLocation().clone();

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setBoolean(UCI_KEY, true);
        ItemStack toThrow = enchantEgg(nbtItem.getItem());

        Item egg = dropEggItem(location, toThrow);

        EggTrackingTask.addEgg(egg);
        takeEggFromPlayer(player, hand);
    }

    private boolean isSpawningBlocked(Player player) {
        return Settings.BLOCKED_SPAWNING_WORLDS.getStringList().contains(player.getWorld().getName())
                && !player.hasPermission("ultimatecatcher.bypass.blockedspawningworld");
    }

    private ItemStack enchantEgg(ItemStack toThrow) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
                toThrow.removeEnchantment(Enchantment.ARROW_KNOCKBACK), 50);

        toThrow.setAmount(1);
        toThrow.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 69);

        return toThrow;
    }

    private Item dropEggItem(Location location, ItemStack toThrow) {
        Item egg = location.getWorld().dropItem(location, toThrow);
        egg.setPickupDelay(9999);
        eggs.put(egg.getUniqueId(), location.getWorld().getPlayers().get(0).getUniqueId());
        XSound.ENTITY_EGG_THROW.play(location, 1L, 1L);
        egg.setVelocity(location.getDirection().normalize().multiply(2));

        return egg;
    }

    public void handleEggHit(Egg egg, ProjectileHitEvent event) {
        Optional<CEgg> optionalCatcher = getCatcherFromEgg(egg);
        if (!optionalCatcher.isPresent()) return;
        CEgg catcher = optionalCatcher.get();

        Optional<LivingEntity> optionalEntity = getCaughtEntity(egg, event);
        if (!optionalEntity.isPresent()) {
            rejectEgg(egg, catcher, false);
            return;
        }

        LivingEntity entity = optionalEntity.get();
        removeTemporaryChicken(egg);

        Optional<Player> optionalPlayer = getShooter(egg);
        if (!optionalPlayer.isPresent()) {
            rejectEgg(egg, catcher, true);
            return;
        }

        Player player = optionalPlayer.get();
        if (!canCatch(player, entity, catcher)) {
            rejectEgg(egg, catcher, true);
            return;
        }

        catchEntity(egg, player, entity, catcher);
    }

    private Optional<CEgg> getCatcherFromEgg(Egg egg) {
        String[] split = egg.getCustomName().split(";");
        if (split.length < 2) return Optional.empty();

        return Optional.ofNullable(plugin.getEggManager().getEgg(split[1]));
    }

    @SuppressWarnings("deprecation")
    private Optional<LivingEntity> getCaughtEntity(Egg egg, ProjectileHitEvent event) {
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)) {
            Entity hitEntity = event.getHitEntity();
            if (hitEntity instanceof LivingEntity) {
                return Optional.of((LivingEntity) hitEntity);
            } else {
                event.setCancelled(true);
                return Optional.empty();
            }
        } else {
            return egg.getNearbyEntities(2, 2, 2).stream()
                    .filter(e -> e instanceof LivingEntity
                            && e.getType() != EntityType.PLAYER
                            && e.getTicksLived() > 20)
                    .map(e -> (LivingEntity) e)
                    .sorted(Comparator.comparingDouble(e -> e.getLocation().distance(egg.getLocation())))
                    .findFirst();
        }
    }

    private void removeTemporaryChicken(Egg egg) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
                        egg.getNearbyEntities(3, 3, 3).stream()
                                .filter(e -> e instanceof LivingEntity
                                        && e.getTicksLived() <= 20
                                        && e.getType() != EntityType.PLAYER
                                        && e.getType() == EntityType.CHICKEN)
                                .findFirst()
                                .ifPresent(Entity::remove),
                0L
        );
    }

    private Optional<Player> getShooter(Egg egg) {
        return Optional.ofNullable(Bukkit.getPlayer(eggs.get(egg.getUniqueId())));
    }

    private boolean canCatch(Player player, LivingEntity entity, CEgg catcher) {
        if (isCatchingBlocked(player)) {
            plugin.getLocale().getMessage("event.catch.blockedcatchingworld")
                    .processPlaceholder("world", player.getWorld().getName())
                    .sendPrefixedMessage(player);
            return false;
        }

        if (isNPC(entity) || plugin.getExternalHookManager().shouldStopCapture(player, entity))
            return false;

        if (!canCatchMob(player, entity))
            return false;

        if (!hasPermissionToCatch(player, entity))
            return false;

        if (!catchConditionsMet(player, catcher, entity))
            return false;

        return true;
    }

    private boolean isCatchingBlocked(Player player) {
        return Settings.BLOCKED_CATCHING_WORLDS.getStringList().contains(player.getWorld().getName())
                && !player.hasPermission("ultimatecatcher.bypass.blockedcatchingworld");
    }

    private boolean isNPC(Entity entity) {
        return Bukkit.getPluginManager().isPluginEnabled("Citizens")
                && net.citizensnpcs.api.CitizensAPI.getNPCRegistry().isNPC(entity);
    }

    private boolean canCatchMob(Player player, Entity entity) {
        ConfigurationSection mobConfig = plugin.getMobConfig();
        String key = createMobConfigKey(entity);
        if (!mobConfig.contains(key)) return false;
        if (!mobConfig.getBoolean(key) && !player.hasPermission("ultimatecatcher.bypass.disabled")) {
            plugin.getLocale().getMessage("event.catch.notenabled")
                    .processPlaceholder("type", EntityUtils.getFormattedEntityType(entity.getType()))
                    .sendPrefixedMessage(player);
            return false;
        }
        return true;
    }

    private String createMobConfigKey(Entity entity) {
        return MOBS_PATH + "." + entity.getType().name() + ".Enabled";
    }

    private boolean hasPermissionToCatch(Player player, Entity entity) {
        return player.hasPermission("ultimatecatcher.catch.*")
                || (player.hasPermission(createPermissionString(entity, "peaceful"))
                && isPassiveMob(entity))
                || (player.hasPermission(createPermissionString(entity, "hostile"))
                && isHostileMob(entity));
    }

    private String createPermissionString(Entity entity, String type) {
        return "ultimatecatcher.catch." + type + "." + entity.getType().name();
    }

    private boolean isPassiveMob(Entity entity) {
        return entity instanceof Animals
                || entity instanceof Ambient
                || entity instanceof WaterMob
                || entity instanceof Golem && !entity.getType().name().equals("SHULKER")
                || entity instanceof AbstractVillager;
    }

    private boolean isHostileMob(Entity entity) {
        return entity instanceof Monster
                || entity instanceof Boss
                || entity instanceof Flying
                || entity instanceof Slime
                || entity.getType().name().equals("SHULKER");
    }

    private boolean catchConditionsMet(Player player, CEgg catcher, LivingEntity entity) {
        if (isTamedByOther(player, entity)) {
            plugin.getLocale().getMessage("event.catch.notyours").sendPrefixedMessage(player);
            return false;
        }

        if (isTrustedFoxByOther(player, entity)) {
            plugin.getLocale().getMessage("event.catch.notyours").sendPrefixedMessage(player);
            return false;
        }

        if (!canAffordCatch(player, catcher, entity))
            return false;

        if (!canCatchByChance(player, catcher, entity))
            return false;

        return true;
    }

    private boolean isTamedByOther(Player player, LivingEntity entity) {
        return entity instanceof Tameable
                && Settings.REJECT_TAMED.getBoolean()
                && ((Tameable) entity).isTamed()
                && ((Tameable) entity).getOwner() != null
                && ((Tameable) entity).getOwner().getUniqueId() != player.getUniqueId();
    }

    private boolean isTrustedFoxByOther(Player player, LivingEntity entity) {
        return ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14)
                && entity instanceof Fox
                && ((Fox) entity).getFirstTrustedPlayer() != null
                && !((Fox) entity).getFirstTrustedPlayer().getUniqueId().equals(player.getUniqueId())
                && Settings.REJECT_TAMED.getBoolean();
    }

    private boolean canAffordCatch(Player player, CEgg catcher, LivingEntity entity) {
        double cost = catcher.getCost();
        if (EconomyManager.isEnabled() && cost != 0 && !player.hasPermission("ultimatecatcher.bypass.free")) {
            if (!EconomyManager.hasBalance(player, cost)) {
                plugin.getLocale().getMessage("event.catch.cantafford")
                        .processPlaceholder("amount", cost)
                        .processPlaceholder("type", EntityUtils.getFormattedEntityType(entity.getType()))
                        .sendPrefixedMessage(player);
                return false;
            }
        }
        return true;
    }

    private boolean canCatchByChance(Player player, CEgg catcher, LivingEntity entity) {
        int chance = catcher.getChance();
        if (!(Math.random() * 100 - chance < 0 || chance == 100) && !player.hasPermission("ultimatecatcher.bypass.chance")) {
            XSound.ENTITY_VILLAGER_NO.play(entity.getLocation(), 1L, 1L);
            plugin.getLocale().getMessage("event.catch.failed")
                    .processPlaceholder("type", EntityUtils.getFormattedEntityType(entity.getType()))
                    .sendPrefixedMessage(player);
            return false;
        }
        return true;
    }

    private void catchEntity(Egg egg, Player player, LivingEntity entity, CEgg catcher) {
        PlayerInteractEvent interactEvent = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, null, entity.getLocation().getBlock(), BlockFace.UP);
        Bukkit.getPluginManager().callEvent(interactEvent);
        if (interactEvent.isCancelled()) {
            rejectEgg(egg, catcher, true);
            return;
        }
        withdrawCatchCost(player, catcher);
        removeEntityFromWorld(entity);

        Optional<ItemStack> optionalCaughtItem = createCaughtItem(player, entity, catcher);
        if (!optionalCaughtItem.isPresent()) return;

        ItemStack caughtItem = optionalCaughtItem.get();

        removeEgg(egg);
        giveCaughtItemToPlayer(player, entity, caughtItem);
        playCatchEffects(entity);
    }

    private void withdrawCatchCost(Player player, CEgg catcher) {
        double cost = catcher.getCost();
        if (EconomyManager.isEnabled() && cost != 0 && !player.hasPermission("ultimatecatcher.bypass.free")) {
            EconomyManager.withdrawBalance(player, cost);
        }
    }

    private void removeEntityFromWorld(LivingEntity entity) {
        if (EntityStackerManager.getStacker() != null && EntityStackerManager.isStacked(entity) && EntityStackerManager.getSize(entity) > 1)
            EntityStackerManager.getStacker().removeOne(entity);
        else
            entity.remove();
    }

    private Optional<ItemStack> createCaughtItem(Player player, LivingEntity entity, CEgg catcher) {
        Optional<XMaterial> spawnEgg = CompatibleMaterial.getSpawnEgg(entity.getType());
        if (!spawnEgg.isPresent()) return Optional.empty();

        ItemStack item = spawnEgg.get().parseItem();
        ItemMeta meta = item.getItemMeta();

        setDisplayName(meta, entity);
        setLore(player, meta, entity);

        item.setItemMeta(meta);

        return Optional.of(EntityUtils.serializeEntity(item, entity));
    }

    private void setDisplayName(ItemMeta meta, LivingEntity entity) {
        meta.setDisplayName(plugin.getLocale().getMessage("general.catcher.spawn")
                .processPlaceholder("type",
                        TextUtils.formatText(getEntityName(entity)))
                .toText());
    }

    private String getEntityName(LivingEntity entity) {
        return entity.getCustomName() != null
                && !entity.getCustomName().contains(String.valueOf(ChatColor.COLOR_CHAR))
                && !(EntityStackerManager.getStacker() != null && !EntityStackerManager.isStacked(entity))
                ? entity.getCustomName()
                : EntityUtils.getFormattedEntityType(entity.getType());
    }

    private void setLore(Player player, ItemMeta meta, LivingEntity entity) {
        List<String> lore = Settings.CATCHER_CAUGHT_LORE_FORMAT.getStringList().stream()
                .map(line -> formatLoreLine(player, line, entity))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        meta.setLore(lore);
    }

    private String formatLoreLine(Player player, String line, LivingEntity entity) {
        Message messageLine = new Message(line);

        if (line.toLowerCase().contains("%age%")) {
            if (entity instanceof Ageable)
                return plugin.getLocale().getMessage("general.catcherinfo.age")
                        .processPlaceholder("value", ((Ageable) entity).isAdult()
                                ? plugin.getLocale().getMessage("general.catcher.adult").toText()
                                : plugin.getLocale().getMessage("general.catcher.baby").toText())
                        .toText();
            return null;
        }

        if (line.toLowerCase().contains("%tamed%")) {
            if (entity instanceof Tameable && ((Tameable) entity).isTamed())
                return plugin.getLocale().getMessage("general.catcherinfo.tamed").toText();
            return null;
        }

        if (line.toLowerCase().contains("%trusted%")) {
            if (isTrustedFoxByOther(player, entity))
                return plugin.getLocale().getMessage("general.catcherinfo.trusted").toText();
            return null;
        }

        return messageLine
                .processPlaceholder("health", getHealthLine(entity))
                .processPlaceholder("type", getTypeLine(entity))
                .toText();
    }

    private String getHealthLine(LivingEntity entity) {
        double health = Math.round(entity.getHealth() * 100.0) / 100.0;
        double maxHealth = entity.getMaxHealth();

        return plugin.getLocale().getMessage("general.catcherinfo.health")
                .processPlaceholder("value", health == maxHealth
                        ? plugin.getLocale().getMessage("general.catcher.max").toText()
                        : health + "/" + maxHealth)
                .toText();
    }

    private String getTypeLine(LivingEntity entity) {
        return plugin.getLocale().getMessage("general.catcherinfo.type")
                .processPlaceholder("value", EntityUtils.getFormattedEntityType(entity.getType()))
                .toText();
    }

    private void removeEgg(Egg egg) {
        egg.remove();
    }

    private void giveCaughtItemToPlayer(Player player, LivingEntity entity, ItemStack caughtItem) {
        plugin.getLocale().getMessage("event.catch.success")
                .processPlaceholder("type", EntityUtils.getFormattedEntityType(entity.getType()))
                .sendPrefixedMessage(player);

        entity.getWorld().dropItem(entity.getLocation(), caughtItem);
    }

    private void playCatchEffects(LivingEntity entity) {
        CompatibleParticleHandler.spawnParticles(CompatibleParticleHandler.ParticleType.SMOKE_NORMAL,
                entity.getLocation(),
                100, .5, .5, .5);
        XSound.ITEM_FIRECHARGE_USE.play(entity.getLocation(), 1L, 1L);
    }

    private void rejectEgg(Egg egg, CEgg catcher, boolean playSound) {
        if (playSound)
            XSound.ENTITY_VILLAGER_NO.play(egg.getLocation(), 1L, 1L);

        egg.getWorld().dropItem(egg.getLocation(), catcher.toItemStack());
        egg.remove();
    }

    public Map<UUID, UUID> getEggs() {
        return eggs;
    }
}