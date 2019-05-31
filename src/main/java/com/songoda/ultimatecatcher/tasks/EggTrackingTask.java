package com.songoda.ultimatecatcher.tasks;

import com.songoda.ultimatecatcher.UltimateCatcher;
import com.songoda.ultimatecatcher.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EggTrackingTask extends BukkitRunnable {

    private static Set<Item> eggs = new HashSet<>();

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
                String[] split = item.getItemStack().getItemMeta().getDisplayName().split("~");
                String json = split[0].replace(String.valueOf(ChatColor.COLOR_CHAR), "");

                Entity entity = Methods.spawnEntity(item.getLocation(), json);

                entity.getWorld().spawnParticle(Particle.SMOKE_NORMAL, entity.getLocation(), 100, .5, .5, .5);
                entity.getWorld().playSound(entity.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1L, 1L);

                eggs.remove(item);
                item.remove();
            }
        }
    }

    public static void addEgg(Item item) {
        eggs.add(item);
    }

}