package com.songoda.ultimatecatcher.stacker;

import org.bukkit.entity.Entity;

public interface Stacker {

    boolean isStacked(Entity entity);

    void removeOne(Entity entity);
}
