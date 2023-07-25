package com.craftaro.ultimatecatcher.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.craftaro.ultimatecatcher.UltimateCatcher;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandReload extends AbstractCommand {

    final UltimateCatcher instance;

    public CommandReload(UltimateCatcher instance) {
        super(false, "reload");
        this.instance = instance;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        instance.onConfigReload();
        instance.getLocale().getMessage("&7Configuration and Language files reloaded.").sendPrefixedMessage(sender);
        return ReturnType.SUCCESS;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateCatcher.admin";
    }

    @Override
    public String getSyntax() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Reload the Configuration and Language files.";
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }
}
