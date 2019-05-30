package com.songoda.ultimatecatcher.command.commands;

import com.songoda.ultimatecatcher.UltimateCatcher;
import com.songoda.ultimatecatcher.command.AbstractCommand;
import com.songoda.ultimatecatcher.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandGive extends AbstractCommand {

    public CommandGive(AbstractCommand abstractCommand) {
        super("give", abstractCommand, false);
    }

    @Override
    protected ReturnType runCommand(UltimateCatcher instance, CommandSender sender, String... args) {
        if (args.length != 2) return ReturnType.SYNTAX_ERROR;

        if (Bukkit.getPlayer(args[1]) == null && !args[1].trim().toLowerCase().equals("all")) {
            sender.sendMessage("Not a player...");
            return ReturnType.SYNTAX_ERROR;
        }

        ItemStack itemStack = Methods.createCatcher();
        if (!args[1].trim().toLowerCase().equals("all")) {
            Player player = Bukkit.getOfflinePlayer(args[1]).getPlayer();
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
        return "ultimatestacker.admin";
    }

    @Override
    public String getSyntax() {
        return "/uc give <player/all>";
    }

    @Override
    public String getDescription() {
        return "Give a catcher.";
    }
}
