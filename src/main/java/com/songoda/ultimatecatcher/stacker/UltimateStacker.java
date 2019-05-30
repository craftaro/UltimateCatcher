package com.songoda.ultimatecatcher.stacker;

import com.songoda.ultimatestacker.entity.EntityStack;
import org.bukkit.entity.Entity;

public class UltimateStacker implements Stacker {

    private final com.songoda.ultimatestacker.UltimateStacker plugin;

    public UltimateStacker() {
        this.plugin = com.songoda.ultimatestacker.UltimateStacker.getInstance();
    }

    @Override
    public boolean isStacked(Entity entity) {
        return plugin.getEntityStackManager().isStacked(entity);
    }

    @Override
    public void removeOne(Entity entity) {
        EntityStack stack = plugin.getEntityStackManager().getStack(entity);
        if (stack.getAmount() <= 0)
            entity.remove();
        else
            stack.setAmount(stack.getAmount() - 1);
    }
}
