package com.songoda.ultimatecatcher.utils;

import com.songoda.core.compatibility.ServerVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.UUID;

public class FoxNMS {

    private static Class<?> clazzCraftFox, clazzEntityFox, clazzNBTagCompound, clazzGameProfileSerializer, clazzNBTTagList;
    private static Method getHandle, b, getList, a, getCompound, b2, setLong, a2;

    static {
        try {
            String ver = Bukkit.getServer().getClass().getPackage().getName().substring(23);

            clazzCraftFox = Class.forName("org.bukkit.craftbukkit." + ver + ".entity.CraftFox");
            clazzEntityFox = Class.forName("net.minecraft.server." + ver + ".EntityFox");
            clazzNBTagCompound = Class.forName("net.minecraft.server." + ver + ".NBTTagCompound");
            clazzGameProfileSerializer = Class.forName("net.minecraft.server." + ver + ".GameProfileSerializer");
            clazzNBTTagList = Class.forName("net.minecraft.server." + ver + ".NBTTagList");

            getHandle = clazzCraftFox.getDeclaredMethod("getHandle");
            b = clazzEntityFox.getDeclaredMethod("b", clazzNBTagCompound);
            getList = clazzNBTagCompound.getDeclaredMethod("getList", String.class, int.class);
            a = clazzGameProfileSerializer.getDeclaredMethod("a", UUID.class);
            getCompound = clazzNBTTagList.getDeclaredMethod("getCompound", int.class);
            b2 = clazzGameProfileSerializer.getDeclaredMethod("b", clazzNBTagCompound);
            setLong = clazzNBTagCompound.getDeclaredMethod("setLong", String.class, long.class);
            a2 = clazzEntityFox.getDeclaredMethod("a", clazzNBTagCompound);

        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static boolean isPlayerTrusted(Entity entity, Player player) {
        if (!(entity instanceof Fox)
                || !ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14)) return false;

        try {
            Object entityFox = getHandle.invoke(entity);
            Object foxNBT = clazzNBTagCompound.newInstance();
            b.invoke(entityFox, foxNBT);
            if (((AbstractCollection) getList.invoke(foxNBT, "TrustedUUIDs", 10)).contains(a.invoke(null, player.getUniqueId())))
                return true;

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isFoxWild(Entity entity) {
        if (!(entity instanceof Fox)
                || !ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14)) return true;
        try {
            Object entityFox = getHandle.invoke(entity);
            Object foxNBT = clazzNBTagCompound.newInstance();
            b.invoke(entityFox, foxNBT);

            Object list = getList.invoke(foxNBT, "TrustedUUIDs", 10);
            if (list == null || ((AbstractCollection) list).isEmpty())
                return true;

        } catch (InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static UUID getOwner(Entity entity) {
        if (!(entity instanceof Fox)
                || !ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14)) return null;
        try {
            Object entityFox = getHandle.invoke(entity);
            Object foxNBT = clazzNBTagCompound.newInstance();
            b.invoke(entityFox, foxNBT);

            Object list = getList.invoke(foxNBT, "TrustedUUIDs", 10);
            if (list == null || ((AbstractCollection) list).isEmpty())
                return null;

            return (UUID) b2.invoke(null, getCompound.invoke(list, 0));

        } catch (InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void applyOwner(Entity entity, UUID owner) {
        if (!(entity instanceof Fox)
                || !ServerVersion.isServerVersionAtLeast(ServerVersion.V1_14)) return;
        try {
            Object entityFox = getHandle.invoke(entity);
            Object foxNBT = clazzNBTagCompound.newInstance();
            b.invoke(entityFox, foxNBT);

            Object trustedCompound = clazzNBTagCompound.newInstance();
            setLong.invoke(trustedCompound, "L", owner.getLeastSignificantBits());
            setLong.invoke(trustedCompound, "M", owner.getMostSignificantBits());

            Object list = getList.invoke(foxNBT, "TrustedUUIDs", 10);
            ((AbstractList) list).add(trustedCompound);
            a2.invoke(entityFox, foxNBT);

        } catch (InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
