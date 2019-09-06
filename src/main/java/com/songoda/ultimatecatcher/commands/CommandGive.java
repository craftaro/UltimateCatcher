package com.songoda.ultimatecatcher.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.ultimatecatcher.UltimateCatcher;
import com.songoda.ultimatecatcher.egg.CEgg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CommandGive extends AbstractCommand {

    final UltimateCatcher instance;

    public CommandGive(UltimateCatcher instance) {
        super(false, "give");
        this.instance = instance;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length != 2) return ReturnType.SYNTAX_ERROR;

        if (Bukkit.getPlayer(args[0]) == null && !args[0].trim().toLowerCase().equals("all")) {
            sender.sendMessage("Not a player...");
            return ReturnType.FAILURE;
        }

        CEgg catcher = instance.getEggManager().getEgg(args[1]);

        if (catcher == null) {
            sender.sendMessage("Not an egg...");
            return ReturnType.FAILURE;
        }

        ItemStack itemStack = catcher.toItemStack();
        if (!args[1].trim().toLowerCase().equals("all")) {
            Player player = Bukkit.getOfflinePlayer(args[0]).getPlayer();
            player.getInventory().addItem(itemStack);
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.getInventory().addItem(itemStack);
            }
        }
        return ReturnType.SUCCESS;
    }

    @Override
    public String getPermissionNode() {
        return "ultimatecatcher.admin";
    }

    @Override
    public String getSyntax() {
        StringBuilder keys = new StringBuilder();
        for (CEgg egg : UltimateCatcher.getInstance().getEggManager().getRegisteredEggs()) {
            keys.append("/").append(egg.getKey());
        }
        return "/uc give <player/all> <" + keys.substring(1) + ">";
    }

    @Override
    public String getDescription() {
        return "Give an egg.";
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        List<String> tab = new ArrayList<>();

        if (args.length == 1) {
            tab.add("all");
            for (Player player : Bukkit.getOnlinePlayers()) tab.add(player.getName());
        } else if (args.length == 2) {
            for (CEgg egg : UltimateCatcher.getInstance().getEggManager().getRegisteredEggs()) tab.add(egg.getKey());
        }

        return tab;
    }
}
