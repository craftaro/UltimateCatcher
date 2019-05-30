package com.songoda.ultimatecatcher.command.commands;

import com.songoda.ultimatecatcher.UltimateCatcher;
import com.songoda.ultimatecatcher.command.AbstractCommand;
import com.songoda.ultimatecatcher.utils.Methods;
import org.bukkit.command.CommandSender;

public class CommandReload extends AbstractCommand {

    public CommandReload(AbstractCommand parent) {
        super("reload", parent, false);
    }

    @Override
    protected ReturnType runCommand(UltimateCatcher instance, CommandSender sender, String... args) {
        instance.reload();
        sender.sendMessage(Methods.formatText(instance.getReferences().getPrefix() + "&7Configuration and Language files reloaded."));
        return ReturnType.SUCCESS;
    }

    @Override
    public String getPermissionNode() {
        return "ultimatestacker.admin";
    }

    @Override
    public String getSyntax() {
        return "/uc reload";
    }

    @Override
    public String getDescription() {
        return "Reload the Configuration and Language files.";
    }
}
