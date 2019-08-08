package com.songoda.ultimatecatcher.command;

import com.songoda.ultimatecatcher.UltimateCatcher;
import com.songoda.ultimatecatcher.command.commands.CommandGive;
import com.songoda.ultimatecatcher.command.commands.CommandReload;
import com.songoda.ultimatecatcher.command.commands.CommandSettings;
import com.songoda.ultimatecatcher.command.commands.CommandUltimateCatcher;
import com.songoda.ultimatecatcher.utils.Methods;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandManager implements CommandExecutor {

    private static final List<AbstractCommand> commands = new ArrayList<>();
    private UltimateCatcher instance;
    private TabManager tabManager;

    public CommandManager(UltimateCatcher instance) {
        this.instance = instance;
        this.tabManager = new TabManager(this);

        instance.getCommand("UltimateCatcher").setExecutor(this);

        AbstractCommand commandUltimateCatcher = addCommand(new CommandUltimateCatcher());

        addCommand(new CommandSettings(commandUltimateCatcher));
        addCommand(new CommandReload(commandUltimateCatcher));
        addCommand(new CommandGive(commandUltimateCatcher));

        for (AbstractCommand abstractCommand : commands) {
            if (abstractCommand.getParent() != null) continue;
            instance.getCommand(abstractCommand.getCommand()).setTabCompleter(tabManager);
        }
    }

    private AbstractCommand addCommand(AbstractCommand abstractCommand) {
        commands.add(abstractCommand);
        return abstractCommand;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        for (AbstractCommand abstractCommand : commands) {
            if (abstractCommand.getCommand() != null && abstractCommand.getCommand().equalsIgnoreCase(command.getName().toLowerCase())) {
                if (strings.length == 0 || abstractCommand.hasArgs()) {
                    processRequirements(abstractCommand, commandSender, strings);
                    return true;
                }
            } else if (strings.length != 0 && abstractCommand.getParent() != null && abstractCommand.getParent().getCommand().equalsIgnoreCase(command.getName())) {
                String cmd = strings[0];
                String cmd2 = strings.length >= 2 ? String.join(" ", strings[0], strings[1]) : null;
                for (String cmds : abstractCommand.getSubCommand()) {
                    if (cmd.equalsIgnoreCase(cmds) || (cmd2 != null && cmd2.equalsIgnoreCase(cmds))) {
                        processRequirements(abstractCommand, commandSender, strings);
                        return true;
                    }
                }
            }
        }
        instance.getLocale().newMessage("&7The command you entered does not exist or is spelt incorrectly.").sendPrefixedMessage(commandSender);
        return true;
    }

    private void processRequirements(AbstractCommand command, CommandSender sender, String[] strings) {
        if (!(sender instanceof Player) && command.isNoConsole()) {
            sender.sendMessage("You must be a player to use this commands.");
            return;
        }
        if (command.getPermissionNode() == null || sender.hasPermission(command.getPermissionNode())) {
            AbstractCommand.ReturnType returnType = command.runCommand(instance, sender, strings);
            if (returnType == AbstractCommand.ReturnType.SYNTAX_ERROR) {
                instance.getLocale().newMessage("&cInvalid Syntax!").sendPrefixedMessage(sender);
                instance.getLocale().newMessage("&7The valid syntax is: &6" + command.getSyntax() + "&7.").sendPrefixedMessage(sender);
            }
            return;
        }
        instance.getLocale().getMessage("event.general.nopermission").sendPrefixedMessage(sender);
    }

    public List<AbstractCommand> getCommands() {
        return Collections.unmodifiableList(commands);
    }
}
