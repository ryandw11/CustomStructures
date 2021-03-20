package com.ryandw11.structure.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Implement a sub command for the /cstruct command.
 */
public interface SubCommand {
    /**
     * The sub command.
     *
     * @param sender The sender.
     * @param cmd    The command.
     * @param s      The command string.
     * @param args   The arguments. (Does not include the subcommand name. So args.length for `/cstruct test` is 0.)
     * @return If the sub-command is valid.
     */
    boolean subCommand(CommandSender sender, Command cmd, String s, String[] args);
}
