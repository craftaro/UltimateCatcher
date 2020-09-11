package com.songoda.ultimatecatcher.tasks;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.CompatibleParticleHandler;
import com.songoda.core.compatibility.CompatibleSound;
import com.songoda.core.nms.NmsManager;
import com.songoda.ultimatecatcher.UltimateCatcher;
import com.songoda.ultimatecatcher.utils.EntityUtils;
import com.songoda.ultimatecatcher.utils.OldEntityUtils;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class EggTrackingTask extends BukkitRunnable {

    private static final Set<Item> eggs = new HashSet<>();

    private static EggTrackingTask instance;
    private static UltimateCatcher plugin;

    private EggTrackingTask(UltimateCatcher plug) {
        plugin = plug;
    }

    public static EggTrackingTask startTask(UltimateCatcher plug) {
        plugin = plug;
        if (instance == null) {
            instance = new EggTrackingTask(plugin);
            instance.runTaskTimer(plugin, 0, 1);
        }

        return instance;
    }

    @Override
    public void run() {
        for (Item item : new HashSet<>(eggs)) {
            if (!item.isValid()) {
                eggs.remove(item);
                item.remove();
                continue;
            }

            if (item.isOnGround() && item.getTicksLived() > 10 || item.getTicksLived() > 50) {

                String displayName = item.getItemStack().getItemMeta().getDisplayName();

                boolean inWater = CompatibleMaterial.getMaterial(item.getLocation().getBlock()) == CompatibleMaterial.WATER;

                Entity entity;
                if (NmsManager.getNbt().of(item.getItemStack()).has("serialized_entity")) {
                    entity = EntityUtils.spawnEntity(inWater ? item.getLocation().getBlock().getLocation().add(.5, .5, .5)
                            : item.getLocation(), item.getItemStack());
                } else if (!displayName.contains("~") && NmsManager.getNbt().of(item.getItemStack()).has("UCI")) {
                    entity = OldEntityUtils.spawnEntity(item.getLocation(), item.getItemStack());
                } else {
                    String[] split = item.getItemStack().getItemMeta().getDisplayName().split("~");
                    String json = split[0].replace(String.valueOf(ChatColor.COLOR_CHAR), "");
                    entity = OldEntityUtils.spawnEntity(item.getLocation(), json);
                }

                eggs.remove(item);

                // Couldn't spawn
                if (entity == null) {
                    plugin.getEntityListeners().getEggs().remove(item.getUniqueId());
                    item.getItemStack().removeEnchantment(Enchantment.ARROW_KNOCKBACK);
                    item.setPickupDelay(1);
                    continue;
                }

                CompatibleParticleHandler.spawnParticles(CompatibleParticleHandler.ParticleType.SMOKE_NORMAL, entity.getLocation(), 100, .5, .5, .5);
                CompatibleSound.ITEM_FIRECHARGE_USE.play(entity.getWorld(), entity.getLocation(), 1L, 1L);

                item.remove();
            }
        }
    }

    public static void addEgg(Item item) {
        eggs.add(item);
    }

}