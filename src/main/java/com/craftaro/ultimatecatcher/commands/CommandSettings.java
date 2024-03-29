package com.craftaro.ultimatecatcher.commands;

import com.craftaro.core.commands.AbstractCommand;
import com.craftaro.core.configuration.editor.PluginConfigGui;
import com.craftaro.core.gui.GuiManager;
import com.craftaro.ultimatecatcher.UltimateCatcher;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandSettings extends AbstractCommand {

    final GuiManager guiManager;

    public CommandSettings(GuiManager guiManager) {
        super(true, "settings");
        this.guiManager = guiManager;
    }

    @Override
    protected ReturnType runCommand(CommandSender sender, String... args) {
        guiManager.showGUI((Player) sender, new PluginConfigGui(UltimateCatcher.getInstance()));
        return ReturnType.SUCCESS;
    }

    @Override
    public String getPermissionNode() {
        return "ultimateCatcher.admin";
    }

    @Override
    public String getSyntax() {
        return "settings";
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
