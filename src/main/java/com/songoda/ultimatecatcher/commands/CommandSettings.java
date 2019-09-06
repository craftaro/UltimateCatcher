package com.songoda.ultimatecatcher.commands;

import com.songoda.core.commands.AbstractCommand;
import com.songoda.core.configuration.editor.PluginConfigGui;
import com.songoda.ultimatecatcher.UltimateCatcher;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandSettings extends AbstractCommand {

    final UltimateCatcher instance;

    public CommandSettings(UltimateCatcher instance) {
        super(true, "settings");
        this.instance = instance;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        instance.getGuiManager().showGUI((Player) sender, new PluginConfigGui(instance));
        return ReturnType.SUCCESS;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateCatcher.admin";
    }

    @Override
    public String getSyntax() {
        return "/uc settings";
    }

    @Override
    public String getDescription() {
        return "Edit the UltimateCatcher Settings.";
    }

    @Override
    protected List<String> onTab(CommandSender sender, String... args) {
        return null;
    }
}
