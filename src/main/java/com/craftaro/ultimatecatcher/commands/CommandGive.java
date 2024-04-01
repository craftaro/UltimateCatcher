package com.craftaro.ultimatecatcher.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.craftaro.core.utils.PlayerUtils;
import com.craftaro.ultimatecatcher.UltimateCatcher;
import com.craftaro.ultimatecatcher.egg.CEgg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandGive extends AbstractCommand {
    private final UltimateCatcher plugin;

    public CommandGive(UltimateCatcher plugin) {
        super(CommandType.CONSOLE_OK, "give");
        this.plugin = plugin;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        if (args.length < 1 || args.length > 2) {
            return ReturnType.SYNTAX_ERROR;
        }

        CEgg egg;
        Player player;

        if (args.length == 2 && Bukkit.getPlayer(args[0]) == null) {
            this.plugin.getLocale().getMessage("command.give.player-not-found").sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        } else if (args.length == 1) {
            if (!(sender instanceof Player)) {
                this.plugin.getLocale().getMessage("command.give.player-only").sendPrefixedMessage(sender);
                return ReturnType.FAILURE;
            }
            player = (Player) sender;
        } else {
            player = Bukkit.getPlayer(args[0]);
        }

        egg = this.plugin.getEggManager().getEgg(args[args.length - 1]);
        if (egg == null) {
            this.plugin.getLocale().getMessage("command.give.invalid-egg")
                    .processPlaceholder("eggs", this.plugin.getEggManager().getRegisteredEggs().stream().map(CEgg::getKey).collect(Collectors.joining(", ")))
                    .sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        ItemStack itemStack = egg.toItemStack();
        player.getInventory().addItem(itemStack);

        this.plugin.getLocale().getMessage("command.give.success")
                .processPlaceholder("egg", egg.getKey())
                .processPlaceholder("player", player.getName())
                .sendPrefixedMessage(sender);

        if (player != sender) {
            this.plugin.getLocale().getMessage("command.give.received")
                    .processPlaceholder("egg", egg.getKey())
                    .sendPrefixedMessage(player);
        }

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        List<String> tab = null;
        if (args.length == 1) {
            tab = new ArrayList<>();
            tab.addAll(PlayerUtils.getVisiblePlayerNames(sender, args[0]));
        } else if (args.length == 2) {
            tab = this.plugin.getEggManager().getRegisteredEggs().stream().map(CEgg::getKey).collect(Collectors.toList());
        }
        return tab;
    }

    @Override
    public String getPermissionNode() {
        return "ultimatecatcher.admin.give";
    }

    @Override
    public String getSyntax() {
        return "give [player] <egg>";
    }

    @Override
    public String getDescription() {
        return "Give an egg to a player.";
    }
}