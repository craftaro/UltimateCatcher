package com.songoda.ultimatecatcher.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.utils.PlayerUtils;
import com.songoda.ultimatecatcher.UltimateCatcher;
import com.songoda.ultimatecatcher.egg.CEgg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandGive extends AbstractCommand {

    final UltimateCatcher instance;

    public CommandGive(UltimateCatcher instance) {
        super(false, "give");
        this.instance = instance;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length != 2) return ReturnType.SYNTAX_ERROR;

        final Player player = Bukkit.getPlayer(args[0]);
        if (player == null && !args[0].trim().toLowerCase().equals("all")) {
            sender.sendMessage("Not a player...");
            return ReturnType.FAILURE;
        }

        CEgg catcher = instance.getEggManager().getEgg(args[1]);

        if (catcher == null) {
            sender.sendMessage("Not an egg...");
            return ReturnType.FAILURE;
        }

        ItemStack itemStack = catcher.toItemStack();
        if (player != null) {
            player.getInventory().addItem(itemStack);
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.getInventory().addItem(itemStack);
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
        return "give <player/all> <" + keys.substring(1) + ">";
    }

    @Override
    public String getDescription() {
        return "Give an egg.";
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        List<String> tab = null;

        if (args.length == 1) {
            tab = new ArrayList();
            tab.add("all");
            tab.addAll(PlayerUtils.getVisiblePlayerNames(sender, args[0]));
        } else if (args.length == 2) {
            tab = UltimateCatcher.getInstance().getEggManager().getRegisteredEggs().stream().map(e -> e.getKey()).collect(Collectors.toList());
        }

        return tab;
    }
}
